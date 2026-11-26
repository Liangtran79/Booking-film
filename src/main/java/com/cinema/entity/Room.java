package com.cinema.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "phong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhong")
    private Integer id;

    @Column(name = "TenPhong", nullable = false)
    private String name;
}
