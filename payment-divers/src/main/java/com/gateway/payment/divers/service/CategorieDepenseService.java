package com.gateway.payment.divers.service;

import com.gateway.payment.divers.domain.CategorieDepense;
import com.gateway.payment.divers.repository.CategorieDepenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategorieDepenseService {

    private final CategorieDepenseRepository categories;

    public CategorieDepenseService(CategorieDepenseRepository categories) {
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<CategorieDepense> list() {
        return categories.findAllByOrderByPriorityAsc();
    }
}
