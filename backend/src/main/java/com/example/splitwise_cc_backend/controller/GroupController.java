package com.example.splitwise_cc_backend.controller;

import com.example.splitwise_cc_backend.model.Group;
import com.example.splitwise_cc_backend.model.request.GroupRequest;
import com.example.splitwise_cc_backend.model.response.ExpenseDisplay;
import com.example.splitwise_cc_backend.model.response.GroupSettlementRow;
import com.example.splitwise_cc_backend.model.response.SettlementResponse;
import com.example.splitwise_cc_backend.model.response.UserLoginDTO;
import com.example.splitwise_cc_backend.service.BalanceService;
import com.example.splitwise_cc_backend.service.ExpenseService;
import com.example.splitwise_cc_backend.service.GroupService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final BalanceService balanceService;
    private final ExpenseService expenseService;

    @PostMapping
    public Group createGroup(@RequestBody GroupRequest request,
                             Authentication authentication) {

        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();

        assert principal != null;
        return groupService.createGroup(
                request.getGroupName(),
                principal.getUserId()
        );
    }

    @GetMapping
    public List<Group> listAllGroups(Authentication authentication){
        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();
        assert principal != null;
        return groupService.listAllGroups(principal.getUserId());
    }

    @GetMapping("/positive-summary")
    public List<GroupSettlementRow> listPositiveSummary(Authentication authentication){
        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();
        assert principal != null;
        return balanceService.getYouAreOwedAmount(principal.getUserId());
    }

    @GetMapping("/negative-summary")
    public List<GroupSettlementRow> listNegativeSummary(Authentication authentication){
        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();
        assert principal != null;
        return balanceService.getYouOweAmount(principal.getUserId());
    }


    @GetMapping("/{groupId}/export/excel")
    public void exportGroupData(
            @PathVariable UUID groupId,
            Authentication authentication,
            HttpServletResponse response
    ) throws IOException {

        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();
        String groupName = sanitizeFileName(groupService.getGroupNameById(groupId));
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=" + groupName + "_export.xlsx"
        );

        Workbook workbook = new XSSFWorkbook();

        writeBalancesSheet(workbook, groupId);
        writeExpensesSheet(workbook, groupId, principal.getUserId());

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private String sanitizeFileName(String name) {
        return name
                .replaceAll("[^a-zA-Z0-9-_]", "_")
                .toLowerCase();
    }

    private void writeBalancesSheet(Workbook workbook, UUID groupId) {
        Sheet sheet = workbook.createSheet("Balances");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("From User");
        header.createCell(1).setCellValue("To User");
        header.createCell(2).setCellValue("Amount");
        header.createCell(3).setCellValue("Status");

        List<SettlementResponse> balances =
                balanceService.getBalanceSettlements(groupId);

        AtomicInteger rowIdx = new AtomicInteger(1);
        balances.forEach(balance -> {
                    Row row = sheet.createRow(rowIdx.getAndIncrement());
                    row.createCell(0).setCellValue(balance.getFromUser());
                    row.createCell(1).setCellValue(balance.getToUser());
                    row.createCell(2).setCellValue(balance.getAmount().doubleValue());
                    row.createCell(3).setCellValue(balance.getStatus());
                }
        );
    }

    private void writeExpensesSheet(
            Workbook workbook,
            UUID groupId,
            UUID userId
    ) {
        Sheet sheet = workbook.createSheet("Expenses");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Description");
        header.createCell(1).setCellValue("Amount");
        header.createCell(2).setCellValue("Paid By");

        List<ExpenseDisplay> expenses =
                expenseService.getAllExpenses(groupId, userId);

        AtomicInteger rowIdx = new AtomicInteger(1);
        expenses.forEach(expenseDisplay -> {
            Row row = sheet.createRow(rowIdx.getAndIncrement());
            row.createCell(0).setCellValue(expenseDisplay.getDescription());
            row.createCell(1).setCellValue(expenseDisplay.getAmount().doubleValue());
            row.createCell(2).setCellValue(expenseDisplay.getPaidBy());
        });
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "false") boolean force
    ) {
        groupService.deleteGroup(groupId, force);
        return ResponseEntity.noContent().build();
    }

}