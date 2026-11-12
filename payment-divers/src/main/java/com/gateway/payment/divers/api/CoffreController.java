package com.gateway.payment.divers.api;

import com.gateway.payment.divers.api.CoffreDtos.CoffreView;
import com.gateway.payment.divers.api.CoffreDtos.CreateCoffreRequest;
import com.gateway.payment.divers.api.CoffreDtos.DepositRequest;
import com.gateway.payment.divers.api.CoffreDtos.LockRequest;
import com.gateway.payment.divers.service.CoffreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/divers/coffres")
public class CoffreController {

    private final CoffreService coffres;

    public CoffreController(CoffreService coffres) {
        this.coffres = coffres;
    }

    @GetMapping
    public List<CoffreView> list() {
        return coffres.list(CurrentUser.username());
    }

    @GetMapping("/{id}")
    public CoffreView get(@PathVariable UUID id) {
        return coffres.get(CurrentUser.username(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoffreView create(@Valid @RequestBody CreateCoffreRequest request) {
        return coffres.create(CurrentUser.username(), request);
    }

    @PostMapping("/{id}/deposits")
    public CoffreView deposit(@PathVariable UUID id, @Valid @RequestBody DepositRequest request) {
        return coffres.deposit(CurrentUser.username(), id, request.amount());
    }

    @PatchMapping("/{id}/lock")
    public CoffreView lock(@PathVariable UUID id, @Valid @RequestBody LockRequest request) {
        return coffres.setLocked(CurrentUser.username(), id, request.locked());
    }
}
