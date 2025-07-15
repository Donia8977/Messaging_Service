package com.example.MessageService.template.controller;

import com.example.MessageService.template.dto.TemplateRequest;
import com.example.MessageService.template.dto.TemplateResponse;
import com.example.MessageService.template.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/dashboard/templates")
@RequiredArgsConstructor
public class TemplateWebController {

    private final TemplateService templateService;


    @GetMapping
    public String listTemplates(Model model) {
        List<TemplateResponse> templates = templateService.getAllTemplates();
        model.addAttribute("templates", templates);
        return "templates-list"; // Renders templates-list.html
    }

    @GetMapping("/new")
    public String showCreateTemplateForm(Model model) {
        model.addAttribute("templateRequest", new TemplateRequest());
        return "template-form";
    }

    // 3. Process the form submission for creating a template
    @PostMapping("/create")
    public String createTemplate(@Valid @ModelAttribute("templateRequest") TemplateRequest templateRequest,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        if (bindingResult.hasErrors()) {
            return "template-form";
        }

        try {
            templateService.createTemplate(templateRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Template '" + templateRequest.getName() + "' created successfully!");
            return "redirect:/dashboard/templates";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create template: " + e.getMessage());
            return "template-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteTemplate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            templateService.deleteTemplate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Template with ID " + id + " has been deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete template with ID " + id + ". Error: " + e.getMessage());
        }
        return "redirect:/dashboard/templates";
    }
}