package com.cinema.service.impl;

import com.cinema.entity.User;
import com.cinema.repository.UserRepository;
import com.cinema.service.EmailService;
import com.cinema.service.UserService;
import com.cinema.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TelegramService telegramService;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public Optional<User> authenticate(String login, String password) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(login, login);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        String storedHash = user.getPassword();

        // 1. Kiểm tra bằng PasswordEncoder (BCrypt)
        if (storedHash != null && (storedHash.startsWith("$2a$") || storedHash.startsWith("$2y$") || storedHash.startsWith("$2b$"))) {
            String standardBcrypt = storedHash.replaceFirst("^\\$2[yb]\\$", "\\$2a\\$");
            if (passwordEncoder.matches(password, standardBcrypt) || passwordEncoder.matches(password, storedHash)) {
                return Optional.of(user);
            }
        }

        // 2. Tương thích mật khẩu cũ plain text (như Tien123@, Duy123@)
        if (password.equals(storedHash)) {
            // Tự động nâng cấp mật khẩu sang BCrypt
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return Optional.of(user);
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public User register(String username, String email, String rawPassword, String role, Boolean isStaff) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã tồn tại: " + email);
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại: " + username);
        }

        boolean staff = isStaff != null && isStaff;
        String userRole = role != null ? role : "Customer";
        
        // Nếu là tài khoản khách hàng thông thường thì yêu cầu xác thực email
        boolean needVerify = !staff && !"Admin".equalsIgnoreCase(userRole);
        String token = needVerify ? UUID.randomUUID().toString() : null;

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(userRole)
                .isStaff(staff)
                .emailVerified(!needVerify)
                .verificationToken(token)
                .build();

        User savedUser = userRepository.save(user);

        // Gửi thông báo thành viên mới về Telegram
        telegramService.sendNewUserAlert(savedUser);

        if (needVerify) {
            emailService.sendVerificationEmail(email, username, token);
        }

        return savedUser;
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        log.info("Xác thực email thành công cho tài khoản: {}", user.getEmail());
        return true;
    }

    @Override
    @Transactional
    public boolean resendVerificationEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return false; // Đã xác thực rồi
        }

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token);
        log.info("Đã gửi lại email xác thực cho: {}", user.getEmail());
        return true;
    }

    @Override
    @Transactional
    public User updateUser(Integer id, String username, String email, String role, Boolean isStaff) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + id));

        user.setUsername(username);
        user.setEmail(email);
        if (role != null) user.setRole(role);
        if (isStaff != null) user.setIsStaff(isStaff);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void updatePassword(Integer id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + id));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}
