package com.example.splitwise_cc_backend.service;


import com.example.splitwise_cc_backend.model.*;
import com.example.splitwise_cc_backend.model.response.BalanceResponse;
import com.example.splitwise_cc_backend.model.response.GroupSettlementRow;
import com.example.splitwise_cc_backend.model.response.PaymentSettleResponse;
import com.example.splitwise_cc_backend.model.response.SettlementResponse;
import com.example.splitwise_cc_backend.repository.ExpenseRepository;
import com.example.splitwise_cc_backend.repository.ExpenseShareRepository;
import com.example.splitwise_cc_backend.repository.SettlementsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final SettlementsRepository settlementsRepository;

    @Transactional
    public BalanceResponse computeBalances(UUID groupId) {

        Map<UUID, BigDecimal> balanceMap = new HashMap<>();
        List<Expense> expenseList = expenseRepository.findByGroupIdAndDeletedFalse(groupId);
        expenseList.stream().forEach(expense -> {
            UUID paidBy = expense.getPaidBy().getId();
            balanceMap.put(
                    paidBy,
                    balanceMap.getOrDefault(paidBy, BigDecimal.ZERO)
                            .add(BigDecimal.valueOf(expense.getAmount()))
            );
        });
        List<ExpenseShare> expenseShareList = expenseShareRepository.findByExpense_GroupId(groupId);
        expenseShareList.stream().forEach(expenseShare -> {
            UUID userId = expenseShare.getUser().getId();
            balanceMap.put(
                    userId,
                    balanceMap.getOrDefault(userId, BigDecimal.ZERO)
                            .subtract(BigDecimal.valueOf(expenseShare.getShareAmount()))
            );
        });
        for (UUID userId : balanceMap.keySet()) {

            BigDecimal settledOut =
                    settlementsRepository.sumSettledOut(groupId, userId);

            BigDecimal settledIn =
                    settlementsRepository.sumSettledIn(groupId, userId);

            BigDecimal net = balanceMap.get(userId);

            // subtract money already RECEIVED
            net = net.subtract(settledIn);

            // add money already PAID
            net = net.add(settledOut);

            balanceMap.put(userId, net);
        }
        return new BalanceResponse(optimize(balanceMap));
    }


    private List<TransactionDetail>  optimize(Map<UUID, BigDecimal> balances) {

        Queue<UserBalance> creditors = new ArrayDeque<>();
        Queue<UserBalance> debtors = new ArrayDeque<>();

        // Separate creditors and debtors
        for (Map.Entry<UUID, BigDecimal> entry : balances.entrySet()) {

            BigDecimal amount = entry.getValue();

            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new UserBalance(entry.getKey(), amount));
            }
            else if (amount.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(
                        new UserBalance(entry.getKey(), amount.negate())
                );
            }
        }

        List<TransactionDetail> transactions = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {

            UserBalance creditor = creditors.poll();
            UserBalance debtor = debtors.poll();

            // BigDecimal min
            BigDecimal settledAmount = creditor.getAmount()
                    .min(debtor.getAmount());

            transactions.add(
                    new TransactionDetail(
                            debtor.getUserId(),   // pays
                            creditor.getUserId(), // receives
                            settledAmount
                    )
            );

            creditor.setAmount(creditor.getAmount().subtract(settledAmount));
            debtor.setAmount(debtor.getAmount().subtract(settledAmount));

            if (creditor.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(creditor);
            }

            if (debtor.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                debtors.add(debtor);
            }
        }

        return transactions;
    }
    private List<TransactionDetail> applySettlements(
            List<TransactionDetail> optimized,
            List<Settlements> settlements
    ) {

        Map<String, TransactionDetail> txnMap = new HashMap<>();

        for (TransactionDetail t : optimized) {
            txnMap.put(key(t.getFrom(), t.getTo()), t);
        }

        for (Settlements s : settlements) {

            String key = key(s.getFromUser(), s.getToUser());
            TransactionDetail txn = txnMap.get(key);

            if (txn == null) continue;

            BigDecimal remaining = txn.getAmount()
                    .subtract(s.getAmount());

            if (remaining.signum() > 0) {
                txn.setAmount(remaining);
            } else {
                txnMap.remove(key);
            }
        }

        return new ArrayList<>(txnMap.values());
    }

    private String key(UUID from, UUID to) {
        return from + "->" + to;
    }

    public List<GroupSettlementRow> getYouAreOwedAmount(UUID userId){
        return settlementsRepository.youAreOwedPerGroup(userId);
    }

    public List<GroupSettlementRow> getYouOweAmount(UUID userId){
        return settlementsRepository.youNeedToSettlePerGroup(userId);
    }

    public List<SettlementResponse> getBalanceSettlements(UUID groupId){
        return settlementsRepository.findByActiveSettlementGroupId(groupId);
    }

    public List<PaymentSettleResponse> getSettlementsUserOwes(
            UUID groupId,
            UUID requesterId
    ) {
        System.out.println("Reaching here " + groupId + " " + requesterId);
        System.out.println(settlementsRepository.findActiveOwedSettlements(
                groupId,
                requesterId
        ));
        return settlementsRepository.findActiveOwedSettlements(
                groupId,
                requesterId
        );
    }

    @Transactional
    public void paySettlement(
            UUID settlementId,
            UUID requesterId,
            BigDecimal paidAmount
    ) {
        Settlements s = settlementsRepository.findById(settlementId)
                .orElseThrow(() -> new RuntimeException("Settlement not found"));

        if (!s.getStatus().equals("ACTIVE")) {
            throw new IllegalStateException("Settlement already settled");
        }

        if (!s.getFromUser().equals(requesterId)) {
            throw new AccessDeniedException("You can only settle what you owe");
        }

        if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (paidAmount.compareTo(s.getAmount()) > 0) {
            throw new IllegalArgumentException("Cannot pay more than owed amount");
        }

        BigDecimal remaining = s.getAmount().subtract(paidAmount);

        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            s.setStatus("SETTLED");
            s.setCreatedAt(Instant.now());
        } else {
            s.setAmount(remaining);
            Settlements newSettlement = Settlements.builder()
                    .groupId(s.getGroupId())
                    .toUser(s.getToUser())
                    .fromUser(requesterId)
                    .amount(paidAmount)
                    .status("SETTLED")
                    .createdAt(Instant.now())
                    .build();
            settlementsRepository.save(newSettlement);
        }

        settlementsRepository.save(s);
    }

}