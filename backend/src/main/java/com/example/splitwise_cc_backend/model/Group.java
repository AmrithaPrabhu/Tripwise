package com.example.splitwise_cc_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    private User createdBy;

    private Instant createdAt;

    @Column(nullable = false)
    private Integer balanceVersion;

    private boolean deleted = false;

    private Instant deletedAt;
}