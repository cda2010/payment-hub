package com.gateway.payment.billing.service;

import com.gateway.payment.billing.api.dto.BillDto;
import com.gateway.payment.billing.api.dto.BillSearchResponse;
import com.gateway.payment.billing.domain.BillerProduct;
import com.gateway.payment.billing.domain.JourneyCode;
import com.gateway.payment.billing.domain.LookupMode;
import com.gateway.payment.billing.repository.BillerProductRepository;
import com.gateway.payment.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Recherche factures côté gateway — plus de catalogue mock.
 * <ul>
 *   <li>PREPAID : synthétique depuis le produit DB (pas de liste factures).</li>
 *   <li>POSTPAID / OPEN_BILLS : doit passer par le connecteur ({@code LOOKUP_BILL} / {@code BILL_LIST}).</li>
 * </ul>
 */
@Service
public class BillCatalogService {

    private final BillerProductRepository productRepository;

    public BillCatalogService(BillerProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public BillSearchResponse searchBySubscriber(String subscriberReference, String billerCode, String productCode) {
        requireQuery(subscriberReference, "Référence abonné requise");
        BillerProduct product = resolveProduct(billerCode, productCode);
        assertLookupAllows(product, LookupMode.SUBSCRIBER);

        if (product.getJourneyCode() == JourneyCode.PREPAID) {
            BillDto synthetic = prepaidSynthetic(product, subscriberReference.trim());
            return new BillSearchResponse("SUBSCRIBER", subscriberReference.trim(), List.of(synthetic), 1);
        }

        throw connectorRequired(billerCode, productCode);
    }

    @Transactional(readOnly = true)
    public BillSearchResponse searchByBillReference(String billReference, String billerCode, String productCode) {
        requireQuery(billReference, "Référence facture requise");
        BillerProduct product = resolveProduct(billerCode, productCode);
        assertLookupAllows(product, LookupMode.BILL_REF);

        if (product.getJourneyCode() == JourneyCode.PREPAID) {
            throw new BusinessException(
                    "LOOKUP_NOT_SUPPORTED",
                    "Le produit prépayé ne s'interroge pas par n° de facture",
                    HttpStatus.BAD_REQUEST);
        }

        throw connectorRequired(billerCode, productCode);
    }

    @Transactional(readOnly = true)
    public BillDto getById(String id) {
        throw new BusinessException(
                "BILL_LOOKUP_VIA_CONNECTOR",
                "Consultation facture hors catalogue local — utiliser LOOKUP_BILL / BILL_LIST via le connecteur"
                        + (id != null ? " (id=" + id + ")" : ""),
                HttpStatus.GONE);
    }

    private static BusinessException connectorRequired(String billerCode, String productCode) {
        return new BusinessException(
                "BILL_LOOKUP_VIA_CONNECTOR",
                "Aucune donnée facture locale. Utiliser le connecteur (LOOKUP_BILL / BILL_LIST) pour "
                        + billerCode + "/" + productCode,
                HttpStatus.NOT_IMPLEMENTED);
    }

    private BillerProduct resolveProduct(String billerCode, String productCode) {
        if (billerCode == null || billerCode.isBlank()) {
            throw new BusinessException("BILLER_REQUIRED", "Facturier requis", HttpStatus.BAD_REQUEST);
        }
        if (productCode == null || productCode.isBlank()) {
            throw new BusinessException("PRODUCT_REQUIRED", "Produit requis", HttpStatus.BAD_REQUEST);
        }
        return productRepository.findByBiller_CodeIgnoreCaseAndCodeIgnoreCase(billerCode.trim(), productCode.trim())
                .filter(BillerProduct::isActif)
                .filter(p -> p.getBiller().isActif())
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND",
                        "Produit introuvable ou inactif: " + billerCode + "/" + productCode,
                        HttpStatus.NOT_FOUND));
    }

    private void assertLookupAllows(BillerProduct product, LookupMode requested) {
        LookupMode mode = product.getLookupMode();
        if (mode == LookupMode.BOTH) {
            return;
        }
        if (mode != requested) {
            throw new BusinessException(
                    "LOOKUP_NOT_SUPPORTED",
                    "Le produit " + product.getCode() + " n'autorise que la recherche " + mode,
                    HttpStatus.BAD_REQUEST);
        }
    }

    private BillDto prepaidSynthetic(BillerProduct product, String subscriber) {
        return new BillDto(
                "PREPAID-" + product.getBiller().getCode() + "-" + product.getCode() + "-" + normalize(subscriber),
                product.getBiller().getCode(),
                product.getBiller().getName(),
                product.getCode(),
                product.getName(),
                "PREPAID",
                subscriber,
                null,
                product.getBiller().getDefaultCurrency(),
                null,
                "OPEN",
                "Recharge " + product.getName(),
                subscriber,
                product.getAmountPolicy(),
                product.getMinAmount(),
                product.getMaxAmount(),
                product.isAllowOverpay(),
                product.getJourneyCode()
        );
    }

    private void requireQuery(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("BILL_QUERY_REQUIRED", message, HttpStatus.BAD_REQUEST);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT).replace(" ", "");
    }
}
