package com.campus.issue_tracker.service;

public interface OtpService {

    void sendOtp(String email);

    boolean verifyOtp(String email, String otp);
}
