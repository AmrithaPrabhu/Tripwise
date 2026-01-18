package com.example.splitwise_cc_backend.repository;

import com.example.splitwise_cc_backend.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    @Modifying
    @Query("""
        UPDATE Group g
        SET g.balanceVersion = g.balanceVersion + 1
        WHERE g.id = :groupId AND g.deleted = false
    """)
    void incrementBalanceVersion(@Param("groupId") UUID groupId);
    @Query("""
            SELECT g
              FROM Group g
              JOIN GroupMember gm ON gm.group.id = g.id
              WHERE gm.user.id = :userId AND g.deleted = false
            """)
    List<Group> findByUser(@Param("userId") UUID userId);
    Group findByIdAndDeletedFalse(UUID groupId);
}
