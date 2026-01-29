package com.campus.issue_tracker.service;

import com.campus.issue_tracker.dto.*;

public interface AuthService {
    String register(RegisterRequest request);
    String verifyEmail(VerifyCodeRequest request);
    String login(LoginRequest request);
    String resetPassword(PasswordResetRequest request);
}
