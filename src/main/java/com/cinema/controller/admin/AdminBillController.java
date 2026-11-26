package com.cinema.controller.admin;

import com.cinema.entity.Bill;
import com.cinema.service.BillService;
import com.cinema.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/bills")
@RequiredArgsConstructor
public class AdminBillController {

    private final BillService billService;
    private final ShowtimeService showtimeService;

    @GetMapping
    public String listBills(@RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            @RequestParam(value = "keyword", required = false) String keyword,
                            Model model) {

        Page<Bill> billPage;
        if (keyword != null && !keyword.isBlank()) {
            billPage = billService.searchBillsPaginated(keyword.trim(), PageRequest.of(page, size));
            model.addAttribute("keyword", keyword);
        } else {
            billPage = billService.getBillsPaginated(PageRequest.of(page, size));
        }

        model.addAttribute("billPage", billPage);
        return "admin/bill";
    }

    
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("showtimes", showtimeService.getAllShowtimes());
        model.addAttribute("bill", new Bill());
        return "admin/create_bill";
    }

    @PostMapping("/create")
    public String createBill(@RequestParam("showtimeId") Integer showtimeId,
                             @RequestParam("seats") String seats,
                             @RequestParam("quantity") Integer quantity,
                             @RequestParam("totalAmount") BigDecimal totalAmount,
                             @RequestParam("guestName") String guestName,
                             @RequestParam(value = "guestEmail", required = false) String guestEmail,
                             @RequestParam(value = "guestPhone", required = false) String guestPhone,
                             RedirectAttributes redirectAttributes) {

        billService.createBill(showtimeId, null, seats, quantity, totalAmount, guestName, guestEmail, guestPhone);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm hóa đơn thành công!");
        return "redirect:/admin/bills";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Bill bill = billService.getBillById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn ID: " + id));

        model.addAttribute("bill", bill);
        model.addAttribute("showtimes", showtimeService.getAllShowtimes());
        return "admin/edit_bill";
    }

    @PostMapping("/edit/{id}")
    public String updateBill(@PathVariable("id") Integer id,
                             @RequestParam("showtimeId") Integer showtimeId,
                             @RequestParam("seats") String seats,
                             @RequestParam("quantity") Integer quantity,
                             @RequestParam("totalAmount") BigDecimal totalAmount,
                             @RequestParam("guestName") String guestName,
                             @RequestParam("guestEmail") String guestEmail,
                             @RequestParam("guestPhone") String guestPhone,
                             RedirectAttributes redirectAttributes) {

        billService.updateBill(id, showtimeId, seats, quantity, totalAmount, guestName, guestEmail, guestPhone);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hóa đơn thành công!");
        return "redirect:/admin/bills";
    }

    @PostMapping("/delete/{id}")
    public String deleteBill(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        billService.deleteBill(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa hóa đơn thành công!");
        return "redirect:/admin/bills";
    }
}
