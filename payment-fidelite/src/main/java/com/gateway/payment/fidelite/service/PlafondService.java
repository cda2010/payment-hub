package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.PlafondPoints;
import com.gateway.payment.fidelite.repository.PlafondPointsRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Réduit une attribution pour rester sous un plafond de période. */
@Service
public class PlafondService {

    private final PlafondPointsRepository plafonds;

    public PlafondService(PlafondPointsRepository plafonds) {
        this.plafonds = plafonds;
    }

    @Transactional(readOnly = true)
    public long deja(String tenantId, UUID programmeId, UUID partyId, String portee, String periode) {
        return plafonds.findByTenantIdAndProgrammeIdAndPartyIdAndPorteeAndPeriode(
                tenantId, programmeId, partyId, portee, periode
        ).map(PlafondPoints::getPoints).orElse(0L);
    }

    @Transactional
    public void ajouter(String tenantId, UUID programmeId, UUID partyId, String portee, String periode, long points) {
        if (points <= 0) {
            return;
        }
        PlafondPoints row = ouvrir(tenantId, programmeId, partyId, portee, periode);
        row.setPoints(row.getPoints() + points);
        plafonds.save(row);
    }

    private PlafondPoints ouvrir(String tenantId, UUID programmeId, UUID partyId, String portee, String periode) {
        return plafonds.findByTenantIdAndProgrammeIdAndPartyIdAndPorteeAndPeriode(
                tenantId, programmeId, partyId, portee, periode
        ).orElseGet(() -> {
            PlafondPoints created = new PlafondPoints();
            created.setTenantId(tenantId);
            created.setProgrammeId(programmeId);
            created.setPartyId(partyId);
            created.setPortee(portee);
            created.setPeriode(periode);
            created.setPoints(0);
            try {
                return plafonds.saveAndFlush(created);
            } catch (DataIntegrityViolationException already) {
                return plafonds.findByTenantIdAndProgrammeIdAndPartyIdAndPorteeAndPeriode(
                        tenantId, programmeId, partyId, portee, periode).orElseThrow(() -> already);
            }
        });
    }
}
