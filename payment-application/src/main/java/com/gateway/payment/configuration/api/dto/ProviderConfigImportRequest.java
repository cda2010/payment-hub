package com.gateway.payment.configuration.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ProviderConfigImportRequest(
        @NotNull @Valid ProviderConfigExportResponse config,
        boolean overwrite
) {}
