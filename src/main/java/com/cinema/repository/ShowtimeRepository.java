package com.cinema.repository;

import com.cinema.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {

    List<Showtime> findByMovieId(Integer movieId);

    List<Showtime> findByRoomId(Integer roomId);

    List<Showtime> findByShowDate(LocalDate showDate);

    List<Showtime> findByMovieIdAndRoomIdAndShowDate(Integer movieId, Integer roomId, LocalDate showDate);

    Optional<Showtime> findByMovieIdAndRoomIdAndShowDateAndStartTime(
            Integer movieId, Integer roomId, LocalDate showDate, LocalTime startTime);

    @Query("SELECT DISTINCT s.showDate FROM Showtime s WHERE s.movie.id = :movieId AND s.showDate >= :today ORDER BY s.showDate ASC")
    List<LocalDate> findDistinctDatesByMovie(@Param("movieId") Integer movieId, @Param("today") LocalDate today);

    @Query("SELECT s.startTime FROM Showtime s WHERE s.movie.id = :movieId AND s.room.id = :roomId AND s.showDate = :date ORDER BY s.startTime ASC")
    List<LocalTime> findTimesByMovieAndRoomAndDate(
            @Param("movieId") Integer movieId,
            @Param("roomId") Integer roomId,
            @Param("date") LocalDate date);

    @Query("SELECT s FROM Showtime s ORDER BY s.showDate DESC, s.startTime ASC")
    List<Showtime> findAllOrderByDateDescTimeAsc();
}
