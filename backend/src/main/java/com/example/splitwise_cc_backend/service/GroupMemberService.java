package com.example.splitwise_cc_backend.service;

import com.example.splitwise_cc_backend.model.*;
import com.example.splitwise_cc_backend.model.response.GroupMemberAddResponse;
import com.example.splitwise_cc_backend.model.response.UserLoginDTO;
import com.example.splitwise_cc_backend.repository.AuditRepository;
import com.example.splitwise_cc_backend.repository.GroupMemberRepository;
import com.example.splitwise_cc_backend.repository.GroupRepository;
import com.example.splitwise_cc_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupMemberService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final AuditRepository auditRepository;
    ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public GroupMemberAddResponse addMembersBatch(
            UUID groupId,
            UUID requesterId,
            List<String> emailIds
    ) {

        Group group = groupRepository.findByIdAndDeletedFalse(groupId);
        if(group.isDeleted()) {
            throw new RuntimeException("Can't add members in the deleted group.");
        }
        boolean requesterIsMember =
                groupMemberRepository.existsByGroupIdAndUserId(groupId, requesterId);

        if (!requesterIsMember) {
            throw new RuntimeException("Only group members can add others");
        }
        List<String> added = new ArrayList<>();
        List<String> alreadyMembers = new ArrayList<>();
        List<String> notFound = new ArrayList<>();

        emailIds.stream().forEach(emailId -> {
            Optional<User> user = userRepository.findByEmail(emailId);
            if(user.isPresent()) {
                boolean exists =
                        groupMemberRepository.existsByGroupIdAndUserId(groupId, user.get().getId());
                if(!exists) {
                    GroupMember member = GroupMember.builder()
                            .group(group)
                            .user(user.get())
                            .build();
                    Map<String,Object> memberJson = Map.of("name", member.getUser().getName());
                    Audit audit = Audit.builder()
                            .groupId(group.getId())
                            .entityType(EntityType.MEMBER)
                            .entityId(user.get().getId())
                            .action(AuditAction.ADDED)
                            .performedBy(requesterId)
                            .metadata(objectMapper.writeValueAsString(memberJson))
                            .createdAt(Instant.now())
                            .build();
                    auditRepository.save(audit);
                    groupMemberRepository.save(member);
                    added.add(emailId);
                }else{
                    alreadyMembers.add(emailId);
                }
            }else {
                notFound.add(emailId);
            }
        });

        return new GroupMemberAddResponse(added, alreadyMembers, notFound);
    }

    public List<UserLoginDTO> getAllGroupMembers(UUID groupId){
        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(groupMember ->
                        new UserLoginDTO(groupMember.getUser().getEmail(), groupMember.getUser().getName(),
                                groupMember.getUser().getId()))
                .toList();
    }
}
