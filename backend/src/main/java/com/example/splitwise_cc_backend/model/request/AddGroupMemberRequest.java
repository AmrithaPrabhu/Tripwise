package com.example.splitwise_cc_backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
@AllArgsConstructor
public class AddGroupMemberRequest {
    private List<String> emailIds;
}
