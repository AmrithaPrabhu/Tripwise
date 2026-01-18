package com.example.splitwise_cc_backend.model.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
