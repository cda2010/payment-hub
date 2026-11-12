package com.gateway.payment.divers.api;

import com.gateway.payment.divers.domain.CategorieDepense;
import com.gateway.payment.divers.service.CategorieDepenseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/divers/categories-depense")
public class CategorieDepenseController {

    private final CategorieDepenseService categories;

    public CategorieDepenseController(CategorieDepenseService categories) {
        this.categories = categories;
    }

    @GetMapping
    public List<CategorieDepenseView> list() {
        return categories.list().stream().map(CategorieDepenseView::from).toList();
    }

    public record CategorieDepenseView(String code, String label, String icon, int priority) {
        static CategorieDepenseView from(CategorieDepense category) {
            return new CategorieDepenseView(
                    category.getCode(),
                    category.getLabel(),
                    category.getIcon(),
                    category.getPriority());
        }
    }
}
