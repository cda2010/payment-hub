package com.m2i.pti.repository;


import com.m2i.pti.domain.SelectComponentData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelectComponentDataRepository extends JpaRepository<SelectComponentData, Long> {

}
