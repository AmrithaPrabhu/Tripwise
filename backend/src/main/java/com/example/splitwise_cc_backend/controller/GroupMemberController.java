package com.example.splitwise_cc_backend.controller;

import com.example.splitwise_cc_backend.model.GroupMember;
import com.example.splitwise_cc_backend.model.request.AddGroupMemberRequest;
import com.example.splitwise_cc_backend.model.response.GroupMemberAddResponse;
import com.example.splitwise_cc_backend.model.response.UserLoginDTO;
import com.example.splitwise_cc_backend.service.GroupMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/groups/{groupId}/members")
@RequiredArgsConstructor
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    @PostMapping
    public GroupMemberAddResponse addMember(@PathVariable UUID groupId,
                                            @RequestBody AddGroupMemberRequest request,
                                            Authentication authentication) {

        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();
        System.out.println("coming here");
        return groupMemberService.addMembersBatch(
                groupId,
                principal.getUserId(),
                request.getEmailIds()
        );
    }

    @GetMapping
    public List<UserLoginDTO> getMembers(@PathVariable UUID groupId, Authentication authentication) {
        return groupMemberService.getAllGroupMembers(groupId);
    }
}