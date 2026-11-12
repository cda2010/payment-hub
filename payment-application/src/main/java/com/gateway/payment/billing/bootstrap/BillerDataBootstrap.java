package com.gateway.payment.billing.bootstrap;

import com.gateway.payment.billing.domain.*;
import com.gateway.payment.billing.repository.BillerProductRepository;
import com.gateway.payment.billing.repository.BillerRepository;
import com.gateway.payment.billing.service.ProductUiSchemaService;
import com.gateway.payment.configuration.config.GatewayBootstrapProperties;
import com.gateway.payment.configuration.domain.FseParticipantKind;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(30)
@ConditionalOnProperty(prefix = "gateway.bootstrap", name = "providers", havingValue = "true")
public class BillerDataBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BillerDataBootstrap.class);

    private final BillerRepository billerRepository;
    private final BillerProductRepository productRepository;
    private final PaymentProviderRepository providerRepository;
    private final ProductUiSchemaService uiSchemaService;
    private final GatewayBootstrapProperties bootstrapProperties;

    public BillerDataBootstrap(
            BillerRepository billerRepository,
            BillerProductRepository productRepository,
            PaymentProviderRepository providerRepository,
            ProductUiSchemaService uiSchemaService,
            GatewayBootstrapProperties bootstrapProperties
    ) {
        this.billerRepository = billerRepository;
        this.productRepository = productRepository;
        this.providerRepository = providerRepository;
        this.uiSchemaService = uiSchemaService;
        this.bootstrapProperties = bootstrapProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!bootstrapProperties.providers()) {
            return;
        }
        PaymentProvider billProvider = resolveBillProvider();
        if (billProvider == null) {
            log.warn("Aucun provider FACTURIER (BILL_PAYMENT) — seed facturiers reporté");
            return;
        }
        seed(billProvider);
        log.info("Facturiers / produits rattachés au provider {}", billProvider.getCode());
    }

    private PaymentProvider resolveBillProvider() {
        return providerRepository.findByCode("BILL_PAYMENT")
                .filter(p -> p.getParticipantKind() == FseParticipantKind.FACTURIER || p.getParticipantKind() == null)
                .or(() -> providerRepository
                        .findByParticipantKindAndActifTrueOrderByOrdreAffichageAsc(FseParticipantKind.FACTURIER)
                        .stream()
                        .findFirst())
                .orElse(null);
    }

    private void seed(PaymentProvider billProvider) {
        // S'assurer que BILL_PAYMENT est bien typé FACTURIER
        if (billProvider.getParticipantKind() != FseParticipantKind.FACTURIER) {
            billProvider.setParticipantKind(FseParticipantKind.FACTURIER);
            billProvider = providerRepository.save(billProvider);
        }

        Biller sonabel = upsertBiller("SONABEL", "SONABEL (électricité)", "Électricité nationale",
                BillerCategory.ENERGY_WATER, 1, billProvider);
        upsertProduct(sonabel, "POSTPAID", "Facture postpayée",
                LookupMode.BOTH, AmountPolicy.FULL_ONLY, JourneyCode.OPEN_BILLS,
                bd("100"), null, false, 1,
                bothLookup("AB-1001", "FAC-998877"), null, null);
        upsertProduct(sonabel, "PREPAID", "Recharge prépayée",
                LookupMode.SUBSCRIBER, AmountPolicy.EDITABLE, JourneyCode.PREPAID,
                bd("500"), bd("200000"), false, 2,
                List.of(field("meterNumber", "N° compteur", "text", true, "CTR-1001",
                        ProductFieldRole.SUBSCRIBER)),
                null,
                List.of(field("amount", "Montant recharge", "number", true, "5000",
                        ProductFieldRole.AMOUNT)));

        Biller onea = upsertBiller("ONEA", "ONEA (eau)", "Eau potable",
                BillerCategory.ENERGY_WATER, 2, billProvider);
        upsertProduct(onea, "WATER", "Facture eau",
                LookupMode.BOTH, AmountPolicy.FULL_ONLY, JourneyCode.OPEN_BILLS,
                bd("100"), null, false, 1,
                bothLookup("AB-2002", "FAC-ONE-8801"), null, null);
        upsertProduct(onea, "TAXE", "Taxe assainissement",
                LookupMode.BOTH, AmountPolicy.PARTIAL, JourneyCode.OPEN_BILLS,
                bd("500"), null, false, 2,
                List.of(field("subscriberReference", "Référence abonné", "text", true, "AB-2002",
                        ProductFieldRole.SUBSCRIBER)),
                null, null);

        Biller canal = upsertBiller("CANAL+", "Canal+", "TV payante",
                BillerCategory.TELECOM_MEDIA, 3, billProvider);
        upsertProduct(canal, "ABO", "Abonnement",
                LookupMode.SUBSCRIBER, AmountPolicy.FULL_ONLY, JourneyCode.OPEN_BILLS,
                bd("1000"), null, false, 1,
                List.of(
                        field("subscriberReference", "N° abonné", "text", true, "AB-3003",
                                ProductFieldRole.SUBSCRIBER),
                        field("period", "Période", "select", true, "2026-08",
                                ProductFieldRole.EXTRA, List.of("2026-07", "2026-08", "2026-09"))
                ),
                null, null);

        Biller impots = upsertBiller("IMPOTS", "Impôts", "Fiscalité",
                BillerCategory.GOVERNMENT, 4, billProvider);
        upsertProduct(impots, "FONCIER", "Taxe foncière",
                LookupMode.BILL_REF, AmountPolicy.PARTIAL, JourneyCode.GET_INFOS,
                bd("1000"), null, false, 1,
                List.of(
                        field("billReference", "N° avis", "text", true, "FAC-IMP-2026-77",
                                ProductFieldRole.BILL_REF),
                        field("nif", "NIF", "text", true, "NIF-12345",
                                ProductFieldRole.EXTRA),
                        field("fiscalYear", "Année fiscale", "number", true, "2026",
                                ProductFieldRole.EXTRA)
                ),
                null, null);
    }

    private Biller upsertBiller(
            String code,
            String name,
            String description,
            BillerCategory category,
            int order,
            PaymentProvider provider
    ) {
        return billerRepository.findByCodeIgnoreCase(code).map(existing -> {
            boolean dirty = false;
            if (existing.getProvider() == null) {
                existing.setProvider(provider);
                dirty = true;
            }
            if (existing.getCategoryCode() == null || existing.getCategoryCode().isBlank()) {
                existing.setCategoryCode(category.name());
                dirty = true;
            }
            return dirty ? billerRepository.save(existing) : existing;
        }).orElseGet(() ->
                billerRepository.save(Biller.builder()
                        .code(code)
                        .name(name)
                        .description(description)
                        .actif(true)
                        .defaultCurrency("XOF")
                        .ordreAffichage(order)
                        .categoryCode(category.name())
                        .provider(provider)
                        .build()));
    }

    private void upsertProduct(
            Biller biller,
            String code,
            String name,
            LookupMode lookupMode,
            AmountPolicy amountPolicy,
            JourneyCode journeyCode,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            boolean allowOverpay,
            int order,
            List<com.gateway.payment.billing.api.dto.ProductFieldDto> lookup,
            List<com.gateway.payment.billing.api.dto.ProductFieldDto> confirm,
            List<com.gateway.payment.billing.api.dto.ProductFieldDto> amount
    ) {
        var existing = productRepository.findByBiller_CodeIgnoreCaseAndCodeIgnoreCase(biller.getCode(), code);
        if (existing.isPresent()) {
            BillerProduct p = existing.get();
            boolean dirty = false;
            if (p.getLookupFieldsJson() == null || p.getLookupFieldsJson().isBlank()) {
                p.setLookupFieldsJson(uiSchemaService.writeFields(lookup));
                p.setConfirmFieldsJson(uiSchemaService.writeFields(confirm));
                p.setAmountFieldsJson(uiSchemaService.writeFields(amount));
                dirty = true;
            }
            if (dirty) {
                productRepository.save(p);
            }
            return;
        }
        productRepository.save(BillerProduct.builder()
                .biller(biller)
                .code(code)
                .name(name)
                .actif(true)
                .lookupMode(lookupMode)
                .amountPolicy(amountPolicy)
                .journeyCode(journeyCode)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .allowOverpay(allowOverpay)
                .ordreAffichage(order)
                .lookupFieldsJson(uiSchemaService.writeFields(lookup))
                .confirmFieldsJson(uiSchemaService.writeFields(confirm))
                .amountFieldsJson(uiSchemaService.writeFields(amount))
                .build());
    }

    private static List<com.gateway.payment.billing.api.dto.ProductFieldDto> bothLookup(
            String subscriberPlaceholder, String billPlaceholder
    ) {
        return List.of(
                field("subscriberReference", "Référence abonné", "text", true, subscriberPlaceholder,
                        ProductFieldRole.SUBSCRIBER),
                field("billReference", "N° de facture", "text", true, billPlaceholder,
                        ProductFieldRole.BILL_REF)
        );
    }

    private static com.gateway.payment.billing.api.dto.ProductFieldDto field(
            String name, String label, String type, boolean required, String placeholder,
            ProductFieldRole role
    ) {
        return field(name, label, type, required, placeholder, role, null);
    }

    private static com.gateway.payment.billing.api.dto.ProductFieldDto field(
            String name, String label, String type, boolean required, String placeholder,
            ProductFieldRole role, List<String> options
    ) {
        return new com.gateway.payment.billing.api.dto.ProductFieldDto(
                name, label, type, required, placeholder, null, role, options, null);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
