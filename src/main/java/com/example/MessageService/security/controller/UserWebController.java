package com.example.MessageService.security.controller;

import com.example.MessageService.exception.NotFoundException;
import com.example.MessageService.exception.UnauthorizedException;
import com.example.MessageService.security.dto.CreateUserRequestDTO;
import com.example.MessageService.security.dto.UpdateUserRequestDTO;
import com.example.MessageService.security.dto.UserResponseDTO;
import com.example.MessageService.security.entity.ChannelType;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.entity.UserType;
import com.example.MessageService.security.repository.TenantRepository;
import com.example.MessageService.security.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/dashboard/users")
@RequiredArgsConstructor
public class UserWebController {

    private final UserService userService;
    private final TenantRepository tenantRepository;

    private Tenant getCurrentTenant(UserDetails userDetails) {
        return tenantRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated tenant not found"));
    }

    @GetMapping("/new")
    public String showCreateUserForm(Model model) {

        model.addAttribute("userRequest", new CreateUserRequestDTO());
        model.addAttribute("allUserTypes", UserType.values());
        model.addAttribute("allChannels", ChannelType.values());
        return "user-form";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("userRequest") CreateUserRequestDTO userRequest,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("allUserTypes", UserType.values());
            model.addAttribute("allChannels", ChannelType.values());
            return "user-form";
        }

        try {
            Tenant currentTenant = getCurrentTenant(userDetails);
            userService.createUser(currentTenant.getId(), userRequest);
            redirectAttributes.addFlashAttribute("successMessage", "User '" + userRequest.getUsername() + "' created successfully!");
            return "redirect:/dashboard";
        } catch (Exception e) {

            model.addAttribute("errorMessage", "Failed to create user: " + e.getMessage());
            model.addAttribute("allUserTypes", UserType.values());
            model.addAttribute("allChannels", ChannelType.values());
            return "user-form";
        }
    }

    @GetMapping
    public String listUsers(Model model, @AuthenticationPrincipal UserDetails userDetails) {

        Tenant currentTenant = getCurrentTenant(userDetails);
        List<UserResponseDTO> users = userService.getUsersByTenant(currentTenant.getId());
        model.addAttribute("users", users);
        return "users-list";
    }

    @PostMapping("/{userId}/delete")
    public String deleteUser(@PathVariable Long userId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {

        Tenant currentTenant = getCurrentTenant(userDetails);
        try {
            userService.deleteUser(userId, currentTenant.getId());
            redirectAttributes.addFlashAttribute("successMessage", "User with ID " + userId + " has been deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/dashboard/users";
    }

    @PostMapping("/delete-all")
    public String deleteAllUsers(@AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {

        Tenant currentTenant = getCurrentTenant(userDetails);
        try {
            userService.deleteAllUsersByTenant(currentTenant.getId());
            redirectAttributes.addFlashAttribute("successMessage", "All users have been successfully deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting all users: " + e.getMessage());
        }
        return "redirect:/dashboard/users";
    }


    @GetMapping("/{userId}/edit")
    public String showEditUserForm(@PathVariable Long userId, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Tenant currentTenant = getCurrentTenant(userDetails);
        try {
            UserResponseDTO userDto = userService.getUserById(userId, currentTenant.getId());


            UpdateUserRequestDTO userRequest = new UpdateUserRequestDTO();
            userRequest.setUsername(userDto.getUsername());
            userRequest.setEmail(userDto.getEmail());
            userRequest.setPhone(userDto.getPhone());
            userRequest.setCity(userDto.getCity());
            userRequest.setUserType(userDto.getUserType());
            userRequest.setUserType(userDto.getUserType());

            model.addAttribute("userRequest", userRequest);
            model.addAttribute("userId", userId);
            model.addAttribute("allUserTypes", UserType.values());
            model.addAttribute("allChannels", ChannelType.values());

            model.addAttribute("userRequest", userRequest);
            return "user-edit-form";
        } catch (Exception e) {
            return "redirect:/dashboard/users";
        }
    }

    @PostMapping("/{userId}/update")
    public String updateUser(@PathVariable Long userId,
                             @Valid @ModelAttribute("userRequest") UpdateUserRequestDTO userRequest,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("userId", userId);
            model.addAttribute("allUserTypes", UserType.values());
            model.addAttribute("allChannels", ChannelType.values());
            return "user-edit-form";
        }

        Tenant currentTenant = getCurrentTenant(userDetails);
        try {

            userService.updateUser(userId, currentTenant.getId(), userRequest);

            redirectAttributes.addFlashAttribute("successMessage", "User '" + userRequest.getUsername() + "' updated successfully!");
            return "redirect:/dashboard/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update user. Error: " + e.getMessage());
            return "redirect:/dashboard/users";
        }
    }


}