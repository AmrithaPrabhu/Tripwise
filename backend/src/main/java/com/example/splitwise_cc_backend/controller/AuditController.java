package com.example.splitwise_cc_backend.controller;

import com.example.splitwise_cc_backend.model.response.UserLoginDTO;
import com.example.splitwise_cc_backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/{groupId}")
    public List<String> getAuditLogsForGroup(@PathVariable UUID groupId, Authentication authentication) {
        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();
        return auditService.getAuditStatementsForGroup(groupId);
    }

    @GetMapping
    public List<String> getAuditLogsForUser(Authentication authentication){
        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();
        return auditService.getAuditStatementsForUser(principal.getUserId());
    }
}
