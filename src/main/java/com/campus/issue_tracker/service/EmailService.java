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
        message.setText("Hello,\n\nThe status of your reported issue #" + issueId +
                " has been updated to: " + newStatus +
                ".\n\nThank you for helping us improve the campus!");

        mailSender.send(message);
        System.out.println("Email sent successfully to " + toEmail);
    }
}
