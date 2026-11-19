package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.AvantageMonetaire;
import com.gateway.payment.fidelite.domain.LoyaltyAccount;
import com.gateway.payment.fidelite.domain.PointMovement;
import com.gateway.payment.fidelite.domain.Programme;
import com.gateway.payment.fidelite.domain.TauxConversion;
import com.gateway.payment.fidelite.repository.AdhesionRepository;
import com.gateway.payment.fidelite.repository.AvantageMonetaireRepository;
import com.gateway.payment.fidelite.repository.LoyaltyAccountRepository;
import com.gateway.payment.fidelite.repository.PointMovementRepository;
import com.gateway.payment.fidelite.repository.ProgrammeRepository;
import com.gateway.payment.fidelite.repository.TauxConversionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;
import java.util.UUID;

/** Transfert entre clients, conversion entre programmes, conversion en montant. */
@Service
public class TransfertService {

    public static final long PLAFOND_MOUVEMENT = 10_000;
    public static final int PLAFOND_NOMBRE_JOUR = 3;
    public static final long PLAFOND_JOUR = 20_000;

    private final LedgerService ledger;
    private final AdhesionRepository adhesions;
    private final ProgrammeRepository programmes;
    private final TauxConversionRepository taux;
    private final AvantageMonetaireRepository avantages;
    private final LoyaltyAccountRepository accounts;
    private final PointMovementRepository movements;
    private final PlafondService plafonds;

    public TransfertService(
            LedgerService ledger,
            AdhesionRepository adhesions,
            ProgrammeRepository programmes,
            TauxConversionRepository taux,
            AvantageMonetaireRepository avantages,
            LoyaltyAccountRepository accounts,
            PointMovementRepository movements,
            PlafondService plafonds
    ) {
        this.ledger = ledger;
        this.adhesions = adhesions;
        this.programmes = programmes;
        this.taux = taux;
        this.avantages = avantages;
        this.accounts = accounts;
        this.movements = movements;
        this.plafonds = plafonds;
    }

    public record Resultat(long pointsSource, long pointsDest, BigDecimal valeurXof) {
    }

    @Transactional
    public Resultat transferer(String tenantId, UUID programmeId, UUID sourceId, UUID destinataireId, long points) {
        if (sourceId == null || destinataireId == null || sourceId.equals(destinataireId)) {
            throw new IllegalArgumentException("Le destinataire doit être un autre client");
        }
        exigerMembre(programmeId, sourceId);
        exigerMembre(programmeId, destinataireId);
        controler(tenantId, programmeId, sourceId, points);
        String reference = "TRANSFERT:" + UUID.randomUUID();
        ledger.deplacer(
                tenantId, programmeId, sourceId, programmeId, destinataireId,
                points, points, "Transfert", "Transfert reçu", reference, "TRANSFERT");
        return new Resultat(points, points, null);
    }

    @Transactional
    public Resultat convertir(String tenantId, UUID sourceProgrammeId, UUID cibleProgrammeId, UUID partyId, long points) {
        if (sourceProgrammeId == null || cibleProgrammeId == null || sourceProgrammeId.equals(cibleProgrammeId)) {
            throw new IllegalArgumentException("Les deux programmes doivent être différents");
        }
        exigerMembre(sourceProgrammeId, partyId);
        exigerMembre(cibleProgrammeId, partyId);
        TauxConversion tauxTrouve = taux.findFirstByTenantIdAndSource_IdAndCible_IdAndActifTrue(
                tenantId, sourceProgrammeId, cibleProgrammeId
        ).orElseThrow(() -> new NoSuchElementException("Taux de conversion introuvable"));
        long dest = convertirPoints(points, tauxTrouve);
        verifierPlafondMois(tenantId, sourceProgrammeId, partyId, points, tauxTrouve.getPlafondMois(), "CONVERSION");
        String reference = "CONVERSION:" + UUID.randomUUID();
        ledger.deplacer(
                tenantId, sourceProgrammeId, partyId, cibleProgrammeId, partyId,
                points, dest, "Conversion", "Conversion reçue", reference, "CONVERSION");
        plafonds.ajouter(tenantId, sourceProgrammeId, partyId, "CONVERSION", mois(), points);
        return new Resultat(points, dest, null);
    }

