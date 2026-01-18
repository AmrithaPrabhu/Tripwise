package com.example.splitwise_cc_backend.repository;

import com.example.splitwise_cc_backend.model.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, UUID> {
    void deleteByExpenseId(UUID expenseId);
    List<ExpenseShare> findByExpense_GroupId(UUID groupId);
    List<ExpenseShare> findByExpense_GroupIdAndExpense_Id(
            UUID groupId,
            UUID expenseId
    );

}
