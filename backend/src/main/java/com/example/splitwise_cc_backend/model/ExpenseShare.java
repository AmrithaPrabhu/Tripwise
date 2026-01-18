package com.example.splitwise_cc_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "expense_shares")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseShare {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Expense expense;

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private Double shareAmount;
}
