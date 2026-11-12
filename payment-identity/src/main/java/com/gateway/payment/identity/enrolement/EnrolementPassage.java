package com.gateway.payment.identity.enrolement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrolement_passage")
@Getter
@Setter
@NoArgsConstructor
public class EnrolementPassage {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "enrolement_id", nullable = false)
    private UUID enrolementId;

    @Column(nullable = false, length = 20)
    private String etape;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
