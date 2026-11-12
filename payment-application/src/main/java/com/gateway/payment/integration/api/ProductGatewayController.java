package com.gateway.payment.integration.api;

import com.gateway.payment.approval.api.dto.ApprovalDtos;
import com.gateway.payment.billing.api.dto.BillSearchResponse;
import com.gateway.payment.billing.api.dto.BillerCategoryResponse;
import com.gateway.payment.billing.api.dto.BillerPageResponse;
import com.gateway.payment.billing.api.dto.BillerProductResponse;
import com.gateway.payment.catalog.api.dto.CatalogLayoutResponse;
import com.gateway.payment.datasources.api.DataSourceDescriptorDto;
import com.gateway.payment.datasources.api.DataSourceResponse;
import com.gateway.payment.integration.api.dto.GatewayActionRequest;
import com.gateway.payment.integration.api.dto.GatewayTransactionPageResponse;
import com.gateway.payment.integration.api.dto.IntegrationScreenResponse;
import com.gateway.payment.catalog.omnicanal.OmnicanalCatalogInvocationService;
import com.gateway.payment.integration.service.ProductGatewayFacade;
import com.gateway.payment.transfer.api.dto.TransferParticipantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contrat produit pour systèmes externes (même surface que le BFF paymentHub2).
 * <p>
 * Doc : {@code docs/INTEGRATION.md}.
 */
@RestController
@RequestMapping("/api/v1/gateway")
@Tag(name = "Product Gateway", description = "Façade produit générique POST /products/{code} "
        + "(+ alias checkout / bill-payment / account-transfer / rtgs / swift).")
@SecurityRequirement(name = "bearerAuth")
public class ProductGatewayController {

    private final ProductGatewayFacade facade;
    private final OmnicanalCatalogInvocationService omnicanal;

    public ProductGatewayController(
            ProductGatewayFacade facade,
            OmnicanalCatalogInvocationService omnicanal
    ) {
        this.facade = facade;
        this.omnicanal = omnicanal;
    }

    // ── Catalogue ──────────────────────────────────────────────────────────

    @GetMapping("/account-balance")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Solde d'un compte via CONSULTATIONSOLDE")
    public Map<String, Object> accountBalance(
            @RequestParam String numeroCompte,
            @RequestParam String providerCode
    ) {
        return omnicanal.consultBalance(providerCode, numeroCompte);
    }

    @GetMapping("/providers")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Providers selon produit catalogue (code ou alias path)")
    public Object providers(@RequestParam(defaultValue = "checkout") String product) {
        return facade.listProvidersForProduct(product);
    }

    @GetMapping("/transfer-destinations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Destinations transfert (canReceive)")
    public List<TransferParticipantResponse> transferDestinations() {
        return facade.listTransferDestinations();
    }

    @GetMapping("/biller-categories")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Catégories facturiers actives (navigation mobile)")
    public List<BillerCategoryResponse> billerCategories() {
        return facade.listBillerCategories();
    }

    @GetMapping("/billers")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Facturiers actifs — page filtrable (category, q, page, size)")
    public BillerPageResponse billers(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return facade.searchBillers(category, q, page, size);
    }

    @GetMapping("/billers/{billerKey}/products")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Produits actifs d'un facturier (UUID ou code métier)")
    public List<BillerProductResponse> billerProducts(@PathVariable String billerKey) {
        return facade.listBillerProducts(billerKey);
    }

    @GetMapping("/biller-products")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Produits actifs filtrés par code facturier (sélection UI)")
    public List<BillerProductResponse> billerProductsByCode(@RequestParam String billerCode) {
        return facade.listBillerProductsByCode(billerCode);
    }

    @GetMapping("/bills/search")
    @PreAuthorize("isAuthenticated()")
    public BillSearchResponse searchBills(
            @RequestParam String billerCode,
            @RequestParam String productCode,
            @RequestParam(required = false) String subscriberReference,
            @RequestParam(required = false) String billReference
    ) {
        return facade.searchBills(billerCode, productCode, subscriberReference, billReference);
    }

    @GetMapping("/products/{productCode}/catalog-layout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Composition catalogue (displayKind + dataSource, sans hydratation)")
    public CatalogLayoutResponse productCatalogLayout(
            @PathVariable String productCode,
            @RequestParam(required = false, defaultValue = "MOBILE") String channel
    ) {
        return facade.productCatalogLayout(productCode, channel);
    }

