package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String username;
    private String otp;
    private String resetToken;
}