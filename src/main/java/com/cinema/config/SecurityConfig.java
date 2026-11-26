package com.cinema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Cho phép form POST thuận tiện cho client/ajax
            .authorizeHttpRequests(auth -> auth
                // Tài nguyên tĩnh
                .requestMatchers("/css/**", "/js/**", "/img/**", "/uploads/**", "/client/**", "/admin-assets/**").permitAll()
                // API và trang public
                .requestMatchers("/", "/home", "/index", "/movies", "/movie/**", "/booking/**", "/api/**", "/login", "/register", "/logout").permitAll()
                // Các trang quản trị admin
                .requestMatchers("/admin/**").permitAll() // Có thể chuyển sang hasRole("ADMIN") khi tích hợp UserDetailsService
                .anyRequest().permitAll()
            )
            .formLogin(form -> form.disable())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}
