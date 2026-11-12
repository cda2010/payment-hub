package com.gateway.payment.configuration.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "provider_webhook_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"hmacSecretEnc", "apiKeyEnc"})
public class ProviderWebhookConfig {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProvider provider;

    @Column(name = "path_suffix", nullable = false, length = 200)
    private String pathSuffix;

    @Column(name = "auth_mode", length = 50)
    private String authMode;

    @Column(name = "hmac_secret_enc", columnDefinition = "TEXT")
    private String hmacSecretEnc;

    @Column(name = "api_key_enc", columnDefinition = "TEXT")
    private String apiKeyEnc;

    @Column(name = "allowed_ips", columnDefinition = "TEXT")
    private String allowedIps;

    @Column(nullable = false)
    private boolean actif;
}
