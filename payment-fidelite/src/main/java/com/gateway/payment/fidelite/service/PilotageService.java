package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.CoalitionMembre;
import com.gateway.payment.fidelite.domain.Contribution;
import com.gateway.payment.fidelite.repository.CoalitionMembreRepository;
import com.gateway.payment.fidelite.repository.ContributionRepository;
import com.gateway.payment.fidelite.repository.PointLotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PilotageService {

    private final PointLotRepository lots;
    private final ContributionRepository contributions;
    private final CoalitionMembreRepository membres;

    public PilotageService(
            PointLotRepository lots,
            ContributionRepository contributions,
            CoalitionMembreRepository membres
    ) {
        this.lots = lots;
        this.contributions = contributions;
        this.membres = membres;
    }

    public record Echeance(Instant expiresAt, long points) {
    }

    public record Compensation(UUID marchandId, long emissions, long usages, long solde) {
    }

    @Transactional(readOnly = true)
    public List<Echeance> passif(UUID programmeId) {
        List<Echeance> rows = new ArrayList<>();
        for (Object[] row : lots.passif(programmeId)) {
            rows.add(new Echeance((Instant) row[0], ((Number) row[1]).longValue()));
        }
        return rows;
    }

    @Transactional(readOnly = true)
    public List<Compensation> compensations(UUID programmeId) {
        Map<UUID, long[]> cumul = new LinkedHashMap<>();
        for (CoalitionMembre membre : membres.findByProgramme_Id(programmeId)) {
            cumul.put(membre.getMarchandId(), new long[2]);
        }
        for (Contribution contribution : contributions.findByProgrammeId(programmeId)) {
            if (contribution.getMarchandId() == null) {
                continue;
            }
            long[] ligne = cumul.computeIfAbsent(contribution.getMarchandId(), id -> new long[2]);
            if ("USAGE".equals(contribution.getSens())) {
                ligne[1] += contribution.getPoints();
            } else {
                ligne[0] += contribution.getPoints();
            }
        }
        List<Compensation> rows = new ArrayList<>();
        cumul.forEach((marchand, ligne) -> rows.add(
                new Compensation(marchand, ligne[0], ligne[1], ligne[0] - ligne[1])));
        return rows;
    }
}
