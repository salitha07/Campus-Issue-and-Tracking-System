package com.campus.issue_tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendStatusUpdateEmail(String toEmail, Long issueId, String newStatus) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("eaahashara01@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Campus Issue Update: #" + issueId);

        String text = "Hello,\n\nThe status of your reported issue #" + issueId +
                " has been updated to: " + newStatus + "." +
                "\n\nThank you for helping us improve the campus!";

        message.setText(text);

        mailSender.send(message);
        System.out.println("Email sent successfully to " + toEmail);
    }

    public void sendVerificationEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("eaahashara01@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Campus Issue Tracker - Verify Account");

        String text = "Hello,\n\nYour verification code is: " + code +
                "\n\nPlease enter this code to verify your account." +
                "\n\nThank you!";

        message.setText(text);

        mailSender.send(message);
        System.out.println("Verification email sent to " + toEmail);
    }
}
