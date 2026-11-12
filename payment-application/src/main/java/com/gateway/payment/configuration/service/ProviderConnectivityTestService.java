package com.gateway.payment.configuration.service;

import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.api.dto.ConnectivityTestResponse;
import com.gateway.payment.configuration.domain.ProviderEnvironment;
import com.gateway.payment.configuration.repository.ProviderEnvironmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ProviderConnectivityTestService {

    private final ProviderService providerService;
    private final ProviderEnvironmentRepository environmentRepository;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public ProviderConnectivityTestService(
            ProviderService providerService,
            ProviderEnvironmentRepository environmentRepository
    ) {
        this.providerService = providerService;
        this.environmentRepository = environmentRepository;
    }

    @Transactional(readOnly = true)
    public ConnectivityTestResponse test(UUID providerId) {
        providerService.requireProvider(providerId);
        ProviderEnvironment environment = resolveEnvironment(providerId);
        String baseUrl = environment.getBaseUrl();
        int timeoutSeconds = environment.getTimeoutSeconds() != null && environment.getTimeoutSeconds() > 0
                ? environment.getTimeoutSeconds()
                : 5;

        long start = System.currentTimeMillis();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeUrl(baseUrl)))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            long latency = System.currentTimeMillis() - start;

            if (response.statusCode() >= 200 && response.statusCode() < 500) {
                return new ConnectivityTestResponse(true,
                        "Connexion OK (HTTP " + response.statusCode() + ")", latency);
            }
            // Some servers reject HEAD — fallback to GET
            return tryGet(baseUrl, timeoutSeconds, start);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long latency = System.currentTimeMillis() - start;
            return new ConnectivityTestResponse(false, "Test interrompu", latency);
        } catch (Exception headError) {
            try {
                return tryGet(baseUrl, timeoutSeconds, start);
            } catch (Exception getError) {
                long latency = System.currentTimeMillis() - start;
                return new ConnectivityTestResponse(false,
                        "Échec de connexion: " + getError.getMessage(), latency);
            }
        }
    }

    private ConnectivityTestResponse tryGet(String baseUrl, int timeoutSeconds, long start) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(normalizeUrl(baseUrl)))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .GET()
                .build();
        HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        long latency = System.currentTimeMillis() - start;
        boolean ok = response.statusCode() >= 200 && response.statusCode() < 500;
        return new ConnectivityTestResponse(ok,
                (ok ? "Connexion OK" : "Réponse inattendue") + " (HTTP " + response.statusCode() + ")",
                latency);
    }

    private ProviderEnvironment resolveEnvironment(UUID providerId) {
        List<ProviderEnvironment> environments = environmentRepository.findByProviderId(providerId);
        if (environments.isEmpty()) {
            throw new BusinessException("ENVIRONMENT_NOT_FOUND",
                    "Aucun environnement configuré pour ce fournisseur", HttpStatus.BAD_REQUEST);
        }
        return environments.stream()
                .filter(ProviderEnvironment::isActif)
                .min(Comparator.comparingInt(this::environmentPriority)
                        .thenComparing(ProviderEnvironment::getCode))
                .orElse(environments.get(0));
    }

    private int environmentPriority(ProviderEnvironment env) {
        if (env == null || env.getCode() == null) {
            return 99;
        }
        return switch (env.getCode().trim().toUpperCase()) {
            case "SANDBOX" -> 0;
            case "DEVELOPPEMENT", "DEVELOPMENT", "DEV" -> 1;
            case "PRODUCTION", "PROD" -> 2;
            default -> 50;
        };
    }

    private String normalizeUrl(String baseUrl) {
        String url = baseUrl.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        return url;
    }
}
