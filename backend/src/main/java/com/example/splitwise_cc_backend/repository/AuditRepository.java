package com.example.splitwise_cc_backend.repository;

import com.example.splitwise_cc_backend.model.Audit;
import com.example.splitwise_cc_backend.model.response.AuditGroupResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AuditRepository extends JpaRepository<Audit, UUID> {
    @Query("""
               SELECT new com.example.splitwise_cc_backend.model.response.AuditGroupResponse(
                  u.name,
                  a.action,
                  a.entityType,
                  e.description,
                  a.groupId,
                  g.name,
                  a.createdAt
               )
               FROM Audit a,
                     User u,
                     Expense e,
                     Group g
                     WHERE a.performedBy = u.id
                     AND a.entityType = 'EXPENSE'
                     AND a.entityId = e.id
                     AND a.groupId = g.id
                     AND g.deleted = false
                     AND g.id = :groupId
                     ORDER BY a.createdAt DESC
            """)
    List<AuditGroupResponse> findByGroupForExpense(@Param("groupId") UUID groupId);

    @Query("""
               SELECT new com.example.splitwise_cc_backend.model.response.AuditGroupResponse(
                  u.name,
                  a.action,
                  a.entityType,
                  u2.name,
                  a.groupId,
                  g.name,
                  a.createdAt
               )
               FROM Audit a,
                     User u,
                     User u2,
                     Group g
                     WHERE a.performedBy = u.id
                     AND a.entityType = 'MEMBER'
                     AND a.entityId = u2.id
                     AND a.groupId = g.id
                     AND g.deleted = false
                     AND g.id = :groupId
                     ORDER BY a.createdAt DESC
            """)
    List<AuditGroupResponse> findByGroupForMember(UUID groupId);

}
