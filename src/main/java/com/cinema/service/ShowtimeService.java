package com.cinema.service;

import com.cinema.entity.Showtime;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ShowtimeService {
    List<Showtime> getAllShowtimes();
    Optional<Showtime> getShowtimeById(Integer id);
    Showtime createShowtime(Integer movieId, Integer roomId, LocalDate showDate, LocalTime startTime, BigDecimal price);
    Showtime updateShowtime(Integer id, Integer movieId, Integer roomId, LocalDate showDate, LocalTime startTime, BigDecimal price);
    void deleteShowtime(Integer id);

    List<LocalDate> getShowDatesForMovie(Integer movieId);
    List<LocalTime> getShowTimesForMovieRoomDate(Integer movieId, Integer roomId, LocalDate date);
    Optional<Showtime> findShowtime(Integer movieId, Integer roomId, LocalDate date, LocalTime time);
}
