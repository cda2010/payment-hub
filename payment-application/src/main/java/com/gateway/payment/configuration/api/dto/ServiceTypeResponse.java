package com.gateway.payment.configuration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCategory;
import com.gateway.payment.common.domain.ServiceTypeCode;

public record ServiceTypeResponse(
        ServiceTypeCode code,
        String libelle,
        ServiceTypeCategory category,
        String description,
        boolean actif,
        Integer ordreAffichage
) {}
