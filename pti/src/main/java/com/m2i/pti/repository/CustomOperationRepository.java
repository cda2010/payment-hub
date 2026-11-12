package com.m2i.pti.repository;

import com.m2i.pti.domain.Operation;
import com.m2i.pti.service.dto.OperationCustom;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;


public class CustomOperationRepository implements OperationCustomRepositoryImpl {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<OperationCustom> findAll() {
        return em.createQuery("SELECT op.montant_op FROM operation op", OperationCustom.class).getResultList();
    }
}
