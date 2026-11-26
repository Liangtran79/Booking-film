package com.cinema.service.impl;

import com.cinema.entity.Bill;
import com.cinema.entity.Movie;
import com.cinema.entity.Room;
import com.cinema.entity.User;
import com.cinema.service.TelegramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class TelegramServiceImpl implements TelegramService {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.chat.id:}")
    private String chatId;

    @Value("${telegram.enabled:true}")
    private boolean enabled;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private boolean isConfigured() {
        return enabled && botToken != null && !botToken.isBlank() && chatId != null && !chatId.isBlank();
    }

    @Override
    @Async
    public void sendMessage(String messageText) {
        if (!isConfigured()) {
            log.info("[Telegram] Chưa cấu hình BOT_TOKEN hoặc CHAT_ID, bỏ qua gửi tin: {}", messageText);
            return;
        }

        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("chat_id", chatId);
            payload.put("text", messageText);
            payload.put("parse_mode", "HTML");
            payload.put("disable_web_page_preview", true);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            boolean sent = false;
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    restTemplate.postForObject(url, entity, String.class);
                    log.info("[Telegram] Gửi tin nhắn thành công đến chat_id: {} (Lần thử {})", chatId, attempt);
                    sent = true;
                    break;
                } catch (Exception ex) {
                    if (attempt == 3) throw ex;
                    try { Thread.sleep(600); } catch (InterruptedException ignored) {}
                }
            }
        } catch (Exception e) {
            log.warn("[Telegram] Gửi tin nhắn thất bại sau các lần thử: {}", e.getMessage());
        }
    }

    @Override
    @Async
    public void sendBookingAlert(Bill bill, Movie movie, Room room, String seats, String date, String time) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        String amountFormatted = (bill.getTotalAmount() != null ? nf.format(bill.getTotalAmount()) : "0") + " VNĐ";

        String message = String.format("""
            🎟️ <b>ĐƠN ĐẶT VÉ MỚI #%d</b>
            ━━━━━━━━━━━━━━━━━
            🎬 <b>Phim:</b> %s
            🏢 <b>Phòng:</b> %s
            📅 <b>Suất chiếu:</b> %s - %s
            💺 <b>Ghế ngồi:</b> %s (%d vé)
            💰 <b>Tổng tiền:</b> <code>%s</code>
            
            👤 <b>Khách hàng:</b> %s
            📧 <b>Email:</b> %s
            📞 <b>Số điện thoại:</b> %s
            ⏰ <b>Thời gian:</b> %s
            """,
            bill.getId(),
            movie != null ? movie.getTitle() : "N/A",
            room != null ? room.getName() : "N/A",
            time, date,
            seats,
            bill.getQuantity() != null ? bill.getQuantity() : 1,
            amountFormatted,
            bill.getGuestName() != null ? bill.getGuestName() : "Khách vãng lai",
            bill.getGuestEmail() != null ? bill.getGuestEmail() : "N/A",
            bill.getGuestPhone() != null ? bill.getGuestPhone() : "N/A",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"))
        );

        sendMessage(message);
    }

    @Override
    @Async
    public void sendNewUserAlert(User user) {
        String message = String.format("""
            👤 <b>THÀNH VIÊN MỚI ĐĂNG KÝ</b>
            ━━━━━━━━━━━━━━━━━
            🆔 <b>ID:</b> %d
            📛 <b>Họ tên:</b> %s
            📧 <b>Email:</b> %s
            🛡️ <b>Vai trò:</b> %s
            ⏰ <b>Đăng ký lúc:</b> %s
            """,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"))
        );

        sendMessage(message);
    }

    @Override
    @Async
    public void broadcastMoviePromotion(Movie movie) {
        if (!isConfigured()) {
            log.info("[Telegram] Chưa cấu hình Telegram để quảng bá phim: {}", movie.getTitle());
            return;
        }

        String movieLink = appUrl + "/movie/" + movie.getId();
        String caption = String.format("""
            🔥 <b>BOM TẤN ĐANG CHIẾU TẠI CINEMA</b> 🔥
            ━━━━━━━━━━━━━━━━━
            🎬 <b>%s</b>
            ⏱️ <b>Thời lượng:</b> %s phút
            🎭 <b>Thể loại:</b> %s
            
            📝 <b>Giới thiệu:</b>
            <i>%s</i>
            
            🍿 <i>Đặt vé ngay để giữ những vị trí ghế đẹp nhất rạp hôm nay!</i>
            """,
            movie.getTitle(),
            120,
            movie.getCategory() != null ? movie.getCategory().getName() : "Chiếu rạp",
            movie.getDescription() != null && movie.getDescription().length() > 200 
                ? movie.getDescription().substring(0, 200) + "..." 
                : (movie.getDescription() != null ? movie.getDescription() : "Phim chiếu rạp đặc sắc.")
        );

        // Tạo Inline Keyboard với nút Đặt Vé Ngay
        Map<String, Object> button = new HashMap<>();
        button.put("text", "🎟️ Đặt Vé Phim Ngay");
        button.put("url", movieLink);

        Map<String, Object> replyMarkup = new HashMap<>();
        replyMarkup.put("inline_keyboard", List.of(List.of(button)));

        // Thử gửi ảnh nếu poster là link online
        String photoUrl = movie.getImage();
        boolean sentPhoto = false;

        if (photoUrl != null && (photoUrl.startsWith("http://") || photoUrl.startsWith("https://"))) {
            try {
                String photoApi = "https://api.telegram.org/bot" + botToken + "/sendPhoto";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> payload = new HashMap<>();
                payload.put("chat_id", chatId);
                payload.put("photo", photoUrl);
                payload.put("caption", caption);
                payload.put("parse_mode", "HTML");
                payload.put("reply_markup", replyMarkup);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                restTemplate.postForObject(photoApi, entity, String.class);
                sentPhoto = true;
                log.info("[Telegram] Đã gửi thông báo quảng bá phim kèm ảnh cho phim: {}", movie.getTitle());
            } catch (Exception e) {
                log.warn("[Telegram] Gửi ảnh thất bại ({}), chuyển sang gửi tin nhắn văn bản.", e.getMessage());
            }
        }

        // Nếu không có ảnh online hoặc gửi ảnh thất bại -> Gửi tin nhắn kèm nút bấm
        if (!sentPhoto) {
            try {
                String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> payload = new HashMap<>();
                payload.put("chat_id", chatId);
                payload.put("text", caption);
                payload.put("parse_mode", "HTML");
                payload.put("reply_markup", replyMarkup);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                restTemplate.postForObject(url, entity, String.class);
                log.info("[Telegram] Đã gửi thông báo quảng bá phim dạng văn bản cho phim: {}", movie.getTitle());
            } catch (Exception e) {
                log.warn("[Telegram] Gửi tin quảng bá thất bại: {}", e.getMessage());
            }
        }
    }

    @Override
    public boolean testConnection() {
        if (!isConfigured()) return false;
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/getMe";
            String res = restTemplate.getForObject(url, String.class);
            return res != null && res.contains("\"ok\":true");
        } catch (Exception e) {
            log.warn("[Telegram] Kiểm tra kết nối Bot thất bại: {}", e.getMessage());
            return false;
        }
    }
}
