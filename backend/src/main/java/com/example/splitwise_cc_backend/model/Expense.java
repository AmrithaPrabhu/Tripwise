package com.example.splitwise_cc_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Group group;

    @ManyToOne(optional = false)
    private User paidBy;

    @Column(nullable = false)
    private Double amount;

    private String description;

    private Instant createdAt;
    private boolean deleted = false;

    private Instant deletedAt;
}
