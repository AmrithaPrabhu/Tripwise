package com.example.splitwise_cc_backend.service;

import com.example.splitwise_cc_backend.model.*;
import com.example.splitwise_cc_backend.model.request.AddExpenseRequest;
import com.example.splitwise_cc_backend.model.response.BalanceResponse;
import com.example.splitwise_cc_backend.model.response.ExpenseDisplay;
import com.example.splitwise_cc_backend.model.response.ExpenseShareDisplay;
import com.example.splitwise_cc_backend.model.response.GroupSettlementRow;
import com.example.splitwise_cc_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final AuditRepository auditRepository;
    ObjectMapper objectMapper = new ObjectMapper();
    private final BalanceService balanceService;
    private final SettlementsRepository settlementsRepository;

    @Transactional
    public Expense addExpense(
            UUID groupId,
            UUID requesterId,
            AddExpenseRequest request
    ) {

        Group group = groupRepository.findByIdAndDeletedFalse(groupId);

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, requesterId)) {
            throw new RuntimeException("Only group members can add expenses");
        }

        Optional<User> paidBy = userRepository.findByEmail(request.getPaidByUserId());
        if(!paidBy.isPresent()) {
            throw new UsernameNotFoundException("User is not present in the group");
        }
        User paidByUser = paidBy.get();
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, paidByUser.getId())) {
            throw new RuntimeException("paidBy user is not a group member");
        }

        Expense expense = Expense.builder()
                .group(group)
                .paidBy(paidByUser)
                .amount(request.getAmount())
                .description(request.getDescription())
                .createdAt(Instant.now())
                .deleted(false)
                .build();

        expenseRepository.save(expense);
        Map<String, Object> auditMap = new HashMap<>();
        auditMap.put("amount", expense.getAmount());
        List<Map<String, Object>> splits = new ArrayList<>();

        request.getShares().stream().forEach(share -> {
            if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, share.getUserId())) {
                throw new RuntimeException("User not in group: " + share.getUserId());
            }

            User user = userRepository.findById(share.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ExpenseShare expenseShare = ExpenseShare.builder()
                    .expense(expense)
                    .user(user)
                    .shareAmount(share.getAmount())
                    .build();
            Map<String, Object> splitEntry = new HashMap<>();
            splitEntry.put("name", user.getName());
            splitEntry.put("amount", share.getAmount());
            splits.add(splitEntry);
            expenseShareRepository.save(expenseShare);
        });
        auditMap.put("splits", splits);
        Audit audit = Audit.builder()
                .groupId(groupId)
                .entityType(EntityType.EXPENSE)
                .entityId(expense.getId())
                .action(AuditAction.ADDED)
                .metadata(objectMapper.writeValueAsString(auditMap))
                .createdAt(Instant.now())
                .performedBy(requesterId)
                .build();
        auditRepository.save(audit);
        groupRepository.incrementBalanceVersion(groupId);

        settlementsRepository.deleteActiveByGroupId(groupId);
        BalanceResponse balanceResponse = balanceService.computeBalances(groupId);
        List<Settlements> newSettlements = balanceResponse.getTransactions().stream()
                .map(transactionDetail -> Settlements.builder()
                        .groupId(groupId)
                        .amount(transactionDetail.getAmount())
                        .fromUser(transactionDetail.getFrom())
                        .toUser(transactionDetail.getTo())
                        .status("ACTIVE")
                        .createdAt(Instant.now())
                        .build()
                ).toList();
        settlementsRepository.saveAll(newSettlements);
        return expense;
    }

    @Transactional
    public Expense editExpense(
            UUID expenseId,
            UUID requesterId,
            AddExpenseRequest request
    ) {

        Expense expense = expenseRepository.findByIdAndDeletedFalse(expenseId);

        UUID groupId = expense.getGroup().getId();

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, requesterId)) {
            throw new RuntimeException("Only group members can edit expenses");
        }
        Optional<User> paidBy = userRepository.findByEmail(request.getPaidByUserId());
        if(!paidBy.isPresent()) {
            throw new UsernameNotFoundException("User is not present in the group");
        }
        User paidByUser = paidBy.get();
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, paidByUser.getId())) {
            throw new RuntimeException("PaidBy user is not a group member");
        }


        expense.setPaidBy(paidByUser);
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());

        expenseRepository.save(expense);
        Map<String, Object> auditMap = new HashMap<>();
        auditMap.put("amount", expense.getAmount());
        List<Map<String, Object>> splits = new ArrayList<>();
        expenseShareRepository.deleteByExpenseId(expenseId);
        request.getShares().stream().forEach(share -> {
            if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, share.getUserId())) {
                throw new RuntimeException("User not in group: " + share.getUserId());
            }

            User user = userRepository.findById(share.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ExpenseShare expenseShare = ExpenseShare.builder()
                    .expense(expense)
                    .user(user)
                    .shareAmount(share.getAmount())
                    .build();

            expenseShareRepository.save(expenseShare);
            Map<String, Object> splitEntry = new HashMap<>();
            splitEntry.put("name", user.getName());
            splitEntry.put("amount", share.getAmount());
            splits.add(splitEntry);
        });
        auditMap.put("splits", splits);
        Audit audit = Audit.builder()
                .groupId(groupId)
                .entityType(EntityType.EXPENSE)
                .entityId(expense.getId())
                .action(AuditAction.UPDATED)
                .metadata(objectMapper.writeValueAsString(auditMap))
                .createdAt(Instant.now())
                .performedBy(requesterId)
                .build();
        auditRepository.save(audit);
        groupRepository.incrementBalanceVersion(groupId);

        settlementsRepository.deleteActiveByGroupId(groupId);
        BalanceResponse balanceResponse = balanceService.computeBalances(groupId);
        List<Settlements> newSettlements = balanceResponse.getTransactions().stream()
                .map(transactionDetail -> Settlements.builder()
                        .groupId(groupId)
                        .amount(transactionDetail.getAmount())
                        .fromUser(transactionDetail.getFrom())
                        .toUser(transactionDetail.getTo())
                        .status("ACTIVE")
                        .createdAt(Instant.now())
                        .build()
                ).toList();
        settlementsRepository.saveAll(newSettlements);
        return expense;
    }

    public List<ExpenseDisplay> getAllExpenses(UUID groupId, UUID userId){
        return expenseRepository.findByGroupIdAndDeletedFalse(groupId).stream()
                .map(expense -> ExpenseDisplay.builder()
                        .expenseId(expense.getId())
                        .amount(BigDecimal.valueOf(expense.getAmount()))
                        .paidBy(expense.getPaidBy().getName())
                        .description(expense.getDescription())
                        .build()
                ).toList();
    }

    public List<ExpenseShareDisplay> getAllExpenseShares(UUID groupId, UUID expenseId){
        return expenseShareRepository.findByExpense_GroupIdAndExpense_Id(groupId, expenseId).stream()
                .map(expenseShare -> ExpenseShareDisplay.builder()
                        .amount(BigDecimal.valueOf(expenseShare.getShareAmount()))
                        .fromUser(expenseShare.getUser().getName())
                        .build()
                ).toList();
    }

    @Transactional
    public void deleteExpense(UUID expenseId, UUID requesterId) {

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        UUID groupId = expense.getGroup().getId();
        expenseShareRepository.deleteByExpenseId(expenseId);
        expense.setDeleted(true);
        expenseRepository.save(expense);

        settlementsRepository.deleteActiveByGroupId(groupId);
        BalanceResponse balanceResponse = balanceService.computeBalances(groupId);
        List<Settlements> newSettlements = balanceResponse.getTransactions().stream()
                .map(transactionDetail -> Settlements.builder()
                        .groupId(groupId)
                        .amount(transactionDetail.getAmount())
                        .fromUser(transactionDetail.getFrom())
                        .toUser(transactionDetail.getTo())
                        .status("ACTIVE")
                        .createdAt(Instant.now())
                        .build()
                ).toList();
        settlementsRepository.saveAll(newSettlements);
        Audit audit = Audit.builder()
                .groupId(groupId)
                .entityType(EntityType.EXPENSE)
                .entityId(expenseId)
                .action(AuditAction.DELETED)
                .createdAt(Instant.now())
                .performedBy(requesterId)
                .build();
        auditRepository.save(audit);
    }
}
