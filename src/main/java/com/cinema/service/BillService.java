package com.cinema.service;

import com.cinema.dto.RevenueByMovieDto;
import com.cinema.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BillService {
    List<Bill> getAllBills();
    Page<Bill> getBillsPaginated(Pageable pageable);
    Optional<Bill> getBillById(Integer id);
    Bill createBill(Integer showtimeId, Integer userId, String seats, Integer quantity, BigDecimal totalAmount,
                    String guestName, String guestEmail, String guestPhone);
    Bill updateBill(Integer id, Integer showtimeId, String seats, Integer quantity, BigDecimal totalAmount,
                    String guestName, String guestEmail, String guestPhone);
    void deleteBill(Integer id);

    List<String> getBookedSeats(Integer showtimeId);
    List<Bill> searchBills(String keyword);
    Page<Bill> searchBillsPaginated(String keyword, Pageable pageable);
    List<Bill> getBillsByDateRange(LocalDate startDate, LocalDate endDate);
    BigDecimal getTotalRevenue();
    List<RevenueByMovieDto> getRevenueByMovie();
    Map<LocalDate, BigDecimal> getRevenueByDate(LocalDate startDate, LocalDate endDate);
}
