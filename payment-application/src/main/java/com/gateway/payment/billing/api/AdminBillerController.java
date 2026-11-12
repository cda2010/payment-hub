package com.gateway.payment.billing.api;

import com.gateway.payment.billing.api.dto.BillerProductRequest;
import com.gateway.payment.billing.api.dto.BillerProductResponse;
import com.gateway.payment.billing.api.dto.BillerRequest;
import com.gateway.payment.billing.api.dto.BillerResponse;
import com.gateway.payment.billing.service.BillerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/billers")
@Tag(name = "Admin Billers")
@SecurityRequirement(name = "bearerAuth")
public class AdminBillerController {

    private final BillerService billerService;

    public AdminBillerController(BillerService billerService) {
        this.billerService = billerService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Lister les facturiers")
    public List<BillerResponse> list() {
        return billerService.listAll();
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Lister tous les produits facturier (grilles RF)")
    public List<BillerProductResponse> listAllProducts() {
        return billerService.listAllProducts();
    }

    @GetMapping("/products/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public BillerProductResponse getProduct(@PathVariable UUID productId) {
        return billerService.getProduct(productId);
    }

    @PutMapping("/products/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public BillerProductResponse updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody BillerProductRequest request
    ) {
        return billerService.updateProduct(productId, request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public BillerResponse get(@PathVariable UUID id) {
        return billerService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public BillerResponse create(@Valid @RequestBody BillerRequest request) {
        return billerService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public BillerResponse update(@PathVariable UUID id, @Valid @RequestBody BillerRequest request) {
        return billerService.update(id, request);
    }

    @GetMapping("/{id}/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public List<BillerProductResponse> listProducts(@PathVariable UUID id) {
        return billerService.listProducts(id, false);
    }

    @PostMapping("/{id}/products")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public BillerProductResponse createProduct(
            @PathVariable UUID id,
            @Valid @RequestBody BillerProductRequest request
    ) {
        return billerService.createProduct(id, request);
    }
}
