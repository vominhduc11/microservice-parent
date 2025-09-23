package com.devwonder.notificationservice.service;

import com.devwonder.common.event.CustomerCreatedEvent;
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

    public void sendCustomerWelcomeEmail(CustomerCreatedEvent event) throws MessagingException, UnsupportedEncodingException {
        log.info("Sending customer welcome email to: {}", event.getEmail());

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail, "DevWonder E-commerce Platform");
        helper.setTo(event.getEmail());
        helper.setSubject("Welcome to DevWonder - Your Account Created Successfully");

        String emailContent = buildCustomerEmailContent(event);
        helper.setText(emailContent, true);

        javaMailSender.send(message);
        log.info("✅ Customer welcome email sent successfully to {}", event.getEmail());
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

    private String buildCustomerEmailContent(CustomerCreatedEvent event) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #f8f9fa; padding: 20px; border-radius: 10px;">
                    <h2 style="color: #28a745; text-align: center;">Welcome to DevWonder E-commerce Platform!</h2>

                    <p>Dear <strong>%s</strong>,</p>

                    <p>Thank you for choosing DevWonder! Your customer account has been successfully created as part of your recent purchase.</p>

                    <div style="background-color: #e9ecef; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #495057; margin-top: 0;">Your Account Details:</h3>
                        <p><strong>Username:</strong> %s</p>
                        <p><strong>Temporary Password:</strong> <span style="background-color: #fff3cd; padding: 2px 8px; border-radius: 3px; font-family: monospace;">%s</span></p>
                        <p><strong>Email:</strong> %s</p>
                        <p><strong>Phone:</strong> %s</p>
                    </div>

                    <div style="background-color: #d1ecf1; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h4 style="color: #0c5460; margin-top: 0;">🔒 Important Security Notice:</h4>
                        <p style="margin-bottom: 0;">Please change your password upon first login for security reasons.</p>
                    </div>

                    <h3 style="color: #495057;">What you can do with your account:</h3>
                    <ul style="color: #666;">
                        <li>View your purchase history and warranties</li>
                        <li>Check warranty status using your product serial numbers</li>
                        <li>Access customer support and services</li>
                        <li>Update your profile information</li>
                    </ul>

                    <div style="text-align: center; margin: 30px 0;">
                        <a href="http://localhost:8080/api/auth/login"
                           style="background-color: #28a745; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">
                           Login to Your Account
                        </a>
                    </div>

                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h4 style="color: #856404; margin-top: 0;">📋 Quick Tip:</h4>
                        <p style="margin-bottom: 0;">You can check your warranty status anytime without logging in at:
                        <a href="http://localhost:8080/api/warranty/check/YOUR_SERIAL_NUMBER">Warranty Check</a></p>
                    </div>

                    <p style="color: #666; font-size: 14px;">
                        For support, contact us at <a href="mailto:support@devwonder.com">support@devwonder.com</a>
                    </p>

                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">

                    <p style="color: #999; font-size: 12px; text-align: center;">
                        Best regards,<br>
                        DevWonder Customer Service Team<br>
                        This is an automated message, please do not reply.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(
                event.getCustomerName(),
                event.getUsername(),
                event.getTempPassword(),
                event.getEmail(),
                event.getPhone()
            );
    }
}