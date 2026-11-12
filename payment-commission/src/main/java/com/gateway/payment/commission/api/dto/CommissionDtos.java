package com.gateway.payment.commission.api.dto;

import com.gateway.payment.commission.domain.*;
import com.gateway.payment.commission.moteur.PartCommission;
import com.gateway.payment.commission.moteur.ResultatSimulation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CommissionDtos {
    private CommissionDtos() {}

    public record FormuleParamDto(String code, String libelle, String dataType, boolean required, String defaultValue) {
        public static FormuleParamDto from(FormuleCommissionParam p) {
            return new FormuleParamDto(p.getCode(), p.getLibelle(), p.getDataType().name(), p.isRequired(), p.getDefaultValue());
        }
    }

    public record FormuleDto(
            UUID id, String code, int version, String libelle, String description, String type, String status,
            List<FormuleParamDto> parametres
    ) {
        public static FormuleDto from(FormuleCommission f) {
            return new FormuleDto(f.getId(), f.getCode(), f.getVersion(), f.getLibelle(), f.getDescription(),
                    f.getType().name(), f.getStatus().name(),
                    f.getParametres().stream().map(FormuleParamDto::from).toList());
        }
    }

    public record RegleDto(
            UUID id, String code, String libelle, String beneficiaryRole,
            String formuleCode, int formuleVersion, String commissionBase, String tierSelectionSource,
            String fundingType, String funderRole, String paramsJson,
            BigDecimal floorAmount, BigDecimal capAmount, int priority, boolean active
    ) {
        public static RegleDto from(RegleCommission r) {
            return new RegleDto(
                    r.getId(), r.getCode(), r.getLibelle(), r.getBeneficiaryRole(),
                    r.getFormule().getCode(), r.getFormule().getVersion(),
                    r.getCommissionBase().name(),
                    r.getTierSelectionSource() != null ? r.getTierSelectionSource().name() : null,
                    r.getFundingType().name(), r.getFunderRole(), r.getParamsJson(),
                    r.getFloorAmount(), r.getCapAmount(), r.getPriority(), r.isActive());
        }
    }

    public record GrilleDto(
            UUID id, String code, int version, String libelle, String description,
            String operationType, String productCode, String channelCode, String currency,
            String status, LocalDate validFrom, LocalDate validTo, String envelopeMode,
            List<RegleDto> regles
    ) {
        public static GrilleDto from(GrilleCommission g) {
            return new GrilleDto(
                    g.getId(), g.getCode(), g.getVersion(), g.getLibelle(), g.getDescription(),
                    g.getOperationType(), g.getProductCode(), g.getChannelCode(), g.getCurrency(),
                    g.getStatus().name(), g.getValidFrom(), g.getValidTo(), g.getEnvelopeMode().name(),
                    g.getRegles().stream().map(RegleDto::from).toList());
        }

        public static GrilleDto resume(GrilleCommission g) {
            return new GrilleDto(
                    g.getId(), g.getCode(), g.getVersion(), g.getLibelle(), g.getDescription(),
                    g.getOperationType(), g.getProductCode(), g.getChannelCode(), g.getCurrency(),
                    g.getStatus().name(), g.getValidFrom(), g.getValidTo(), g.getEnvelopeMode().name(),
                    List.of());
        }
    }

    public record CreateGrilleRequest(
            String code,
            String libelle,
            String description,
            String operationType,
            String productCode,
            String channelCode,
            String currency,
            LocalDate validFrom,
            LocalDate validTo,
            String envelopeMode
    ) {}

    public record UpdateGrilleRequest(
            String libelle,
            String description,
            String operationType,
            String productCode,
            String channelCode,
            String currency,
            LocalDate validFrom,
            LocalDate validTo,
            String envelopeMode
    ) {}

    public record UpsertRegleRequest(
            String code,
            String libelle,
            String beneficiaryRole,
            String formuleCode,
            Integer formuleVersion,
            String commissionBase,
            String tierSelectionSource,
            String fundingType,
            String funderRole,
            String paramsJson,
            BigDecimal floorAmount,
            BigDecimal capAmount,
            Integer priority,
            Boolean active
    ) {}

    public record SimulationRequest(
            String transactionId,
            String operation,
            String productCode,
            String channel,
            String currency,
            BigDecimal amount,
            BigDecimal fees,
            Map<String, BigDecimal> aggregates
    ) {}

    public record SimulationResponse(
            UUID grilleId, String grilleCode, int grilleVersion,
            BigDecimal fees, BigDecimal totalCommissions, BigDecimal reliquat, String envelopeMode,
            List<PartCommission> parts, List<String> warnings
    ) {
        public static SimulationResponse from(ResultatSimulation r) {
            return new SimulationResponse(
                    r.grilleId(), r.grilleCode(), r.grilleVersion(),
                    r.fees(), r.totalCommissions(), r.reliquat(), r.envelopeMode(),
                    r.parts(), r.warnings());
        }
    }
}
