package com.cinema.service.impl;

import com.cinema.dto.AiSqlResult;
import com.cinema.entity.Movie;
import com.cinema.service.OllamaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaServiceImpl implements OllamaService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ollama.enabled:true}")
    private boolean enabled;

    @Value("${ollama.url:http://host.docker.internal:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:qwen2.5:3b}")
    private String ollamaModel;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    private static final Pattern FORBIDDEN_SQL_PATTERN = Pattern.compile(
            "\\b(INSERT|UPDATE|DELETE|DROP|ALTER|TRUNCATE|CREATE|REPLACE|GRANT|REVOKE|CALL|EXEC|EXECUTE|INTO|SET|SHOW|USE|LOCK|UNLOCK)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LIMIT_PATTERN = Pattern.compile("\\bLIMIT\\s+\\d+", Pattern.CASE_INSENSITIVE);

    private static final String SCHEMA_CONTEXT = """
            Database: MariaDB / MySQL (`cinema`).
            Tables and exact columns:
            1. categories (id, name) -- Thể loại phim
            2. movies (id, title, category_id, release_date, description, image) -- Phim (category_id -> categories.id)
            3. rooms (id, name) -- Phòng chiếu
            4. showtimes (id, movie_id, room_id, show_date, start_time, price) -- Suất chiếu (movie_id -> movies.id, room_id -> rooms.id)
            5. orders (id, user_id, showtime_id, seats, quantity, total_amount, guest_name, guest_email, guest_phone) -- Hóa đơn đặt vé (showtime_id -> showtimes.id, user_id -> users.user_id)
            6. tickets (id, movie_id, price) -- Giá vé tham chiếu theo phim
            7. users (user_id, user_name, user_email, user_role, IsStaff, email_verified) -- Tài khoản người dùng (IsStaff: 1=Admin, 0=Customer; email_verified: 1=Đã xác thực)
            """;

    private RestTemplate createRestTemplate(int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }

    @Override
    public String getConfiguredModel() {
        return ollamaModel;
    }

    @Override
    public boolean isAvailable() {
        if (!enabled || ollamaUrl == null || ollamaUrl.isBlank()) {
            return false;
        }
        try {
            String url = ollamaUrl.replaceAll("/+$", "") + "/api/tags";
            String res = createRestTemplate(2500).getForObject(url, String.class);
            return res != null && res.contains("models");
        } catch (Exception e) {
            log.debug("[Ollama] Không kết nối được tới {}: {}", ollamaUrl, e.getMessage());
            return false;
        }
    }

    @Override
    public String chat(String systemPrompt, String userPrompt, boolean jsonFormat) {
        if (!enabled) {
            throw new IllegalStateException("Ollama AI đang bị tắt trong cấu hình.");
        }

        try {
            String url = ollamaUrl.replaceAll("/+$", "") + "/api/chat";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", ollamaModel);
            body.put("stream", false);
            if (jsonFormat) {
                body.put("format", "json");
            }
            body.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));
            body.put("options", Map.of("temperature", 0.2));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            String responseJson = createRestTemplate(60000).postForObject(url, request, String.class);
            JsonNode root = objectMapper.readTree(responseJson);
            return root.path("message").path("content").asText("").trim();
        } catch (Exception e) {
            log.error("[Ollama] Lỗi gọi API: {}", e.getMessage());
            throw new RuntimeException("Không thể kết nối tới Ollama (" + ollamaUrl + "): " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AiSqlResult queryDatabaseWithAi(String question) {
        long start = System.currentTimeMillis();
        if (question == null || question.isBlank()) {
            return AiSqlResult.builder()
                    .question(question)
                    .error("Vui lòng nhập câu hỏi cần tra cứu.")
                    .elapsedMs(0)
                    .build();
        }

        String systemPrompt = """
                Bạn là chuyên gia phân tích dữ liệu SQL cho hệ thống rạp chiếu phim MariaDB/MySQL.
                Dưới đây là cấu trúc bảng chính xác:
                """ + SCHEMA_CONTEXT + """
                Quy tắc bắt buộc:
                1. Chỉ viết DUY NHẤT 1 câu lệnh SELECT an toàn (tuyệt đối không INSERT, UPDATE, DELETE, DROP, ALTER).
                2. Dùng đúng tên bảng và tên cột tiếng Anh như mô tả ở trên. Đặt alias tiếng Việt không dấu hoặc dễ hiểu nếu cần.
                3. Luôn thêm LIMIT 200 nếu truy vấn trả về nhiều dòng.
                4. Trả về đúng định dạng JSON với 2 trường:
                   {"sql": "SELECT ...", "explanation": "Giải thích ngắn gọn bằng tiếng Việt câu truy vấn này lấy dữ liệu gì."}
                """;

        try {
            String rawJson = chat(systemPrompt, "Câu hỏi: " + question.trim(), true);
            JsonNode parsed = objectMapper.readTree(rawJson);
            String rawSql = parsed.path("sql").asText("").trim();
            String explanation = parsed.path("explanation").asText("").trim();

            String safeSql = validateAndSanitizeSelectSql(rawSql);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(safeSql);

            List<String> columns = new ArrayList<>();
            if (!rows.isEmpty()) {
                columns.addAll(rows.get(0).keySet());
            }

            return AiSqlResult.builder()
                    .question(question)
                    .sql(safeSql)
                    .explanation(explanation)
                    .columns(columns)
                    .rows(rows)
                    .elapsedMs(System.currentTimeMillis() - start)
                    .build();
        } catch (Exception e) {
            return AiSqlResult.builder()
                    .question(question)
                    .error(e.getMessage())
                    .elapsedMs(System.currentTimeMillis() - start)
                    .build();
        }
    }

    private String validateAndSanitizeSelectSql(String rawSql) {
        if (rawSql == null || rawSql.isBlank()) {
            throw new IllegalArgumentException("AI không sinh được câu lệnh SQL hợp lệ.");
        }

        String cleaned = rawSql
                .replaceAll("(?s)```sql\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();

        // Bỏ dấu ; ở cuối nếu có
        while (cleaned.endsWith(";")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }

        // Chặn nhiều câu lệnh hoặc comment SQL nguy hiểm
        if (cleaned.contains(";") || cleaned.contains("--") || cleaned.contains("/*")) {
            throw new SecurityException("Phát hiện cú pháp SQL không được phép (đa câu lệnh hoặc comment).");
        }

        String upper = cleaned.toUpperCase(Locale.ROOT);
        if (!upper.startsWith("SELECT") && !upper.startsWith("WITH")) {
            throw new SecurityException("Chỉ cho phép thực thi câu lệnh SELECT.");
        }

        if (FORBIDDEN_SQL_PATTERN.matcher(cleaned).find()) {
            throw new SecurityException("Câu lệnh SQL chứa từ khóa không được phép (chỉ hỗ trợ đọc dữ liệu).");
        }

        if (!LIMIT_PATTERN.matcher(cleaned).find()) {
            cleaned = cleaned + " LIMIT 200";
        }

        return cleaned;
    }

    @Override
    public String generateMoviePromo(Movie movie) {
        String systemPrompt = """
                Bạn là chuyên viên truyền thông rạp chiếu phim Movies Cinema.
                Hãy viết 1 bài đăng ngắn gọn (khoảng 4-6 dòng) bằng tiếng Việt thật hấp dẫn để gửi qua Telegram quảng bá phim.
                Quy tắc:
                - Chỉ dùng văn bản thuần hoặc các thẻ HTML cơ bản được Telegram hỗ trợ: <b>...</b>, <i>...</i>.
                - Tuyệt đối KHÔNG dùng Markdown (** hoặc #), KHÔNG dùng thẻ <br> hoặc <p>.
                - Kết thúc bằng lời kêu gọi đặt vé kèm đường link đặt vé được cung cấp.
                """;

        String userPrompt = String.format("""
                Tên phim: %s
                Thể loại: %s
                Ngày khởi chiếu: %s
                Mô tả: %s
                Link đặt vé: %s/movie/%d
                """,
                movie.getTitle(),
                movie.getCategory() != null ? movie.getCategory().getName() : "Chiếu rạp",
                movie.getReleaseDate() != null ? movie.getReleaseDate().toString() : "Đang cập nhật",
                movie.getDescription() != null ? movie.getDescription() : "Phim chiếu rạp đặc sắc.",
                appUrl,
                movie.getId()
        );

        return chat(systemPrompt, userPrompt, false);
    }
}
