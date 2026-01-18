package com.example.splitwise_cc_backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GroupMemberAddResponse {
    private List<String> added;
    private List<String> alreadyMembers;
    private List<String> notFound;
}
