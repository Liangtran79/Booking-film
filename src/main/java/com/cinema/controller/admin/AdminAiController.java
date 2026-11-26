package com.cinema.controller.admin;

import com.cinema.dto.AiSqlResult;
import com.cinema.entity.Movie;
import com.cinema.service.MovieService;
import com.cinema.service.OllamaService;
import com.cinema.service.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/ai")
@RequiredArgsConstructor
public class AdminAiController {

    private final OllamaService ollamaService;
    private final MovieService movieService;
    private final TelegramService telegramService;

    private void populateCommonModel(Model model) {
        model.addAttribute("ollamaAvailable", ollamaService.isAvailable());
        model.addAttribute("ollamaModel", ollamaService.getConfiguredModel());
        model.addAttribute("movies", movieService.getAllMovies());
    }

    @GetMapping
    public String aiPage(Model model) {
        populateCommonModel(model);
        return "admin/ai";
    }

    @PostMapping("/query")
    public String runAiQuery(@RequestParam("question") String question, Model model) {
        populateCommonModel(model);
        AiSqlResult result = ollamaService.queryDatabaseWithAi(question);
        model.addAttribute("question", question);
        model.addAttribute("sqlResult", result);
        return "admin/ai";
    }

    @PostMapping("/promo/generate")
    public String generatePromo(@RequestParam("movieId") Integer movieId, Model model) {
        populateCommonModel(model);
        model.addAttribute("selectedMovieId", movieId);

        Movie movie = movieService.getMovieById(movieId).orElse(null);
        if (movie == null) {
            model.addAttribute("promoError", "Không tìm thấy phim ID: " + movieId);
            return "admin/ai";
        }

        try {
            String draft = ollamaService.generateMoviePromo(movie);
            model.addAttribute("promoDraft", draft);
        } catch (Exception e) {
            model.addAttribute("promoError", "Lỗi sinh nội dung từ Ollama: " + e.getMessage());
        }
        return "admin/ai";
    }

    @PostMapping("/promo/send")
    public String sendPromoToTelegram(@RequestParam("promoText") String promoText,
                                      RedirectAttributes redirectAttributes) {
        if (promoText == null || promoText.isBlank()) {
            redirectAttributes.addFlashAttribute("promoError", "Nội dung tin nhắn không được để trống.");
            return "redirect:/admin/ai";
        }

        telegramService.sendMessage(promoText.trim());
        redirectAttributes.addFlashAttribute("promoSuccess", "Đã gửi bài quảng bá phim qua Telegram Bot thành công!");
        return "redirect:/admin/ai";
    }
}
