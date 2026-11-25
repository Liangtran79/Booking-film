# Hướng dẫn chạy dự án với Docker

Dự án website đặt vé xem phim sử dụng **PHP 8.2 (Apache)** và **MariaDB 10.6**, đi kèm **phpMyAdmin** để quản lý cơ sở dữ liệu trực quan.

---

## 1. Yêu cầu hệ thống
- Đã cài đặt [Docker Desktop](https://www.docker.com/products/docker-desktop/) và Docker Desktop đang chạy.

---

## 2. Lệnh khởi chạy

Mở Terminal / PowerShell tại thư mục dự án và chạy lệnh:

```bash
docker compose up -d --build
```

> **Lưu ý:** Lần đầu chạy, Docker sẽ tải image, build web container và tự động import file `cinema.sql` vào cơ sở dữ liệu `cinema`. Quá trình mất khoảng 1-2 phút tùy tốc độ mạng.

---

## 3. Các địa chỉ truy cập

Sau khi container khởi động thành công:

| Dịch vụ | URL | Ghi chú |
| :--- | :--- | :--- |
| **Giao diện Client (Khách hàng)** | [http://localhost:8080/](http://localhost:8080/) | Hoặc `http://localhost:8080/client/index.php` |
| **Giao diện Admin (Quản trị)** | [http://localhost:8080/admin/](http://localhost:8080/admin/) | `http://localhost:8080/admin/index.php` |
| **phpMyAdmin (Quản lý DB)** | [http://localhost:8082/](http://localhost:8082/) | Xem và quản lý các bảng CSDL |

---

## 4. Thông tin kết nối CSDL (nếu cần dùng)

- **Host (từ ngoài máy thật):** `localhost` hoặc `127.0.0.1`
- **Port máy thật:** `3309` (để tránh xung đột nếu máy bạn đang chạy sẵn MySQL cổng 3306/3307/3308)
- **Database:** `cinema`
- **User:** `root`
- **Password:** `rootpassword`

*(Trong môi trường Docker nội bộ giữa web và db, web kết nối tới db qua hostname `db:3306`).*

---

## 5. Các lệnh quản lý Docker thông dụng

- **Xem trạng thái các container:**
  ```bash
  docker compose ps
  ```

- **Xem log hệ thống (khi cần debug lỗi):**
  ```bash
  docker compose logs -f
  ```

- **Dừng các container:**
  ```bash
  docker compose down
  ```

- **Dừng và xoá dữ liệu database để khởi tạo lại từ đầu:**
  ```bash
  docker compose down -v
  ```
