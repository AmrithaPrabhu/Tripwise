package com.example.splitwise_cc_backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserLoginDTO {
    private String email;
    private String name;
    private UUID userId;
}
