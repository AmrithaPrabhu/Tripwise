package com.example.splitwise_cc_backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Data
@Getter
@AllArgsConstructor
public class AddExpenseRequest {
    private String paidByUserId;
    private Double amount;
    private String description;
    private List<Share> shares;

    @Data
    @Getter
    @AllArgsConstructor
    public static class Share {
        private UUID userId;
        private Double amount;
    }
}
