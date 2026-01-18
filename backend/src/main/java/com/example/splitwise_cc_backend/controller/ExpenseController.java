package com.example.splitwise_cc_backend.controller;

import com.example.splitwise_cc_backend.model.Expense;
import com.example.splitwise_cc_backend.model.request.AddExpenseRequest;
import com.example.splitwise_cc_backend.model.response.ExpenseDisplay;
import com.example.splitwise_cc_backend.model.response.ExpenseShareDisplay;
import com.example.splitwise_cc_backend.model.response.UserLoginDTO;
import com.example.splitwise_cc_backend.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups/{groupId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public Expense addExpense(@PathVariable UUID groupId,
                              @RequestBody AddExpenseRequest request,
                              Authentication authentication) {

        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();

        return expenseService.addExpense(
                groupId,
                principal.getUserId(),
                request
        );
    }

    @PutMapping("/{expenseId}")
    public Expense editExpense(@PathVariable UUID expenseId,
                               @RequestBody AddExpenseRequest request,
                               Authentication authentication) {

        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();

        return expenseService.editExpense(
                expenseId,
                principal.getUserId(),
                request
        );
    }

    @GetMapping
    public List<ExpenseDisplay> listAllExpenses(@PathVariable UUID groupId, Authentication authentication){
        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();
        return expenseService.getAllExpenses(groupId, principal.getUserId());
    }

    @GetMapping("/{expenseId}/shares")
    public List<ExpenseShareDisplay> listAllExpenseShares(@PathVariable UUID groupId, @PathVariable UUID expenseId,
                                                          Authentication authentication){
        return expenseService.getAllExpenseShares(groupId, expenseId);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID groupId,
            @PathVariable UUID expenseId,
            Authentication authentication
    ) {
        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();
        expenseService.deleteExpense(expenseId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}