package com.campus.issue_tracker.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component   // 🔥 THIS IS MANDATORY
public class OtpStore {

    private final Map<String, OtpData> otpMap = new ConcurrentHashMap<>();

    public void saveOtp(String email, String otp) {
        otpMap.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(5)));
    }

    public boolean verifyOtp(String email, String otp) {
        OtpData data = otpMap.get(email);
        if (data == null) return false;
        if (data.expiry().isBefore(LocalDateTime.now())) return false;
        return data.otp().equals(otp);
    }

    record OtpData(String otp, LocalDateTime expiry) {}
}
