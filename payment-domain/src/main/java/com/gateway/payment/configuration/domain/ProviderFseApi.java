package com.gateway.payment.configuration.domain;

import com.gateway.payment.common.domain.AuthType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Api au sens Omnicanal (connectivité host/port/auth) — parent des Méthodes.
 * Ne pas confondre avec {@link ProviderApi} (= endpoint HTTP / nomMethode).
 */
@Entity
@Table(name = "provider_fse_api")
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
public class ProviderFseApi {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProvider provider;

    /** Environnement de déploiement (DEVELOPPEMENT / PRODUCTION) — pas le parent métier. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id")
    private ProviderEnvironment environment;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(name = "type_api", nullable = false, length = 20)
    private String typeApi;

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    @Column(length = 255)
    private String host;

    @Column(length = 20)
    private String port;

    @Column(name = "use_ssl", nullable = false)
    private boolean useSsl;

    @Column(name = "use_proxy", nullable = false)
    private boolean useProxy;

    @Column(nullable = false)
    private boolean actif;

    /** Auth inline Omnicanal (sur l'Api, pas sur l'environnement). */
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", length = 50)
    private AuthType authType;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_secret_enc", columnDefinition = "TEXT")
    private String clientSecretEnc;

    @Column(name = "api_key_enc", columnDefinition = "TEXT")
    private String apiKeyEnc;

    @Column(name = "token_url", length = 500)
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

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (typeApi == null || typeApi.isBlank()) {
            typeApi = "REST";
        }
        if (authType == null) {
            authType = AuthType.NONE;
        }
    }
}
