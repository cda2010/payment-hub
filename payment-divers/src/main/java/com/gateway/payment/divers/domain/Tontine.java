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
@Table(name = "tontine")
@Getter
@Setter
@NoArgsConstructor
public class Tontine {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal contribution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TontineFrequency frequency;

    @Column(name = "round_no", nullable = false)
    private int roundNo;

    @Column(name = "invite_code", nullable = false, length = 16, unique = true)
    private String inviteCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TontineStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
