package com.example.splitwise_cc_backend.model.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class GroupSettlementRow {
    private UUID groupId;
    private String groupName;
    private BigDecimal amount;
}
