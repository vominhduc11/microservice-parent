package com.devwonder.notificationservice.service;

import com.devwonder.common.event.DealerEmailEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender javaMailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public void sendDealerWelcomeEmail(DealerEmailEvent event) throws MessagingException, UnsupportedEncodingException {
        log.info("Sending dealer welcome email to: {}", event.getEmail());
        
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail, "DevWonder E-commerce Platform");
        helper.setTo(event.getEmail());
        helper.setSubject("Welcome to DevWonder E-commerce Platform - Account Created");
        
        String emailContent = buildEmailContent(event);
        helper.setText(emailContent, true);
        
        javaMailSender.send(message);
        log.info("✅ Email sent successfully to {}", event.getEmail());
    }

    
    private String buildEmailContent(DealerEmailEvent event) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #f8f9fa; padding: 20px; border-radius: 10px;">
                    <h2 style="color: #007bff; text-align: center;">Welcome to DevWonder E-commerce Platform!</h2>
                    
                    <p>Dear <strong>%s</strong>,</p>
                    
                    <p>Welcome to DevWonder E-commerce Platform! Your dealer account has been successfully created.</p>
                    
                    <div style="background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #1976d2; margin-top: 0;">Your Login Credentials:</h3>
                        <p><strong>Username:</strong> %s</p>
                        <p><strong>Password:</strong> %s</p>
                    </div>
                    
                    <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #424242; margin-top: 0;">Company Information:</h3>
                        <p><strong>Company Name:</strong> %s</p>
                        <p><strong>Email:</strong> %s</p>
                        <p><strong>Phone:</strong> %s</p>
                        <p><strong>Address:</strong> %s</p>
                        <p><strong>City:</strong> %s, %s</p>
                    </div>
                    
                    <h3 style="color: #2e7d32;">Next Steps:</h3>
                    <ol style="color: #424242;">
                        <li>Login to our dealer portal using your credentials</li>
                        <li>Complete your dealer profile verification</li>
                        <li>Browse our wholesale product catalog</li>
                        <li>Start placing your first orders</li>
                    </ol>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="http://localhost:8080/api/auth/login"
                           style="background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">
                           Access Portal
                        </a>
                    </div>
                    
                    <p style="color: #666; font-size: 14px;">
                        For support, contact us at <a href="mailto:support@devwonder.com">support@devwonder.com</a>
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        Best regards,<br>
                        DevWonder E-commerce Team<br>
                        This is an automated message, please do not reply.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(
                event.getCompanyName(),
                event.getUsername(),
                event.getPassword(),
                event.getCompanyName(),
                event.getEmail(),
                event.getPhone(),
                event.getAddress(),
                event.getDistrict(),
                event.getCity()
            );
    }

}