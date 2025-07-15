package com.example.MessageService.segment.controller;

import com.example.MessageService.security.dto.UserResponseDTO;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.entity.UserType;
import com.example.MessageService.security.repository.TenantRepository;
import com.example.MessageService.security.repository.UserRepository;
import com.example.MessageService.security.repository.UserRepositoryCustom;
import com.example.MessageService.security.service.UserService;
import com.example.MessageService.segment.dto.SegmentRequest;
import com.example.MessageService.segment.dto.SegmentResponse;
import com.example.MessageService.segment.dto.SegmentRuleRequest;
import com.example.MessageService.segment.service.SegmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/dashboard/segments")
@RequiredArgsConstructor
public class SegmentWebController {

    private final SegmentService segmentService;
    private final UserService userService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private Tenant getCurrentTenant(UserDetails userDetails) {
        return tenantRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated tenant not found"));
    }

    @GetMapping
    public String listSegments(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Tenant currentTenant = getCurrentTenant(userDetails);
        List<SegmentResponse> segments = segmentService.getAllSegments(currentTenant.getId());
        model.addAttribute("segments", segments);
        return "segments-list";
    }


//    @GetMapping("/new")
//    public String showCreateSegmentForm(Model model, @AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {
//        Tenant currentTenant = getCurrentTenant(userDetails);
//
//        List<String> distinctCities = userRepository.findDistinctCitiesByTenantId(currentTenant.getId());
//        model.addAttribute("distinctCities", distinctCities);
//        model.addAttribute("allUserTypes", UserType.values());
//
//        List<String> allGenders = Arrays.asList("Male", "Female");
//        model.addAttribute("allGenders", allGenders);
//        List<UserResponseDTO> tenantUsers = userService.getUsersByTenant(currentTenant.getId());
//        model.addAttribute("tenantUsersJson", objectMapper.writeValueAsString(tenantUsers));
//
//        model.addAttribute("segmentRequest" , new SegmentRuleRequest());
////        model.addAttribute("distinctCities", distinctCities);
////        model.addAttribute("allUserTypes", UserType.values());
//
//        return "segment-form";
//    }
//
//    @PostMapping("/create")
//    public String createSegment(@Valid @ModelAttribute("segmentRequest") SegmentRuleRequest segmentRuleRequest,
//                                BindingResult bindingResult,
//                                @AuthenticationPrincipal UserDetails userDetails,
//                                RedirectAttributes redirectAttributes,
//                                Model model) {
//
//        Tenant currentTenant = getCurrentTenant(userDetails);
//
//        if (bindingResult.hasErrors()) {
//            List<UserResponseDTO> tenantUsers = userService.getUsersByTenant(currentTenant.getId());
//            model.addAttribute("tenantUsers", tenantUsers);
//            return "segment-form";
//        }
//
//        try {
//
//            segmentService.createSegmentFromRules(segmentRuleRequest, currentTenant.getId());
//            redirectAttributes.addFlashAttribute("successMessage", "Segment '" + segmentRuleRequest.getName() + "' created successfully based on your rules!");
//            return "redirect:/dashboard/segments";
//        } catch (Exception e) {
//
//            List<String> distinctCities = userRepository.findDistinctCitiesByTenantId(currentTenant.getId());
//            model.addAttribute("distinctCities", distinctCities);
//            model.addAttribute("allUserTypes", UserType.values());
//            model.addAttribute("errorMessage", "Failed to create segment: " + e.getMessage());
//            return "segment-form";
//        }
//    }


// This helper method will prevent code duplication
private void populateSegmentFormModel(Model model, Long tenantId) {
    model.addAttribute("distinctCities", userRepository.findDistinctCitiesByTenantId(tenantId));
    model.addAttribute("allUserTypes", UserType.values());
  //  model.addAttribute("allGenders", Arrays.asList("Male", "Female"));

    List<UserResponseDTO> tenantUsers = userService.getUsersByTenant(tenantId);
    try {
        model.addAttribute("tenantUsersJson", objectMapper.writeValueAsString(tenantUsers));
    } catch (JsonProcessingException e) {
        // Provide a fallback empty array if JSON processing fails
        model.addAttribute("tenantUsersJson", "[]");
        // Optionally log the error
    }
}

    @GetMapping("/new")
    public String showCreateSegmentForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Tenant currentTenant = getCurrentTenant(userDetails);
        populateSegmentFormModel(model, currentTenant.getId());
        model.addAttribute("segmentRequest", new SegmentRuleRequest());
        return "segment-form";
    }

    @PostMapping("/create")
    public String createSegment(@Valid @ModelAttribute("segmentRequest") SegmentRuleRequest segmentRuleRequest,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        Tenant currentTenant = getCurrentTenant(userDetails);

        if (bindingResult.hasErrors()) {
            // If validation fails, repopulate the model and return to the form
            populateSegmentFormModel(model, currentTenant.getId());
            return "segment-form";
        }

        try {
            segmentService.createSegmentFromRules(segmentRuleRequest, currentTenant.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Segment '" + segmentRuleRequest.getName() + "' created successfully!");
            return "redirect:/dashboard/segments";
        } catch (Exception e) {
            // If an exception occurs, repopulate the model and return to the form with an error message
            model.addAttribute("errorMessage", "Failed to create segment: " + e.getMessage());
            populateSegmentFormModel(model, currentTenant.getId());
            return "segment-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteSegment(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        Tenant currentTenant = getCurrentTenant(userDetails);

        try {
            segmentService.deleteSegment(id, currentTenant.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Segment with ID " + id + " has been deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete segment. Error: " + e.getMessage());
        }

        return "redirect:/dashboard/segments";
    }
}