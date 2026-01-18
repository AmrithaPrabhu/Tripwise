package com.example.splitwise_cc_backend.repository;

import com.example.splitwise_cc_backend.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByGroupIdAndDeletedFalse(UUID groupId);
    Expense findByIdAndDeletedFalse(UUID expenseId);
}
