package com.example.splitwise_cc_backend.repository;

import com.example.splitwise_cc_backend.model.Settlements;
import com.example.splitwise_cc_backend.model.response.GroupSettlementRow;
import com.example.splitwise_cc_backend.model.response.PaymentSettleResponse;
import com.example.splitwise_cc_backend.model.response.SettlementResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SettlementsRepository
        extends JpaRepository<Settlements, UUID> {

    @Query("""
        SELECT COALESCE(SUM(s.amount), 0)
        FROM Settlements s
        WHERE s.groupId = :groupId
          AND s.fromUser = :userId
          AND s.status = 'SETTLED'
    """)
    BigDecimal sumSettledOut(UUID groupId, UUID userId);

    @Query("""
        SELECT COALESCE(SUM(s.amount), 0)
        FROM Settlements s
        WHERE s.groupId = :groupId
          AND s.toUser = :userId
          AND s.status = 'SETTLED'
    """)
    BigDecimal sumSettledIn(UUID groupId, UUID userId);

    @Modifying
    @Query("""
        DELETE FROM Settlements s
        WHERE s.groupId = :groupId
          AND s.status = 'ACTIVE'
    """)
    void deleteActiveByGroupId(UUID groupId);

    @Query("""
        SELECT new com.example.splitwise_cc_backend.model.response.GroupSettlementRow(
            g.id,
            g.name,
            COALESCE(SUM(s.amount), 0)
        )
        FROM Settlements s
        JOIN Group g ON s.groupId = g.id
        JOIN GroupMember gm ON gm.group = g
        WHERE gm.user.id = :userId
          AND s.toUser = :userId
          AND s.status = 'ACTIVE'
          AND g.deleted = false
        GROUP BY g.id, g.name
    """)
    List<GroupSettlementRow> youAreOwedPerGroup(UUID userId);

    // PER GROUP: You need to settle
    @Query("""
        SELECT new com.example.splitwise_cc_backend.model.response.GroupSettlementRow(
            g.id,
            g.name,
            COALESCE(SUM(s.amount), 0)
        )
        FROM Settlements s
        JOIN Group g ON s.groupId = g.id
        JOIN GroupMember gm ON gm.group = g
        WHERE gm.user.id = :userId
          AND s.fromUser = :userId
          AND s.status = 'ACTIVE'
          AND g.deleted = false
        GROUP BY g.id, g.name
    """)
    List<GroupSettlementRow> youNeedToSettlePerGroup(UUID userId);

    @Query("""
        SELECT new com.example.splitwise_cc_backend.model.response.SettlementResponse(
            fu.name,
            tu.name,
            s.amount,
            s.status
        )
        FROM Settlements s,
             User fu,
             User tu
        WHERE s.groupId = :groupId
          AND s.status IN ('ACTIVE', 'SETTLED')
          AND fu.id = s.fromUser
          AND tu.id = s.toUser
        ORDER BY s.fromUser
    """)
    List<SettlementResponse> findByActiveSettlementGroupId(UUID groupId);
    List<Settlements> findByGroupId(UUID groupId);

    @Query("""
        SELECT new com.example.splitwise_cc_backend.model.response.PaymentSettleResponse(
            fu.name,
            fu.id,
            tu.name,
            tu.id,
            s.amount,
            s.status,
            s.id
        )
        FROM Settlements s,
             User fu,
             User tu
        WHERE s.groupId = :groupId
          AND s.status = 'ACTIVE'
          AND s.fromUser = :requesterId
          AND fu.id = s.fromUser
          AND tu.id = s.toUser
        ORDER BY s.createdAt ASC
    """)
    List<PaymentSettleResponse> findActiveOwedSettlements(
            UUID groupId,
            UUID requesterId
    );

    boolean existsByGroupIdAndStatus(UUID groupId, String status);
}
