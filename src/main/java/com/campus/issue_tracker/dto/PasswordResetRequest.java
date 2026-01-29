package com.campus.issue_tracker.dto;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email;
    private String newPassword;
    private String verificationCode;
}
