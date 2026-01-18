package com.example.splitwise_cc_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "settlements")
public class Settlements {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID groupId;

    private UUID fromUser;

    private UUID toUser;

    private BigDecimal amount;

    private String status;

    private Instant createdAt;
}

