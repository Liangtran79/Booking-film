package com.cinema.service.impl;

import com.cinema.entity.User;
import com.cinema.repository.UserRepository;
import com.cinema.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
            // Thay $2y$ hoặc $2b$ thành $2a$ nếu thư viện yêu cầu
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

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role != null ? role : "Customer")
                .isStaff(isStaff != null && isStaff)
                .build();

        return userRepository.save(user);
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
