package com.example.splitwise_cc_backend.model.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ExpenseShareDisplay {
    private String fromUser;
    private BigDecimal amount;
}
