package com.gateway.payment.orchestration.service;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.orchestration.api.dto.CompteFseRequest;
import com.gateway.payment.orchestration.api.dto.CompteFseResponse;
import com.gateway.payment.orchestration.api.dto.TypeCompteResponse;
import com.gateway.payment.orchestration.domain.CompteFse;
import com.gateway.payment.orchestration.domain.RelationFse;
import com.gateway.payment.orchestration.domain.TypeCompte;
import com.gateway.payment.orchestration.repository.CompteFseRepository;
import com.gateway.payment.orchestration.repository.RelationFseRepository;
import com.gateway.payment.orchestration.repository.TypeCompteRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class CompteFseAdminService {

    private final CompteFseRepository compteRepository;
    private final RelationFseRepository relationRepository;
    private final TypeCompteRepository typeCompteRepository;
    private final ObjectProvider<OmnicanalCompteSource> compteSource;

    public CompteFseAdminService(
            CompteFseRepository compteRepository,
            RelationFseRepository relationRepository,
            TypeCompteRepository typeCompteRepository,
            ObjectProvider<OmnicanalCompteSource> compteSource
    ) {
        this.compteRepository = compteRepository;
        this.relationRepository = relationRepository;
        this.typeCompteRepository = typeCompteRepository;
        this.compteSource = compteSource;
    }

    @Transactional(readOnly = true)
    public List<CompteFseResponse> listAll() {
        OmnicanalCompteSource source = remote();
        if (source != null) {
            return source.list();
        }
        return compteRepository.findAllWithTypeAndRelation().stream()
                .sorted((a, b) -> {
                    int c1 = nullSafe(a.getFseTeneur()).compareToIgnoreCase(nullSafe(b.getFseTeneur()));
                    if (c1 != 0) {
                        return c1;
                    }
                    String ta = a.getTypeCompte() != null ? a.getTypeCompte().getCodeParametre() : "";
                    String tb = b.getTypeCompte() != null ? b.getTypeCompte().getCodeParametre() : "";
                    int c2 = ta.compareToIgnoreCase(tb);
                    if (c2 != 0) {
                        return c2;
                    }
                    return nullSafe(a.getNumeroCompte()).compareToIgnoreCase(nullSafe(b.getNumeroCompte()));
                })
                .map(this::toResponse)
                .toList();
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    @Transactional(readOnly = true)
    public CompteFseResponse get(UUID id) {
        OmnicanalCompteSource source = remote();
        if (source != null) {
            Optional<CompteFseResponse> remoteCompte = source.get(id);
            if (remoteCompte.isPresent()) {
                return remoteCompte.get();
            }
        }
        return toResponse(requireCompte(id));
    }

    private OmnicanalCompteSource remote() {
        OmnicanalCompteSource source = compteSource.getIfAvailable();
        if (source == null || !source.enabled()) {
            return null;
        }
        return source;
    }

    @Transactional(readOnly = true)
    public List<TypeCompteResponse> listTypes() {
        return typeCompteRepository.findAll().stream()
                .sorted((a, b) -> a.getCodeParametre().compareToIgnoreCase(b.getCodeParametre()))
                .map(t -> new TypeCompteResponse(t.getCodeParametre(), t.getLibelle()))
                .toList();
    }

    @Transactional
    public CompteFseResponse create(CompteFseRequest request) {
        CompteFse compte = new CompteFse();
        apply(compte, request);
        return toResponse(compteRepository.save(compte));
    }

    @Transactional
    public CompteFseResponse update(UUID id, CompteFseRequest request) {
        CompteFse compte = requireCompte(id);
        apply(compte, request);
        return toResponse(compteRepository.save(compte));
    }

    @Transactional
    public void delete(UUID id) {
        CompteFse compte = requireCompte(id);
        compteRepository.delete(compte);
    }

    private void apply(CompteFse compte, CompteFseRequest request) {
        RelationFse relation = resolveRelation(request.relationId());
        TypeCompte type = requireType(request.typeCompteCode());
        String teneur = normalizeFse(request.fseTeneur());
        String titulaire = StringUtils.hasText(request.fseTitulaire())
                ? request.fseTitulaire().trim()
                : teneur;

        compte.setNumeroCompte(normalizeNumero(request.numeroCompte()));
        compte.setTypeCompte(type);
        compte.setFseTeneur(teneur);
        compte.setFseTitulaire(titulaire);
        compte.setCodeBanque(blankTo(request.codeBanque(), "00000"));
        compte.setCodeAgence(request.codeAgence() != null ? request.codeAgence().trim() : "");
        compte.setCle(blankToNull(request.cle()));
        compte.setDevise(blankToNull(request.devise()));
        compte.setLibelleCompte(request.libelleCompte() != null ? request.libelleCompte() : "");
        compte.setCodeUoTitulaire(blankToNull(request.codeUoTitulaire()));
        compte.setAgenceCode(blankTo(
                request.agenceCode(),
                StringUtils.hasText(request.codeAgence()) ? request.codeAgence().trim() : "01001"
        ));
        compte.setExtype(blankTo(request.extype(), "CompteFse"));
        compte.setRelation(relation);
        compte.setServiceType(resolveServiceType(request.serviceType(), relation));
        compte.setMirroir(resolveMirroir(request.mirroirId()));
    }

    private CompteFse requireCompte(UUID id) {
        return compteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "COMPTE_FSE_NOT_FOUND",
                        "Compte catalogue introuvable",
                        HttpStatus.NOT_FOUND
                ));
    }

    private RelationFse resolveRelation(UUID id) {
        if (id == null) {
            return null;
        }
        return relationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "CORRIDOR_NOT_FOUND",
                        "Corridor / relation FSE introuvable",
                        HttpStatus.BAD_REQUEST
                ));
    }

    private CompteFse resolveMirroir(UUID id) {
        if (id == null) {
            return null;
        }
        return compteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "COMPTE_MIROIR_NOT_FOUND",
                        "Compte miroir lié introuvable",
                        HttpStatus.BAD_REQUEST
                ));
    }

    private static ServiceTypeCode resolveServiceType(ServiceTypeCode requested, RelationFse relation) {
        if (requested != null) {
            return requested;
        }
        return relation != null ? relation.getServiceType() : null;
    }

    private TypeCompte requireType(String code) {
        String normalized = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        return typeCompteRepository.findById(normalized)
                .orElseThrow(() -> new BusinessException(
                        "TYPE_COMPTE_NOT_FOUND",
                        "Type de compte inconnu: " + code,
                        HttpStatus.BAD_REQUEST
                ));
    }

    private static String normalizeNumero(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new BusinessException("COMPTE_NUMERO_REQUIRED", "Numéro de compte requis", HttpStatus.BAD_REQUEST);
        }
        return raw.trim();
    }

    private static String normalizeFse(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("FSE_REQUIRED", "Code FSE teneur requis", HttpStatus.BAD_REQUEST);
        }
        return code.trim();
    }

    private static String blankTo(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private CompteFseResponse toResponse(CompteFse c) {
        RelationFse rel = c.getRelation();
        return new CompteFseResponse(
                c.getId(),
                c.getOmniId(),
                c.getNumeroCompte(),
                c.getCodeBanque(),
                c.getCodeAgence(),
                c.getCle(),
                c.getExtype(),
                c.getTypeCompte() != null ? c.getTypeCompte().getCodeParametre() : null,
                c.getTypeCompte() != null ? c.getTypeCompte().getLibelle() : null,
                c.getDevise(),
                c.getFseTeneur(),
                c.getFseTitulaire(),
                c.getServiceType(),
                c.getLibelleCompte(),
                c.getCodeUoTitulaire(),
                c.getMirroir() != null ? c.getMirroir().getId() : null,
                c.getAgenceCode(),
                rel != null ? rel.getId() : null,
                rel != null ? rel.getFseSource() : null,
                rel != null ? rel.getFseDestination() : null,
                rel != null ? rel.getServiceType() : null,
                c.getCreatedAt()
        );
    }
}
