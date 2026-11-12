package com.m2i.omnicanal.presentation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface EntityRepository<T, ID> extends JpaRepository<T, ID> {

    default T findOne(ID id) {
        return findById(id).orElse(null);
    }
}
