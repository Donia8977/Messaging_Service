package com.example.MessageService.security.controller;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class TenantDashboardController {


    private final TenantRepository tenantRepository;

    @GetMapping
    public String showDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {

        Tenant currentTenant = tenantRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated tenant not found in database"));

        model.addAttribute("tenantName", currentTenant.getName());
        return "dashboard";
    }
}