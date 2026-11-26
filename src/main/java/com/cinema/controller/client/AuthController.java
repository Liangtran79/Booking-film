package com.cinema.controller.client;

import com.cinema.entity.User;
import com.cinema.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "next", required = false) String next,
                            @RequestParam(value = "tab", required = false, defaultValue = "login") String tab,
                            HttpSession session,
                            Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            if (next != null && !next.isBlank()) return "redirect:" + next;
            return currentUser.isAdmin() ? "redirect:/admin/dashboard" : "redirect:/";
        }

        model.addAttribute("next", next);
        model.addAttribute("active_tab", tab);
        return "client/login";
    }

    @PostMapping("/login")
    public String handleAuth(@RequestParam(value = "action", defaultValue = "login") String action,
                             @RequestParam(value = "email", required = false) String email,
                             @RequestParam(value = "name", required = false) String name,
                             @RequestParam(value = "password", required = false) String password,
                             @RequestParam(value = "next", required = false) String next,
                             HttpSession session,
                             Model model) {

        model.addAttribute("next", next);

        if ("register".equalsIgnoreCase(action)) {
            model.addAttribute("active_tab", "register");
            model.addAttribute("name", name);
            model.addAttribute("email", email);

            if (name == null || name.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
                model.addAttribute("register_error", "Vui lòng nhập đầy đủ thông tin!");
                return "client/login";
            }

            try {
                User user = userService.register(name.trim(), email.trim(), password, "Customer", false);
                session.setAttribute("currentUser", user);
                session.setAttribute("user_id", user.getId());
                session.setAttribute("user_name", user.getUsername());
                session.setAttribute("user_email", user.getEmail());
                session.setAttribute("user_role", user.getRole());
                session.setAttribute("is_staff", user.isAdmin());

                if (next != null && !next.isBlank()) return "redirect:" + next;
                return "redirect:/";
            } catch (Exception e) {
                model.addAttribute("register_error", "Email hoặc Tên tài khoản này đã được sử dụng!");
                return "client/login";
            }
        } else {
            // Đăng nhập
            model.addAttribute("active_tab", "login");
            model.addAttribute("email", email);

            if (email == null || email.isBlank() || password == null || password.isBlank()) {
                model.addAttribute("login_error", "Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
                return "client/login";
            }

            Optional<User> userOpt = userService.authenticate(email.trim(), password);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                session.setAttribute("currentUser", user);
                session.setAttribute("user_id", user.getId());
                session.setAttribute("user_name", user.getUsername());
                session.setAttribute("user_email", user.getEmail());
                session.setAttribute("user_role", user.getRole());
                session.setAttribute("is_staff", user.isAdmin());

                if (next != null && !next.isBlank()) return "redirect:" + next;
                return user.isAdmin() ? "redirect:/admin/dashboard" : "redirect:/";
            } else {
                model.addAttribute("login_error", "Tài khoản hoặc mật khẩu không chính xác!");
                return "client/login";
            }
        }
    }

    @GetMapping("/register")
    public String registerPage(@RequestParam(value = "next", required = false) String next) {
        return "redirect:/login?tab=register" + (next != null ? "&next=" + next : "");
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request) {
        session.invalidate();
        return "redirect:/";
    }
}
