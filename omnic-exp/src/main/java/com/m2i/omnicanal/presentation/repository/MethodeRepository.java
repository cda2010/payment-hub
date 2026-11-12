package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.model.enumeration.TypeMethode;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import java.util.List;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Methode entity.
 */
@SuppressWarnings("unused")
public interface MethodeRepository extends EntityRepository<MethodeFonction,Long> {
@Query("Select m from MethodeFonction m where m.api.id=?1")
    public List<MethodeFonction> findByApi(Long idApi);
    @Query("Select m from MethodeFonction m where m.codeMethode=?1 " )
    public MethodeFonction findByCodeMethode(String codeFonction);
 @Query("Select m from MethodeFonction m where m.typeMethode=?1 and m.api.fournisseurService.codeFse=?2")
    public MethodeFonction findBycodeMethodeAndFSE(TypeMethode codeMethode, String codeFSE);
    
    
    @Query("Select m from MethodeFonction m where m.typeMethode=?1 ")
    public List<MethodeFonction> findBycodeMethode(TypeMethode codeMethode);

}
