package com.cinema.controller.admin;

import com.cinema.service.BillService;
import com.cinema.service.MovieService;
import com.cinema.service.RoomService;
import com.cinema.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final MovieService movieService;
    private final RoomService roomService;
    private final ShowtimeService showtimeService;
    private final BillService billService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalMovies", movieService.getAllMovies().size());
        model.addAttribute("totalRooms", roomService.getAllRooms().size());
        model.addAttribute("totalShowtimes", showtimeService.getAllShowtimes().size());
        model.addAttribute("totalBills", billService.getAllBills().size());
        model.addAttribute("totalRevenue", billService.getTotalRevenue());
        model.addAttribute("revenueByMovie", billService.getRevenueByMovie());

        return "admin/dashboard";
    }
}
