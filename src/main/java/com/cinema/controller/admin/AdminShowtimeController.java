package com.cinema.controller.admin;

import com.cinema.entity.Showtime;
import com.cinema.service.MovieService;
import com.cinema.service.RoomService;
import com.cinema.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/admin/shows")
@RequiredArgsConstructor
public class AdminShowtimeController {

    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final RoomService roomService;

    @GetMapping
    public String listShowtimes(Model model) {
        model.addAttribute("showtimes", showtimeService.getAllShowtimes());
        return "admin/show";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("showtime", new Showtime());
        return "admin/create_show";
    }

    @PostMapping("/create")
    public String createShowtime(@RequestParam("movieId") Integer movieId,
                                 @RequestParam("roomId") Integer roomId,
                                 @RequestParam("showDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate,
                                 @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                 @RequestParam("price") BigDecimal price,
                                 RedirectAttributes redirectAttributes) {

        showtimeService.createShowtime(movieId, roomId, showDate, startTime, price);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm suất chiếu thành công!");
        return "redirect:/admin/shows";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Showtime showtime = showtimeService.getShowtimeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu ID: " + id));

        model.addAttribute("showtime", showtime);
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "admin/edit_show";
    }

    @PostMapping("/edit/{id}")
    public String updateShowtime(@PathVariable("id") Integer id,
                                 @RequestParam("movieId") Integer movieId,
                                 @RequestParam("roomId") Integer roomId,
                                 @RequestParam("showDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate,
                                 @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                 @RequestParam("price") BigDecimal price,
                                 RedirectAttributes redirectAttributes) {

        showtimeService.updateShowtime(id, movieId, roomId, showDate, startTime, price);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật suất chiếu thành công!");
        return "redirect:/admin/shows";
    }

    @PostMapping("/delete/{id}")
    public String deleteShowtime(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        showtimeService.deleteShowtime(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa suất chiếu thành công!");
        return "redirect:/admin/shows";
    }
}
