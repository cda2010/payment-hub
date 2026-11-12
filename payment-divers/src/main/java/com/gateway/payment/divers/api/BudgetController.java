package com.gateway.payment.divers.api;

import com.gateway.payment.divers.service.BudgetService;
import com.gateway.payment.divers.service.BudgetService.BudgetView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/divers/budgets")
public class BudgetController {

    private final BudgetService budgets;

    public BudgetController(BudgetService budgets) {
        this.budgets = budgets;
    }

    @GetMapping
    public List<BudgetView> list(@RequestParam(required = false) String month) {
        return budgets.list(partyId(), month);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetView create(@Valid @RequestBody CreateBudgetRequest request) {
        return budgets.create(partyId(), request.name(), request.categorieCode(), request.limit());
    }

    private static UUID partyId() {
        UUID partyId = CurrentUser.partyId();
        if (partyId == null) {
            throw new IllegalArgumentException("partyId requis");
        }
        return partyId;
    }

    public record CreateBudgetRequest(
            @NotBlank String name,
            @NotBlank String categorieCode,
            @NotNull BigDecimal limit
    ) {
    }
}
