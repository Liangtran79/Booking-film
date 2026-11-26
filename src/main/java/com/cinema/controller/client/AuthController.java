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

                // Nếu cần xác thực email
                if (Boolean.FALSE.equals(user.getEmailVerified())) {
                    model.addAttribute("active_tab", "login");
                    model.addAttribute("login_success", "Đăng ký thành công! Một email kích hoạt đã được gửi tới " + email.trim() + ". Vui lòng kiểm tra hộp thư (kể cả mục Spam) để kích hoạt tài khoản.");
                    return "client/login";
                }

                // Nếu đã được xác thực (hoặc admin)
                session.setAttribute("currentUser", user);
                session.setAttribute("user_id", user.getId());
                session.setAttribute("user_name", user.getUsername());
                session.setAttribute("user_email", user.getEmail());
                session.setAttribute("user_role", user.getRole());
                session.setAttribute("is_staff", user.isAdmin());

                if (next != null && !next.isBlank()) return "redirect:" + next;
                return "redirect:/";
            } catch (Exception e) {
                model.addAttribute("register_error", e.getMessage() != null ? e.getMessage() : "Email hoặc Tên tài khoản này đã được sử dụng!");
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

                // Kiểm tra đã xác thực email chưa
                if (Boolean.FALSE.equals(user.getEmailVerified())) {
                    model.addAttribute("login_error", "Tài khoản của bạn chưa kích hoạt email. Vui lòng kiểm tra hộp thư để xác nhận!");
                    model.addAttribute("unverified_email", user.getEmail());
                    return "client/login";
                }

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

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam(value = "token", required = false) String token, Model model) {
        boolean success = userService.verifyEmail(token);
        model.addAttribute("success", success);
        if (success) {
            model.addAttribute("message", "Xác thực email thành công! Tài khoản của bạn đã được kích hoạt.");
        } else {
            model.addAttribute("message", "Liên kết xác thực không hợp lệ hoặc đã hết hạn sử dụng.");
        }
        return "client/verify_result";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam("email") String email, Model model) {
        boolean sent = userService.resendVerificationEmail(email);
        if (sent) {
            model.addAttribute("login_success", "Đã gửi lại email kích hoạt tới " + email + ". Vui lòng kiểm tra hộp thư!");
        } else {
            model.addAttribute("login_error", "Không thể gửi email kích hoạt. Email không tồn tại hoặc tài khoản đã được kích hoạt rồi.");
        }
        model.addAttribute("active_tab", "login");
        return "client/login";
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
