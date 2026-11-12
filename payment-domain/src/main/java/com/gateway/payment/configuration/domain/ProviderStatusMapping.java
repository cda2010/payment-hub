package com.gateway.payment.configuration.domain;

import com.gateway.payment.common.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "provider_status_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderStatusMapping {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProvider provider;

    @Column(name = "provider_status", nullable = false, length = 100)
    private String providerStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway_status", nullable = false, length = 50)
    private PaymentStatus gatewayStatus;

    /**
     * Messages multi-langue, ex. {@code {"fr":"Succès","en":"Success"}}.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "messages", columnDefinition = "jsonb")
    private Map<String, String> messages;
}
