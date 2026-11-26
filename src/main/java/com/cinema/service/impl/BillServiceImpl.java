package com.cinema.service.impl;

import com.cinema.dto.RevenueByMovieDto;
import com.cinema.entity.Bill;
import com.cinema.entity.Showtime;
import com.cinema.entity.User;
import com.cinema.repository.BillRepository;
import com.cinema.repository.ShowtimeRepository;
import com.cinema.repository.UserRepository;
import com.cinema.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final ShowtimeRepository showtimeRepository;
    private final UserRepository userRepository;

    @Override
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    @Override
    public Page<Bill> getBillsPaginated(Pageable pageable) {
        return billRepository.findAll(pageable);
    }

    @Override
    public Optional<Bill> getBillById(Integer id) {
        return billRepository.findById(id);
    }

    @Override
    @Transactional
    public Bill createBill(Integer showtimeId, Integer userId, String seats, Integer quantity, BigDecimal totalAmount,
                           String guestName, String guestEmail, String guestPhone) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu ID: " + showtimeId));

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        // Tạo mã hóa đơn ngẫu nhiên kiểu INT(11) như bản gốc PHP
        int orderId = 100000 + new Random().nextInt(900000);
        while (billRepository.existsById(orderId)) {
            orderId = 100000 + new Random().nextInt(900000);
        }

        Bill bill = Bill.builder()
                .id(orderId)
                .showtime(showtime)
                .user(user)
                .seats(seats)
                .quantity(quantity != null ? quantity : (seats.contains(",") ? seats.split(",").length : 1))
                .totalAmount(totalAmount)
                .guestName(guestName)
                .guestEmail(guestEmail)
                .guestPhone(guestPhone)
                .build();

        return billRepository.save(bill);
    }

    @Override
    @Transactional
    public Bill updateBill(Integer id, Integer showtimeId, String seats, Integer quantity, BigDecimal totalAmount,
                           String guestName, String guestEmail, String guestPhone) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn ID: " + id));

        if (showtimeId != null) {
            Showtime showtime = showtimeRepository.findById(showtimeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu ID: " + showtimeId));
            bill.setShowtime(showtime);
        }

        bill.setSeats(seats);
        bill.setQuantity(quantity);
        bill.setTotalAmount(totalAmount);
        bill.setGuestName(guestName);
        bill.setGuestEmail(guestEmail);
        bill.setGuestPhone(guestPhone);

        return billRepository.save(bill);
    }

    @Override
    @Transactional
    public void deleteBill(Integer id) {
        billRepository.deleteById(id);
    }

    @Override
    public List<String> getBookedSeats(Integer showtimeId) {
        List<String> seatStrings = billRepository.findBookedSeatsByShowtimeId(showtimeId);
        List<String> bookedSeats = new ArrayList<>();
        for (String s : seatStrings) {
            if (s != null && !s.isBlank()) {
                String[] parts = s.split(",");
                for (String p : parts) {
                    String trimmed = p.trim();
                    if (!trimmed.isEmpty()) {
                        bookedSeats.add(trimmed);
                    }
                }
            }
        }
        return bookedSeats;
    }

    @Override
    public List<Bill> searchBills(String keyword) {
        return billRepository.searchByKeyword(keyword);
    }

    @Override
    public Page<Bill> searchBillsPaginated(String keyword, Pageable pageable) {
        return billRepository.searchByKeyword(keyword, pageable);
    }

    @Override
    public List<Bill> getBillsByDateRange(LocalDate startDate, LocalDate endDate) {
        return billRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return billRepository.calculateTotalRevenue();
    }

    @Override
    public List<RevenueByMovieDto> getRevenueByMovie() {
        List<Object[]> rawList = billRepository.getRevenueByMovieRaw();
        List<RevenueByMovieDto> result = new ArrayList<>();
        for (Object[] row : rawList) {
            Integer movieId = (Integer) row[0];
            String movieTitle = (String) row[1];
            BigDecimal totalRev = (BigDecimal) row[2];
            Long count = (Long) row[3];
            result.add(new RevenueByMovieDto(movieId, movieTitle, totalRev, count));
        }
        return result;
    }

    @Override
    public Map<LocalDate, BigDecimal> getRevenueByDate(LocalDate startDate, LocalDate endDate) {
        List<Object[]> rawList = billRepository.getRevenueByDateRaw(startDate, endDate);
        Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rawList) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal rev = (BigDecimal) row[1];
            map.put(date, rev);
        }
        return map;
    }
}
