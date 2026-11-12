package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.application.domain.MethodeFonction;
import java.util.List;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Methode entity.
 */
@SuppressWarnings("unused")
public interface MethodeRepository extends EntityRepository<MethodeFonction,Long> {
@Query("Select m from MethodeFonction m where m.api.id=?1")
    public List<MethodeFonction> findByApi(Long idApi);
@Query("select coalesce(max(cast(substring(m.codeMethode, 5, 2) as integer)), 0) + 1 from MethodeFonction m where m.api.id = ?1")
    public Integer findMaxByApi(Long id);
@Query("Select m from MethodeFonction m where m.api.fournisseurService.id=?1")
    public List<MethodeFonction> findByApiFse(Long idFse);

}
