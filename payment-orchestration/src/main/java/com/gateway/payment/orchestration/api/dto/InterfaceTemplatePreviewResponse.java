package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.orchestration.service.InterfaceParamTemplateCatalog;

import java.util.List;
import java.util.UUID;

public record InterfaceTemplatePreviewResponse(
        String typeMethode,
        ServiceTypeCode suggestedServiceType,
        UUID suggestedOperationId,
        String suggestedOperationCode,
        UUID suggestedApiId,
        String suggestedApiCode,
        List<InterfaceParamTemplateCatalog.ParamTemplatePreview> defaultParametres
) {}
