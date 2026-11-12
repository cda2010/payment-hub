package com.gateway.payment.billing.service;

import com.gateway.payment.billing.api.dto.BillerCategoryResponse;
import com.gateway.payment.billing.api.dto.BillerPageResponse;
import com.gateway.payment.billing.api.dto.BillerProductRequest;
import com.gateway.payment.billing.api.dto.BillerProductResponse;
import com.gateway.payment.billing.api.dto.BillerRequest;
import com.gateway.payment.billing.api.dto.BillerResponse;
import com.gateway.payment.billing.domain.Biller;
import com.gateway.payment.billing.domain.BillerCategory;
import com.gateway.payment.billing.domain.BillerProduct;
import com.gateway.payment.billing.repository.BillerProductRepository;
import com.gateway.payment.billing.repository.BillerRepository;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.domain.FseParticipantKind;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.api.dto.ProviderResponse;
import com.gateway.payment.omnicanal.external.OmnicanalProviderCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class BillerService {

    private final BillerRepository billerRepository;
    private final BillerProductRepository productRepository;
    private final PaymentProviderRepository providerRepository;
    private final ProductUiSchemaService uiSchemaService;
    private final OmnicanalProviderCatalog omnicanalCatalog;

    public BillerService(
            BillerRepository billerRepository,
            BillerProductRepository productRepository,
            PaymentProviderRepository providerRepository,
            ProductUiSchemaService uiSchemaService,
            OmnicanalProviderCatalog omnicanalCatalog
    ) {
        this.billerRepository = billerRepository;
        this.productRepository = productRepository;
        this.providerRepository = providerRepository;
        this.uiSchemaService = uiSchemaService;
        this.omnicanalCatalog = omnicanalCatalog;
    }

    @Transactional(readOnly = true)
    public List<BillerResponse> listAll() {
        return billerRepository.findAllByOrderByOrdreAffichageAscNameAsc().stream()
                .map(this::toBillerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BillerResponse> listActive() {
        return billerRepository.findByActifTrueOrderByOrdreAffichageAscNameAsc().stream()
                .map(this::toBillerResponse)
                .toList();
    }

    /**
     * Catégories ayant au moins un facturier actif, ordonnées pour la navigation mobile.
     */
    @Transactional(readOnly = true)
    public List<BillerCategoryResponse> listActiveCategories() {
        Map<String, Long> counts = new HashMap<>();
        for (Object[] row : billerRepository.countActiveByCategory()) {
            String code = row[0] != null ? String.valueOf(row[0]) : BillerCategory.OTHER.name();
            long count = row[1] instanceof Number n ? n.longValue() : 0L;
            counts.merge(BillerCategory.orOther(code).name(), count, Long::sum);
        }
        return Arrays.stream(BillerCategory.values())
                .filter(c -> counts.getOrDefault(c.name(), 0L) > 0)
                .sorted(Comparator.comparingInt(BillerCategory::getSortOrder))
                .map(c -> new BillerCategoryResponse(
                        c.name(), c.getLabel(), c.getSortOrder(), counts.getOrDefault(c.name(), 0L)))
                .toList();
    }

    /**
     * Recherche paginée des facturiers actifs (catégorie et/ou texte).
     */
    @Transactional(readOnly = true)
    public BillerPageResponse searchActive(String categoryCode, String query, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String category = normalizeOptionalCategory(categoryCode);
        String q = normalizeOptionalQuery(query);
        Page<Biller> result = billerRepository.searchActive(
                category, q, PageRequest.of(safePage, safeSize));
        List<BillerResponse> items = result.getContent().stream().map(this::toBillerResponse).toList();
        boolean hasMore = result.hasNext();
        return new BillerPageResponse(items, safePage, safeSize, result.getTotalElements(), hasMore);
    }

    /** Chaîne vide = pas de filtre (évite bind JDBC null → bytea sous PostgreSQL). */
    private String normalizeOptionalCategory(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return "";
        }
        return BillerCategory.orOther(categoryCode).name();
    }

    private String normalizeOptionalQuery(String query) {
        if (query == null) {
            return "";
        }
        String trimmed = query.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    @Transactional(readOnly = true)
    public BillerResponse get(UUID id) {
        return toBillerResponse(requireBiller(id));
    }

    @Transactional
    public BillerResponse create(BillerRequest request) {
        String code = normalizeCode(request.code());
        if (billerRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("BILLER_EXISTS", "Facturier déjà existant: " + code);
        }
        ChosenProvider chosen = requireProvider(request.providerId());
        Biller biller = Biller.builder()
                .code(code)
                .name(request.name().trim())
                .description(request.description())
                .logoUrl(request.logoUrl())
                .actif(Boolean.TRUE.equals(request.actif()))
                .defaultCurrency(request.defaultCurrency() != null && !request.defaultCurrency().isBlank()
                        ? request.defaultCurrency().trim().toUpperCase(Locale.ROOT) : "XOF")
                .ordreAffichage(request.ordreAffichage())
                .categoryCode(normalizeCategory(request.categoryCode()).name())
                .provider(chosen.local())
                .providerCode(chosen.code())
                .build();
        return toBillerResponse(billerRepository.save(biller));
    }

    @Transactional
    public BillerResponse update(UUID id, BillerRequest request) {
        Biller biller = requireBiller(id);
        String code = normalizeCode(request.code());
        if (!biller.getCode().equalsIgnoreCase(code) && billerRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("BILLER_EXISTS", "Facturier déjà existant: " + code);
        }
        biller.setCode(code);
        biller.setName(request.name().trim());
        biller.setDescription(request.description());
        biller.setLogoUrl(request.logoUrl());
        biller.setActif(Boolean.TRUE.equals(request.actif()));
        if (request.defaultCurrency() != null && !request.defaultCurrency().isBlank()) {
            biller.setDefaultCurrency(request.defaultCurrency().trim().toUpperCase(Locale.ROOT));
        }
        biller.setOrdreAffichage(request.ordreAffichage());
        biller.setCategoryCode(normalizeCategory(request.categoryCode()).name());
        ChosenProvider chosen = requireProvider(request.providerId());
        biller.setProvider(chosen.local());
        biller.setProviderCode(chosen.code());
        return toBillerResponse(billerRepository.save(biller));
    }

    @Transactional(readOnly = true)
    public List<BillerProductResponse> listProducts(UUID billerId, boolean activeOnly) {
        requireBiller(billerId);
        List<BillerProduct> products = activeOnly
                ? productRepository.findByBillerIdAndActifTrueOrderByOrdreAffichageAscNameAsc(billerId)
                : productRepository.findByBillerIdOrderByOrdreAffichageAscNameAsc(billerId);
        return products.stream().map(this::toProductResponse).toList();
    }

    /**
     * Produits d'un facturier identifié par UUID ou par code métier (ex. {@code SENELEC}).
     */
    @Transactional(readOnly = true)
    public List<BillerProductResponse> listProductsByBillerKey(String billerKey, boolean activeOnly) {
        return listProducts(resolveBiller(billerKey).getId(), activeOnly);
    }

    @Transactional(readOnly = true)
    public List<BillerProductResponse> listProductsByBillerCode(String billerCode, boolean activeOnly) {
        if (billerCode == null || billerCode.isBlank()) {
            throw new BusinessException("BILLER_CODE_REQUIRED", "billerCode est requis", HttpStatus.BAD_REQUEST);
        }
        Biller biller = billerRepository.findByCodeIgnoreCase(billerCode.trim())
                .orElseThrow(() -> new BusinessException(
                        "BILLER_NOT_FOUND",
                        "Facturier introuvable: " + billerCode.trim(),
                        HttpStatus.NOT_FOUND));
        return listProducts(biller.getId(), activeOnly);
    }

    @Transactional(readOnly = true)
    public List<BillerProductResponse> listAllProducts() {
        return productRepository.findAll().stream()
                .sorted((a, b) -> {
                    int o = Integer.compare(
                            a.getOrdreAffichage() != null ? a.getOrdreAffichage() : 0,
                            b.getOrdreAffichage() != null ? b.getOrdreAffichage() : 0);
                    if (o != 0) {
                        return o;
                    }
                    return a.getName().compareToIgnoreCase(b.getName());
                })
                .map(this::toProductResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BillerProductResponse getProduct(UUID productId) {
        return toProductResponse(requireProduct(productId));
    }

    @Transactional(readOnly = true)
    public BillerProduct requireProductByCodes(String billerCode, String productCode) {
        return productRepository.findByBiller_CodeIgnoreCaseAndCodeIgnoreCase(billerCode, productCode)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND",
                        "Produit introuvable: " + billerCode + "/" + productCode,
                        HttpStatus.NOT_FOUND));
    }

    /**
     * Code du fournisseur FACTURIER rattaché au produit (sinon au facturier).
     */
    @Transactional(readOnly = true)
    public Optional<String> resolveFacturierProviderCode(String billerCode, String productCode) {
        if (billerCode == null || billerCode.isBlank()) {
            return Optional.empty();
        }
        if (productCode != null && !productCode.isBlank()) {
            return productRepository.findByBiller_CodeIgnoreCaseAndCodeIgnoreCase(billerCode.trim(), productCode.trim())
                    .map(this::providerCodeOf);
        }
        return billerRepository.findByCodeIgnoreCase(billerCode.trim())
                .map(this::providerCodeOf);
    }

    @Transactional
    public BillerProductResponse createProduct(UUID billerId, BillerProductRequest request) {
        Biller biller = requireBiller(billerId);
        String code = normalizeCode(request.code());
        if (productRepository.existsByBillerIdAndCodeIgnoreCase(billerId, code)) {
            throw new BusinessException("PRODUCT_EXISTS", "Produit déjà existant: " + code);
        }
        BillerProduct product = mapProduct(new BillerProduct(), biller, request, code);
        return toProductResponse(productRepository.save(product));
    }

    @Transactional
    public BillerProductResponse updateProduct(UUID productId, BillerProductRequest request) {
        BillerProduct product = requireProduct(productId);
        String code = normalizeCode(request.code());
        if (!product.getCode().equalsIgnoreCase(code)
                && productRepository.existsByBillerIdAndCodeIgnoreCase(product.getBiller().getId(), code)) {
            throw new BusinessException("PRODUCT_EXISTS", "Produit déjà existant: " + code);
        }
        mapProduct(product, product.getBiller(), request, code);
        return toProductResponse(productRepository.save(product));
    }

    private BillerProduct mapProduct(BillerProduct product, Biller biller, BillerProductRequest request, String code) {
        product.setBiller(biller);
        product.setCode(code);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setActif(Boolean.TRUE.equals(request.actif()));
        product.setLookupMode(request.lookupMode());
        product.setAmountPolicy(request.amountPolicy());
        product.setMinAmount(request.minAmount());
        product.setMaxAmount(request.maxAmount());
        product.setAllowOverpay(Boolean.TRUE.equals(request.allowOverpay()));
        product.setJourneyCode(request.journeyCode());
        product.setPaymentFlowId(request.paymentFlowId());
        product.setOrdreAffichage(request.ordreAffichage());
        ChosenProvider chosen = resolveOptionalProvider(request.providerId());
        product.setProvider(chosen.local());
        product.setProviderCode(chosen.code());
        product.setLookupFieldsJson(uiSchemaService.writeFields(request.lookupFields()));
        product.setConfirmFieldsJson(uiSchemaService.writeFields(request.confirmFields()));
        product.setAmountFieldsJson(uiSchemaService.writeFields(request.amountFields()));
        return product;
    }

    private ChosenProvider requireProvider(UUID providerId) {
        if (providerId == null) {
            throw new BusinessException("PROVIDER_REQUIRED", "Fournisseur obligatoire");
        }
        return resolveProvider(providerId);
    }

    private ChosenProvider resolveOptionalProvider(UUID providerId) {
        if (providerId == null) {
            return new ChosenProvider(null, null, null);
        }
        return resolveProvider(providerId);
    }

    private ChosenProvider resolveProvider(UUID providerId) {
        PaymentProvider local = providerRepository.findById(providerId).orElse(null);
        if (local != null) {
            if (local.getParticipantKind() != FseParticipantKind.FACTURIER) {
                throw new BusinessException("PROVIDER_KIND_INVALID",
                        "Le fournisseur doit être de type FACTURIER (reçu: "
                                + (local.getParticipantKind() != null
                                ? local.getParticipantKind().name() : "null") + ")");
            }
            if (!local.isActif()) {
                throw new BusinessException("PROVIDER_INACTIVE", "Fournisseur facturier inactif");
            }
            String code = StringUtils.hasText(local.getCodeFse()) ? local.getCodeFse().trim() : local.getCode();
            return new ChosenProvider(local, code, local.getLibelle());
        }
        ProviderResponse remote = omnicanalCatalog.provider(providerId)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND",
                        "Fournisseur introuvable", HttpStatus.NOT_FOUND));
        return new ChosenProvider(null, remote.codeFse(), remote.libelle());
    }

    private String providerCodeOf(Biller biller) {
        if (biller.getProvider() != null) {
            return biller.getProvider().getCode();
        }
        return StringUtils.hasText(biller.getProviderCode()) ? biller.getProviderCode() : null;
    }

    private String providerCodeOf(BillerProduct product) {
        if (product.getProvider() != null) {
            return product.getProvider().getCode();
        }
        if (StringUtils.hasText(product.getProviderCode())) {
            return product.getProviderCode();
        }
        return product.getBiller() != null ? providerCodeOf(product.getBiller()) : null;
    }

    private ShownProvider show(PaymentProvider local, String code) {
        if (local != null) {
            return new ShownProvider(local.getId(), local.getCode(), local.getLibelle());
        }
        if (!StringUtils.hasText(code)) {
            return new ShownProvider(null, null, null);
        }
        String trimmed = code.trim();
        ProviderResponse remote = omnicanalCatalog.byCode(trimmed).orElse(null);
        UUID id = remote != null ? remote.id() : omnicanalCatalog.idOfCode(trimmed);
        String libelle = remote != null ? remote.libelle() : trimmed;
        return new ShownProvider(id, trimmed, libelle);
    }

    private record ChosenProvider(PaymentProvider local, String code, String libelle) {}

    private record ShownProvider(UUID id, String code, String libelle) {}

    private Biller requireBiller(UUID id) {
        return billerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("BILLER_NOT_FOUND", "Facturier introuvable", HttpStatus.NOT_FOUND));
    }

    private Biller resolveBiller(String billerKey) {
        if (billerKey == null || billerKey.isBlank()) {
            throw new BusinessException("BILLER_REQUIRED", "Identifiant ou code facturier requis", HttpStatus.BAD_REQUEST);
        }
        String key = billerKey.trim();
        try {
            return requireBiller(UUID.fromString(key));
        } catch (IllegalArgumentException ignored) {
            return billerRepository.findByCodeIgnoreCase(key)
                    .orElseThrow(() -> new BusinessException(
                            "BILLER_NOT_FOUND",
                            "Facturier introuvable: " + key,
                            HttpStatus.NOT_FOUND));
        }
    }

    private BillerProduct requireProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Produit introuvable", HttpStatus.NOT_FOUND));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private BillerCategory normalizeCategory(String categoryCode) {
        return BillerCategory.orOther(categoryCode);
    }

    private BillerResponse toBillerResponse(Biller b) {
        int count = b.getProducts() != null ? b.getProducts().size() : 0;
        if (b.getId() != null && (b.getProducts() == null || b.getProducts().isEmpty())) {
            count = productRepository.findByBillerIdOrderByOrdreAffichageAscNameAsc(b.getId()).size();
        }
        BillerCategory category = BillerCategory.orOther(b.getCategoryCode());
        ShownProvider shown = show(b.getProvider(), b.getProviderCode());
        return new BillerResponse(
                b.getId(), b.getCode(), b.getName(), b.getDescription(), b.getLogoUrl(),
                b.isActif(), b.getDefaultCurrency(), b.getOrdreAffichage(),
                category.name(), category.getLabel(), count,
                shown.id(), shown.code(), shown.libelle()
        );
    }

    private BillerProductResponse toProductResponse(BillerProduct p) {
        ShownProvider own = show(p.getProvider(), p.getProviderCode());
        ShownProvider resolved = StringUtils.hasText(p.getProviderCode()) || p.getProvider() != null
                ? own
                : show(p.getBiller() != null ? p.getBiller().getProvider() : null,
                p.getBiller() != null ? p.getBiller().getProviderCode() : null);
        return new BillerProductResponse(
                p.getId(),
                p.getBiller().getId(),
                p.getBiller().getCode(),
                p.getCode(),
                p.getName(),
                p.getDescription(),
                p.isActif(),
                p.getLookupMode(),
                p.getAmountPolicy(),
                p.getMinAmount(),
                p.getMaxAmount(),
                p.isAllowOverpay(),
                p.getJourneyCode(),
                p.getPaymentFlowId(),
                p.getOrdreAffichage(),
                own.id(),
                own.code(),
                own.libelle(),
                resolved.id(),
                resolved.code(),
                uiSchemaService.resolveLookupFields(p),
                uiSchemaService.resolveConfirmFields(p),
                uiSchemaService.resolveAmountFields(p)
        );
    }
}
