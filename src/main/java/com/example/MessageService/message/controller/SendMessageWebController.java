package com.example.MessageService.message.controller;

import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.dto.TargetType;
import com.example.MessageService.message.service.MessageService;
import com.example.MessageService.security.entity.ChannelType;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.repository.TenantRepository;
import com.example.MessageService.segment.dto.SegmentResponse;
import com.example.MessageService.segment.service.SegmentService;
import com.example.MessageService.template.dto.TemplateResponse;
import com.example.MessageService.template.service.TemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Data;
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
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard/send")
@RequiredArgsConstructor
public class SendMessageWebController {

    private final TemplateService templateService;
    private final SegmentService segmentService;
    private final MessageService messageService;
    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;

    private Tenant getCurrentTenant(UserDetails userDetails) {
        return tenantRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated tenant not found in database"));
    }


    @GetMapping("/select-template")
    public String showSelectTemplatePage(Model model) {
        List<TemplateResponse> templates = templateService.getAllTemplates();
        model.addAttribute("templates", templates);
        return "send-select-template";
    }


    @GetMapping("/select-segment")
    public String showSelectSegmentPage(@RequestParam("templateId") Long templateId,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        Model model) {

        Tenant currentTenant = getCurrentTenant(userDetails);

        List<SegmentResponse> segments = segmentService.getAllSegments(currentTenant.getId());
        TemplateResponse template = templateService.getTemplateById(templateId);

        List<SegmentDisplay> displaySegments = segments.stream()
                .map(segment -> new SegmentDisplay(
                        segment.getId(),
                        segment.getName(),
                        formatRules(segment.getRulesJson()) // Use the helper method
                ))
                .collect(Collectors.toList());

        model.addAttribute("segments", segments);
        model.addAttribute("template", template);
        return "send-select-segment"; // Renders send-select-segment.html
    }


    @PostMapping("/process")
    public String processMessage(
            @RequestParam("templateId") Long templateId,
            @RequestParam("segmentId") Long segmentId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Tenant currentTenant = getCurrentTenant(userDetails);

            MessageSchedulerDto request = new MessageSchedulerDto();
            request.setTenantId(currentTenant.getId());
            request.setTemplateId(templateId);
            request.setTargetId(segmentId);
            request.setTargetType(TargetType.SEGMENT);
            request.setChannel(ChannelType.EMAIL);


            messageService.processAndRouteMessage(request);

            redirectAttributes.addFlashAttribute("successMessage", "Message campaign successfully scheduled!");
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send message: " + e.getMessage());
            return "redirect:/dashboard/send/select-segment?templateId=" + templateId;
        }
    }

    @Data
    @AllArgsConstructor
    private static class SegmentDisplay {
        private final Long id;
        private final String name;
        private final String formattedRules;
    }

    private String formatRules(String rulesJson) {
        if (rulesJson == null || rulesJson.isBlank() || rulesJson.equals("{}")) {
            return "All users in this tenant.";
        }

        try {
            Map<String, Object> rules = objectMapper.readValue(rulesJson, new TypeReference<>() {});
            StringBuilder sb = new StringBuilder("Users ");
            boolean firstRule = true;

            if (rules.get("cities") instanceof List<?> cities && !cities.isEmpty()) {
                sb.append("from cities ").append(cities);
                firstRule = false;
            }

            if (rules.get("userTypes") instanceof List<?> types && !types.isEmpty()) {
                if (!firstRule) sb.append(" and ");
                sb.append("of type ").append(types);
                firstRule = false;
            }

            if (rules.get("age") instanceof Number age && age.intValue() > 0) {
                if (!firstRule) sb.append(" and ");
                sb.append("with age greater than or equal to ").append(age);
            }

            sb.append(".");
            return sb.toString();

        } catch (IOException | JsonProcessingException e) {
            return "Rules are defined but could not be displayed.";
        }
    }
}