package com.cinema.service.impl;

import com.cinema.service.EmailService;
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

    @Override
    @Async
    public void sendVerificationEmail(String toEmail, String username, String token) {
        String verifyUrl = appUrl + "/verify-email?token=" + token;
        log.info("Bắt đầu gửi email xác thực tài khoản cho: {} (URL: {})", toEmail, verifyUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Movies Cinema");
            helper.setTo(toEmail);
            helper.setSubject("Xác thực tài khoản của bạn - Movies Cinema");

            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin: 0; padding: 20px; background-color: #121212; font-family: 'Montserrat', Arial, sans-serif;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #020307; border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 12px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.8);">
                        <div style="background-color: #141414; padding: 25px; text-align: center; border-bottom: 2px solid #ff2c1f;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 26px; letter-spacing: 1px;">
                                <span style="color: #ff2c1f;">🎬</span> Movies Cinema
                            </h1>
                        </div>
                        <div style="padding: 35px 30px; color: #ffffff;">
                            <h2 style="font-size: 20px; color: #ffffff; margin-top: 0;">Xin chào, %s!</h2>
                            <p style="color: #cccccc; font-size: 15px; line-height: 1.6;">
                                Cảm ơn bạn đã đăng ký tài khoản tại <strong>Movies Cinema</strong>. Để bảo vệ tài khoản và kích hoạt đầy đủ chức năng đặt vé xem phim, vui lòng bấm vào nút xác nhận bên dưới:
                            </p>
                            <div style="text-align: center; margin: 35px 0;">
                                <a href="%s" style="background-color: #ff2c1f; color: #ffffff; padding: 14px 32px; text-decoration: none; border-radius: 30px; font-weight: bold; font-size: 15px; display: inline-block; box-shadow: 0 4px 15px rgba(255, 44, 31, 0.4);">
                                    Xác Nhận Tài Khoản Ngay
                                </a>
                            </div>
                            <p style="color: #888888; font-size: 13px; line-height: 1.5;">
                                Hoặc bạn có thể sao chép liên kết này vào trình duyệt:<br>
                                <a href="%s" style="color: #ff2c1f; word-break: break-all;">%s</a>
                            </p>
                            <hr style="border: none; border-top: 1px solid rgba(255, 255, 255, 0.1); margin: 30px 0;">
                            <p style="color: #666666; font-size: 12px; margin: 0; text-align: center;">
                                Nếu bạn không thực hiện đăng ký này, xin vui lòng bỏ qua email này.
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
