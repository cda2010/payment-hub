package com.gateway.payment.divers.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "depense")
@Getter
@Setter
@NoArgsConstructor
public class Depense {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "payment_id", nullable = false, unique = true, length = 64)
    private String paymentId;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(name = "tenant_id", length = 64)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_code", nullable = false)
    private CategorieDepense categorie;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "merchant_name", length = 160)
    private String merchantName;

    @Column(name = "product_code", length = 80)
    private String productCode;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
