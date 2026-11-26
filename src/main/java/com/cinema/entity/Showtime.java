package com.cinema.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "suatchieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSuatChieu")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaPhim", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaPhong", nullable = false)
    private Room room;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "NgayChieu", nullable = false)
    private LocalDate showDate;

    @DateTimeFormat(pattern = "HH:mm:ss")
    @Column(name = "GioBatDau")
    private LocalTime startTime;

    @Column(name = "GiaVe", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal price = new BigDecimal("75000.00");
}
