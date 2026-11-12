package com.m2i.omnicanal.application.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m2i.omnicanal.model.Api;

/**
 * Spring Data JPA repository for the Api entity.
 */
@SuppressWarnings("unused")
public interface ApiRepository extends EntityRepository<Api,Long> {
    @Query("select coalesce(max(CAST(a.codeApi as int)), 0)+1 from Api a")
    public Integer findMaxCodeApi();
      
    @Query("SELECT a FROM Api a WHERE (:nomApi is null OR a.nomApi = :nomApi) " +
           "AND (:fournisseur is null OR a.fournisseurService.nom = :fournisseur)")
    Page<Api> findByFilters(@Param("nomApi") String nomApi, 
                           @Param("fournisseur") String fournisseur, 
                           Pageable pageable);

    @Query("SELECT DISTINCT api FROM Api api " +
           "LEFT JOIN FETCH api.constitues methodes " +
           "LEFT JOIN FETCH methodes.possedes params " +
           "WHERE api.id = :id")
    Api findOneWithMethodesAndParams(@Param("id") Long id);
}
