package com.m2i.pti.repository;

import com.m2i.pti.domain.InfoSupp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InfosuppRepository extends JpaRepository<InfoSupp, Long>{

    @Query(value = "select i.libelle from info_supp i where i.id=:id" , nativeQuery = true)
    String getInfoLibelle(@Param("id") Long id) ;
}