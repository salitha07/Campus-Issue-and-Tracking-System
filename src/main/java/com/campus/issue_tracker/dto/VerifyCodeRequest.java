package com.campus.issue_tracker.dto;

import lombok.Data;

@Data
public class VerifyCodeRequest {
    private String email;
    private String verificationCode;
}
