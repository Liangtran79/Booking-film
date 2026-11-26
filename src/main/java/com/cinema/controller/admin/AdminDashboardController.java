package com.cinema.controller.admin;

import com.cinema.entity.Bill;
import com.cinema.entity.Movie;
import com.cinema.entity.User;
import com.cinema.service.BillService;
import com.cinema.service.MovieService;
import com.cinema.service.RoomService;
import com.cinema.service.ShowtimeService;
import com.cinema.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final MovieService movieService;
    private final RoomService roomService;
    private final ShowtimeService showtimeService;
    private final BillService billService;
    private final UserService userService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        List<Movie> allMovies = movieService.getAllMovies();
        List<Movie> nowPlayingMovies = movieService.getNowPlaying();
        List<Movie> comingSoonMovies = movieService.getComingSoon();
        List<Bill> allBills = billService.getAllBills();
        List<User> allUsers = userService.getAllUsers();

        int totalTicketsSold = allBills.stream()
                .mapToInt(b -> b.getQuantity() != null ? b.getQuantity() : 1)
                .sum();

        long verifiedUsersCount = allUsers.stream()
                .filter(u -> Boolean.TRUE.equals(u.getEmailVerified()))
                .count();

        List<Bill> recentBills = allBills.stream()
                .sorted(Comparator.comparing(Bill::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .toList();

        List<Movie> recentMovies = allMovies.stream()
                .sorted(Comparator.comparing(Movie::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .toList();

        model.addAttribute("totalMovies", allMovies.size());
        model.addAttribute("nowPlayingCount", nowPlayingMovies.size());
        model.addAttribute("comingSoonCount", comingSoonMovies.size());
        model.addAttribute("totalRooms", roomService.getAllRooms().size());
        model.addAttribute("totalShowtimes", showtimeService.getAllShowtimes().size());
        model.addAttribute("totalBills", allBills.size());
        model.addAttribute("totalTicketsSold", totalTicketsSold);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("verifiedUsersCount", verifiedUsersCount);
        model.addAttribute("totalRevenue", billService.getTotalRevenue());
        model.addAttribute("revenueByMovie", billService.getRevenueByMovie());
        model.addAttribute("recentBills", recentBills);
        model.addAttribute("recentMovies", recentMovies);

        return "admin/dashboard";
    }
}
