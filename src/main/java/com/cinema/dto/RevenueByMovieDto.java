package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueByMovieDto {
    private Integer movieId;
    private String movieTitle;
    private BigDecimal totalRevenue;
    private Long ticketCount;
}
