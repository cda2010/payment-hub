package com.gateway.payment.transfer.api.dto;

import java.util.UUID;

public record TransferParticipantRef(
        UUID id,
        String code,
        String name
) {}
