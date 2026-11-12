package com.gateway.payment.orchestration.service;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.orchestration.api.dto.RelationFseCorridorRequest;
import com.gateway.payment.orchestration.api.dto.RelationFseCorridorResponse;
import com.gateway.payment.orchestration.domain.CompteFse;
import com.gateway.payment.orchestration.domain.RelationFse;
import com.gateway.payment.orchestration.domain.TypeCompte;
import com.gateway.payment.orchestration.repository.CompteFseRepository;
import com.gateway.payment.orchestration.repository.RelationFseRepository;
import com.gateway.payment.orchestration.repository.TypeCompteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Admin des corridors inter-FSE ({@link RelationFse} + comptes LORO / MIROIR).
 */
@Service
public class RelationFseCorridorService {

    public static final String TYPE_LORO = "CPTELORO";
    public static final String TYPE_MIROIR = "CPTEMIROIR";

    private final RelationFseRepository relationRepository;
    private final CompteFseRepository compteRepository;
    private final TypeCompteRepository typeCompteRepository;

    public RelationFseCorridorService(
            RelationFseRepository relationRepository,
            CompteFseRepository compteRepository,
            TypeCompteRepository typeCompteRepository
    ) {
        this.relationRepository = relationRepository;
        this.compteRepository = compteRepository;
        this.typeCompteRepository = typeCompteRepository;
    }

