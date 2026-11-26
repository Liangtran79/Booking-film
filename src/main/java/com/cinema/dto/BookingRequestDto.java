package com.cinema.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookingRequestDto {
    private Integer movieId;
    private Integer roomId;
    private Integer showtimeId;
    private String date;
    private String time;
    private String selectedSeats;
    private BigDecimal totalPrice;
    private BigDecimal ticketPrice;

    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String paymentMethod;
}
