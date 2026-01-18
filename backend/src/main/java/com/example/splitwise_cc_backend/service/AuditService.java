package com.example.splitwise_cc_backend.service;

import com.example.splitwise_cc_backend.model.Group;
import com.example.splitwise_cc_backend.model.response.AuditGroupResponse;
import com.example.splitwise_cc_backend.repository.AuditRepository;
import com.example.splitwise_cc_backend.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditRepository auditRepository;
    private final GroupRepository groupRepository;
    public List<String> getAuditStatementsForGroup(UUID groupId){
        List<AuditGroupResponse> totalAudits = auditRepository.findByGroupForExpense(groupId);
        List<AuditGroupResponse> memberAudits = auditRepository.findByGroupForMember(groupId);
        totalAudits.addAll(memberAudits);
        return totalAudits.stream()
                .sorted(Comparator.comparing(
                        AuditGroupResponse::getCreatedAt
                ).reversed())
                .map(auditGroupResponse -> String.format(
                    "%s %s %s %s",
                    auditGroupResponse.getPerformedBy(),
                    auditGroupResponse.getAction().toString().toLowerCase(Locale.ENGLISH),
                    auditGroupResponse.getEntityType().toString().toLowerCase(Locale.ENGLISH),
                    auditGroupResponse.getEntityName()))
                .toList();
    }

    public List<String> getAuditStatementsForUser(UUID userId) {
        List<Group> groups = groupRepository.findByUser(userId);
        System.out.println("Groups " + groups.size());
        List<AuditGroupResponse> totalAudits = new ArrayList<>();
        groups.forEach(group -> {
            List<AuditGroupResponse> expenseAudits = auditRepository.findByGroupForExpense(group.getId());
            List<AuditGroupResponse> memberAudits = auditRepository.findByGroupForMember(group.getId());
            totalAudits.addAll(expenseAudits);
            totalAudits.addAll(memberAudits);
        });
        return totalAudits.stream()
                .sorted(Comparator.comparing(
                        AuditGroupResponse::getCreatedAt
                ).reversed())
                .map(auditGroupResponse -> String.format(
                        "%s %s %s %s in %s",
                        auditGroupResponse.getPerformedBy(),
                        auditGroupResponse.getAction().toString().toLowerCase(Locale.ENGLISH),
                        auditGroupResponse.getEntityType().toString().toLowerCase(Locale.ENGLISH),
                        auditGroupResponse.getEntityName(),
                        auditGroupResponse.getGroupName()))
                .toList();
    }
}
