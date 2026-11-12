package com.gateway.payment.divers.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "coffre")
@Getter
@Setter
@NoArgsConstructor
public class Coffre {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "owner_username", nullable = false, length = 120)
    private String ownerUsername;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    @Column(name = "target_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal targetAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CoffreGoal goal;

    @Column(nullable = false)
    private boolean locked;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
