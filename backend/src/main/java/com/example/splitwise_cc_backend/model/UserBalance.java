package com.example.splitwise_cc_backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class UserBalance {
    UUID userId;
    public BigDecimal amount;
}
