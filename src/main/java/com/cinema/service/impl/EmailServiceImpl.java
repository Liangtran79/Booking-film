package com.cinema.service.impl;

import com.cinema.service.EmailService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    @Value("${spring.mail.username:noreply@cinema.com}")
    private String fromEmail;

    @Value("${spring.mail.sender-name:Movies Cinema}")
    private String senderName;

    @Override
    @Async
    public void sendVerificationEmail(String toEmail, String username, String token) {
        String verifyUrl = appUrl + "/verify-email?token=" + token;
        log.info("Bắt đầu gửi email xác thực tài khoản cho: {} (URL: {})", toEmail, verifyUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, senderName, "UTF-8"));
            helper.setTo(toEmail);
            helper.setSubject("[Movies Cinema] Kích hoạt tài khoản của bạn");

            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin: 0; padding: 25px 15px; background-color: #0b0c10; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
                    <div style="max-width: 580px; margin: 0 auto; background-color: #141414; border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 12px; overflow: hidden; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.6);">
                        <!-- Header -->
                        <div style="background-color: #020307; padding: 25px 30px; text-align: center; border-bottom: 2px solid #ff2c1f;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 22px; font-weight: 700; letter-spacing: 2px;">
                                <span style="color: #ff2c1f;">MOVIES</span> CINEMA
                            </h1>
                        </div>
                        
                        <!-- Body Content -->
                        <div style="padding: 35px 30px; color: #ffffff;">
                            <h2 style="font-size: 19px; color: #ffffff; margin-top: 0; font-weight: 600;">Xin chào, %s</h2>
                            <p style="color: #cccccc; font-size: 15px; line-height: 1.6; margin-bottom: 25px;">
                                Cảm ơn bạn đã tạo tài khoản tại <strong>Movies Cinema</strong>. Vui lòng xác nhận địa chỉ email của bạn để kích hoạt tài khoản và sử dụng đầy đủ các dịch vụ đặt vé xem phim trực tuyến.
                            </p>
                            
                            <!-- CTA Button -->
                            <div style="text-align: center; margin: 35px 0;">
                                <a href="%s" style="background-color: #ff2c1f; color: #ffffff; padding: 13px 36px; text-decoration: none; border-radius: 6px; font-weight: 600; font-size: 15px; display: inline-block; letter-spacing: 0.5px;">
                                    Xác Nhận Email
                                </a>
                            </div>
                            
                            <p style="color: #888888; font-size: 13px; line-height: 1.5; margin-top: 30px;">
                                Hoặc bạn có thể sao chép liên kết này và dán vào thanh địa chỉ trình duyệt:<br>
                                <a href="%s" style="color: #ff2c1f; word-break: break-all; text-decoration: underline;">%s</a>
                            </p>
                            
                            <hr style="border: none; border-top: 1px solid rgba(255, 255, 255, 0.1); margin: 30px 0;">
                            
                            <!-- Security note -->
                            <p style="color: #777777; font-size: 12px; margin: 0; line-height: 1.5;">
                                Nếu bạn không thực hiện yêu cầu đăng ký này, vui lòng bỏ qua email. Tài khoản sẽ không được kích hoạt nếu chưa xác nhận liên kết trên.
                            </p>
                        </div>
                        
                        <!-- Footer -->
                        <div style="background-color: #0d0d0d; padding: 18px 30px; text-align: center; border-top: 1px solid rgba(255, 255, 255, 0.05);">
                            <p style="color: #555555; font-size: 11px; margin: 0;">
                                &copy; 2026 Movies Cinema. Tất cả các quyền được bảo lưu.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """, username, verifyUrl, verifyUrl, verifyUrl);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Đã gửi thành công email xác thực đến: {}", toEmail);

        } catch (Exception e) {
            log.warn("Gửi email qua SMTP thất bại: {} (Nguyên nhân: {}). Link xác thực trực tiếp: {}", toEmail, e.getMessage(), verifyUrl);
        }
    }
}
