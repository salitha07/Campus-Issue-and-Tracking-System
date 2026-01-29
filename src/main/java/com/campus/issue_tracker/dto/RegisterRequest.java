package com.campus.issue_tracker.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nameOrUsername;
    private String email;
    private String password;
    private String role; // "student" or "admin"
}
