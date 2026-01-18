package com.example.splitwise_cc_backend.controller;

import com.example.splitwise_cc_backend.model.request.PaySettlementRequest;
import com.example.splitwise_cc_backend.model.response.BalanceResponse;
import com.example.splitwise_cc_backend.model.response.PaymentSettleResponse;
import com.example.splitwise_cc_backend.model.response.SettlementResponse;
import com.example.splitwise_cc_backend.model.response.UserLoginDTO;
import com.example.splitwise_cc_backend.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/groups/{groupId}")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/balances")
    public List<SettlementResponse> getBalances(@PathVariable UUID groupId) {
        return balanceService.getBalanceSettlements(groupId);
    }

    @GetMapping("/settlements")
    public List<PaymentSettleResponse> getSettlements(@PathVariable UUID groupId, Authentication authentication) {
        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();
        return balanceService.getSettlementsUserOwes(groupId, principal.getUserId());
    }

    @PostMapping("/settlements/{settlementId}")
    public ResponseEntity<Void> paySettlement(
            @PathVariable UUID groupId,
            @PathVariable UUID settlementId,
            @RequestBody PaySettlementRequest request,
            Authentication authentication
    ) {
        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();

        balanceService.paySettlement(
                settlementId,
                principal.getUserId(),
                request.getAmount()
        );

        return ResponseEntity.ok().build();
    }
}

