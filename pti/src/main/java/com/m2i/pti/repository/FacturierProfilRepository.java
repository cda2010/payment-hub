package com.m2i.pti.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import com.m2i.pti.domain.FacturierProfils;

public interface FacturierProfilRepository extends JpaRepository<FacturierProfils, Long>{
        @Query(value = "select fact.id as facturier_id, fact.nom_facturier, gpa.id as groupe_profil_acteur_id, gpa.nom, type_acteur FROM facturier_profil fact_pro inner join facturier fact ON fact.id = fact_pro.facturier_id inner join groupe_profil_acteur gpa ON gpa.id = fact_pro.groupe_profil_acteur_id order by fact.id desc", nativeQuery = true)
        List<Object[]> AllFacturierProfils(); 
}
