package com.gateway.payment.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_execution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentExecution {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "payment_id", nullable = false, length = 64)
    private String paymentId;

    @Column(name = "step_code", length = 100)
    private String stepCode;

    @Column(name = "operation_code", length = 100)
    private String operationCode;

    @Column(name = "provider_code", length = 50)
    private String providerCode;

    @Column(name = "request_timestamp")
    private Instant requestTimestamp;

    @Column(name = "response_timestamp")
    private Instant responseTimestamp;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "provider_code_status", length = 50)
    private String providerCodeStatus;

    @Column(name = "provider_message", length = 500)
    private String providerMessage;

    @Column(name = "gateway_status", length = 40)
    private String gatewayStatus;

    @Column(name = "execution_duration_ms")
    private Long executionDurationMs;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
