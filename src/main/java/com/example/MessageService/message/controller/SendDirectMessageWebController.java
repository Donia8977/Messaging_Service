package com.example.MessageService.message.controller;

import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.dto.TargetType;
import com.example.MessageService.message.service.MessageService;
import com.example.MessageService.security.dto.UserResponseDTO;
import com.example.MessageService.security.entity.ChannelType;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.repository.TenantRepository;
import com.example.MessageService.security.service.UserService;
import com.example.MessageService.template.dto.TemplateResponse;
import com.example.MessageService.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/dashboard/send-direct")
@RequiredArgsConstructor
public class SendDirectMessageWebController {

    private final UserService userService;
    private final TemplateService templateService;
    private final MessageService messageService;
    private final TenantRepository tenantRepository;

    private Tenant getCurrentTenant(UserDetails userDetails) {
        return tenantRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated tenant not found"));
    }

    /**
     * Step 1: Show a list of all users to choose from.
     */
    @GetMapping("/select-user")
    public String showSelectUserPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Tenant currentTenant = getCurrentTenant(userDetails);
        List<UserResponseDTO> users = userService.getUsersByTenant(currentTenant.getId());
        model.addAttribute("users", users);
        return "send-direct-select-user"; // Renders a new HTML page
    }

    /**
     * Step 2: After a user is selected, show a list of templates.
     */
    @GetMapping("/select-template")
    public String showSelectTemplatePage(@RequestParam("userId") Long userId, Model model) {
        List<TemplateResponse> templates = templateService.getAllTemplates();
        model.addAttribute("templates", templates);
        model.addAttribute("userId", userId); // Pass the chosen userId to the next step
        return "send-direct-select-template"; // Renders another new HTML page
    }

    /**
     * Final Step: Process the submission and send the message to the selected user.
     */
    @PostMapping("/process")
    public String processDirectMessage(
            @RequestParam("userId") Long userId,
            @RequestParam("templateId") Long templateId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Tenant currentTenant = getCurrentTenant(userDetails);

            MessageSchedulerDto request = new MessageSchedulerDto();
            request.setTenantId(currentTenant.getId());
            request.setTemplateId(templateId);
            request.setTargetId(userId); // The target is the selected user
            request.setTargetType(TargetType.USER); // The target type is USER
            request.setChannel(ChannelType.EMAIL); // Assuming email for now

            messageService.processAndRouteMessage(request);

            redirectAttributes.addFlashAttribute("successMessage", "Direct message successfully scheduled!");
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send message: " + e.getMessage());
            return "redirect:/dashboard/send-direct/select-user";
        }
    }
}