    @GetMapping("/products/{productCode}/catalog")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Catalogue produit matérialisé (sections displayKind + items)")
    public CatalogLayoutResponse productCatalog(
            @PathVariable String productCode,
            @RequestParam(required = false, defaultValue = "MOBILE") String channel,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "true") boolean hydrate
    ) {
        return facade.productCatalog(productCode, channel, category, q, page, size, hydrate);
    }

    @GetMapping("/data-sources")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Inventaire des sources de données (designer RapidFrame / Formio)")
    public List<DataSourceDescriptorDto> listDataSources() {
        return facade.listDataSources();
    }

    @GetMapping("/data-sources/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Résoudre une source LOCAL, OMNICANAL ou GATEWAY (app peer)")
    public DataSourceResponse resolveDataSource(
            @PathVariable String code,
            @RequestParam(required = false, defaultValue = "LOCAL") String origin,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) String productCode,
            @RequestParam Map<String, String> query
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        if (query != null) {
            for (Map.Entry<String, String> e : query.entrySet()) {
                String k = e.getKey();
                if (k == null || e.getValue() == null || e.getValue().isBlank()) {
                    continue;
                }
                if (Set.of("origin", "category", "q", "page", "size", "productCode").contains(k)) {
                    continue;
                }
                params.put(k, e.getValue().trim());
            }
        }
        return facade.resolveDataSource(code, origin, params, category, q, page, size, productCode);
    }

    // ── Démarrage produits ─────────────────────────────────────────────────

    @PostMapping("/products/{productCode}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Démarrer n'importe quel produit catalogue PUBLISHED (générique)")
    public IntegrationScreenResponse startProduct(
            @PathVariable String productCode,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        return facade.startProduct(productCode, body != null ? body : Map.of());
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alias CHECKOUT → /products/CHECKOUT")
    public IntegrationScreenResponse checkout(@RequestBody Map<String, Object> body) {
        return facade.startCheckout(body != null ? body : Map.of());
    }

    @PostMapping("/coffre")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alias COFFRE → /products/COFFRE")
    public IntegrationScreenResponse coffre(@RequestBody(required = false) Map<String, Object> body) {
        return facade.startProduct("COFFRE", body != null ? body : Map.of());
    }

    @PostMapping("/tontine")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alias TONTINE → /products/TONTINE")
    public IntegrationScreenResponse tontine(@RequestBody(required = false) Map<String, Object> body) {
        return facade.startProduct("TONTINE", body != null ? body : Map.of());
    }

    @PostMapping("/bill-payment")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Démarrer BILL_PAYMENT → IntegrationScreenResponse")
    public IntegrationScreenResponse billPayment(@RequestBody Map<String, Object> body) {
        return facade.startBillPayment(body != null ? body : Map.of());
    }

    @PostMapping("/account-transfer")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Démarrer ACCOUNT_TRANSFER → IntegrationScreenResponse")
    public IntegrationScreenResponse accountTransfer(@RequestBody Map<String, Object> body) {
        return facade.startAccountTransfer(body != null ? body : Map.of());
    }

    @PostMapping("/rtgs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alias RTGS → /products/RTGS")
    public IntegrationScreenResponse rtgs(@RequestBody Map<String, Object> body) {
        return facade.startRtgs(body != null ? body : Map.of());
    }

    @PostMapping("/swift")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alias SWIFT → /products/SWIFT")
    public IntegrationScreenResponse swift(@RequestBody Map<String, Object> body) {
        return facade.startSwift(body != null ? body : Map.of());
    }

    @PostMapping("/cash-out")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alias CASH_OUT → /products/CASH_OUT")
    public IntegrationScreenResponse cashOut(@RequestBody Map<String, Object> body) {
        return facade.startProduct("CASH_OUT", body != null ? body : Map.of());
    }

    @PostMapping("/cash-in")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alias CASH_IN → /products/CASH_IN")
    public IntegrationScreenResponse cashIn(@RequestBody Map<String, Object> body) {
        return facade.startProduct("CASH_IN", body != null ? body : Map.of());
    }

    @PostMapping("/link")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alias LINK → /products/LINK (liaison de compte)")
    public IntegrationScreenResponse link(@RequestBody Map<String, Object> body) {
        return facade.startProduct("LINK", body != null ? body : Map.of());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alias REGISTER → /products/LINK")
    public IntegrationScreenResponse register(@RequestBody Map<String, Object> body) {
        return facade.startProduct("LINK", body != null ? body : Map.of());
    }

    @PostMapping("/remittance")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alias REMITTANCE → /products/REMITTANCE")
    public IntegrationScreenResponse remittance(@RequestBody Map<String, Object> body) {
        return facade.startProduct("REMITTANCE", body != null ? body : Map.of());
    }

    @PostMapping("/remittance-receive")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alias REMITTANCE_RECEIVE → /products/REMITTANCE_RECEIVE")
    public IntegrationScreenResponse remittanceReceive(@RequestBody Map<String, Object> body) {
        return facade.startProduct("REMITTANCE_RECEIVE", body != null ? body : Map.of());
    }

    // ── Mid-flow ───────────────────────────────────────────────────────────

    @GetMapping("/transactions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Historique des transactions (payment_transaction, page filtrable)")
    public GatewayTransactionPageResponse transactions(
            @RequestParam(required = false) String customerReference,
            @RequestParam(required = false) String initiatedBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return facade.searchTransactions(
                customerReference, initiatedBy, status, serviceType, product, provider, q, from, to, page, size);
    }

    @GetMapping("/payments/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Snapshot écran (nextAction mappé)")
    public IntegrationScreenResponse get(@PathVariable String paymentId) {
        return facade.getScreen(paymentId);
    }

    @PostMapping("/payments/{paymentId}/actions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "SUBMIT_FORM / SUBMIT_OTP / …")
    public IntegrationScreenResponse action(
            @PathVariable String paymentId,
            @RequestBody GatewayActionRequest body
    ) {
        String action = body != null && body.action() != null ? body.action() : "SUBMIT_FORM";
        Map<String, Object> data = body != null && body.data() != null ? body.data() : Map.of();
        return facade.submitAction(paymentId, action, data);
    }

    // ── Approvals ──────────────────────────────────────────────────────────

    @GetMapping("/approvals/inbox")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Inbox validations (rôles JWT)")
    public List<ApprovalDtos.InboxItem> approvalInbox() {
        return facade.approvalInbox();
    }

    @GetMapping("/approvals/by-payment/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    public ApprovalDtos.Detail approvalDetail(@PathVariable String paymentId) {
        return facade.approvalDetail(paymentId);
    }

    @PostMapping("/approvals/{paymentId}/decide")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "APPROVE|REJECT → reprend le parcours (écran)")
    public IntegrationScreenResponse decide(
            @PathVariable String paymentId,
            @RequestBody GatewayActionRequest body
    ) {
        String action = body != null ? body.action() : null;
        Map<String, Object> data = body != null && body.data() != null ? body.data() : Map.of();
        return facade.decideApproval(paymentId, action, data);
    }
}
