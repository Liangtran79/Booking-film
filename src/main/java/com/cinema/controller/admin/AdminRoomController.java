package com.cinema.controller.admin;

import com.cinema.entity.Room;
import com.cinema.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/rooms")
@RequiredArgsConstructor
public class AdminRoomController {

    private final RoomService roomService;

    @GetMapping
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        return "admin/room";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("room", new Room());
        return "admin/create_room";
    }

    @PostMapping("/create")
    public String createRoom(@RequestParam("name") String name, RedirectAttributes redirectAttributes) {
        roomService.saveRoom(Room.builder().name(name).build());
        redirectAttributes.addFlashAttribute("successMessage", "Thêm phòng mới thành công!");
        return "redirect:/admin/rooms";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng ID: " + id));
        model.addAttribute("room", room);
        return "admin/edit_room";
    }

    @PostMapping("/edit/{id}")
    public String updateRoom(@PathVariable("id") Integer id,
                             @RequestParam("name") String name,
                             RedirectAttributes redirectAttributes) {
        roomService.updateRoom(id, name);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phòng thành công!");
        return "redirect:/admin/rooms";
    }

    @PostMapping("/delete/{id}")
    public String deleteRoom(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        roomService.deleteRoom(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa phòng thành công!");
        return "redirect:/admin/rooms";
    }
}
