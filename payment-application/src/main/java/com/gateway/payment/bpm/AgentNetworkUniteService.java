package com.gateway.payment.bpm;

import com.gateway.payment.bc.AliasBcClient;
import com.gateway.payment.bc.NetworkBcClient;
import com.gateway.payment.bc.NetworkBcClient.Position;
import com.gateway.payment.bc.NetworkBcClient.Unite;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.domain.PaymentProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Vérifie qu'un code agent (UO payment-network) appartient au réseau wallet choisi
 * (ex. {@code CORIS_MONEY} → {@code codeFse=0011} sur la FSE ascendante),
 * puis résout le compte destination via payment-alias ({@code ownerKind=UNITE}).
 */
@Service
public class AgentNetworkUniteService {

    private final NetworkBcClient network;
    private final AliasBcClient alias;

    public AgentNetworkUniteService(NetworkBcClient network, AliasBcClient alias) {
        this.network = network;
        this.alias = alias;
    }

    /**
     * @param agentNetwork code provider (CORIS_MONEY, WAVE…)
     * @param agentCode    code UO (AG-OUAHIGOUYA, SD-VOL001-5-1…)
     * @param provider     provider résolu (avec {@code codeFse} Omnicanal éventuel)
     * @return variables à poser dans le contexte / BPM (récap + destinationAccount)
     */
    public Map<String, Object> resolveAndValidate(String agentNetwork, String agentCode, PaymentProvider provider) {
        Map<String, Object> out = new LinkedHashMap<>();
        String rail = agentNetwork == null ? "" : agentNetwork.trim().toUpperCase(Locale.ROOT);
        String code = agentCode == null ? "" : agentCode.trim().toUpperCase(Locale.ROOT);
        String codeFse = provider != null && StringUtils.hasText(provider.getCodeFse())
                ? provider.getCodeFse().trim().toUpperCase(Locale.ROOT)
                : null;

        out.put("agentNetwork", rail);

        // Rail Omnicanal (CORIS_MONEY…) : le code agent = UO réseau, obligatoire.
        if (StringUtils.hasText(codeFse) && !StringUtils.hasText(code)) {
            throw new BusinessException(
                    "AGENT_CODE_REQUIRED",
                    "Indiquez le code de l'unité d'organisation (agent / point) pour le réseau " + rail + ".",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (!StringUtils.hasText(code)) {
            return out;
        }
        if (!network.enabled()) {
            throw new BusinessException(
                    "NETWORK_DISABLED",
                    "La vérification réseau (payment-network) est désactivée.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }

        Position position;
        try {
            position = network.positionByCode(code).orElse(null);
        } catch (IllegalStateException e) {
            throw new BusinessException(
                    "NETWORK_UNAVAILABLE",
                    e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (position == null || position.unite() == null) {
            throw new BusinessException(
                    "AGENT_UNITE_NOT_FOUND",
                    "Unité d'organisation « " + code + " » introuvable dans le réseau.",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Unite u = position.unite();
        if (u.statut() != null && !"ACTIF".equalsIgnoreCase(u.statut())) {
            throw new BusinessException(
                    "AGENT_UNITE_INACTIVE",
                    "L'unité « " + code + " » n'est pas active.",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Unite fse = null;
        if (StringUtils.hasText(codeFse)) {
            fse = position.lignee().stream()
                    .filter(x -> x != null && "FSE".equalsIgnoreCase(x.type()))
                    .filter(x -> codeFse.equalsIgnoreCase(x.codeFse()))
                    .findFirst()
                    .orElse(null);
            if (fse == null) {
                throw new BusinessException(
                        "AGENT_UNITE_WRONG_NETWORK",
                        "L'unité « " + code + " » n'appartient pas au réseau " + rail
                                + " (FSE codeFse=" + codeFse + ").",
                        HttpStatus.UNPROCESSABLE_ENTITY);
            }
        } else {
            fse = position.lignee().stream()
                    .filter(x -> x != null && "FSE".equalsIgnoreCase(x.type()))
                    .findFirst()
                    .orElse(null);
        }

        out.put("agentCode", u.code());
        out.put("agentUniteId", u.id());
        out.put("agentUniteLibelle", u.libelle());
        out.put("agentUniteType", u.type());
        out.put("destinationUniteId", u.id());
        out.put("destinationUniteType", u.type());
        out.put("lignee", lineage(position.lignee(), u));
        out.put("agentZone", position.zone() != null ? position.zone() : u.zone());
        if (fse != null) {
            out.put("agentFseCode", fse.code());
            out.put("agentFseLibelle", fse.libelle());
            if (StringUtils.hasText(fse.codeFse())) {
                out.put("agentFseCodeFse", fse.codeFse());
            }
        }
        // Affichage récap compact
        out.put("agentUniteDisplay",
                u.code() + " — " + (u.libelle() != null ? u.libelle() : "")
                        + (u.type() != null ? " (" + u.type() + ")" : ""));

        enrichDestinationFromAlias(out, u.id(), rail);
        return out;
    }

    /**
     * Compte destination agent = alias UNITE ({@code externalRef}), ex. {@code 000000104}.
     */
    private static List<Map<String, String>> lineage(List<NetworkBcClient.Unite> nodes, NetworkBcClient.Unite self) {
        List<Map<String, String>> rows = new ArrayList<>();
        if (nodes != null) {
            for (NetworkBcClient.Unite node : nodes) {
                addUnite(rows, node);
            }
        }
        if (rows.isEmpty()) {
            addUnite(rows, self);
        }
        return rows;
    }

    private static void addUnite(List<Map<String, String>> rows, NetworkBcClient.Unite unite) {
        if (unite == null || !StringUtils.hasText(unite.id())) {
            return;
        }
        Map<String, String> row = new LinkedHashMap<>();
        row.put("id", unite.id().trim());
        if (StringUtils.hasText(unite.type())) {
            row.put("type", unite.type().trim());
        }
        rows.add(row);
    }

    private void enrichDestinationFromAlias(Map<String, Object> out, String uniteIdRaw, String agentNetwork) {
        if (!StringUtils.hasText(uniteIdRaw) || !alias.enabled()) {
            return;
        }
        UUID uniteId;
        try {
            uniteId = UUID.fromString(uniteIdRaw.trim());
        } catch (IllegalArgumentException e) {
            return;
        }
        try {
            alias.resolveUniteAccount(uniteId, "CORIS_DEMO", agentNetwork).ifPresent(a -> {
                out.put("destinationAccount", a.externalRef());
                out.put("agentAccount", a.externalRef());
                out.put("agentAliasCode", a.aliasCode());
                out.put("agentAccountType", a.aliasType());
                if (StringUtils.hasText(a.label())) {
                    out.put("destinationAccountLabel", a.label());
                }
            });
        } catch (IllegalStateException e) {
            throw new BusinessException(
                    "ALIAS_UNAVAILABLE",
                    e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
