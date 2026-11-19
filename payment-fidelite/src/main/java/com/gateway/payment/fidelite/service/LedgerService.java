package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.LoyaltyAccount;
import com.gateway.payment.fidelite.domain.PointLot;
import com.gateway.payment.fidelite.domain.PointMovement;
import com.gateway.payment.fidelite.domain.Programme;
import com.gateway.payment.fidelite.repository.LoyaltyAccountRepository;
import com.gateway.payment.fidelite.repository.PointLotRepository;
import com.gateway.payment.fidelite.repository.PointMovementRepository;
import com.gateway.payment.fidelite.repository.ProgrammeRepository;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Solde disponible = balance − réservé.
 * Chaque crédit ouvre un lot. La consommation prend les lots qui expirent le plus tôt.
 */
@Service
public class LedgerService {

    private final LoyaltyAccountRepository accounts;
    private final PointMovementRepository movements;
    private final PointLotRepository lots;
    private final ProgrammeRepository programmes;
    private final EntityManager entities;

    public LedgerService(
            LoyaltyAccountRepository accounts,
            PointMovementRepository movements,
            PointLotRepository lots,
            ProgrammeRepository programmes,
            EntityManager entities
    ) {
        this.accounts = accounts;
        this.movements = movements;
        this.lots = lots;
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
    public boolean dejaEcrit(String tenantId, String reference) {
        return reference != null && movements.existsByTenantIdAndBusinessReference(tenantId, reference.trim());
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
        ouvrirLot(account, tenantId, points, businessReference, echeanceProgramme(programmeId, Instant.now()));
        return disponible(tenantId, programmeId, partyId);
    }

    /**
     * Réserve puis consomme dans la même transaction.
     * S'il existe des lots, les plus proches de l'échéance partent d'abord.
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
        return debiter(tenantId, programmeId, partyId, points, motif, reference, "DEBIT");
    }

    @Transactional
    public long debiter(
            String tenantId,
            UUID programmeId,
            UUID partyId,
            long points,
            String motif,
            String reference,
            String movementType
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
        prelever(account, points);
        if (accounts.reserve(account.getId(), tenantId, points) != 1
                || accounts.consume(account.getId(), tenantId, points) != 1) {
            throw new IllegalArgumentException("Points insuffisants pour cette récompense.");
        }
        write(account, tenantId, businessReference, movementType, -points, motif, null);
        return disponible(tenantId, programmeId, partyId);
    }

    /**
     * Déplace des points en gardant l'échéance la plus proche des lots consommés.
     * Sans programme cible, les points sont seulement retirés.
     */
    @Transactional
    public long deplacer(
            String tenantId,
            UUID sourceProgrammeId,
            UUID sourcePartyId,
            UUID cibleProgrammeId,
            UUID ciblePartyId,
            long pointsSource,
            long pointsDest,
            String motifSource,
            String motifDest,
            String reference,
            String movementType
    ) {
        if (pointsSource <= 0) {
            throw new IllegalArgumentException("Le nombre de points est positif");
        }
        String sourceRef = reference(reference, "DEPLACEMENT") + ":SOURCE";
        if (movements.existsByTenantIdAndBusinessReference(tenantId, sourceRef)) {
            return disponible(tenantId, sourceProgrammeId, sourcePartyId);
        }
        LoyaltyAccount source = accounts.findByTenantIdAndProgramme_IdAndPartyId(
                tenantId, sourceProgrammeId, sourcePartyId
        ).orElseThrow(() -> new IllegalArgumentException("Points insuffisants."));
        List<PointLot> open = ouverts(source.getId());
        List<Fefo.Lot> vue = vue(open);
        long somme = vue.stream().mapToLong(Fefo.Lot::remaining).sum();
        long cap = available(source);
        if (pointsSource > cap) {
            throw new IllegalArgumentException("Points insuffisants.");
        }
        List<Fefo.Prise> prises = Fefo.prelever(vue, Math.min(pointsSource, somme));
        long pris = prises.stream().mapToLong(Fefo.Prise::points).sum();
        long legacy = Math.max(0, cap - Math.min(somme, cap));
        if (pointsSource - pris > legacy) {
            throw new IllegalArgumentException("Points insuffisants.");
        }
        Instant echeanceSource = Fefo.echeance(vue, prises);
        appliquer(open, prises);
        if (accounts.reserve(source.getId(), tenantId, pointsSource) != 1
                || accounts.consume(source.getId(), tenantId, pointsSource) != 1) {
            throw new IllegalArgumentException("Points insuffisants.");
        }
        write(source, tenantId, sourceRef, movementType, -pointsSource, motifSource, null);
        if (cibleProgrammeId != null && ciblePartyId != null && pointsDest > 0) {
            String destRef = reference(reference, "DEPLACEMENT") + ":DEST";
            LoyaltyAccount dest = open(tenantId, cibleProgrammeId, ciblePartyId);
            write(dest, tenantId, destRef, "CREDIT", pointsDest, motifDest, null);
            if (accounts.credit(dest.getId(), tenantId, pointsDest) != 1) {
                throw new IllegalStateException("Crédit refusé");
            }
            ouvrirLot(dest, tenantId, pointsDest, destRef, echeanceDest(cibleProgrammeId, echeanceSource));
        }
        return disponible(tenantId, sourceProgrammeId, sourcePartyId);
    }

    /** Retire le reste d'un crédit identifié par sa référence. Les points déjà consommés restent consommés. */
    @Transactional
    public long annuler(String tenantId, String reference) {
        if (reference == null || reference.isBlank()) {
            return 0;
        }
        String businessReference = reference.trim();
        PointMovement original = movements.findByTenantIdAndBusinessReference(tenantId, businessReference).orElse(null);
        if (original == null || original.getPoints() <= 0 || !"CREDIT".equals(original.getMovementType())) {
            return 0;
        }
        String reversal = "ANNULATION:" + businessReference;
        if (movements.existsByTenantIdAndBusinessReference(tenantId, reversal)) {
            return disponibleCompte(original.getAccount());
        }
        PointLot lot = lots.findByTenantIdAndBusinessReference(tenantId, businessReference).orElse(null);
        if (lot == null || lot.getRemainingPoints() <= 0) {
            return disponibleCompte(original.getAccount());
        }
        long points = lot.getRemainingPoints();
        if (accounts.debit(lot.getAccount().getId(), tenantId, points) != 1) {
            throw new IllegalArgumentException("Ces points sont déjà réservés.");
        }
        lot.setRemainingPoints(0);
        lots.save(lot);
        write(lot.getAccount(), tenantId, reversal, "ANNULATION", -points, "Annulation", null);
        return disponibleCompte(lot.getAccount());
    }

    @Transactional
    public long annulerEvenement(String tenantId, UUID eventId) {
        if (eventId == null) {
            return 0;
        }
        long total = 0;
        for (PointMovement movement : movements.findByTenantIdAndBusinessReferenceStartingWith(tenantId, eventId.toString())) {
            if (movement.getPoints() > 0 && "CREDIT".equals(movement.getMovementType())) {
                long avant = disponibleCompte(movement.getAccount());
                long apres = annuler(tenantId, movement.getBusinessReference());
                if (avant > apres) {
                    total += avant - apres;
                }
            }
        }
        return total;
    }

    /** Expire un lot dû. Zéro si le lot est déjà vide ou si le solde est réservé. */
    @Transactional
    public long expirer(UUID lotId) {
        PointLot lot = lots.findById(lotId).orElse(null);
        if (lot == null || lot.getRemainingPoints() <= 0 || lot.getExpiresAt() == null) {
            return 0;
        }
        String reference = "EXPIRATION:" + lot.getId();
        if (movements.existsByTenantIdAndBusinessReference(lot.getTenantId(), reference)) {
            lot.setRemainingPoints(0);
            lots.save(lot);
            return 0;
        }
        long points = lot.getRemainingPoints();
        if (accounts.debit(lot.getAccount().getId(), lot.getTenantId(), points) != 1) {
            return 0;
        }
        lot.setRemainingPoints(0);
        lots.save(lot);
        write(lot.getAccount(), lot.getTenantId(), reference, "EXPIRATION", -points, "Expiration", null);
        return points;
    }

    private void prelever(LoyaltyAccount account, long points) {
        List<PointLot> open = ouverts(account.getId());
        if (open.isEmpty()) {
            return;
        }
        List<Fefo.Lot> vue = vue(open);
        long somme = vue.stream().mapToLong(Fefo.Lot::remaining).sum();
        long cap = available(account);
        if (points > cap) {
            throw new IllegalArgumentException("Points insuffisants pour cette récompense.");
        }
        List<Fefo.Prise> prises = Fefo.prelever(vue, Math.min(points, somme));
        long pris = prises.stream().mapToLong(Fefo.Prise::points).sum();
        long legacy = Math.max(0, cap - Math.min(somme, cap));
        if (points - pris > legacy) {
            throw new IllegalArgumentException("Points insuffisants pour cette récompense.");
        }
        appliquer(open, prises);
    }

    private void appliquer(List<PointLot> open, List<Fefo.Prise> prises) {
        for (Fefo.Prise prise : prises) {
            for (PointLot lot : open) {
                if (lot.getId().equals(prise.id())) {
                    lot.setRemainingPoints(lot.getRemainingPoints() - prise.points());
                    lots.save(lot);
                }
            }
        }
    }

    private void ouvrirLot(
            LoyaltyAccount account,
            String tenantId,
            long points,
            String businessReference,
            Instant expiresAt
    ) {
        Optional<PointLot> existing = lots.findByTenantIdAndBusinessReference(tenantId, businessReference);
        if (existing != null && existing.isPresent()) {
            return;
        }
        PointLot lot = new PointLot();
        lot.setTenantId(tenantId);
        lot.setAccount(account);
        lot.setOriginalPoints(points);
        lot.setRemainingPoints(points);
        lot.setEarnedAt(Instant.now());
        lot.setExpiresAt(expiresAt);
        lot.setBusinessReference(businessReference);
        lots.save(lot);
    }

    private Instant echeanceProgramme(UUID programmeId, Instant depuis) {
        Integer mois = validite(programmeId);
        if (mois == null) {
            return null;
        }
        return depuis.atZone(ZoneOffset.UTC).plusMonths(mois).toInstant();
    }

    private Instant echeanceDest(UUID programmeId, Instant echeanceSource) {
        Instant programme = echeanceProgramme(programmeId, Instant.now());
        if (programme == null) {
            return echeanceSource;
        }
        if (echeanceSource == null || programme.isBefore(echeanceSource)) {
            return programme;
        }
        return echeanceSource;
    }

    private Integer validite(UUID programmeId) {
        if (programmeId == null) {
            return null;
        }
        Optional<Programme> found = programmes.findById(programmeId);
        if (found == null || found.isEmpty()) {
            return null;
        }
        return found.get().getValiditeMois();
    }

    private List<PointLot> ouverts(UUID accountId) {
        List<PointLot> open = lots.ouverts(accountId);
        return open == null ? List.of() : open;
    }

    private static List<Fefo.Lot> vue(List<PointLot> open) {
        return open.stream()
                .map(lot -> new Fefo.Lot(lot.getId(), lot.getRemainingPoints(), lot.getExpiresAt(), lot.getEarnedAt()))
                .toList();
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

    private long disponibleCompte(LoyaltyAccount account) {
        if (account == null || account.getProgramme() == null) {
            return 0;
        }
        return disponible(account.getTenantId(), account.getProgramme().getId(), account.getPartyId());
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
