package com.gateway.payment.orchestration.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.orchestration.domain.InterfaceUtilisateur;
import com.gateway.payment.orchestration.domain.TypeEnvoi;
import com.gateway.payment.orchestration.repository.InterfaceUtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Cache runtime des interfaces Omnicanal (évite requêtes JPA + lazy loads par jambe).
 */
@Service
public class OrchestrationInterfaceCache {

    private final LoadingCache<CacheKey, InterfaceUtilisateur> cache;

    public OrchestrationInterfaceCache(
            InterfaceUtilisateurRepository interfaceRepository,
            @Value("${gateway.performance.cache-ttl-seconds:600}") int cacheTtlSeconds,
            @Value("${gateway.performance.cache-max-size:2000}") int cacheMaxSize
    ) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(Math.max(100, cacheMaxSize))
                .expireAfterWrite(Duration.ofSeconds(Math.max(60, cacheTtlSeconds)))
                .build(key -> interfaceRepository.findActiveByCodeFseAndTypeMethodeAndTypeEnvoi(
                        key.codeFse(),
                        key.typeMethode(),
                        key.typeEnvoi()
                ).orElseThrow(() -> new BusinessException(
                        "INTERFACE_NOT_FOUND",
                        "Interface utilisateur introuvable pour FSE " + key.codeFse() + " / " + key.typeMethode(),
                        HttpStatus.NOT_FOUND
                )));
    }

    public InterfaceUtilisateur resolve(String codeFse, String typeMethode, TypeEnvoi typeEnvoi) {
        return cache.get(new CacheKey(
                codeFse,
                typeMethode.trim().toUpperCase(),
                typeEnvoi != null ? typeEnvoi : TypeEnvoi.REQUEST
        ));
    }

    public void invalidate(String codeFse, String typeMethode, TypeEnvoi typeEnvoi) {
        if (codeFse == null || typeMethode == null) {
            return;
        }
        cache.invalidate(new CacheKey(
                codeFse,
                typeMethode.trim().toUpperCase(),
                typeEnvoi != null ? typeEnvoi : TypeEnvoi.REQUEST
        ));
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    private record CacheKey(String codeFse, String typeMethode, TypeEnvoi typeEnvoi) {}
}
