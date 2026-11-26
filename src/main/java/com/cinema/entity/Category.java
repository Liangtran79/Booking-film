package com.cinema.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "theloai")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MATL")
    private Integer id;

    @Column(name = "TENTL", nullable = false)
    private String name;
}
