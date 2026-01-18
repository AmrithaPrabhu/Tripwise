package com.example.splitwise_cc_backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class SettlementResponse {
    private String fromUser;
    private String toUser;
    private BigDecimal amount;
    private String status;
}
