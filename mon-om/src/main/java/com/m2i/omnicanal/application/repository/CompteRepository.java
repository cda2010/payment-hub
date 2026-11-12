package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.model.Compte;
import com.m2i.omnicanal.model.ServiceTraitement;
import com.m2i.omnicanal.application.domain.CompteFse;
import com.m2i.omnicanal.application.domain.FournisseurFSE;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Spring Data JPA repository for the Compte entity.
 */
@SuppressWarnings("unused")
public interface CompteRepository extends EntityRepository<CompteFse, Long> {

    @Query("select c from Compte c where c.relation.fseSource=?1 and c.relation.fseDestination=?2 and c.relation.service=?3")
    public List<Compte> findByFournisseurService(FournisseurFSE fseSource, FournisseurFSE fseDest, ServiceTraitement service);

    @Query("select c from Compte c where c.relation.id=?1")
    public List<CompteFse> findByRelation(Long idrelation);

    @Query("select c from Compte c where c.fseTitulaire.codeFse=?1 and c.typeCompte.codeParametre=?2 and  COALESCE(c.codeUoTitulaire ,?3)=?3 and c.service.id=?4")
    public CompteFse findCompteFse(String codeFseTitulaire, String codeParametre, String codeUoTitulaire, Long codeTypeService);

    @Query("select c from Compte c where c.id <>?1")
    public List<CompteFse> findCompteMorroir(Long idCompte);

    @Query("select  c from Compte c where c.fseTeneur.id= COALESCE(?1,c.fseTeneur.id)  and  c.fseTitulaire.id= COALESCE(?2,c.fseTitulaire.id) and    c.typeCompte.id= COALESCE(?3,c.typeCompte.id)")
    public Page<CompteFse> findAll(Pageable pageable, Long teneur, Long titulaire, Long type);
}
