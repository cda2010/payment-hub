package com.gateway.payment.divers.repository;

import com.gateway.payment.divers.domain.CategorieDepense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategorieDepenseRepository extends JpaRepository<CategorieDepense, String> {

    List<CategorieDepense> findAllByOrderByPriorityAsc();
}