    @Transactional(readOnly = true)
    public List<RelationFseCorridorResponse> listAll() {
        return relationRepository.findAllOrdered().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RelationFseCorridorResponse get(UUID id) {
        return toResponse(requireRelation(id));
    }

    @Transactional
    public List<RelationFseCorridorResponse> create(RelationFseCorridorRequest request) {
        String src = normalizeFse(request.fseSource());
        String dest = normalizeFse(request.fseDestination());
        if (src.equals(dest)) {
            throw new BusinessException(
                    "CORRIDOR_SAME_FSE",
                    "Un corridor inter-FSE exige deux codes FSE distincts",
                    HttpStatus.BAD_REQUEST
            );
        }
        ensureTypeComptes();

        List<RelationFseCorridorResponse> created = new ArrayList<>();
        created.add(createOne(
                src,
                dest,
                request.serviceType(),
                request.numeroCompteLoroSource().trim(),
                request.numeroCompteMiroirDest().trim()
        ));

        if (request.alsoCreateReverse()) {
            // Sens inverse : retag des mêmes numéros (LORO chez dest, MIROIR chez source)
            if (!relationRepository.existsByFseSourceAndFseDestinationAndServiceType(dest, src, request.serviceType())) {
                created.add(createOne(
                        dest,
                        src,
                        request.serviceType(),
                        request.numeroCompteMiroirDest().trim(),
                        request.numeroCompteLoroSource().trim()
                ));
            }
        }
        return created;
    }

    @Transactional
    public RelationFseCorridorResponse update(UUID id, RelationFseCorridorRequest request) {
        RelationFse relation = requireRelation(id);
        String src = normalizeFse(request.fseSource());
        String dest = normalizeFse(request.fseDestination());
        if (src.equals(dest)) {
            throw new BusinessException(
                    "CORRIDOR_SAME_FSE",
                    "Un corridor inter-FSE exige deux codes FSE distincts",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (!relation.getFseSource().equals(src)
                || !relation.getFseDestination().equals(dest)
                || relation.getServiceType() != request.serviceType()) {
            if (relationRepository.existsByFseSourceAndFseDestinationAndServiceType(src, dest, request.serviceType())) {
                throw conflict(src, dest, request.serviceType());
            }
        }
        ensureTypeComptes();
        relation.setFseSource(src);
        relation.setFseDestination(dest);
        relation.setServiceType(request.serviceType());
        relationRepository.save(relation);

        upsertCompte(relation, TYPE_LORO, src, request.numeroCompteLoroSource().trim(), request.serviceType());
        upsertCompte(relation, TYPE_MIROIR, dest, request.numeroCompteMiroirDest().trim(), request.serviceType());
        return toResponse(relation);
    }

    @Transactional
    public void delete(UUID id) {
        RelationFse relation = requireRelation(id);
        compteRepository.deleteByRelation_Id(relation.getId());
        relationRepository.delete(relation);
    }

    private RelationFseCorridorResponse createOne(
            String src,
            String dest,
            ServiceTypeCode serviceType,
            String loroNumero,
            String miroirNumero
    ) {
        if (relationRepository.existsByFseSourceAndFseDestinationAndServiceType(src, dest, serviceType)) {
            throw conflict(src, dest, serviceType);
        }
        RelationFse relation = relationRepository.save(RelationFse.builder()
                .fseSource(src)
                .fseDestination(dest)
                .serviceType(serviceType)
                .build());
        upsertCompte(relation, TYPE_LORO, src, loroNumero, serviceType);
        upsertCompte(relation, TYPE_MIROIR, dest, miroirNumero, serviceType);
        return toResponse(relation);
    }

    private void upsertCompte(
            RelationFse relation,
            String typeCode,
            String fseTeneur,
            String numero,
            ServiceTypeCode serviceType
    ) {
        TypeCompte type = typeCompteRepository.findById(typeCode)
                .orElseThrow(() -> new BusinessException(
                        "TYPE_COMPTE_NOT_FOUND",
                        "Type de compte manquant: " + typeCode,
                        HttpStatus.BAD_REQUEST
                ));
        CompteFse compte = compteRepository
                .findCompteFse(typeCode, serviceType, relation.getId(), fseTeneur)
                .orElseGet(CompteFse::new);
        compte.setRelation(relation);
        compte.setTypeCompte(type);
        compte.setFseTeneur(fseTeneur);
        compte.setFseTitulaire(fseTeneur);
        compte.setNumeroCompte(numero);
        compte.setServiceType(serviceType);
        if (compte.getCodeBanque() == null) {
            compte.setCodeBanque("00000");
        }
        if (compte.getCodeAgence() == null) {
            compte.setCodeAgence("");
        }
        if (compte.getExtype() == null) {
            compte.setExtype("CompteFse");
        }
        if (compte.getLibelleCompte() == null) {
            compte.setLibelleCompte("");
        }
        if (compte.getAgenceCode() == null) {
            compte.setAgenceCode("01001");
        }
        compteRepository.save(compte);
    }

    private void ensureTypeComptes() {
        if (typeCompteRepository.findById(TYPE_LORO).isEmpty()) {
            typeCompteRepository.save(TypeCompte.builder()
                    .codeParametre(TYPE_LORO)
                    .libelle("Compte LORO")
                    .build());
        }
        if (typeCompteRepository.findById(TYPE_MIROIR).isEmpty()) {
            typeCompteRepository.save(TypeCompte.builder()
                    .codeParametre(TYPE_MIROIR)
                    .libelle("Compte miroir")
                    .build());
        }
    }

    private RelationFse requireRelation(UUID id) {
        return relationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "CORRIDOR_NOT_FOUND",
                        "Corridor introuvable",
                        HttpStatus.NOT_FOUND
                ));
    }

    private RelationFseCorridorResponse toResponse(RelationFse relation) {
        List<RelationFseCorridorResponse.CompteFseItem> comptes = compteRepository
                .findByRelationIdWithType(relation.getId())
                .stream()
                .map(c -> new RelationFseCorridorResponse.CompteFseItem(
                        c.getId(),
                        c.getNumeroCompte(),
                        c.getTypeCompte().getCodeParametre(),
                        c.getTypeCompte().getLibelle(),
                        c.getFseTeneur(),
                        c.getFseTitulaire(),
                        c.getCodeBanque(),
                        c.getCodeAgence(),
                        c.getCle(),
                        c.getDevise(),
                        c.getAgenceCode()
                ))
                .toList();
        return new RelationFseCorridorResponse(
                relation.getId(),
                relation.getFseSource(),
                relation.getFseDestination(),
                relation.getServiceType(),
                relation.getCreatedAt(),
                comptes
        );
    }

    private static String normalizeFse(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("FSE_REQUIRED", "Code FSE requis", HttpStatus.BAD_REQUEST);
        }
        return code.trim();
    }

    private static BusinessException conflict(String src, String dest, ServiceTypeCode serviceType) {
        return new BusinessException(
                "CORRIDOR_EXISTS",
                "Corridor déjà défini pour " + src + " → " + dest + " / " + serviceType,
                HttpStatus.CONFLICT
        );
    }
}
