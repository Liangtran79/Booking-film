package com.cinema.controller.api;

import com.cinema.service.MovieService;
import com.cinema.service.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
public class ApiTelegramController {

    private final TelegramService telegramService;
    private final MovieService movieService;

    @GetMapping("/test")
    public ResponseEntity<?> testTelegram() {
        boolean connected = telegramService.testConnection();
        Map<String, Object> res = new HashMap<>();
        if (connected) {
            telegramService.sendMessage("🔔 <b>Hệ thống Movies Cinema</b> kết nối Telegram thành công!");
            res.put("success", true);
            res.put("message", "Kết nối Bot Telegram thành công và đã gửi tin nhắn thử nghiệm!");
            return ResponseEntity.ok(res);
        } else {
            res.put("success", false);
            res.put("message", "Chưa thể kết nối tới Bot Telegram. Vui lòng kiểm tra TELEGRAM_BOT_TOKEN và TELEGRAM_CHAT_ID trong .env");
            return ResponseEntity.ok(res);
        }
    }

    @PostMapping("/promote/{movieId}")
    public ResponseEntity<?> promoteMovie(@PathVariable("movieId") Integer movieId) {
        return movieService.getMovieById(movieId)
            .map(movie -> {
                telegramService.broadcastMoviePromotion(movie);
                Map<String, Object> res = new HashMap<>();
                res.put("success", true);
                res.put("message", "Đã phát thông báo quảng bá phim: " + movie.getTitle());
                return ResponseEntity.ok(res);
            })
            .orElseGet(() -> {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Không tìm thấy phim ID: " + movieId);
                return ResponseEntity.badRequest().body(err);
            });
    }
}
