package com.gateway.payment.transfer.api;

import com.gateway.payment.transfer.domain.TransferBeneficiary;
import com.gateway.payment.transfer.service.TransferBeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfer-beneficiaries")
@Tag(name = "Transfer beneficiaries", description = "Carnet de bénéficiaires")
public class TransferBeneficiaryController {

    private final TransferBeneficiaryService service;

    public TransferBeneficiaryController(TransferBeneficiaryService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les bénéficiaires actifs")
    public List<Map<String, Object>> list() {
        return service.listActive().stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Détail d'un bénéficiaire")
    public Map<String, Object> get(@PathVariable UUID id) {
        try {
            return toDto(service.get(id));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ajouter un bénéficiaire")
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        TransferBeneficiary draft = TransferBeneficiary.builder()
                .label(str(body.get("label")))
                .phone(str(body.get("phone")))
                .accountNumber(firstStr(body, "accountNumber", "numeroCompte"))
                .agencyCode(firstStr(body, "agencyCode", "agence", "codeAgence"))
                .destinationProvider(str(body.get("destinationProvider")))
                .icon(str(body.get("icon")))
                .ownerKey(str(body.get("ownerKey")))
                .build();
        return toDto(service.create(draft));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Modifier un bénéficiaire")
    public Map<String, Object> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        try {
            TransferBeneficiary patch = TransferBeneficiary.builder()
                    .label(str(body.get("label")))
                    .phone(str(body.get("phone")))
                    .accountNumber(firstStr(body, "accountNumber", "numeroCompte"))
                    .agencyCode(firstStr(body, "agencyCode", "agence", "codeAgence"))
                    .destinationProvider(str(body.get("destinationProvider")))
                    .icon(str(body.get("icon")))
                    .build();
            return toDto(service.update(id, patch));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Supprimer un bénéficiaire")
    public void delete(@PathVariable UUID id) {
        try {
            service.delete(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private Map<String, Object> toDto(TransferBeneficiary b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId() != null ? b.getId().toString() : null);
        m.put("label", b.getLabel());
        m.put("phone", b.getPhone());
        m.put("accountNumber", b.getAccountNumber());
        m.put("agencyCode", b.getAgencyCode());
        m.put("agence", b.getAgencyCode());
        m.put("destinationProvider", b.getDestinationProvider());
        m.put("icon", b.getIcon());
        m.put("actif", b.isActif());
        return m;
    }

    private static String firstStr(Map<String, Object> body, String... keys) {
        if (body == null || keys == null) return null;
        for (String key : keys) {
            String s = str(body.get(key));
            if (s != null) return s;
        }
        return null;
    }

    private static String str(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() || "null".equals(s) ? null : s;
    }
}
