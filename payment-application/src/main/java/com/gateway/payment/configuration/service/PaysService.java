package com.gateway.payment.configuration.service;

import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.api.dto.PaysRequest;
import com.gateway.payment.configuration.api.dto.PaysResponse;
import com.gateway.payment.configuration.domain.Pays;
import com.gateway.payment.configuration.repository.PaysRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PaysService {

    private final PaysRepository paysRepository;

    public PaysService(PaysRepository paysRepository) {
        this.paysRepository = paysRepository;
    }

    @Transactional(readOnly = true)
    public List<PaysResponse> list() {
        return paysRepository.findAllByOrderByOrdreAscNomAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaysResponse get(UUID id) {
        return toResponse(require(id));
    }

    @Transactional
    public PaysResponse create(PaysRequest request) {
        String code = normalizeCode(request.code());
        String nom = request.nom().trim();
        if (paysRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("PAYS_CODE_EXISTS", "Code pays déjà utilisé");
        }
        if (paysRepository.existsByNomIgnoreCase(nom)) {
            throw new BusinessException("PAYS_NOM_EXISTS", "Ce pays existe déjà");
        }
        Pays pays = Pays.builder()
                .code(code)
                .nom(nom)
                .indicatif(normaliserIndicatif(request.indicatif()))
                .devise(request.devise().trim().toUpperCase(Locale.ROOT))
                .actif(request.actif())
                .ordre(request.ordre() == null ? 0 : request.ordre())
                .build();
        return toResponse(paysRepository.save(pays));
    }

    @Transactional
    public PaysResponse update(UUID id, PaysRequest request) {
        Pays pays = require(id);
        String code = normalizeCode(request.code());
        String nom = request.nom().trim();
        paysRepository.findByCodeIgnoreCase(code)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("PAYS_CODE_EXISTS", "Code pays déjà utilisé");
                });
        paysRepository.findByNomIgnoreCase(nom)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("PAYS_NOM_EXISTS", "Ce pays existe déjà");
                });
        pays.setCode(code);
        pays.setNom(nom);
        pays.setIndicatif(normaliserIndicatif(request.indicatif()));
        pays.setDevise(request.devise().trim().toUpperCase(Locale.ROOT));
        pays.setActif(request.actif());
        pays.setOrdre(request.ordre() == null ? 0 : request.ordre());
        return toResponse(paysRepository.save(pays));
    }

    private Pays require(UUID id) {
        return paysRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PAYS_NOT_FOUND", "Pays introuvable", HttpStatus.NOT_FOUND));
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    /** Accepte 226, 00226 ou +226 et conserve le préfixe +. */
    private static String normaliserIndicatif(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException("PAYS_INDICATIF_REQUIS", "L'indicatif du pays est obligatoire");
        }
        String valeur = raw.trim().replace(" ", "").replace("-", "");
        if (valeur.startsWith("00")) {
            valeur = "+" + valeur.substring(2);
        }
        if (!valeur.startsWith("+")) {
            valeur = "+" + valeur;
        }
        if (!valeur.matches("\\+[1-9]\\d{0,3}")) {
            throw new BusinessException("PAYS_INDICATIF_INVALIDE", "L'indicatif du pays est invalide");
        }
        return valeur;
    }

    private PaysResponse toResponse(Pays pays) {
        return new PaysResponse(
                pays.getId(),
                pays.getCode(),
                pays.getNom(),
                pays.getIndicatif(),
                pays.getDevise(),
                pays.isActif(),
                pays.getOrdre()
        );
    }
}