    @Transactional
    public Resultat monetiser(String tenantId, UUID programmeId, UUID partyId, long points) {
        exigerMembre(programmeId, partyId);
        TauxConversion tauxTrouve = taux.findFirstByTenantIdAndSource_IdAndCibleIsNullAndActifTrue(tenantId, programmeId)
                .orElseThrow(() -> new NoSuchElementException("Taux monétaire introuvable"));
        long unites = convertirPoints(points, tauxTrouve);
        verifierPlafondMois(tenantId, programmeId, partyId, points, tauxTrouve.getPlafondMois(), "MONETAIRE");
        String reference = "MONETAIRE:" + UUID.randomUUID();
        ledger.deplacer(
                tenantId, programmeId, partyId, null, null,
                points, 0, "Conversion monétaire", null, reference, "MONETISATION");
        BigDecimal valeur = BigDecimal.valueOf(unites).setScale(2, RoundingMode.UNNECESSARY);
        AvantageMonetaire avantage = new AvantageMonetaire();
        avantage.setTenantId(tenantId);
        avantage.setProgrammeId(programmeId);
        avantage.setPartyId(partyId);
        avantage.setPoints(points);
        avantage.setValeurXof(valeur);
        avantage.setBusinessReference(reference);
        avantage.setCreatedAt(Instant.now());
        avantages.save(avantage);
        plafonds.ajouter(tenantId, programmeId, partyId, "MONETAIRE", mois(), points);
        return new Resultat(points, 0, valeur);
    }

    @Transactional
    public TauxConversion publierTaux(
            String tenantId, UUID sourceId, UUID cibleId, int pointsSource, int pointsCible, long plafondMois
    ) {
        if (pointsSource <= 0 || pointsCible <= 0 || plafondMois <= 0) {
            throw new IllegalArgumentException("Le taux et le plafond sont positifs");
        }
        Programme source = programmes.findById(sourceId)
                .orElseThrow(() -> new NoSuchElementException("Programme source introuvable"));
        if (!tenantId.equals(source.getTenantId())) {
            throw new IllegalArgumentException("Le programme n'appartient pas à ce tenant");
        }
        TauxConversion row = new TauxConversion();
        row.setTenantId(tenantId);
        row.setSource(source);
        if (cibleId != null) {
            Programme cible = programmes.findById(cibleId)
                    .orElseThrow(() -> new NoSuchElementException("Programme cible introuvable"));
            row.setCible(cible);
        }
        row.setPointsSource(pointsSource);
        row.setPointsCible(pointsCible);
        row.setPlafondMois(plafondMois);
        row.setActif(true);
        return taux.save(row);
    }

    private void controler(String tenantId, UUID programmeId, UUID partyId, long points) {
        if (points <= 0 || points > PLAFOND_MOUVEMENT) {
            throw new IllegalArgumentException("Le transfert dépasse le plafond de " + PLAFOND_MOUVEMENT + " points");
        }
        LoyaltyAccount account = accounts.findByTenantIdAndProgramme_IdAndPartyId(tenantId, programmeId, partyId)
                .orElse(null);
        if (account == null) {
            throw new IllegalArgumentException("Points insuffisants.");
        }
        Instant debut = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        var duJour = movements.findByAccount_IdAndMovementTypeAndCreatedAtGreaterThanEqual(
                account.getId(), "TRANSFERT", debut);
        if (duJour.size() >= PLAFOND_NOMBRE_JOUR) {
            throw new IllegalArgumentException("Trop de transferts aujourd'hui");
        }
        long deja = 0;
        for (PointMovement movement : duJour) {
            deja += Math.abs(movement.getPoints());
        }
        if (deja + points > PLAFOND_JOUR) {
            throw new IllegalArgumentException("Plafond journalier de transfert atteint");
        }
    }

    private void verifierPlafondMois(String tenantId, UUID programmeId, UUID partyId, long points, long plafond, String portee) {
        long deja = plafonds.deja(tenantId, programmeId, partyId, portee, mois());
        if (deja + points > plafond) {
            throw new IllegalArgumentException("Plafond mensuel de conversion atteint");
        }
    }

    private static long convertirPoints(long points, TauxConversion tauxTrouve) {
        if (points % tauxTrouve.getPointsSource() != 0) {
            throw new IllegalArgumentException(
                    "Le nombre de points doit être un multiple de " + tauxTrouve.getPointsSource());
        }
        return points / tauxTrouve.getPointsSource() * tauxTrouve.getPointsCible();
    }

    private void exigerMembre(UUID programmeId, UUID partyId) {
        if (!adhesions.existsByProgramme_IdAndPartyId(programmeId, partyId)) {
            throw new IllegalArgumentException("Le client doit d'abord adhérer au programme");
        }
    }

    private static String mois() {
        return java.time.YearMonth.now(ZoneOffset.UTC).toString();
    }
}
