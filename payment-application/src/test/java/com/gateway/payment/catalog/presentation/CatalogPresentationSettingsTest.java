package com.gateway.payment.catalog.presentation;

import com.gateway.payment.common.domain.AccessChannel;
import com.gateway.payment.datasources.CatalogDataOrigin;
import com.gateway.payment.datasources.CatalogDataSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CatalogPresentationSettingsTest {

    @Test
    void parsesSectionsAndKinds() {
        CatalogPresentationSettings settings = CatalogPresentationSettings.from(Map.of(
                "presentation", Map.of(
                        "version", 2,
                        "sections", List.of(
                                Map.of(
                                        "id", "categories",
                                        "displayKind", "FILTER_CHIPS",
                                        "dataSource", "BILLER_CATEGORIES",
                                        "bindsTo", "billers"
                                ),
                                Map.of(
                                        "id", "billers",
                                        "displayKind", "LEAF_LIST",
                                        "data_source", "BILLERS",
                                        "searchable", true,
                                        "pageable", true
                                )
                        )
                )
        ));

        assertEquals(2, settings.version());
        assertEquals(2, settings.sections().size());
        assertEquals(DisplayKind.FILTER_CHIPS, settings.sections().get(0).displayKind());
        assertEquals(CatalogDataSource.BILLER_CATEGORIES, settings.sections().get(0).dataSource());
        assertEquals(CatalogDataOrigin.LOCAL, settings.sections().get(0).origin());
        assertEquals("billers", settings.sections().get(0).bindsTo());
        assertEquals(DisplayKind.LEAF_LIST, settings.sections().get(1).displayKind());
        assertTrue(settings.sections().get(1).searchable());
    }

    @Test
    void parsesOmnicanalOriginAndCustomDataSource() {
        CatalogPresentationSettings settings = CatalogPresentationSettings.from(Map.of(
                "presentation", Map.of(
                        "sections", List.of(Map.of(
                                "id", "emitters",
                                "displayKind", "LEAF_LIST",
                                "dataSource", "PARTICIPANTS",
                                "origin", "OMNICANAL",
                                "params", Map.of(
                                        "codeFse", "FSE_0001",
                                        "dataKey", "PARTICIPANTS"
                                )
                        ))
                )
        ));

        assertEquals(1, settings.sections().size());
        var sec = settings.sections().get(0);
        assertEquals(CatalogDataSource.CUSTOM, sec.dataSource());
        assertEquals("PARTICIPANTS", sec.dataSourceCode());
        assertEquals(CatalogDataOrigin.OMNICANAL, sec.origin());
        assertEquals("FSE_0001", sec.params().get("codeFse"));
    }

    @Test
    void defaultOriginFromPresentationApplies() {
        CatalogPresentationSettings settings = CatalogPresentationSettings.from(Map.of(
                "presentation", Map.of(
                        "defaultOrigin", "OMNICANAL",
                        "sections", List.of(Map.of(
                                "id", "x",
                                "dataSource", "STATIC_ITEMS"
                        ))
                )
        ));
        assertEquals(CatalogDataOrigin.OMNICANAL, settings.sections().get(0).origin());
    }

    @Test
    void channelOverlayReplacesSections() {
        CatalogPresentationSettings settings = CatalogPresentationSettings.from(
                Map.of(
                        "presentation", Map.of(
                                "sections", List.of(Map.of(
                                        "id", "a",
                                        "displayKind", "LEAF_LIST",
                                        "dataSource", "BILLERS"
                                ))
                        ),
                        "channelDefaults", Map.of(
                                "MOBILE", Map.of(
                                        "presentation", Map.of(
                                                "sections", List.of(Map.of(
                                                        "id", "favorites",
                                                        "displayKind", "SHORTCUT_SLIDE",
                                                        "dataSource", "STATIC_ITEMS",
                                                        "items", List.of(Map.of(
                                                                "id", "X",
                                                                "label", "Demo"
                                                        ))
                                                ))
                                        )
                                ),
                                "WEB", Map.of(
                                        "presentation", Map.of(
                                                "sections", List.of(Map.of(
                                                        "id", "billers",
                                                        "displayKind", "ENTRY_GRID",
                                                        "dataSource", "BILLERS"
                                                ))
                                        )
                                )
                        )
                ),
                AccessChannel.MOBILE
        );

        assertEquals(1, settings.sections().size());
        assertEquals("favorites", settings.sections().get(0).id());
        assertEquals(1, settings.sections().get(0).staticItems().size());

        CatalogPresentationSettings web = CatalogPresentationSettings.from(
                Map.of(
                        "presentation", Map.of(
                                "sections", List.of(Map.of(
                                        "id", "a",
                                        "displayKind", "LEAF_LIST",
                                        "dataSource", "BILLERS"
                                ))
                        ),
                        "channelDefaults", Map.of(
                                "WEB", Map.of(
                                        "presentation", Map.of(
                                                "sections", List.of(Map.of(
                                                        "id", "billers",
                                                        "displayKind", "ENTRY_GRID",
                                                        "dataSource", "BILLERS"
                                                ))
                                        )
                                )
                        )
                ),
                AccessChannel.WEB
        );
        assertEquals(DisplayKind.ENTRY_GRID, web.sections().get(0).displayKind());
    }

    @Test
    void defaultBillPaymentHasThreeSections() {
        CatalogPresentationSettings d = CatalogPresentationSettings.defaultBillPayment();
        assertEquals(3, d.sections().size());
        assertEquals(DisplayKind.SHORTCUT_SLIDE, d.sections().get(0).displayKind());
        assertEquals(DisplayKind.FILTER_CHIPS, d.sections().get(1).displayKind());
        assertEquals(DisplayKind.LEAF_LIST, d.sections().get(2).displayKind());
    }

    @Test
    void unknownKindFallsBackToLeaf() {
        assertEquals(DisplayKind.LEAF_LIST, DisplayKind.parseOrLeaf("NOPE"));
        assertEquals(DisplayKind.FILTER_CHIPS, DisplayKind.parseOrLeaf("filter-chips"));
    }
}
