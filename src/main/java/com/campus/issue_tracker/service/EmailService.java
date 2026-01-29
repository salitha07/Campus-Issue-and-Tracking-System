package com.campus.issue_tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ==============================
    // Send OTP Email
    // ==============================
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Campus Issue Tracker - OTP Verification");
        message.setText(
                "Your OTP code is: " + otp +
                        "\nIt will expire in 5 minutes."
        );
        mailSender.send(message);
    }

    // ==============================
    // Send Issue Status Update Email
    // ==============================
    public void sendStatusUpdateEmail(String toEmail, Long issueId, String status) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Issue Status Updated - Issue #" + issueId);
        message.setText(
                "Your issue with ID #" + issueId +
                        " has been updated to status: " + status +
                        "\n\nCampus Issue Tracker"
        );
        mailSender.send(message);
    }
}
