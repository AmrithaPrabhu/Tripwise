package com.example.splitwise_cc_backend.service;

import com.example.splitwise_cc_backend.exception.ConflictException;
import com.example.splitwise_cc_backend.model.*;
import com.example.splitwise_cc_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final AuditRepository auditRepository;
    private final SettlementsRepository settlementsRepository;

    @Transactional
    public Group createGroup(String groupName, UUID creatorUserId) {

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = Group.builder()
                .name(groupName)
                .createdBy(creator)
                .createdAt(Instant.now())
                .balanceVersion(0)
                .build();

        groupRepository.save(group);

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(creator)
                .build();

        groupMemberRepository.save(member);
        Audit audit = Audit.builder()
                .groupId(group.getId())
                .entityType(EntityType.GROUP)
                .entityId(group.getId())
                .action(AuditAction.CREATED)
                .performedBy(creator.getId())
                .createdAt(Instant.now())
                .build();
        auditRepository.save(audit);
        return group;
    }

    public List<Group> listAllGroups(UUID userId){
        List<GroupMember> groupMembers = groupMemberRepository.findByUserId(userId);
        return groupMembers.stream().map(GroupMember::getGroup)
                .filter(gm -> !gm.isDeleted())
                .collect(Collectors.toSet())
                .stream().toList();
    }

    public String getGroupNameById(UUID groupId) {
        Group group = groupRepository.findByIdAndDeletedFalse(groupId);
        return group.getName();
    }

    @Transactional
    public void deleteGroup(UUID groupId, boolean force) {

        boolean hasActiveSettlements =
                settlementsRepository.existsByGroupIdAndStatus(
                        groupId,
                        "ACTIVE"
                );

        if (hasActiveSettlements && !force) {
            throw new ConflictException(
                    "There are pending settlements in this group. Do you want to delete anyway?"
            );
        }
        Group group = groupRepository.findById(groupId).get();
        group.setDeleted(true);
        group.setDeletedAt(Instant.now());
        groupRepository.save(group);
    }
}

