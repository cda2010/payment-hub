package com.gateway.payment.transfer.service;

import com.gateway.payment.datasources.api.CatalogItemDto;
import com.gateway.payment.transfer.domain.TransferBeneficiary;
import com.gateway.payment.transfer.repository.TransferBeneficiaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Carnet de bénéficiaires — hydratation catalogue + CRUD.
 * Un bénéficiaire a un libellé, un rail, et un téléphone ou un numéro de compte.
 */
@Service
public class TransferBeneficiaryService {

    private final TransferBeneficiaryRepository repository;

    public TransferBeneficiaryService(TransferBeneficiaryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CatalogItemDto> listCatalogItems(int limit) {
        List<TransferBeneficiary> rows = repository.findByActifTrueOrderByOrdreAffichageAscLabelAsc();
        List<CatalogItemDto> out = new ArrayList<>(rows.size());
        int max = limit > 0 ? Math.min(limit, rows.size()) : rows.size();
        for (int i = 0; i < max; i++) {
            out.add(toItem(rows.get(i)));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<TransferBeneficiary> listActive() {
        return repository.findByActifTrueOrderByOrdreAffichageAscLabelAsc();
    }

    @Transactional(readOnly = true)
    public TransferBeneficiary get(UUID id) {
        TransferBeneficiary b = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("beneficiary not found: " + id));
        if (!b.isActif()) {
            throw new NoSuchElementException("beneficiary not found: " + id);
        }
        return b;
    }

    @Transactional
    public TransferBeneficiary create(TransferBeneficiary draft) {
        normalizeAndValidate(draft);
        if (!StringUtils.hasText(draft.getIcon())) {
            draft.setIcon("person-outline");
        }
        if (!StringUtils.hasText(draft.getOwnerKey())) {
            draft.setOwnerKey("demo");
        }
        draft.setActif(true);
        return repository.save(draft);
    }

    @Transactional
    public TransferBeneficiary update(UUID id, TransferBeneficiary patch) {
        TransferBeneficiary existing = get(id);
        existing.setLabel(patch.getLabel());
        existing.setPhone(patch.getPhone());
        existing.setAccountNumber(patch.getAccountNumber());
        existing.setAgencyCode(patch.getAgencyCode());
        existing.setDestinationProvider(patch.getDestinationProvider());
        if (StringUtils.hasText(patch.getIcon())) {
            existing.setIcon(patch.getIcon().trim());
        }
        normalizeAndValidate(existing);
        return repository.save(existing);
    }

    /** Soft-delete — le carnet et le catalogue n'affichent que les actifs. */
    @Transactional
    public void delete(UUID id) {
        TransferBeneficiary existing = get(id);
        existing.setActif(false);
        repository.save(existing);
    }

    /**
     * Aligne phone / accountNumber sur le schéma participant FSE
     * ({@code beneficiaryFieldsJson} + {@link ParticipantKind}).
     */
    private void normalizeAndValidate(TransferBeneficiary b) {
        if (!StringUtils.hasText(b.getLabel())) {
            throw new IllegalArgumentException("label is required");
        }
        if (!StringUtils.hasText(b.getDestinationProvider())) {
            throw new IllegalArgumentException("destinationProvider is required");
        }
        b.setLabel(b.getLabel().trim());
        String providerCode = b.getDestinationProvider().trim();
        b.setDestinationProvider(providerCode);

        if (!StringUtils.hasText(b.getPhone()) && !StringUtils.hasText(b.getAccountNumber())) {
            throw new IllegalArgumentException("phone or accountNumber is required for " + providerCode);
        }
        if (StringUtils.hasText(b.getPhone())) {
            b.setPhone(b.getPhone().trim());
        } else {
            b.setPhone(null);
        }
        if (StringUtils.hasText(b.getAccountNumber())) {
            b.setAccountNumber(b.getAccountNumber().trim());
        } else {
            b.setAccountNumber(null);
        }
        if (StringUtils.hasText(b.getAgencyCode())) {
            b.setAgencyCode(b.getAgencyCode().trim());
        } else {
            b.setAgencyCode(null);
        }
    }

    private CatalogItemDto toItem(TransferBeneficiary b) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("beneficiaryId", b.getId() != null ? b.getId().toString() : null);
        meta.put("destinationProvider", b.getDestinationProvider());
        if (StringUtils.hasText(b.getPhone())) {
            meta.put("phone", b.getPhone());
            meta.put("destPhone", b.getPhone());
            meta.put("msisdn", b.getPhone());
        }
        if (StringUtils.hasText(b.getAccountNumber())) {
            meta.put("accountNumber", b.getAccountNumber());
            meta.put("destAccount", b.getAccountNumber());
            meta.put("numeroCompte", b.getAccountNumber());
        }
        if (StringUtils.hasText(b.getAgencyCode())) {
            meta.put("agencyCode", b.getAgencyCode());
            meta.put("agence", b.getAgencyCode());
            meta.put("codeAgence", b.getAgencyCode());
        }
        meta.put("label", b.getLabel());
        String sub = firstNonBlank(
                b.getPhone(),
                StringUtils.hasText(b.getAgencyCode()) && StringUtils.hasText(b.getAccountNumber())
                        ? b.getAgencyCode() + " · " + b.getAccountNumber()
                        : null,
                b.getAccountNumber(),
                b.getDestinationProvider());
        return new CatalogItemDto(
                b.getId() != null ? b.getId().toString() : b.getLabel(),
                b.getLabel(),
                sub,
                StringUtils.hasText(b.getIcon()) ? b.getIcon() : "person-outline",
                b.getId() != null ? b.getId().toString() : null,
                meta
        );
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (StringUtils.hasText(v)) return v.trim();
        }
        return null;
    }
}
