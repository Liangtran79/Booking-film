package com.cinema.controller.admin;

import com.cinema.entity.User;
import com.cinema.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/user";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/create_user";
    }

    @PostMapping("/create")
    public String createUser(@RequestParam("username") String username,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             @RequestParam(value = "role", defaultValue = "Customer") String role,
                             @RequestParam(value = "isStaff", defaultValue = "false") Boolean isStaff,
                             RedirectAttributes redirectAttributes) {

        try {
            userService.register(username, email, password, role, isStaff);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm người dùng thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users/create";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + id));
        model.addAttribute("user", user);
        return "admin/edit_user";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable("id") Integer id,
                             @RequestParam("username") String username,
                             @RequestParam("email") String email,
                             @RequestParam("role") String role,
                             @RequestParam(value = "isStaff", defaultValue = "false") Boolean isStaff,
                             @RequestParam(value = "newPassword", required = false) String newPassword,
                             RedirectAttributes redirectAttributes) {

        userService.updateUser(id, username, email, role, isStaff);
        if (newPassword != null && !newPassword.isBlank()) {
            userService.updatePassword(id, newPassword);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật người dùng thành công!");
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa người dùng thành công!");
        return "redirect:/admin/users";
    }
}
