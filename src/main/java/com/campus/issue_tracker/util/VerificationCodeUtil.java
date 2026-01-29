package com.campus.issue_tracker.util;

import java.util.Random;

public class VerificationCodeUtil {

    public static String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }
}
