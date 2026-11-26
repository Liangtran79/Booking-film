package com.cinema.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "user_name", nullable = false)
    private String username;

    @Column(name = "user_email", nullable = false, unique = true)
    private String email;

    @Column(name = "user_password", nullable = false)
    private String password;

    @Column(name = "user_role")
    @Builder.Default
    private String role = "Customer";

    @Column(name = "IsStaff")
    @Builder.Default
    private Boolean isStaff = false;

    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(this.role) || Boolean.TRUE.equals(this.isStaff);
    }
}
