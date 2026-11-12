package com.gateway.payment.transfer.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfer_rail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRail {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rail_scope", nullable = false, length = 20)
    @Builder.Default
    private RailScope railScope = RailScope.EXTERNAL;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "XOF";

    @Column(name = "min_amount", precision = 18, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "requires_bank", nullable = false)
    @Builder.Default
    private boolean requiresBank = false;

    @Column(name = "same_country_only", nullable = false)
    @Builder.Default
    private boolean sameCountryOnly = false;

    @Column(name = "same_group_only", nullable = false)
    @Builder.Default
    private boolean sameGroupOnly = false;

    @Column(name = "cross_border_only", nullable = false)
    @Builder.Default
    private boolean crossBorderOnly = false;

    @Column(name = "extra_fields_json", columnDefinition = "TEXT")
    private String extraFieldsJson;

    @Column(nullable = false)
    private boolean actif;

    @Column(nullable = false)
    @Builder.Default
    private Integer ordre = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
