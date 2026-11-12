package com.gateway.payment.configuration.api;

import com.gateway.payment.configuration.api.dto.ServiceTypeResponse;
import com.gateway.payment.configuration.service.ServiceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/service-types")
@Tag(name = "Service types")
@SecurityRequirement(name = "bearerAuth")
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    public ServiceTypeController(ServiceTypeService serviceTypeService) {
        this.serviceTypeService = serviceTypeService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les types de service actifs (LINK, TRANSFERT, PAYEMENT…)")
    public List<ServiceTypeResponse> listActive() {
        return serviceTypeService.listActive();
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Lister tous les types de service")
    public List<ServiceTypeResponse> listAll() {
        return serviceTypeService.listAll();
    }
}
