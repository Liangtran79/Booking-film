package com.cinema.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hoadon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @Column(name = "MaHoaDon")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaSuatChieu", nullable = false)
    private Showtime showtime;

    @Column(name = "Ghe", nullable = false)
    private String seats;

    @Column(name = "SoLuong")
    private Integer quantity;

    @Column(name = "ThanhTien", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_phone")
    private String guestPhone;
}
