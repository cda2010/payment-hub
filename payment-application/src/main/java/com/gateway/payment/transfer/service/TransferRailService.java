package com.gateway.payment.transfer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.transfer.api.dto.TransferFieldDto;
import com.gateway.payment.transfer.api.dto.TransferRailRequest;
import com.gateway.payment.transfer.api.dto.TransferRailResponse;
import com.gateway.payment.transfer.domain.RailScope;
import com.gateway.payment.transfer.domain.TransferRail;
import com.gateway.payment.transfer.repository.TransferRailRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TransferRailService {

    private final TransferRailRepository railRepository;
    private final ObjectMapper objectMapper;

    public TransferRailService(
            TransferRailRepository railRepository,
            ObjectMapper objectMapper
    ) {
        this.railRepository = railRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<TransferRailResponse> listAll(boolean activeOnly) {
        List<TransferRail> rails = activeOnly
                ? railRepository.findByActifTrueOrderByOrdreAscCodeAsc()
                : railRepository.findAllByOrderByOrdreAscCodeAsc();
        return rails.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TransferRailResponse get(UUID id) {
        return toResponse(require(id));
    }

    @Transactional(readOnly = true)
    public TransferRail requireByCode(String code) {
        return railRepository.findByCodeIgnoreCase(code)
                .filter(TransferRail::isActif)
                .orElseThrow(() -> new BusinessException(
                        "RAIL_NOT_FOUND", "Rail introuvable ou inactif: " + code, HttpStatus.NOT_FOUND));
    }

    /**
     * Rails éligibles pour une paire émetteur/bénéficiaire + montant.
     */
    @Transactional(readOnly = true)
    public List<TransferRailResponse> listEligible(
            String emitterCode, String beneficiaryCode, BigDecimal amount
    ) {
        return findEligible(emitterCode, beneficiaryCode, amount).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransferRail> findEligible(
            String emitterCode, String beneficiaryCode, BigDecimal amount
    ) {
        if (emitterCode == null || emitterCode.isBlank() || beneficiaryCode == null || beneficiaryCode.isBlank()) {
            throw new BusinessException("PAIR_REQUIRED", "Émetteur et bénéficiaire sont requis");
        }
        return railRepository.findByActifTrueOrderByOrdreAscCodeAsc().stream()
                .filter(rail -> isEligible(rail, amount))
                .toList();
    }

    public record RailUxMeta(String displayLabel, String etaLabel, String warning) {}

    public RailUxMeta uxMeta(TransferRail r) {
        String code = r.getCode() != null ? r.getCode().trim().toUpperCase(Locale.ROOT) : "";
        return switch (code) {
            case "INTRA_GROUP" -> new RailUxMeta("Transfert intra-groupe", "Instantané", null);
            case "LOCAL" -> new RailUxMeta("Envoi local", "Quelques minutes", null);
            case "STAR_UEMOA" -> new RailUxMeta("Virement STAR UEMOA", "J+0 à J+1 (jours ouvrés)", null);
            case "RTGS" -> new RailUxMeta(
                    "Virement RTGS",
                    "Temps réel (jours ouvrés)",
                    "Réservé aux gros montants · irrévocable");
            case "SWIFT" -> new RailUxMeta(
                    "Transfert international SWIFT",
                    "1 à 3 jours ouvrés",
                    "Change et frais correspondants possibles");
            default -> new RailUxMeta(r.getName(), "Selon le réseau", null);
        };
    }

    public boolean isEligible(TransferRail rail, BigDecimal amount) {
        if (!rail.isActif()) {
            return false;
        }
        if (amount != null) {
            if (rail.getMinAmount() != null && amount.compareTo(rail.getMinAmount()) < 0) {
                return false;
            }
            if (rail.getMaxAmount() != null && amount.compareTo(rail.getMaxAmount()) > 0) {
                return false;
            }
        }
        return true;
    }

    public void assertEligible(TransferRail rail, BigDecimal amount) {
        if (!isEligible(rail, amount)) {
            throw new BusinessException(
                    "RAIL_NOT_ELIGIBLE",
                    "Le rail " + rail.getCode() + " n'est pas éligible pour ce montant");
        }
    }

    public List<TransferFieldDto> railFields(TransferRail rail) {
        return readFields(rail.getExtraFieldsJson());
    }

    @Transactional
    public TransferRailResponse create(TransferRailRequest request) {
        String code = normalize(request.code());
        if (railRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("RAIL_EXISTS", "Rail déjà existant");
        }
        TransferRail rail = map(new TransferRail(), request, code);
        return toResponse(railRepository.save(rail));
    }

    @Transactional
    public TransferRailResponse update(UUID id, TransferRailRequest request) {
        TransferRail rail = require(id);
        String code = normalize(request.code());
        if (!rail.getCode().equalsIgnoreCase(code) && railRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("RAIL_EXISTS", "Rail déjà existant");
        }
        map(rail, request, code);
        return toResponse(railRepository.save(rail));
    }

    private TransferRail map(TransferRail rail, TransferRailRequest request, String code) {
        rail.setCode(code);
        rail.setName(request.name().trim());
        rail.setDescription(request.description());
        rail.setRailScope(request.railScope() != null ? request.railScope() : RailScope.EXTERNAL);
        rail.setCurrency(request.currency() != null ? request.currency().trim().toUpperCase(Locale.ROOT) : "XOF");
        rail.setMinAmount(request.minAmount());
        rail.setMaxAmount(request.maxAmount());
        rail.setRequiresBank(Boolean.TRUE.equals(request.requiresBank()));
        rail.setSameCountryOnly(Boolean.TRUE.equals(request.sameCountryOnly()));
        rail.setSameGroupOnly(Boolean.TRUE.equals(request.sameGroupOnly()));
        rail.setCrossBorderOnly(Boolean.TRUE.equals(request.crossBorderOnly()));
        rail.setExtraFieldsJson(writeFields(request.extraFields()));
        rail.setActif(Boolean.TRUE.equals(request.actif()));
        rail.setOrdre(request.ordre() != null ? request.ordre() : 0);
        return rail;
    }

    private TransferRail require(UUID id) {
        return railRepository.findById(id)
                .orElseThrow(() -> new BusinessException("RAIL_NOT_FOUND", "Rail introuvable", HttpStatus.NOT_FOUND));
    }

    private TransferRailResponse toResponse(TransferRail r) {
        RailUxMeta ux = uxMeta(r);
        return new TransferRailResponse(
                r.getId(),
                r.getCode(),
                r.getName(),
                r.getDescription(),
                r.getRailScope(),
                r.getCurrency(),
                r.getMinAmount(),
                r.getMaxAmount(),
                r.isRequiresBank(),
                r.isSameCountryOnly(),
                r.isSameGroupOnly(),
                r.isCrossBorderOnly(),
                readFields(r.getExtraFieldsJson()),
                r.isActif(),
                r.getOrdre(),
                ux.displayLabel(),
                ux.etaLabel(),
                ux.warning()
        );
    }

    private List<TransferFieldDto> readFields(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String writeFields(List<TransferFieldDto> fields) {
        try {
            return objectMapper.writeValueAsString(fields != null ? fields : List.of());
        } catch (Exception ex) {
            return "[]";
        }
    }

    private static String normalize(String code) {
        return code.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }
}
