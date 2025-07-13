package com.example.MessageService.segment.controller;

import com.example.MessageService.security.dto.UserResponseDTO;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.repository.TenantRepository;
import com.example.MessageService.security.service.UserService;
import com.example.MessageService.segment.dto.SegmentRequest;
import com.example.MessageService.segment.dto.SegmentResponse;
import com.example.MessageService.segment.service.SegmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/dashboard/segments")
@RequiredArgsConstructor
public class SegmentWebController {

    private final SegmentService segmentService;
    private final UserService userService;
    private final TenantRepository tenantRepository;

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


    @GetMapping("/new")
    public String showCreateSegmentForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Tenant currentTenant = getCurrentTenant(userDetails);
        List<UserResponseDTO> tenantUsers = userService.getUsersByTenant(currentTenant.getId());
        model.addAttribute("segmentRequest", new SegmentRequest());
        model.addAttribute("tenantUsers", tenantUsers);
        return "segment-form";
    }

    @PostMapping("/create")
    public String createSegment(@Valid @ModelAttribute("segmentRequest") SegmentRequest segmentRequest,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        Tenant currentTenant = getCurrentTenant(userDetails);

        if (bindingResult.hasErrors()) {
            List<UserResponseDTO> tenantUsers = userService.getUsersByTenant(currentTenant.getId());
            model.addAttribute("tenantUsers", tenantUsers);
            return "segment-form";
        }

        try {

            segmentRequest.setTenantId(currentTenant.getId());
            segmentService.createSegment(segmentRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Segment '" + segmentRequest.getName() + "' created successfully!");
            return "redirect:/dashboard/segments";
        } catch (Exception e) {

            List<UserResponseDTO> tenantUsers = userService.getUsersByTenant(currentTenant.getId());
            model.addAttribute("tenantUsers", tenantUsers);
            model.addAttribute("errorMessage", "Failed to create segment: " + e.getMessage());
            return "segment-form";
        }
    }
}