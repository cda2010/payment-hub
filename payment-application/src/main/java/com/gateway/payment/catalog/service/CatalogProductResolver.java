package com.gateway.payment.catalog.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.gateway.payment.catalog.domain.CatalogDeService;
import com.gateway.payment.catalog.domain.CatalogDeServiceStatus;
import com.gateway.payment.catalog.domain.CatalogDeServiceVersion;
import com.gateway.payment.catalog.repository.CatalogDeServiceRepository;
import com.gateway.payment.catalog.repository.CatalogDeServiceVersionRepository;
import com.gateway.payment.common.config.GatewayPerformanceProperties;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Résout un produit catalogue (code / version) vers une version PUBLISHED — cache Caffeine hot path.
 */
@Service
public class CatalogProductResolver {

    private final CatalogDeServiceRepository serviceRepository;
    private final CatalogDeServiceVersionRepository versionRepository;
    private final LoadingCache<CacheKey, ResolvedCatalogProduct> cache;

    public CatalogProductResolver(
            CatalogDeServiceRepository serviceRepository,
            CatalogDeServiceVersionRepository versionRepository,
            GatewayPerformanceProperties performance
    ) {
        this.serviceRepository = serviceRepository;
        this.versionRepository = versionRepository;
        this.cache = Caffeine.newBuilder()
                .maximumSize(Math.max(100, performance.getCacheMaxSize()))
                .expireAfterWrite(Duration.ofSeconds(Math.max(60, performance.getCacheTtlSeconds())))
                .build(key -> load(key.code(), key.versionId()));
    }

    /**
     * @return null si aucun critère catalogue fourni (parcours legacy).
     */
    @Transactional(readOnly = true)
    public ResolvedCatalogProduct resolve(String catalogServiceCode, UUID catalogServiceVersionId) {
        if (catalogServiceVersionId == null && !StringUtils.hasText(catalogServiceCode)) {
            return null;
        }
        String code = StringUtils.hasText(catalogServiceCode) ? normalizeCode(catalogServiceCode) : "";
        return cache.get(new CacheKey(code, catalogServiceVersionId));
    }

    private ResolvedCatalogProduct load(String catalogServiceCode, UUID catalogServiceVersionId) {
        CatalogDeServiceVersion version;
        if (catalogServiceVersionId != null) {
            version = versionRepository.findById(catalogServiceVersionId)
                    .orElseThrow(() -> new BusinessException(
                            "CATALOG_SERVICE_VERSION_NOT_FOUND",
                            "Version catalogue introuvable: " + catalogServiceVersionId,
                            HttpStatus.NOT_FOUND));
        } else {
            String code = normalizeCode(catalogServiceCode);
            CatalogDeService service = serviceRepository.findByCodeIgnoreCase(code)
                    .orElseThrow(() -> new BusinessException(
                            "CATALOG_SERVICE_NOT_FOUND",
                            "Service catalogue introuvable: " + code,
                            HttpStatus.NOT_FOUND));
            if (service.getCurrentVersion() != null
                    && service.getCurrentVersion().getStatus() == CatalogDeServiceStatus.PUBLISHED) {
                version = service.getCurrentVersion();
            } else {
                version = versionRepository
                        .findFirstByCatalogDeServiceIdAndStatusOrderByEffectiveFromDesc(
                                service.getId(), CatalogDeServiceStatus.PUBLISHED)
                        .orElseThrow(() -> new BusinessException(
                                "CATALOG_SERVICE_NOT_PUBLISHED",
                                "Aucune version PUBLISHED pour: " + code));
            }
        }

        if (version.getStatus() != CatalogDeServiceStatus.PUBLISHED) {
            throw new BusinessException(
                    "CATALOG_SERVICE_VERSION_NOT_PUBLISHED",
                    "Version catalogue non publiée: " + version.getCatalogDeService().getCode()
                            + " v" + version.getVersion());
        }

        CatalogDeService service = version.getCatalogDeService();
        Map<String, Object> configuration = version.getConfiguration();
        ServiceTypeCode mapped = mapToServiceType(service.getCode(), configuration);
        return new ResolvedCatalogProduct(
                service.getCode(),
                version,
                blankToNull(version.getWorkflowKey()),
                mapped,
                configuration,
                configString(configuration, "defaultPayProvider", "default_pay_provider", "payProvider"),
                configString(configuration, "defaultBillProvider", "default_bill_provider", "billProvider"),
                configString(configuration, "defaultProvider", "default_provider", "provider")
        );
    }

    /**
     * Type de service = {@code configuration.serviceType} uniquement.
     * Absent / invalide → {@link ServiceTypeCode#DEFAUT} (sélectionnable en admin).
     * Plus aucun mapping code produit → type en Java.
     */
    static ServiceTypeCode mapToServiceType(String productCode, Map<String, Object> configuration) {
        if (configuration != null) {
            Object st = configuration.get("serviceType");
            if (st == null) {
                st = configuration.get("service_type");
            }
            if (st != null && StringUtils.hasText(String.valueOf(st))) {
                ServiceTypeCode fromConfig = ServiceTypeCode.fromString(String.valueOf(st));
                if (fromConfig != null) {
                    return fromConfig;
                }
            }
        }
        return ServiceTypeCode.CATALOG_DEFAULT;
    }

    static String configString(Map<String, Object> configuration, String... keys) {
        if (configuration == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = configuration.get(key);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value).trim();
            }
        }
        return null;
    }

    private static String normalizeCode(String raw) {
        return raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private record CacheKey(String code, UUID versionId) {}

    public void invalidate(String catalogServiceCode) {
        if (!StringUtils.hasText(catalogServiceCode)) {
            return;
        }
        String code = normalizeCode(catalogServiceCode);
        cache.asMap().keySet().stream()
                .filter(k -> code.equals(k.code()) || k.code().isEmpty())
                .forEach(cache::invalidate);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public record ResolvedCatalogProduct(
            String productCode,
            CatalogDeServiceVersion version,
            String workflowKey,
            ServiceTypeCode serviceType,
            Map<String, Object> configuration,
            String defaultPayProvider,
            String defaultBillProvider,
            String defaultProvider
    ) {}
}
