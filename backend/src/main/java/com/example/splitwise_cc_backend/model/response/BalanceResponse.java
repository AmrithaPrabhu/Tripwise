package com.example.splitwise_cc_backend.model.response;

import com.example.splitwise_cc_backend.model.TransactionDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class BalanceResponse {
    List<TransactionDetail> transactions;
}
