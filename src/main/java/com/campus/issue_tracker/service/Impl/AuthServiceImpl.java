package com.campus.issue_tracker.service.Impl;

import com.campus.issue_tracker.dto.*;
import com.campus.issue_tracker.entity.Admin;
import com.campus.issue_tracker.entity.Student;
import com.campus.issue_tracker.repository.AdminRepository;
import com.campus.issue_tracker.repository.StudentRepository;
import com.campus.issue_tracker.service.AuthService;
import com.campus.issue_tracker.util.VerificationCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StudentRepository studentRepo;
    private final AdminRepository adminRepo;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String register(RegisterRequest request) {
        String code = VerificationCodeUtil.generateCode();

        if (request.getRole().equalsIgnoreCase("student")) {
            if (studentRepo.findByEmail(request.getEmail()).isPresent()) return "Email already used";
            Student student = Student.builder()
                    .name(request.getNameOrUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .verificationCode(code)
                    .verified(false)
                    .build();
            studentRepo.save(student);
        } else if (request.getRole().equalsIgnoreCase("admin")) {
            if (adminRepo.findByEmail(request.getEmail()).isPresent()) return "Email already used";
            Admin admin = Admin.builder()
                    .username(request.getNameOrUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .verificationCode(code)
                    .verified(false)
                    .build();
            adminRepo.save(admin);
        } else return "Invalid role";

        sendVerificationEmail(request.getEmail(), code);
        return "Verification email sent to " + request.getEmail();
    }

    private void sendVerificationEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Campus Verification Code");
        message.setText("Your verification code is: " + code);
        mailSender.send(message);
    }

    @Override
    public String verifyEmail(VerifyCodeRequest request) {
        Optional<Student> student = studentRepo.findByEmail(request.getEmail());
        if (student.isPresent() && student.get().getVerificationCode().equals(request.getVerificationCode())) {
            student.get().setVerified(true);
            student.get().setVerificationCode(null);
            studentRepo.save(student.get());
            return "Student verified successfully";
        }

        Optional<Admin> admin = adminRepo.findByEmail(request.getEmail());
        if (admin.isPresent() && admin.get().getVerificationCode().equals(request.getVerificationCode())) {
            admin.get().setVerified(true);
            admin.get().setVerificationCode(null);
            adminRepo.save(admin.get());
            return "Admin verified successfully";
        }

        return "Invalid verification code";
    }

    @Override
    public String login(LoginRequest request) {
        Optional<Student> student = studentRepo.findByEmail(request.getEmail());
        if (student.isPresent() && passwordEncoder.matches(request.getPassword(), student.get().getPassword())) {
            if (!student.get().isVerified()) return "Please verify your email first";
            return "Student logged in successfully";
        }

        Optional<Admin> admin = adminRepo.findByEmail(request.getEmail());
        if (admin.isPresent() && passwordEncoder.matches(request.getPassword(), admin.get().getPassword())) {
            if (!admin.get().isVerified()) return "Please verify your email first";
            return "Admin logged in successfully";
        }

        return "Invalid credentials";
    }

    @Override
    public String resetPassword(PasswordResetRequest request) {
        Optional<Student> student = studentRepo.findByEmail(request.getEmail());
        if (student.isPresent() && student.get().getVerificationCode().equals(request.getVerificationCode())) {
            student.get().setPassword(passwordEncoder.encode(request.getNewPassword()));
            student.get().setVerificationCode(null);
            studentRepo.save(student.get());
            return "Student password reset successfully";
        }

        Optional<Admin> admin = adminRepo.findByEmail(request.getEmail());
        if (admin.isPresent() && admin.get().getVerificationCode().equals(request.getVerificationCode())) {
            admin.get().setPassword(passwordEncoder.encode(request.getNewPassword()));
            admin.get().setVerificationCode(null);
            adminRepo.save(admin.get());
            return "Admin password reset successfully";
        }

        return "Invalid verification code";
    }
}
