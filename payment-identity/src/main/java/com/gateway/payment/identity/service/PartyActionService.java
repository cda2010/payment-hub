package com.gateway.payment.identity.service;

import com.gateway.payment.identity.api.dto.PartyActionDtos.CompleteActionRequest;
import com.gateway.payment.identity.api.dto.PartyActionDtos.OpenActionRequest;
import com.gateway.payment.identity.api.dto.PartyActionDtos.PartyActionView;
import com.gateway.payment.identity.domain.ActionType;
import com.gateway.payment.identity.domain.PartyAction;
import com.gateway.payment.identity.domain.PartyActionStatus;
import com.gateway.payment.identity.repository.ActionTypeRepository;
import com.gateway.payment.identity.repository.PartyActionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PartyActionService {

    private final PartyActionRepository repository;
    private final ActionTypeRepository types;

    public PartyActionService(PartyActionRepository repository, ActionTypeRepository types) {
        this.repository = repository;
        this.types = types;
    }

    @Transactional
    public List<PartyActionView> listOpen(UUID partyId) {
        if (partyId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "partyId requis");
        }
        expireDue(partyId);
        return repository.findOpenByParty(partyId, PartyActionStatus.OPEN)
                .stream()
                .map(PartyActionView::from)
                .toList();
    }

    /** Ouvre ou rafraîchit une action OPEN (idempotent sur party + type + agrégat). */
    @Transactional
    public PartyActionView open(UUID partyId, OpenActionRequest req) {
        if (partyId == null || req == null || !StringUtils.hasText(req.actionType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "partyId et actionType requis");
        }
        String code = req.actionType().trim().toUpperCase(Locale.ROOT);
        ActionType type = types.findById(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type d'action inconnu"));
        String aggType = blankToNull(req.aggregateType());
        String aggId = blankToNull(req.aggregateId());

        PartyAction action = repository.findOpen(partyId, code, aggType, aggId)
                .orElseGet(PartyAction::new);

        action.setPartyId(partyId);
        action.setTenantId(blankToNull(req.tenantId()));
        action.setType(type);
        action.setStatus(PartyActionStatus.OPEN);
        action.setSubtitle(blankToNull(req.subtitle()));
        action.setAggregateType(aggType);
        action.setAggregateId(aggId);
        if (StringUtils.hasText(req.sourceEventId())) {
            action.setSourceEventId(req.sourceEventId().trim());
        }
        action.setExpiresAt(req.expiresAt());
        action.setCompletedAt(null);
        repository.save(action);
        return PartyActionView.from(action);
    }

    @Transactional
    public PartyActionView complete(UUID partyId, CompleteActionRequest req) {
        if (partyId == null || req == null || !StringUtils.hasText(req.actionType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "partyId et actionType requis");
        }
        PartyActionStatus status = PartyActionStatus.DONE;
        if (StringUtils.hasText(req.status())) {
            try {
                status = PartyActionStatus.valueOf(req.status().trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut inconnu");
            }
        }
        if (status == PartyActionStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut terminal requis");
        }
        String code = req.actionType().trim().toUpperCase(Locale.ROOT);
        PartyAction action = repository.findOpen(partyId, code, blankToNull(req.aggregateType()), blankToNull(req.aggregateId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Action introuvable"));
        action.setStatus(status);
        action.setCompletedAt(Instant.now());
        repository.save(action);
        return PartyActionView.from(action);
    }

    @Transactional
    public PartyActionView dismiss(UUID partyId, UUID actionId) {
        PartyAction action = repository.findByIdAndPartyId(actionId, partyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Action introuvable"));
        if (action.getStatus() != PartyActionStatus.OPEN) {
            return PartyActionView.from(action);
        }
        action.setStatus(PartyActionStatus.DISMISSED);
        action.setCompletedAt(Instant.now());
        repository.save(action);
        return PartyActionView.from(action);
    }

    private int expireDue(UUID partyIdFilter) {
        List<PartyAction> expired = repository.findExpiredOpen(Instant.now());
        int n = 0;
        for (PartyAction a : expired) {
            if (partyIdFilter != null && !partyIdFilter.equals(a.getPartyId())) {
                continue;
            }
            a.setStatus(PartyActionStatus.EXPIRED);
            a.setCompletedAt(Instant.now());
            repository.save(a);
            n++;
        }
        return n;
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
