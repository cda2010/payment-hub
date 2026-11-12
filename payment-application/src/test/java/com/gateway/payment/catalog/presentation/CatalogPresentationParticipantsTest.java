package com.gateway.payment.catalog.presentation;

import com.gateway.payment.datasources.api.CatalogItemDto;
import com.gateway.payment.catalog.presentation.datasource.CatalogLocalItemMapper;
import com.gateway.payment.transfer.api.dto.TransferParticipantResponse;
import com.gateway.payment.transfer.domain.ParticipantKind;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Source LOCAL {@code TRANSFER_PARTICIPANTS} : le rôle demandé par la section
 * décide du sens retenu, et les bornes voyagent dans {@code meta}.
 */
class CatalogPresentationParticipantsTest {

    private static TransferParticipantResponse participant(
            String code,
            String name,
            ParticipantKind kind,
            boolean canEmit,
            boolean canReceive
    ) {
        return new TransferParticipantResponse(
                UUID.randomUUID(),
                code,
                name,
                "BF",
                true,
                canEmit,
                canReceive,
                kind,
                "CORIS",
                new BigDecimal("100"),
                new BigDecimal("2000000"),
                List.of(),
                List.of(),
                1,
                null
        );
    }

    private static final List<TransferParticipantResponse> ALL = List.of(
            participant("CORIS_MONEY", "Coris Money", ParticipantKind.WALLET, true, true),
            participant("CORIS_BANK", "Coris Bank International", ParticipantKind.BANK, false, true),
            participant("LEGACY", "Ancien rail", ParticipantKind.WALLET, true, false)
    );

    @Test
    void keepsOnlyReceiversForDestinationRole() {
        List<CatalogItemDto> items = CatalogLocalItemMapper.mapParticipants(
                ALL, Map.of("role", "destination")
        );

        assertEquals(List.of("CORIS_MONEY", "CORIS_BANK"), items.stream().map(CatalogItemDto::id).toList());
    }

    @Test
    void keepsOnlyEmittersForSourceRole() {
        List<CatalogItemDto> items = CatalogLocalItemMapper.mapParticipants(
                ALL, Map.of("role", "SOURCE")
        );

        assertEquals(List.of("CORIS_MONEY", "LEGACY"), items.stream().map(CatalogItemDto::id).toList());
    }

    @Test
    void keepsEveryoneWithoutRole() {
        assertEquals(3, CatalogLocalItemMapper.mapParticipants(ALL, Map.of()).size());
    }

    @Test
    void carriesBoundsAndKindInMeta() {
        CatalogItemDto bank = CatalogLocalItemMapper.mapParticipants(ALL, Map.of())
                .stream()
                .filter(i -> "CORIS_BANK".equals(i.id()))
                .findFirst()
                .orElseThrow();

        assertEquals("Coris Bank International", bank.label());
        assertEquals("Compte bancaire", bank.sub());
        assertEquals("business-outline", bank.icon());
        assertNull(bank.value());
        assertEquals("CORIS_BANK", bank.meta().get("code"));
        assertEquals("BANK", bank.meta().get("participantKind"));
        assertEquals(Boolean.FALSE, bank.meta().get("canEmit"));
        assertEquals(new BigDecimal("100"), bank.meta().get("minAmount"));
        assertEquals(new BigDecimal("2000000"), bank.meta().get("maxAmount"));
    }

    @Test
    void wardsOffMissingKind() {
        List<CatalogItemDto> items = CatalogLocalItemMapper.mapParticipants(
                List.of(participant("UNKNOWN", "Sans nature", null, true, true)), Map.of()
        );

        assertEquals("wallet-outline", items.get(0).icon());
        assertFalse(items.get(0).meta().containsKey("participantKind"));
    }
}
