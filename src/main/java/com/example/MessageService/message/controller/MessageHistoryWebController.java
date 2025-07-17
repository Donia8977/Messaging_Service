package com.example.MessageService.message.controller;

import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.service.MessageHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/messages")
@RequiredArgsConstructor
public class MessageHistoryWebController {

    private final MessageHistoryService messageHistoryService;

    @GetMapping
    public String showMessageHistory(Model model) {
        List<Message> messages = messageHistoryService.getAllMessages();
        model.addAttribute("messages", messages);
        return "admin-message-history";
    }

    @PostMapping("/{messageId}/resend")
    public String resendMessage(@PathVariable Long messageId, RedirectAttributes redirectAttributes) {
        try {
            messageHistoryService.resendMessage(messageId);
            redirectAttributes.addFlashAttribute("successMessage", "Message #" + messageId + " has been sent for reprocessing.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not resend message #" + messageId + ". Error: " + e.getMessage());
        }
        return "redirect:/admin/messages";
    }
}