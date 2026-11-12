package com.gateway.payment.divers.service;

import com.gateway.payment.divers.domain.CategorieDepense;
import com.gateway.payment.divers.repository.CategorieDepenseRepository;
import com.gateway.payment.divers.repository.DepenseRepository;
import com.gateway.payment.divers.spend.SpendMonth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InsightsService {

    private final DepenseRepository depenses;
    private final CategorieDepenseRepository categories;

    public InsightsService(DepenseRepository depenses, CategorieDepenseRepository categories) {
        this.depenses = depenses;
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public InsightsView month(UUID partyId, String monthRaw) {
        SpendMonth month = SpendMonth.parse(monthRaw);
        SpendMonth previous = month.previous();
        BigDecimal total = money(depenses.sumForParty(partyId, month.start(), month.end()));
        BigDecimal previousTotal = money(depenses.sumForParty(partyId, previous.start(), previous.end()));
        Map<String, BigDecimal> sums = depenses.sumByCategory(partyId, month.start(), month.end()).stream()
                .collect(Collectors.toMap(row -> String.valueOf(row[0]), row -> money((BigDecimal) row[1])));
        List<CategoryAmount> lines = new ArrayList<>();
        for (CategorieDepense category : categories.findAllByOrderByPriorityAsc()) {
            lines.add(new CategoryAmount(
                    category.getCode(),
                    category.getLabel(),
                    category.getIcon(),
                    sums.getOrDefault(category.getCode(), BigDecimal.ZERO)));
        }
        List<MerchantAmount> merchants = depenses.topMerchants(partyId, month.start(), month.end()).stream()
                .limit(5)
                .map(row -> new MerchantAmount(String.valueOf(row[0]), money((BigDecimal) row[1])))
                .toList();
        return new InsightsView(month.toString(), total, previousTotal, change(total, previousTotal), lines, merchants);
    }

    private static int change(BigDecimal current, BigDecimal previous) {
        if (previous.signum() <= 0) {
            return 0;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record CategoryAmount(String code, String label, String icon, BigDecimal amount) {
    }

    public record MerchantAmount(String name, BigDecimal amount) {
    }

    public record InsightsView(
            String month,
            BigDecimal total,
            BigDecimal previousTotal,
            int changePercent,
            List<CategoryAmount> categories,
            List<MerchantAmount> merchants
    ) {
    }
}
