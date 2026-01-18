package com.example.splitwise_cc_backend.model.response;

import com.example.splitwise_cc_backend.model.AuditAction;
import com.example.splitwise_cc_backend.model.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class AuditGroupResponse {
    private String performedBy;
    private AuditAction action;
    private EntityType entityType;
    private String entityName;
    private UUID groupId;
    private String groupName;
    private Instant createdAt;
}
