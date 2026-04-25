package com.payflow.userservice.dto;

import lombok.*;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private String name;
    private String email;
    private String role;
}