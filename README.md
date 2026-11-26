# Hướng dẫn Dự Án Website Đặt Vé Xem Phim (Spring Boot Java)

Dự án website đặt vé xem phim được chuyển đổi và xây dựng chuẩn theo kiến trúc **Spring Boot 3 (Java 17+)**, kết nối **MariaDB / MySQL**, giao diện kết hợp **Thymeleaf**, **HTML5/CSS3/JavaScript**, hỗ trợ triển khai trực tiếp hoặc qua **Docker Compose**.

---

## 1. Cấu Trúc Dự Án Chuẩn Spring Boot

```text
MOVIE WEBSITES-java/
├── pom.xml                                   # File cấu hình Maven và các dependencies
├── Dockerfile                                # Multi-stage build Docker cho Spring Boot
├── docker-compose.yml                        # Khởi chạy đồng bộ App, MariaDB, phpMyAdmin
├── cinema.sql                                # Dữ liệu mẫu CSDL
├── src/
│   ├── main/
│   │   ├── java/com/cinema/
│   │   │   ├── CinemaApplication.java        # Entry point của ứng dụng Spring Boot
│   │   │   ├── config/                       # Cấu hình Security, MVC, Interceptors
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── WebMvcConfig.java
│   │   │   │   └── AdminAuthInterceptor.java
│   │   │   ├── controller/                   # Tầng Controller (MVC & REST API)
│   │   │   │   ├── client/                   # Giao diện khách hàng
│   │   │   │   │   ├── HomeController.java
│   │   │   │   │   ├── ClientMovieController.java
│   │   │   │   │   ├── BookingController.java
│   │   │   │   │   └── AuthController.java
│   │   │   │   ├── admin/                    # Giao diện quản trị Admin
│   │   │   │   │   ├── AdminDashboardController.java
│   │   │   │   │   ├── AdminMovieController.java
│   │   │   │   │   ├── AdminRoomController.java
│   │   │   │   │   ├── AdminShowtimeController.java
│   │   │   │   │   ├── AdminBillController.java
│   │   │   │   │   └── AdminUserController.java
│   │   │   │   └── api/                      # REST API (AJAX chọn suất chiếu/ghế)
│   │   │   │       └── ApiShowtimeController.java
│   │   │   ├── dto/                          # Data Transfer Objects
│   │   │   │   ├── BookingRequestDto.java
│   │   │   │   ├── UserRegisterDto.java
│   │   │   │   └── RevenueByMovieDto.java
│   │   │   ├── entity/                       # JPA Entities (Mapping CSDL cinema)
│   │   │   │   ├── User.java                 # Bảng users
│   │   │   │   ├── Category.java             # Bảng theloai
│   │   │   │   ├── Movie.java                # Bảng phim
│   │   │   │   ├── Room.java                 # Bảng phong
│   │   │   │   ├── Showtime.java             # Bảng suatchieu
│   │   │   │   ├── Bill.java                 # Bảng hoadon
│   │   │   │   └── Ticket.java               # Bảng ve
│   │   │   ├── repository/                   # Spring Data JPA Repositories
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── MovieRepository.java
│   │   │   │   ├── RoomRepository.java
│   │   │   │   ├── ShowtimeRepository.java
│   │   │   │   └── BillRepository.java
│   │   │   └── service/                      # Tầng nghiệp vụ (Service & Impl)
│   │   │       ├── UserService.java & UserServiceImpl.java
│   │   │       ├── MovieService.java & MovieServiceImpl.java
│   │   │       ├── RoomService.java & RoomServiceImpl.java
│   │   │       ├── ShowtimeService.java & ShowtimeServiceImpl.java
│   │   │       └── BillService.java & BillServiceImpl.java
│   │   └── resources/
│   │       ├── application.properties        # Cấu hình kết nối DB, upload, Thymeleaf
│   │       ├── static/                       # File tĩnh (CSS, JS, Hình ảnh, Posters)
│   │       └── templates/                    # Giao diện Thymeleaf
│   │           ├── fragments/                # Header, Footer dùng chung
│   │           ├── client/                   # Giao diện Client (Trang chủ, phim, đặt ghế, thanh toán, login...)
│   │           └── admin/                    # Giao diện Quản trị (Dashboard, CRUD phim, phòng, suất, hóa đơn, user...)
```

---

## 2. Hướng Dẫn Khởi Chạy

### Cách 1: Khởi chạy trọn gói bằng Docker Compose (Khuyên dùng)
Yêu cầu: Đã cài Docker Desktop.

1. Mở Terminal / PowerShell tại thư mục dự án và chạy:
```bash
docker compose up -d --build
```
2. Docker sẽ tự động:
   - Khởi động MariaDB và nạp dữ liệu từ `cinema.sql`.
   - Build ứng dụng Spring Boot thành container độc lập.
   - Khởi động phpMyAdmin.

---

### Cách 2: Chạy trực tiếp với Maven trên máy cục bộ
Yêu cầu: Java 17+ và Maven đã được cài đặt, MariaDB/MySQL đang chạy (cổng 3309 hoặc 3306).

1. Đảm bảo Database `cinema` đã được import từ file `cinema.sql`.
2. Chạy lệnh:
```bash
mvn spring-boot:run
```

---

## 3. Các Địa Chỉ Truy Cập

| Chức năng | Đường dẫn URL | Mô tả |
| :--- | :--- | :--- |
| **Trang chủ Khách hàng** | [http://localhost:8080/](http://localhost:8080/) | Xem phim đang chiếu, sắp chiếu, đặt vé |
| **Danh sách phim** | [http://localhost:8080/movies](http://localhost:8080/movies) | Lọc và tìm kiếm phim |
| **Đăng nhập** | [http://localhost:8080/login](http://localhost:8080/login) | Đăng nhập tài khoản |
| **Đăng ký** | [http://localhost:8080/register](http://localhost:8080/register) | Tạo tài khoản mới |
| **Bảng điều khiển Quản trị** | [http://localhost:8080/admin/dashboard](http://localhost:8080/admin/dashboard) | Thống kê doanh thu, quản lý hệ thống |
| **Quản lý CSDL (phpMyAdmin)** | [http://localhost:8082/](http://localhost:8082/) | Xem và quản trị bảng CSDL |

---

## 4. Tài Khoản Thử Nghiệm

Hệ thống kế thừa toàn bộ tài khoản và mật khẩu từ cơ sở dữ liệu `cinema.sql`:

| Loại tài khoản | Tên đăng nhập / Email | Mật khẩu |
| :--- | :--- | :--- |
| **Admin** | `admin` hoặc `admin@cinema.com` | `Tien123@` (hoặc tài khoản Admin: `Tiến` / `Tien123@`, `Duy` / `Duy123@`) |
| **Customer** | `Nguyen Van A` hoặc `NguyenVanA@gmail.com` | Tài khoản mẫu có sẵn trong database |
