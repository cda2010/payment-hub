package com.gateway.payment.divers.service;

import com.gateway.payment.divers.domain.Budget;
import com.gateway.payment.divers.domain.CategorieDepense;
import com.gateway.payment.divers.repository.BudgetRepository;
import com.gateway.payment.divers.repository.CategorieDepenseRepository;
import com.gateway.payment.divers.repository.DepenseRepository;
import com.gateway.payment.divers.spend.SpendMonth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BudgetService {

    private final BudgetRepository budgets;
    private final CategorieDepenseRepository categories;
    private final DepenseRepository depenses;

    public BudgetService(
            BudgetRepository budgets,
            CategorieDepenseRepository categories,
            DepenseRepository depenses
    ) {
        this.budgets = budgets;
        this.categories = categories;
        this.depenses = depenses;
    }

    @Transactional(readOnly = true)
    public List<BudgetView> list(UUID partyId, String monthRaw) {
        SpendMonth month = SpendMonth.parse(monthRaw);
        return budgets.findByPartyIdOrderByCategorie_PriorityAsc(partyId).stream()
                .map(budget -> view(budget, month))
                .toList();
    }

    @Transactional
    public BudgetView create(UUID partyId, String name, String categorieCode, BigDecimal limit) {
        if (partyId == null) {
            throw new IllegalArgumentException("partyId requis");
        }
        String label = name == null ? "" : name.trim();
        if (label.length() < 2) {
            throw new IllegalArgumentException("Le nom de l'enveloppe est requis");
        }
        if (limit == null || limit.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Le plafond doit être d'au moins 1");
        }
        String code = categorieCode == null ? "" : categorieCode.trim().toLowerCase();
        CategorieDepense categorie = categories.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie inconnue"));
        if (budgets.existsByPartyIdAndCategorie_Code(partyId, code)) {
            throw new IllegalArgumentException("Une enveloppe existe déjà pour cette catégorie");
        }
        Budget budget = new Budget();
        budget.setPartyId(partyId);
        budget.setName(label);
        budget.setCategorie(categorie);
        budget.setLimitAmount(limit);
        budget.setCreatedAt(Instant.now());
        return view(budgets.save(budget), SpendMonth.parse(null));
    }

    private BudgetView view(Budget budget, SpendMonth month) {
        BigDecimal spent = depenses.sumForCategory(
                budget.getPartyId(), budget.getCategorie().getCode(), month.start(), month.end());
        if (spent == null) {
            spent = BigDecimal.ZERO;
        }
        BigDecimal remaining = budget.getLimitAmount().subtract(spent).max(BigDecimal.ZERO);
        return new BudgetView(
                budget.getId(),
                budget.getName(),
                budget.getCategorie().getCode(),
                budget.getCategorie().getLabel(),
                budget.getCategorie().getIcon(),
                budget.getLimitAmount(),
                spent,
                remaining,
                month.toString());
    }

    public record BudgetView(
            UUID id,
            String name,
            String categorieCode,
            String categorieLabel,
            String icon,
            BigDecimal limit,
            BigDecimal spent,
            BigDecimal remaining,
            String month
    ) {
    }
}
