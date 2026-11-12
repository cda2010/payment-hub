package com.gateway.payment.transfer.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fee_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeRule {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "XOF";

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean actif;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_bearer", nullable = false, length = 20)
    @Builder.Default
    private FeeBearer feeBearer = FeeBearer.SENDER;

    @OneToMany(mappedBy = "feeRule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    @Builder.Default
    private List<FeeRuleLine> lines = new ArrayList<>();

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

    public void replaceLines(List<FeeRuleLine> newLines) {
        lines.clear();
        if (newLines != null) {
            int i = 0;
            for (FeeRuleLine line : newLines) {
                line.setFeeRule(this);
                if (line.getOrdre() == null) {
                    line.setOrdre(i);
                }
                lines.add(line);
                i++;
            }
        }
    }
}

