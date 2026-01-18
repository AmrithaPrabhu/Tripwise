package com.example.splitwise_cc_backend.repository;

import com.example.splitwise_cc_backend.model.Group;
import com.example.splitwise_cc_backend.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);
    List<GroupMember> findByUserId(UUID userId);
    List<GroupMember> findByGroupId(UUID groupId);
}
