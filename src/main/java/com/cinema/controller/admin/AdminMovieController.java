package com.cinema.controller.admin;

import com.cinema.entity.Category;
import com.cinema.entity.Movie;
import com.cinema.service.MovieService;
import com.cinema.service.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final MovieService movieService;
    private final TelegramService telegramService;

    @GetMapping
    public String listMovies(@RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             Model model) {
        Page<Movie> moviePage = movieService.getMoviesPaginated(PageRequest.of(page, size));
        model.addAttribute("moviePage", moviePage);
        return "admin/movie";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("categories", movieService.getAllCategories());
        model.addAttribute("movie", new Movie());
        return "admin/create_movie";
    }

    @PostMapping("/create")
    public String createMovie(@RequestParam("title") String title,
                              @RequestParam("categoryId") Integer categoryId,
                              @RequestParam("releaseDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate,
                              @RequestParam("description") String description,
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              RedirectAttributes redirectAttributes) {

        Category category = movieService.getCategoryById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Thể loại không hợp lệ"));

        Movie movie = Movie.builder()
                .title(title)
                .category(category)
                .releaseDate(releaseDate)
                .description(description)
                .build();

        movieService.saveMovie(movie, image);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm phim mới thành công!");
        return "redirect:/admin/movies";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Movie movie = movieService.getMovieById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim ID: " + id));

        model.addAttribute("movie", movie);
        model.addAttribute("categories", movieService.getAllCategories());
        return "admin/edit_movie";
    }

    @PostMapping("/edit/{id}")
    public String updateMovie(@PathVariable("id") Integer id,
                              @RequestParam("title") String title,
                              @RequestParam("categoryId") Integer categoryId,
                              @RequestParam("releaseDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate,
                              @RequestParam("description") String description,
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              RedirectAttributes redirectAttributes) {

        movieService.updateMovie(id, title, categoryId, releaseDate, description, image);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phim thành công!");
        return "redirect:/admin/movies";
    }

    @PostMapping("/delete/{id}")
    public String deleteMovie(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        movieService.deleteMovie(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa phim thành công!");
        return "redirect:/admin/movies";
    }

    @PostMapping("/{id}/broadcast-telegram")
    public String broadcastToTelegram(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        movieService.getMovieById(id).ifPresent(telegramService::broadcastMoviePromotion);
        redirectAttributes.addFlashAttribute("successMessage", "Đã gửi thông báo quảng bá phim lên kênh Telegram!");
        return "redirect:/admin/movies";
    }
}
