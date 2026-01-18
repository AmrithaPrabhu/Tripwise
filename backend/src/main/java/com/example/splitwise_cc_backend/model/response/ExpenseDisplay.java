package com.example.splitwise_cc_backend.model.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ExpenseDisplay {
    private UUID expenseId;
    private String description;
    private String paidBy;
    private BigDecimal amount;
}
