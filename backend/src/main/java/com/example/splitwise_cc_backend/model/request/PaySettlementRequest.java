package com.example.splitwise_cc_backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
public class PaySettlementRequest {
    private UUID toUser;
    private String toUserName;
    private BigDecimal amount;
}
