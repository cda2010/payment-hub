package com.gateway.payment.divers.api;

import com.gateway.payment.divers.api.TontineDtos.CreateTontineRequest;
import com.gateway.payment.divers.api.TontineDtos.JoinTontineRequest;
import com.gateway.payment.divers.api.TontineDtos.TontineView;
import com.gateway.payment.divers.service.TontineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/divers/tontines")
public class TontineController {

    private final TontineService tontines;

    public TontineController(TontineService tontines) {
        this.tontines = tontines;
    }

    @GetMapping
    public List<TontineView> list() {
        return tontines.list(CurrentUser.username());
    }

    @GetMapping("/{id}")
    public TontineView get(@PathVariable UUID id) {
        return tontines.get(CurrentUser.username(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TontineView create(@Valid @RequestBody CreateTontineRequest request) {
        return tontines.create(CurrentUser.username(), CurrentUser.partyId(), request);
    }

    @PostMapping("/join")
    public TontineView join(@Valid @RequestBody JoinTontineRequest request) {
        return tontines.join(CurrentUser.username(), CurrentUser.partyId(), request.inviteCode());
    }

    @PostMapping("/{id}/contributions")
    public TontineView contribute(@PathVariable UUID id) {
        return tontines.contribute(CurrentUser.username(), CurrentUser.partyId(), id);
    }
}
