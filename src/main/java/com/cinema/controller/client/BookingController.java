package com.cinema.controller.client;

import com.cinema.dto.BookingRequestDto;
import com.cinema.entity.Bill;
import com.cinema.entity.Movie;
import com.cinema.entity.Room;
import com.cinema.entity.Showtime;
import com.cinema.entity.User;
import com.cinema.service.BillService;
import com.cinema.service.MovieService;
import com.cinema.service.RoomService;
import com.cinema.service.ShowtimeService;
import com.cinema.service.TelegramService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class BookingController {

    private final MovieService movieService;
    private final RoomService roomService;
    private final ShowtimeService showtimeService;
    private final BillService billService;
    private final TelegramService telegramService;

    @GetMapping({"/booking/seats", "/chair.php"})
    public String selectSeats(@RequestParam(value = "id", required = false) Integer movieIdParam,
                              @RequestParam(value = "movie_id", required = false) Integer movieIdAlt,
                              @RequestParam(value = "room", required = false) Integer roomIdParam,
                              @RequestParam(value = "room_id", required = false) Integer roomIdAlt,
                              @RequestParam(value = "date", required = false) String dateStrParam,
                              @RequestParam(value = "time", required = false) String timeStrParam,
                              @RequestParam(value = "showtimeId", required = false) Integer showtimeIdParam,
                              @RequestParam(value = "showtime_id", required = false) Integer showtimeIdAlt,
                              Model model) {
        final Integer resolvedShowtimeId = showtimeIdParam != null ? showtimeIdParam : showtimeIdAlt;
        final Integer resolvedMovieId = movieIdParam != null ? movieIdParam : movieIdAlt;
        final Integer resolvedRoomId = roomIdParam != null ? roomIdParam : roomIdAlt;
        String dateStr = dateStrParam;
        String timeStr = timeStrParam;

        Showtime showtime;
        Movie movie;
        Room room;

        if (resolvedShowtimeId != null) {
            showtime = showtimeService.getShowtimeById(resolvedShowtimeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu ID: " + resolvedShowtimeId));
            movie = showtime.getMovie();
            room = showtime.getRoom();
            dateStr = showtime.getShowDate().toString();
            timeStr = showtime.getStartTime() != null ? showtime.getStartTime().toString() : "10:00";
        } else {
            if (resolvedMovieId == null || resolvedRoomId == null || dateStr == null || timeStr == null) {
                throw new IllegalArgumentException("Thiếu thông tin suất chiếu hoặc phim!");
            }
            movie = movieService.getMovieById(resolvedMovieId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));
            room = roomService.getRoomById(resolvedRoomId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));

            LocalDate date = LocalDate.parse(dateStr);
            LocalTime time = LocalTime.parse(timeStr.length() == 5 ? timeStr + ":00" : timeStr);

            showtime = showtimeService.findShowtime(resolvedMovieId, resolvedRoomId, date, time)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu"));
        }

        LocalDate date = LocalDate.parse(dateStr);



        List<String> bookedSeats = billService.getBookedSeats(showtime.getId());

        model.addAttribute("movie", movie);
        model.addAttribute("roomDetails", room);
        model.addAttribute("showtime", showtime);
        model.addAttribute("selectedDate", dateStr);
        model.addAttribute("selectedDateFormatted", date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("selectedTime", timeStr);
        model.addAttribute("bookedSeats", bookedSeats);
        model.addAttribute("ticketPrice", showtime.getPrice() != null ? showtime.getPrice().intValue() : 75000);

        return "client/chair";
    }

    @PostMapping({"/booking/payment", "/payment.php"})
    public String preparePayment(HttpServletRequest request,
                                 @RequestParam("movie_id") Integer movieId,
                                 @RequestParam("room_id") Integer roomId,
                                 @RequestParam("date") String dateStr,
                                 @RequestParam("time") String timeStr,
                                 @RequestParam("showtime_id") Integer showtimeId,
                                 @RequestParam("selected_seats") String selectedSeats,
                                 @RequestParam("total_price") Integer totalPrice,
                                 @RequestParam(value = "ticket_price", required = false) Integer ticketPrice,
                                 Model model) {

        Movie movie = movieService.getMovieById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));
        Room room = roomService.getRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));

        List<String> seatsList = Arrays.asList(selectedSeats.split(","));
        int orderId = (int) (System.currentTimeMillis() % 1000000000L);

        LocalDate date = LocalDate.parse(dateStr);

        model.addAttribute("movie", movie);
        model.addAttribute("roomDetails", room);
        model.addAttribute("movieId", movieId);
        model.addAttribute("roomId", roomId);
        model.addAttribute("showtimeId", showtimeId);
        model.addAttribute("selectedDate", dateStr);
        model.addAttribute("selectedDateFormatted", date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("selectedTime", timeStr);
        model.addAttribute("selectedSeats", seatsList);
        model.addAttribute("rawSeats", selectedSeats);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("ticketPrice", (ticketPrice != null) ? ticketPrice : (totalPrice / Math.max(1, seatsList.size())));
        model.addAttribute("orderId", orderId);

        return "client/payment";
    }

    @PostMapping({"/booking/confirm", "/confirm.php"})
    public String confirmBooking(HttpServletRequest request,
                                 @RequestParam("movie_id") Integer movieId,
                                 @RequestParam("room_id") Integer roomId,
                                 @RequestParam("showtime_id") Integer showtimeId,
                                 @RequestParam("selected_seats") String selectedSeats,
                                 @RequestParam("total_price") BigDecimal totalPrice,
                                 @RequestParam(value = "ticket_price", required = false) BigDecimal ticketPrice,
                                 @RequestParam("date") String dateStr,
                                 @RequestParam("time") String timeStr,
                                 @RequestParam("guest_name") String guestName,
                                 @RequestParam("guest_email") String guestEmail,
                                 @RequestParam("guest_phone") String guestPhone,
                                 @RequestParam(value = "payment_method", defaultValue = "momo") String paymentMethod,
                                 HttpSession session,
                                 Model model) {

        Movie movie = movieService.getMovieById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));
        Room room = roomService.getRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));

        Integer userId = (Integer) session.getAttribute("user_id");
        List<String> seatsList = Arrays.asList(selectedSeats.split(","));

        Bill bill = billService.createBill(
                showtimeId,
                userId,
                selectedSeats,
                seatsList.size(),
                totalPrice,
                guestName,
                guestEmail,
                guestPhone
        );

        // Gửi thông báo đơn đặt vé mới về Telegram
        telegramService.sendBookingAlert(bill, movie, room, selectedSeats, dateStr, timeStr);

        LocalDate date = LocalDate.parse(dateStr);
        String qrCodeData = String.format("ORDER:%d|MOVIE:%d|SEATS:%s", bill.getId(), movieId, selectedSeats);

        Map<String, String> paymentMethodLabels = Map.of(
                "momo", "MoMo",
                "vnpay", "VNPay",
                "card", "Thẻ tín dụng/ghi nợ"
        );

        model.addAttribute("paymentSuccess", true);
        model.addAttribute("bill", bill);
        model.addAttribute("movie", movie);
        model.addAttribute("roomDetails", room);
        model.addAttribute("orderId", bill.getId());
        model.addAttribute("selectedDate", dateStr);
        model.addAttribute("selectedDateFormatted", date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("selectedTime", timeStr);
        model.addAttribute("selectedSeats", selectedSeats);
        model.addAttribute("seatCount", seatsList.size());
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("ticketPrice", (ticketPrice != null) ? ticketPrice : totalPrice.divide(BigDecimal.valueOf(Math.max(1, seatsList.size()))));
        model.addAttribute("guestName", guestName);
        model.addAttribute("guestEmail", guestEmail);
        model.addAttribute("guestPhone", guestPhone);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("paymentMethodLabel", paymentMethodLabels.getOrDefault(paymentMethod, paymentMethod));
        model.addAttribute("qrCodeData", qrCodeData);

        return "client/confirm";
    }
}
