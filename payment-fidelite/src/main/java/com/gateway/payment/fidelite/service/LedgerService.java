package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.LoyaltyAccount;
import com.gateway.payment.fidelite.domain.PointMovement;
import com.gateway.payment.fidelite.repository.LoyaltyAccountRepository;
import com.gateway.payment.fidelite.repository.PointMovementRepository;
import com.gateway.payment.fidelite.repository.ProgrammeRepository;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Solde disponible = balance − réservé.
 * Un débit n'aboutit que si la mise à jour conditionnelle touche une ligne.
 * Une référence métier déjà écrite n'est pas rejouée.
 */
@Service
public class LedgerService {

    private final LoyaltyAccountRepository accounts;
    private final PointMovementRepository movements;
    private final ProgrammeRepository programmes;
    private final EntityManager entities;

    public LedgerService(
            LoyaltyAccountRepository accounts,
            PointMovementRepository movements,
            ProgrammeRepository programmes,
            EntityManager entities
    ) {
        this.accounts = accounts;
        this.movements = movements;
        this.programmes = programmes;
        this.entities = entities;
    }

    @Transactional(readOnly = true)
    public long disponible(String tenantId, UUID programmeId, UUID partyId) {
        return accounts.findByTenantIdAndProgramme_IdAndPartyId(tenantId, programmeId, partyId)
                .map(this::available)
                .orElse(0L);
    }

    @Transactional(readOnly = true)
    public List<PointMovement> historique(String tenantId, UUID programmeId, UUID partyId) {
        return accounts.findByTenantIdAndProgramme_IdAndPartyId(tenantId, programmeId, partyId)
                .map(account -> movements.findByAccount_IdOrderByCreatedAtDesc(account.getId()))
                .orElse(List.of());
    }

    @Transactional
    public long crediter(
            String tenantId,
            UUID programmeId,
            UUID partyId,
            long points,
            String motif,
            String reference
    ) {
        if (points <= 0) {
            return disponible(tenantId, programmeId, partyId);
        }
        String businessReference = reference(reference, "CREDIT");
        if (movements.existsByTenantIdAndBusinessReference(tenantId, businessReference)) {
            return disponible(tenantId, programmeId, partyId);
        }
        LoyaltyAccount account = open(tenantId, programmeId, partyId);
        try {
            write(account, tenantId, businessReference, "CREDIT", points, motif, null);
        } catch (DataIntegrityViolationException alreadyWritten) {
            return disponible(tenantId, programmeId, partyId);
        }
        if (accounts.credit(account.getId(), tenantId, points) != 1) {
            throw new IllegalStateException("Crédit refusé");
        }
        return disponible(tenantId, programmeId, partyId);
    }

    /**
     * Réserve puis consomme dans la même transaction.
     * La réservation échoue si le disponible ne couvre pas le coût.
     */
    @Transactional
    public long consommer(
            String tenantId,
            UUID programmeId,
            UUID partyId,
            long points,
            String motif,
            String reference
    ) {
        if (points <= 0) {
            throw new IllegalArgumentException("Le coût est positif");
        }
        String businessReference = reference(reference, "DEBIT");
        if (movements.existsByTenantIdAndBusinessReference(tenantId, businessReference)) {
            return disponible(tenantId, programmeId, partyId);
        }
        LoyaltyAccount account = accounts.findByTenantIdAndProgramme_IdAndPartyId(tenantId, programmeId, partyId)
                .orElseThrow(() -> new IllegalArgumentException("Points insuffisants pour cette récompense."));
        if (accounts.reserve(account.getId(), tenantId, points) != 1
                || accounts.consume(account.getId(), tenantId, points) != 1) {
            throw new IllegalArgumentException("Points insuffisants pour cette récompense.");
        }
        write(account, tenantId, businessReference, "DEBIT", -points, motif, null);
        return disponible(tenantId, programmeId, partyId);
    }

    private LoyaltyAccount open(String tenantId, UUID programmeId, UUID partyId) {
        return accounts.findByTenantIdAndProgramme_IdAndPartyId(tenantId, programmeId, partyId)
                .orElseGet(() -> {
                    LoyaltyAccount account = new LoyaltyAccount();
                    account.setTenantId(tenantId);
                    account.setProgramme(programmes.getReferenceById(programmeId));
                    account.setPartyId(partyId);
                    try {
                        return accounts.save(account);
                    } catch (DataIntegrityViolationException alreadyOpened) {
                        return accounts.findByTenantIdAndProgramme_IdAndPartyId(tenantId, programmeId, partyId)
                                .orElseThrow(() -> alreadyOpened);
                    }
                });
    }

    private void write(
            LoyaltyAccount account,
            String tenantId,
            String reference,
            String type,
            long points,
            String motif,
            UUID ruleVersionId
    ) {
        PointMovement movement = new PointMovement();
        movement.setTenantId(tenantId);
        movement.setAccount(account);
        movement.setBusinessReference(reference);
        movement.setMovementType(type);
        movement.setPoints(points);
        movement.setMotif(motif == null || motif.isBlank() ? type : motif.trim());
        movement.setRuleVersionId(ruleVersionId);
        movement.setCreatedAt(Instant.now());
        movements.save(movement);
    }

    private long available(LoyaltyAccount account) {
        if (entities != null && entities.contains(account)) {
            entities.refresh(account);
        }
        return account.getBalance() - account.getReservedBalance();
    }

    private static String reference(String reference, String prefix) {
        if (reference == null || reference.isBlank()) {
            return prefix + ":" + UUID.randomUUID();
        }
        return reference.trim();
    }
}
