package com.gateway.payment.transfer.api;

import com.gateway.payment.transfer.api.dto.*;
import com.gateway.payment.transfer.service.TransferCatalogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/transfers")
@Tag(name = "Admin Transfers")
@SecurityRequirement(name = "bearerAuth")
public class AdminTransferController {

    private final TransferCatalogService catalogService;

    public AdminTransferController(TransferCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/providers")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public List<TransferParticipantResponse> listProviders() {
        return catalogService.listParticipants(false);
    }

    @GetMapping("/providers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public TransferParticipantResponse getProvider(@PathVariable UUID id) {
        return catalogService.getParticipant(id);
    }

    @PostMapping("/providers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public TransferParticipantResponse createProvider(@Valid @RequestBody TransferParticipantRequest request) {
        return catalogService.createParticipant(request);
    }

    @PutMapping("/providers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public TransferParticipantResponse updateProvider(
            @PathVariable UUID id,
            @Valid @RequestBody TransferParticipantRequest request
    ) {
        return catalogService.updateParticipant(id, request);
    }

    /** @deprecated alias — préférer /providers */
    @GetMapping("/participants")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public List<TransferParticipantResponse> listParticipants() {
        return catalogService.listParticipants(false);
    }

    @PostMapping("/participants")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public TransferParticipantResponse createParticipant(@Valid @RequestBody TransferParticipantRequest request) {
        return catalogService.createParticipant(request);
    }

    @PutMapping("/participants/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public TransferParticipantResponse updateParticipant(
            @PathVariable UUID id,
            @Valid @RequestBody TransferParticipantRequest request
    ) {
        return catalogService.updateParticipant(id, request);
    }

    @GetMapping("/fee-policies")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public List<TransferFeePolicyResponse> listFeePolicies() {
        return catalogService.listFeePolicies();
    }

    @PostMapping("/fee-policies")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public TransferFeePolicyResponse createFeePolicy(@Valid @RequestBody TransferFeePolicyRequest request) {
        return catalogService.createFeePolicy(request);
    }

    @PutMapping("/fee-policies/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public TransferFeePolicyResponse updateFeePolicy(
            @PathVariable UUID id,
            @Valid @RequestBody TransferFeePolicyRequest request
    ) {
        return catalogService.updateFeePolicy(id, request);
    }

    @GetMapping("/fee-rules")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public List<FeeRuleResponse> listFeeRules() {
        return catalogService.listFeeRules();
    }

    @PostMapping("/fee-rules")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public FeeRuleResponse createFeeRule(@Valid @RequestBody FeeRuleRequest request) {
        return catalogService.createFeeRule(request);
    }

    @PutMapping("/fee-rules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public FeeRuleResponse updateFeeRule(@PathVariable UUID id, @Valid @RequestBody FeeRuleRequest request) {
        return catalogService.updateFeeRule(id, request);
    }

    @GetMapping("/rails")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public List<TransferRailResponse> listRails() {
        return catalogService.listRails(false);
    }

    @GetMapping("/rails/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public TransferRailResponse getRail(@PathVariable UUID id) {
        return catalogService.getRail(id);
    }

    @PostMapping("/rails")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public TransferRailResponse createRail(@Valid @RequestBody TransferRailRequest request) {
        return catalogService.createRail(request);
    }

    @PutMapping("/rails/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public TransferRailResponse updateRail(
            @PathVariable UUID id,
            @Valid @RequestBody TransferRailRequest request
    ) {
        return catalogService.updateRail(id, request);
    }
}
