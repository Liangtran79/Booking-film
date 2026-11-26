package com.cinema.repository;

import com.cinema.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Integer> {

    List<Bill> findByUserId(Integer userId);

    List<Bill> findByShowtimeId(Integer showtimeId);

    @Query("SELECT b.seats FROM Bill b WHERE b.showtime.id = :showtimeId")
    List<String> findBookedSeatsByShowtimeId(@Param("showtimeId") Integer showtimeId);

    @Query("SELECT b FROM Bill b WHERE " +
           "LOWER(b.guestName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.guestEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.guestPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.showtime.movie.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Bill> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT b FROM Bill b WHERE " +
           "LOWER(b.guestName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.guestEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.guestPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.showtime.movie.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Bill> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT b FROM Bill b WHERE b.showtime.showDate BETWEEN :startDate AND :endDate ORDER BY b.showtime.showDate DESC")
    List<Bill> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b")
    java.math.BigDecimal calculateTotalRevenue();

    @Query("SELECT p.id, p.title, SUM(b.totalAmount), COUNT(b.id) " +
           "FROM Bill b " +
           "JOIN b.showtime s " +
           "JOIN s.movie p " +
           "GROUP BY p.id, p.title " +
           "ORDER BY SUM(b.totalAmount) DESC")
    List<Object[]> getRevenueByMovieRaw();

    @Query("SELECT s.showDate, SUM(b.totalAmount), COUNT(b.id) " +
           "FROM Bill b " +
           "JOIN b.showtime s " +
           "WHERE s.showDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.showDate " +
           "ORDER BY s.showDate ASC")
    List<Object[]> getRevenueByDateRaw(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
