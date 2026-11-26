package com.cinema.controller.client;

import com.cinema.entity.Category;
import com.cinema.entity.Movie;
import com.cinema.entity.Room;
import com.cinema.service.MovieService;
import com.cinema.service.RoomService;
import com.cinema.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ClientMovieController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;
    private final RoomService roomService;

    @GetMapping({"/movies", "/movie.php"})
    public String listMovies(@RequestParam(value = "filter", required = false) String filter,
                             @RequestParam(value = "genre", required = false) String genre,
                             Model model) {

        String selectedFilter = (genre != null && !genre.isBlank()) ? genre : filter;

        List<Movie> allMovies = movieService.getAllMovies();
        List<Category> categories = movieService.getAllCategories();

        Map<String, List<Movie>> moviesByGenre = new LinkedHashMap<>();

        if (selectedFilter != null && !selectedFilter.isBlank() && !selectedFilter.equalsIgnoreCase("all")) {
            List<Movie> filtered = allMovies.stream()
                    .filter(m -> m.getCategory() != null &&
                            (selectedFilter.equalsIgnoreCase(m.getCategory().getName()) ||
                             selectedFilter.equalsIgnoreCase(String.valueOf(m.getCategory().getId()))))
                    .collect(Collectors.toList());

            String genreName = categories.stream()
                    .filter(c -> selectedFilter.equalsIgnoreCase(c.getName()) || selectedFilter.equalsIgnoreCase(String.valueOf(c.getId())))
                    .map(Category::getName)
                    .findFirst()
                    .orElse(selectedFilter);

            moviesByGenre.put(genreName, filtered);
        } else {
            for (Category cat : categories) {
                List<Movie> inCategory = allMovies.stream()
                        .filter(m -> m.getCategory() != null && m.getCategory().getId().equals(cat.getId()))
                        .collect(Collectors.toList());
                if (!inCategory.isEmpty()) {
                    moviesByGenre.put(cat.getName(), inCategory);
                }
            }

            List<Movie> uncategorized = allMovies.stream()
                    .filter(m -> m.getCategory() == null)
                    .collect(Collectors.toList());
            if (!uncategorized.isEmpty()) {
                moviesByGenre.put("Phim Khác", uncategorized);
            }
        }

        model.addAttribute("categories", categories);
        model.addAttribute("moviesByGenre", moviesByGenre);
        model.addAttribute("selectedFilter", selectedFilter != null ? selectedFilter : "all");

        return "client/movie";
    }

    @GetMapping({"/movie/{id}", "/detail.php"})
    public Object movieDetail(@PathVariable(value = "id", required = false) Integer pathId,
                              @RequestParam(value = "id", required = false) Integer paramId,
                              @RequestParam(value = "date", required = false) String dateStr,
                              @RequestParam(value = "room", required = false) Integer roomId,
                              @RequestParam(value = "time", required = false) String timeStr,
                              @RequestParam(value = "part", required = false) String part,
                              Model model) {

        Integer id = (pathId != null) ? pathId : paramId;
        if (id == null) {
            return "redirect:/movies";
        }

        Movie movie = movieService.getMovieById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim ID: " + id));

        // Lấy tất cả ngày chiếu có sẵn cho phim này
        List<LocalDate> availableDates = showtimeService.getShowDatesForMovie(id);

        // Tính ngày Thứ 2 tuần hiện tại giống PHP
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        String[] weekdayLabels = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};

        List<Map<String, Object>> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("date", d.toString());
            dayMap.put("label", weekdayLabels[i]);
            dayMap.put("formatted", d.format(DateTimeFormatter.ofPattern("dd/MM")));
            boolean isAvail = availableDates.contains(d) && (movie.getReleaseDate() == null || !d.isBefore(movie.getReleaseDate()));
            dayMap.put("isAvailable", isAvail);
            weekDays.add(dayMap);
        }

        LocalDate selectedDate = null;
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                selectedDate = LocalDate.parse(dateStr);
            } catch (Exception ignored) {}
        }
        if (selectedDate == null) {
            if (availableDates.contains(today)) {
                selectedDate = today;
            } else {
                // Ưu tiên ngày đầu tiên có suất chiếu trong tuần
                Optional<LocalDate> firstAvailInWeek = weekDays.stream()
                        .filter(m -> Boolean.TRUE.equals(m.get("isAvailable")))
                        .map(m -> LocalDate.parse((String) m.get("date")))
                        .findFirst();
                if (firstAvailInWeek.isPresent()) {
                    selectedDate = firstAvailInWeek.get();
                } else if (!availableDates.isEmpty()) {
                    selectedDate = availableDates.get(0);
                } else {
                    selectedDate = today;
                }
            }
        }

        // Xử lý AJAX part giống nguyên bản PHP
        if ("rooms".equalsIgnoreCase(part)) {
            List<Room> allRooms = roomService.getAllRooms();
            List<Room> availableRooms = roomService.getRoomsByMovieAndDate(id, selectedDate);
            Set<Integer> availableIds = availableRooms.stream().map(Room::getId).collect(Collectors.toSet());

            StringBuilder sb = new StringBuilder();
            for (Room r : allRooms) {
                boolean isAvail = availableIds.contains(r.getId());
                boolean isActive = (roomId != null && roomId.equals(r.getId()));
                String roomClass = isAvail ? (isActive ? "room-item active" : "room-item") : "room-item disabled";

                if (isAvail) {
                    sb.append(String.format("<a href='javascript:void(0)' onclick=\"selectRoom('%d')\" class='%s'>%s</a>",
                            r.getId(), roomClass, r.getName()));
                } else {
                    sb.append(String.format("<div class='%s'>%s</div>", roomClass, r.getName()));
                }
            }
            return ResponseEntity.ok(sb.toString());
        }

        if ("times".equalsIgnoreCase(part)) {
            StringBuilder sb = new StringBuilder();
            if (roomId != null) {
                List<LocalTime> times = showtimeService.getShowTimesForMovieRoomDate(id, roomId, selectedDate);
                if (!times.isEmpty()) {
                    for (LocalTime t : times) {
                        String tStr = t.format(DateTimeFormatter.ofPattern("HH:mm"));
                        String timeClass = (timeStr != null && timeStr.startsWith(tStr)) ? "time-slot active" : "time-slot";
                        sb.append(String.format("<a href='javascript:void(0)' onclick=\"selectTime('%s')\" class='%s'>%s</a>",
                                tStr, timeClass, tStr));
                    }
                } else {
                    sb.append("<p>Không có lịch chiếu cho phòng này</p>");
                }
            } else {
                sb.append("<p>Vui lòng chọn phòng chiếu</p>");
            }
            return ResponseEntity.ok(sb.toString());
        }

        if ("booking-info".equalsIgnoreCase(part)) {
            StringBuilder sb = new StringBuilder();
            if (roomId != null && timeStr != null && !timeStr.isBlank()) {
                String roomName = roomService.getRoomById(roomId).map(Room::getName).orElse("Phòng chiếu");
                String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                sb.append("<h3>Thông tin đặt vé</h3>");
                sb.append("<div class='info-row'><div class='info-label'>Phim:</div><div>").append(movie.getTitle()).append("</div></div>");
                sb.append("<div class='info-row'><div class='info-label'>Ngày chiếu:</div><div>").append(formattedDate).append("</div></div>");
                sb.append("<div class='info-row'><div class='info-label'>Phòng chiếu:</div><div>").append(roomName).append("</div></div>");
                sb.append("<div class='info-row'><div class='info-label'>Giờ chiếu:</div><div>").append(timeStr).append("</div></div>");
                sb.append(String.format("<a href='/chair.php?id=%d&room=%d&date=%s&time=%s' class='btn-proceed'>Tiếp tục</a>",
                        id, roomId, selectedDate, timeStr));
            }
            return ResponseEntity.ok(sb.toString());
        }

        // Lấy danh sách phòng và phòng có suất chiếu
        List<Room> allRooms = roomService.getAllRooms();
        List<Room> availableRooms = roomService.getRoomsByMovieAndDate(id, selectedDate);
        Set<Integer> availableRoomIds = availableRooms.stream().map(Room::getId).collect(Collectors.toSet());

        Integer finalRoomId = roomId;
        if (finalRoomId == null || !availableRoomIds.contains(finalRoomId)) {
            finalRoomId = !availableRoomIds.isEmpty() ? availableRoomIds.iterator().next() : null;
        }

        String selectedRoomName = "";
        List<String> showtimes = new ArrayList<>();
        if (finalRoomId != null) {
            for (Room r : allRooms) {
                if (r.getId().equals(finalRoomId)) {
                    selectedRoomName = r.getName();
                    break;
                }
            }
            List<LocalTime> rawTimes = showtimeService.getShowTimesForMovieRoomDate(id, finalRoomId, selectedDate);
            for (LocalTime t : rawTimes) {
                showtimes.add(t.format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }

        List<Movie> relatedMovies = Collections.emptyList();
        if (movie.getCategory() != null) {
            relatedMovies = movieService.getRelatedMovies(movie.getCategory().getId(), id, 4);
        }

        model.addAttribute("weekDays", weekDays);
        model.addAttribute("movie", movie);
        model.addAttribute("movieId", id);
        model.addAttribute("availableDates", availableDates);
        model.addAttribute("selectedDate", selectedDate.toString());
        model.addAttribute("selectedDateFormatted", selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("rooms", allRooms);
        model.addAttribute("roomIdsWithShowtimes", availableRoomIds);
        model.addAttribute("selectedRoomId", finalRoomId);
        model.addAttribute("selectedRoomName", selectedRoomName);
        model.addAttribute("showtimes", showtimes);
        model.addAttribute("selectedTime", timeStr);
        model.addAttribute("relatedMovies", relatedMovies);

        return "client/detail";
    }
}
