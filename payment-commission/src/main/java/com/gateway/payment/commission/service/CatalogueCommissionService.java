package com.gateway.payment.commission.service;

import com.gateway.payment.commission.api.dto.CommissionDtos.*;
import com.gateway.payment.commission.domain.*;
import com.gateway.payment.commission.repository.FormuleCommissionRepository;
import com.gateway.payment.commission.repository.GrilleCommissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class CatalogueCommissionService {

    private final FormuleCommissionRepository formules;
    private final GrilleCommissionRepository grilles;

    public CatalogueCommissionService(FormuleCommissionRepository formules, GrilleCommissionRepository grilles) {
        this.formules = formules;
        this.grilles = grilles;
    }

    @Transactional(readOnly = true)
    public List<FormuleDto> listerFormules() {
        return formules.findByStatusOrderByCodeAscVersionAsc(StatutFormuleCommission.ACTIVE).stream()
                .map(FormuleDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GrilleDto> listerGrilles(StatutGrilleCommission status) {
        List<GrilleCommission> list = status == null
                ? grilles.findAllByOrderByCodeAscVersionDesc()
                : grilles.findByStatusOrderByCodeAscVersionDesc(status);
        return list.stream().map(GrilleDto::resume).toList();
    }

    @Transactional(readOnly = true)
    public GrilleDto detailGrille(UUID id) {
        return GrilleDto.from(requireGrille(id));
    }

    public GrilleDto creerGrille(CreateGrilleRequest req) {
        if (req == null || !StringUtils.hasText(req.code()) || !StringUtils.hasText(req.libelle())
                || !StringUtils.hasText(req.operationType())) {
            throw new IllegalArgumentException("code, libelle et operationType sont obligatoires");
        }
        String code = req.code().trim().toUpperCase();
        int version = grilles.findByCodeAndVersion(code, 1).isPresent()
                ? grilles.findAllByOrderByCodeAscVersionDesc().stream()
                .filter(g -> code.equalsIgnoreCase(g.getCode()))
                .mapToInt(GrilleCommission::getVersion)
                .max()
                .orElse(0) + 1
                : 1;
        if (grilles.findByCodeAndVersion(code, version).isPresent()) {
            throw new IllegalArgumentException("Grille déjà existante: " + code + " v" + version);
        }
        GrilleCommission g = GrilleCommission.builder()
                .code(code)
                .version(version)
                .libelle(req.libelle().trim())
                .description(blankToNull(req.description()))
                .operationType(req.operationType().trim().toUpperCase())
                .productCode(blankToNull(req.productCode()))
                .channelCode(blankToNull(req.channelCode()))
                .currency(StringUtils.hasText(req.currency()) ? req.currency().trim().toUpperCase() : "XOF")
                .status(StatutGrilleCommission.BROUILLON)
                .validFrom(req.validFrom() != null ? req.validFrom() : LocalDate.now())
                .validTo(req.validTo())
                .envelopeMode(parseEnvelope(req.envelopeMode()))
                .build();
        return GrilleDto.from(grilles.save(g));
    }

    public GrilleDto majGrille(UUID id, UpdateGrilleRequest req) {
        GrilleCommission g = requireBrouillon(id);
        if (req == null) {
            throw new IllegalArgumentException("Corps requis");
        }
        if (StringUtils.hasText(req.libelle())) {
            g.setLibelle(req.libelle().trim());
        }
        g.setDescription(blankToNull(req.description()));
        if (StringUtils.hasText(req.operationType())) {
            g.setOperationType(req.operationType().trim().toUpperCase());
        }
        g.setProductCode(blankToNull(req.productCode()));
        g.setChannelCode(blankToNull(req.channelCode()));
        if (StringUtils.hasText(req.currency())) {
            g.setCurrency(req.currency().trim().toUpperCase());
        }
        if (req.validFrom() != null) {
            g.setValidFrom(req.validFrom());
        }
        g.setValidTo(req.validTo());
        if (StringUtils.hasText(req.envelopeMode())) {
            g.setEnvelopeMode(parseEnvelope(req.envelopeMode()));
        }
        return GrilleDto.from(grilles.save(g));
    }

    public GrilleDto publier(UUID id) {
        GrilleCommission g = requireGrille(id);
        if (g.getStatus() == StatutGrilleCommission.PUBLIEE) {
            throw new IllegalArgumentException("Grille déjà publiée");
        }
        if (g.getRegles().isEmpty()) {
            throw new IllegalArgumentException("Impossible de publier une grille sans règle");
        }
        g.setStatus(StatutGrilleCommission.PUBLIEE);
        return GrilleDto.from(grilles.save(g));
    }

    public void supprimerGrille(UUID id) {
        GrilleCommission g = requireBrouillon(id);
        grilles.delete(g);
    }

    /** Nouvelle version brouillon à partir d'une grille existante (copie des règles). */
    public GrilleDto nouvelleVersion(UUID id) {
        GrilleCommission src = requireGrille(id);
        int next = grilles.findAllByOrderByCodeAscVersionDesc().stream()
                .filter(g -> src.getCode().equalsIgnoreCase(g.getCode()))
                .mapToInt(GrilleCommission::getVersion)
                .max()
                .orElse(src.getVersion()) + 1;
        GrilleCommission copy = GrilleCommission.builder()
                .code(src.getCode())
                .version(next)
                .libelle(src.getLibelle())
                .description(src.getDescription())
                .operationType(src.getOperationType())
                .productCode(src.getProductCode())
                .channelCode(src.getChannelCode())
                .currency(src.getCurrency())
                .status(StatutGrilleCommission.BROUILLON)
                .validFrom(LocalDate.now())
                .validTo(src.getValidTo())
                .envelopeMode(src.getEnvelopeMode())
                .build();
        for (RegleCommission r : src.getRegles()) {
            copy.getRegles().add(RegleCommission.builder()
                    .grille(copy)
                    .code(r.getCode())
                    .libelle(r.getLibelle())
                    .beneficiaryRole(r.getBeneficiaryRole())
                    .formule(r.getFormule())
                    .commissionBase(r.getCommissionBase())
                    .tierSelectionSource(r.getTierSelectionSource())
                    .fundingType(r.getFundingType())
                    .funderRole(r.getFunderRole())
                    .paramsJson(r.getParamsJson())
                    .floorAmount(r.getFloorAmount())
                    .capAmount(r.getCapAmount())
                    .priority(r.getPriority())
                    .active(r.isActive())
                    .build());
        }
        return GrilleDto.from(grilles.save(copy));
    }

    public RegleDto ajouterRegle(UUID grilleId, UpsertRegleRequest req) {
        GrilleCommission g = requireBrouillon(grilleId);
        RegleCommission r = buildRegle(g, req, null);
        g.getRegles().add(r);
        grilles.save(g);
        return RegleDto.from(r);
    }

    public RegleDto majRegle(UUID grilleId, UUID regleId, UpsertRegleRequest req) {
        GrilleCommission g = requireBrouillon(grilleId);
        RegleCommission existing = g.getRegles().stream()
                .filter(r -> r.getId().equals(regleId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Règle introuvable: " + regleId));
        RegleCommission updated = buildRegle(g, req, existing);
        return RegleDto.from(updated);
    }

    public void supprimerRegle(UUID grilleId, UUID regleId) {
        GrilleCommission g = requireBrouillon(grilleId);
        boolean removed = g.getRegles().removeIf(r -> r.getId().equals(regleId));
        if (!removed) {
            throw new NoSuchElementException("Règle introuvable: " + regleId);
        }
        grilles.save(g);
    }

    private RegleCommission buildRegle(GrilleCommission g, UpsertRegleRequest req, RegleCommission existing) {
        if (req == null || !StringUtils.hasText(req.code()) || !StringUtils.hasText(req.libelle())
                || !StringUtils.hasText(req.beneficiaryRole()) || !StringUtils.hasText(req.formuleCode())) {
            throw new IllegalArgumentException("code, libelle, beneficiaryRole et formuleCode sont obligatoires");
        }
        int formuleVersion = req.formuleVersion() != null ? req.formuleVersion() : 1;
        FormuleCommission formule = formules.findByCodeAndVersion(req.formuleCode().trim().toUpperCase(), formuleVersion)
                .orElseThrow(() -> new NoSuchElementException(
                        "Formule introuvable: " + req.formuleCode() + " v" + formuleVersion));

        RegleCommission r = existing != null ? existing : RegleCommission.builder().grille(g).build();
        r.setCode(req.code().trim().toUpperCase());
        r.setLibelle(req.libelle().trim());
        r.setBeneficiaryRole(req.beneficiaryRole().trim().toUpperCase());
        r.setFormule(formule);
        r.setCommissionBase(parseSource(req.commissionBase(), SourceCommission.TRANSACTION_FEES));
        r.setTierSelectionSource(StringUtils.hasText(req.tierSelectionSource())
                ? parseSource(req.tierSelectionSource(), null) : null);
        r.setFundingType(parseFunding(req.fundingType()));
        r.setFunderRole(blankToNull(req.funderRole()));
        r.setParamsJson(StringUtils.hasText(req.paramsJson()) ? req.paramsJson().trim() : "{}");
        r.setFloorAmount(req.floorAmount());
        r.setCapAmount(req.capAmount());
        r.setPriority(req.priority() != null ? req.priority() : 100);
        r.setActive(req.active() == null || req.active());
        return r;
    }

    private GrilleCommission requireGrille(UUID id) {
        return grilles.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Grille not found: " + id));
    }

    private GrilleCommission requireBrouillon(UUID id) {
        GrilleCommission g = requireGrille(id);
        if (g.getStatus() != StatutGrilleCommission.BROUILLON) {
            throw new IllegalArgumentException("Seules les grilles BROUILLON sont modifiables (créez une nouvelle version)");
        }
        return g;
    }

    private static EnvelopeMode parseEnvelope(String raw) {
        if (!StringUtils.hasText(raw)) {
            return EnvelopeMode.FULL_FEES;
        }
        try {
            return EnvelopeMode.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("envelopeMode invalide: " + raw);
        }
    }

    private static SourceCommission parseSource(String raw, SourceCommission def) {
        if (!StringUtils.hasText(raw)) {
            if (def == null) {
                throw new IllegalArgumentException("source requise");
            }
            return def;
        }
        try {
            return SourceCommission.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("commissionBase/source invalide: " + raw);
        }
    }

    private static FundingType parseFunding(String raw) {
        if (!StringUtils.hasText(raw)) {
            return FundingType.TRANSACTION_FEES;
        }
        try {
            return FundingType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("fundingType invalide: " + raw);
        }
    }

    private static String blankToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }
}
