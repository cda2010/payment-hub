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

import java.util.UUID;

@Entity
@Table(name = "tontine_member")
@Getter
@Setter
@NoArgsConstructor
public class TontineMember {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tontine_id", nullable = false)
    private Tontine tontine;

    @Column(nullable = false, length = 120)
    private String username;

    @Column(name = "party_id")
    private UUID partyId;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "turn_no", nullable = false)
    private int turnNo;

    @Column(nullable = false)
    private boolean paid;

    @Column(name = "admin_member", nullable = false)
    private boolean adminMember;
}
