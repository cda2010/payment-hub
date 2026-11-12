package com.gateway.payment.identity.api.dto;

import com.gateway.payment.identity.domain.ActionType;
import com.gateway.payment.identity.domain.PartyAction;
import com.gateway.payment.identity.domain.PartyActionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class PartyActionDtos {

    private PartyActionDtos() {
    }

    public record PartyActionView(
            UUID id,
            UUID partyId,
            String tenantId,
            String actionType,
            PartyActionStatus status,
            String title,
            String subtitle,
            String icon,
            String route,
            int priority,
            String aggregateType,
            String aggregateId,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static PartyActionView from(PartyAction a) {
            ActionType type = a.getType();
            String subtitle = a.getSubtitle();
            if (subtitle == null || subtitle.isBlank()) {
                subtitle = type.getSubtitle();
            }
            return new PartyActionView(
                    a.getId(),
                    a.getPartyId(),
                    a.getTenantId(),
                    type.getCode(),
                    a.getStatus(),
                    type.getTitle(),
                    subtitle,
                    type.getIcon(),
                    resolveRoute(type, a),
                    type.getPriority(),
                    a.getAggregateType(),
                    a.getAggregateId(),
                    a.getExpiresAt(),
                    a.getCreatedAt(),
                    a.getUpdatedAt()
            );
        }

        static String resolveRoute(ActionType type, PartyAction action) {
            String template = type.getRoute();
            String aggregateId = action.getAggregateId() == null ? "" : action.getAggregateId();
            boolean needsAggregate = template.contains("{aggregateId}") || template.contains("{paymentId}");
            if (needsAggregate && aggregateId.isEmpty() && type.getRouteFallback() != null) {
                return type.getRouteFallback();
            }
            String partyId = action.getPartyId() == null ? "" : action.getPartyId().toString();
            return template
                    .replace("{aggregateId}", aggregateId)
                    .replace("{paymentId}", aggregateId)
                    .replace("{partyId}", partyId);
        }
    }

    /**
     * Le type vient du catalogue {@code action_type}. Le sous-titre, s'il est fourni,
     * remplace celui du type pour cette occurrence.
     */
    public record OpenActionRequest(
            @Size(max = 64) String tenantId,
            @NotBlank @Size(max = 64) String actionType,
            @Size(max = 512) String subtitle,
            @Size(max = 40) String aggregateType,
            @Size(max = 80) String aggregateId,
            Instant expiresAt,
            @Size(max = 64) String sourceEventId
    ) {
    }

    public record CompleteActionRequest(
            @NotBlank @Size(max = 64) String actionType,
            @Size(max = 40) String aggregateType,
            @Size(max = 80) String aggregateId,
            @Size(max = 20) String status
    ) {
    }
}
