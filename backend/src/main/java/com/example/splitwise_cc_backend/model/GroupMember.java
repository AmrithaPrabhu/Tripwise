package com.example.splitwise_cc_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "group_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Group group;

    @ManyToOne(optional = false)
    private User user;
}