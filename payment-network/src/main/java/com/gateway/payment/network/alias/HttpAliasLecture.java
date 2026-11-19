package com.gateway.payment.network.alias;

import com.gateway.payment.network.NetworkException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Component
public class HttpAliasLecture implements AliasLecture {

    private final RestClient http;
    private final String baseUrl;

    public HttpAliasLecture(@Value("${network.alias.base-url:http://localhost:8097}") String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.http = RestClient.create();
    }

    @Override
    public AliasVu lire(UUID id, String authorization) {
        try {
            AliasVu alias = http.get()
                    .uri(baseUrl + "/api/v1/aliases/" + id)
                    .header("Authorization", authorization == null ? "" : authorization)
                    .retrieve()
                    .body(AliasVu.class);
            if (alias == null || !alias.active()) {
                throw NetworkException.introuvable("ALIAS_INEXISTANT", "Alias " + id + " introuvable.");
            }
            return alias;
        } catch (NetworkException e) {
            throw e;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw NetworkException.introuvable("ALIAS_INEXISTANT", "Alias " + id + " introuvable.");
            }
            throw new NetworkException(HttpStatus.BAD_GATEWAY, "ALIAS_INJOIGNABLE",
                    "Le service alias n'a pas répondu.");
        } catch (RuntimeException e) {
            throw new NetworkException(HttpStatus.BAD_GATEWAY, "ALIAS_INJOIGNABLE",
                    "Le service alias n'a pas répondu.");
        }
    }

    @Override
    public AliasVu compteOperation(UUID pointVenteId, String authorization) {
        try {
            AliasVu[] liste = http.get()
                    .uri(baseUrl + "/api/v1/aliases?ownerKind=POINT_VENTE&ownerId=" + pointVenteId
                            + "&etiquette=CPTEOPERATION")
                    .header("Authorization", authorization == null ? "" : authorization)
                    .retrieve()
                    .body(AliasVu[].class);
            if (liste != null) {
                for (AliasVu alias : liste) {
                    if (alias != null && alias.active() && "CPTEOPERATION".equals(alias.etiquette())) {
                        return alias;
                    }
                }
            }
            throw NetworkException.regle("COMPTE_OPERATION_ABSENT",
                    "Le point de vente n'a pas de compte d'opération actif.");
        } catch (NetworkException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new NetworkException(HttpStatus.BAD_GATEWAY, "ALIAS_INJOIGNABLE",
                    "Le service alias n'a pas répondu.");
        }
    }
}
