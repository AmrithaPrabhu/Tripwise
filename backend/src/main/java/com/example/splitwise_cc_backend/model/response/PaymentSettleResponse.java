package com.example.splitwise_cc_backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class PaymentSettleResponse {
    private String fromUser;
    private UUID fromUserId;
    private String toUser;
    private UUID toUserId;
    private BigDecimal amount;
    private String status;
    private UUID id;
}

