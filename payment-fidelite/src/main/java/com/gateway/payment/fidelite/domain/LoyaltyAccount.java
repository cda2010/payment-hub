package com.gateway.payment.fidelite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(
        name = "loyalty_account",
        uniqueConstraints = @UniqueConstraint(
                name = "loyalty_account_party_uk",
                columnNames = {"tenant_id", "programme_id", "party_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class LoyaltyAccount {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(nullable = false)
    private long balance;

    @Column(name = "reserved_balance", nullable = false)
    private long reservedBalance;

    @Column(nullable = false)
    private long version;
}
