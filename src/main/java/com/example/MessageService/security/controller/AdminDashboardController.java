package com.example.MessageService.security.controller;

import com.example.MessageService.message.entity.MessageStatus;
import com.example.MessageService.message.repository.MessageRepository;
import com.example.MessageService.message.service.MessageHistoryService;
import com.example.MessageService.security.dto.RegisterRequestDTO;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.exception.EmailAlreadyExistsException;
import com.example.MessageService.security.repository.TenantRepository;
import com.example.MessageService.security.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminService adminService;
    private final TenantRepository tenantRepository;
    private final MessageHistoryService messageHistoryService;


    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {

        List<Tenant> admins = tenantRepository.findAll().stream()
                .filter(tenant -> tenant.getRole().name().equals("ADMIN"))
                .collect(Collectors.toList());
        model.addAttribute("admins", admins);

        return "admin-dashboard";
    }

    /**
     * Displays the form to create a new administrator.
     */
    @GetMapping("/create-admin")
    public String showCreateAdminForm(Model model) {
        model.addAttribute("adminRequest", new RegisterRequestDTO());
        return "admin-create-form";
    }

    /**
     * Processes the submission of the "create admin" form.
     */
    @PostMapping("/create-admin")
    public String createAdmin(
            @Valid @ModelAttribute("adminRequest") RegisterRequestDTO req,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "admin-create-form"; // If there are validation errors, show the form again
        }
        try {

            adminService.createAdmin(req);
            redirectAttributes.addFlashAttribute("successMessage", "New administrator created successfully!");
            return "redirect:/admin/dashboard";
        } catch (EmailAlreadyExistsException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin-create-form";
        }
    }
}
