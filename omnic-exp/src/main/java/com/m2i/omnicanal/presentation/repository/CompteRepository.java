package com.m2i.omnicanal.presentation.repository;



import com.m2i.omnicanal.model.Agence;
import com.m2i.omnicanal.model.Compte;
import com.m2i.omnicanal.model.ServiceTraitement;
import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.presentation.domain.AgenceFse;
import com.m2i.omnicanal.presentation.domain.CompteFse;
import com.m2i.omnicanal.presentation.domain.FournisseurFSE;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Compte entity.
 */
@SuppressWarnings("unused")
public interface CompteRepository extends EntityRepository<CompteFse,Long> {
@Query("select c from Compte c where c.relation.fseSource=?1 and c.relation.fseDestination=?2 and c.relation.service=?3")
    public List<Compte> findByFournisseurService(FournisseurFSE fseSource, FournisseurFSE fseDest, TypeService service);
    @Query("select c from Compte c where  (?1 is null or c.agence = ?1)  and  c.fseTitulaire.codeFse=?2 and c.typeCompte.codeParametre=?3 and  COALESCE(c.codeUoTitulaire,?4)=?4  and  COALESCE(c.service.id,?5)=?5")
    public CompteFse findCompteFse(Agence agence,String codeFseTitulaire, String codeParametre, String codeUoTitulaire, Long codeTypeService);
 @Query("select c from Compte c where c.typeCompte.codeParametre=?1 and (?2 is null or c.agence = ?2)  and    COALESCE(c.service.id,?3)=?3 and  COALESCE(c.relation.id,?4)=?4 and   c.fseTeneur.codeFse=?5")
    public CompteFse findCompteFse(String codeParametre,Agence agence, Long typeServiceId, Long relationId, String codeFseTeneur);
 @Query("select c from Compte c where   c.fseTitulaire.codeFse=?1 and c.typeCompte.codeParametre=?2 ")
    public CompteFse findCompteByEtiquetteAndFse(String codeFse, String etiquette);

}
