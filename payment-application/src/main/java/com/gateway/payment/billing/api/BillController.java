package com.gateway.payment.billing.api;



import com.gateway.payment.billing.api.dto.BillDto;

import com.gateway.payment.billing.api.dto.BillSearchResponse;

import com.gateway.payment.billing.api.dto.BillerProductResponse;

import com.gateway.payment.billing.api.dto.BillerResponse;

import com.gateway.payment.billing.service.BillCatalogService;

import com.gateway.payment.billing.service.BillerService;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;



import java.util.List;



@RestController

@RequestMapping("/api/v1/bills")

@Tag(name = "Bills")

@SecurityRequirement(name = "bearerAuth")

public class BillController {



    private final BillCatalogService billCatalogService;

    private final BillerService billerService;



    public BillController(BillCatalogService billCatalogService, BillerService billerService) {

        this.billCatalogService = billCatalogService;

        this.billerService = billerService;

    }



    @GetMapping("/billers")

    @PreAuthorize("isAuthenticated()")

    @Operation(summary = "Lister les facturiers actifs")

    public List<BillerResponse> listBillers() {

        return billerService.listActive();

    }



    @GetMapping("/billers/{billerKey}/products")

    @PreAuthorize("isAuthenticated()")

    @Operation(summary = "Lister les produits actifs d'un facturier (UUID ou code)")

    public List<BillerProductResponse> listProducts(@PathVariable String billerKey) {

        return billerService.listProductsByBillerKey(billerKey, true);

    }



    @GetMapping("/biller-products")

    @PreAuthorize("isAuthenticated()")

    @Operation(summary = "Produits actifs filtrés par code facturier")

    public List<BillerProductResponse> listProductsByCode(@RequestParam String billerCode) {

        return billerService.listProductsByBillerCode(billerCode, true);

    }



    @GetMapping("/search")

    @PreAuthorize("isAuthenticated()")

    @Operation(summary = "Rechercher des factures par abonné ou par référence facture")

    public BillSearchResponse search(

            @RequestParam(required = false) String subscriberReference,

            @RequestParam(required = false) String billReference,

            @RequestParam String billerCode,

            @RequestParam String productCode

    ) {

        if (billReference != null && !billReference.isBlank()) {

            return billCatalogService.searchByBillReference(billReference, billerCode, productCode);

        }

        return billCatalogService.searchBySubscriber(subscriberReference, billerCode, productCode);

    }



    @GetMapping("/{id}")

    @PreAuthorize("isAuthenticated()")

    @Operation(summary = "Détail d'une facture")

    public BillDto get(@PathVariable String id) {

        return billCatalogService.getById(id);

    }

}


