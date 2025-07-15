package com.example.MessageService.security.controller;

import com.example.MessageService.security.dto.LoginRequestDTO;
import com.example.MessageService.security.dto.RegisterRequestDTO;
import com.example.MessageService.security.exception.EmailAlreadyExistsException;
import com.example.MessageService.security.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthWebController {
    private final AuthService authService;

    public AuthWebController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequestDTO req,
            BindingResult binding,
            Model model
    ) {
        if (binding.hasErrors()) {
            return "register";
        }
        try {
            authService.registerTenant(req);
            return "redirect:/login?registered";
        } catch (EmailAlreadyExistsException e) {
            binding.rejectValue("email", "error.registerRequest", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(
            Model model,
            @RequestParam(value = "registered", required = false) String justRegistered
    ) {
        model.addAttribute("loginRequest", new LoginRequestDTO());
        model.addAttribute("registered", justRegistered != null);
        return "login";
    }


}
