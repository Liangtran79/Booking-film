package com.cinema.service.impl;

import com.cinema.entity.Movie;
import com.cinema.entity.Room;
import com.cinema.entity.Showtime;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.RoomRepository;
import com.cinema.repository.ShowtimeRepository;
import com.cinema.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    @Override
    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAllOrderByDateDescTimeAsc();
    }

    @Override
    public Optional<Showtime> getShowtimeById(Integer id) {
        return showtimeRepository.findById(id);
    }

    @Override
    @Transactional
    public Showtime createShowtime(Integer movieId, Integer roomId, LocalDate showDate, LocalTime startTime, BigDecimal price) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim ID: " + movieId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng ID: " + roomId));

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .room(room)
                .showDate(showDate)
                .startTime(startTime)
                .price(price != null ? price : new BigDecimal("75000.00"))
                .build();

        return showtimeRepository.save(showtime);
    }

    @Override
    @Transactional
    public Showtime updateShowtime(Integer id, Integer movieId, Integer roomId, LocalDate showDate, LocalTime startTime, BigDecimal price) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu ID: " + id));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim ID: " + movieId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng ID: " + roomId));

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setShowDate(showDate);
        showtime.setStartTime(startTime);
        if (price != null) {
            showtime.setPrice(price);
        }

        return showtimeRepository.save(showtime);
    }

    @Override
    @Transactional
    public void deleteShowtime(Integer id) {
        showtimeRepository.deleteById(id);
    }

    @Override
    public List<LocalDate> getShowDatesForMovie(Integer movieId) {
        return showtimeRepository.findDistinctDatesByMovie(movieId, LocalDate.now());
    }

    @Override
    public List<LocalTime> getShowTimesForMovieRoomDate(Integer movieId, Integer roomId, LocalDate date) {
        return showtimeRepository.findTimesByMovieAndRoomAndDate(movieId, roomId, date);
    }

    @Override
    public Optional<Showtime> findShowtime(Integer movieId, Integer roomId, LocalDate date, LocalTime time) {
        return showtimeRepository.findByMovieIdAndRoomIdAndShowDateAndStartTime(movieId, roomId, date, time);
    }
}
