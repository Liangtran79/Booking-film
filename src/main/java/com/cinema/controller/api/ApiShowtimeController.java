package com.cinema.controller.api;

import com.cinema.entity.Room;
import com.cinema.entity.Showtime;
import com.cinema.service.BillService;
import com.cinema.service.RoomService;
import com.cinema.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ApiShowtimeController {

    private final RoomService roomService;
    private final ShowtimeService showtimeService;
    private final BillService billService;

    // Lấy danh sách phòng có suất chiếu của phim vào ngày đã chọn
    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> getRooms(
            @RequestParam("movieId") Integer movieId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(roomService.getRoomsByMovieAndDate(movieId, date));
    }

    // Lấy các giờ chiếu của phim theo phòng và ngày
    @GetMapping("/times")
    public ResponseEntity<List<LocalTime>> getTimes(
            @RequestParam("movieId") Integer movieId,
            @RequestParam("roomId") Integer roomId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showtimeService.getShowTimesForMovieRoomDate(movieId, roomId, date));
    }

    // Lấy thông tin chi tiết và ghế đã đặt cho suất chiếu
    @GetMapping("/details")
    public ResponseEntity<?> getShowtimeDetails(
            @RequestParam("movieId") Integer movieId,
            @RequestParam("roomId") Integer roomId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {

        Optional<Showtime> showtimeOpt = showtimeService.findShowtime(movieId, roomId, date, time);
        if (showtimeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Showtime st = showtimeOpt.get();
        List<String> bookedSeats = billService.getBookedSeats(st.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("showtimeId", st.getId());
        response.put("price", st.getPrice());
        response.put("bookedSeats", bookedSeats);

        return ResponseEntity.ok(response);
    }
}
