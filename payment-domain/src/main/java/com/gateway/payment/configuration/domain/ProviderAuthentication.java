package com.gateway.payment.configuration.domain;

import com.gateway.payment.common.domain.AuthType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "provider_authentication")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {
        "clientSecretEnc",
        "apiKeyEnc",
        "usernameEnc",
        "passwordEnc",
        "customHeaderValueEnc"
})
public class ProviderAuthentication {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProvider provider;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    private ProviderEnvironment environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 50)
    private AuthType authType;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_secret_enc", columnDefinition = "TEXT")
    private String clientSecretEnc;

    @Column(name = "api_key_enc", columnDefinition = "TEXT")
    private String apiKeyEnc;

    @Column(name = "token_url")
    private String tokenUrl;

    @Column(name = "username_enc", columnDefinition = "TEXT")
    private String usernameEnc;

    @Column(name = "password_enc", columnDefinition = "TEXT")
    private String passwordEnc;

    @Column(name = "custom_header_name", length = 150)
    private String customHeaderName;

    @Column(name = "custom_header_value_enc", columnDefinition = "TEXT")
    private String customHeaderValueEnc;

    @Column(name = "extra_config_json", columnDefinition = "TEXT")
    private String extraConfigJson;

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
