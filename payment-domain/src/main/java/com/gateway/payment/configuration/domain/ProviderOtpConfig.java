package com.gateway.payment.configuration.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "provider_otp_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderOtpConfig {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false, unique = true)
    private PaymentProvider provider;

    @Column(name = "otp_required", nullable = false)
    private boolean otpRequired;

    @Column
    private Integer length;

    @Column(name = "validity_seconds")
    private Integer validitySeconds;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "resend_allowed", nullable = false)
    private boolean resendAllowed;

    @Column(name = "resend_delay_seconds")
    private Integer resendDelaySeconds;

    @Column(name = "generate_operation", length = 50)
    private String generateOperation;

    @Column(name = "validate_operation", length = 50)
    private String validateOperation;
}
