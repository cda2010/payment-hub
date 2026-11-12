package com.gateway.payment.security.api;

import com.gateway.payment.security.api.dto.CreateUserRequest;
import com.gateway.payment.security.api.dto.UserResponse;
import com.gateway.payment.security.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister les utilisateurs")
    public Page<UserResponse> list(Pageable pageable) {
        return userManagementService.list(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un utilisateur")
    public UserResponse create(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return userManagementService.create(request, principal.getUsername());
    }

    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer / désactiver un utilisateur")
    public UserResponse setEnabled(
            @PathVariable UUID id,
            @RequestParam boolean enabled,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return userManagementService.setEnabled(id, enabled, principal.getUsername());
    }
}
