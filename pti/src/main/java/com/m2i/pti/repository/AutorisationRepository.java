package com.m2i.pti.repository;

import com.m2i.pti.domain.Autorisation;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.service.dto.AutorisationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;


/**
 * Spring Data JPA repository for the Autorisation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AutorisationRepository extends JpaRepository<Autorisation, Long> {

    Page<Autorisation> findByUniteOrganisation(UniteOrganisation uniteOrganisation, Pageable pageable);

    List<Autorisation> findByUniteOrganisation(UniteOrganisation uniteOrganisation);
    void deleteByUniteOrganisation(UniteOrganisation uniteOrganisation);
    List<Autorisation> findByClientId(Long clientId);
    List<Autorisation> findByTypeServiceIdAndClientId(Long typeServiceId,Long clientId);
    List<Autorisation> findTop1ByTypeServiceIdAndClientId(Long typeServiceId,Long clientId);
    Autorisation findTopByUniteOrganisationAndTypeService(UniteOrganisation uniteOrganisation, TypeService typeService);
    @Query(name = "Autorisation.findByCondition", nativeQuery = true)
    AutorisationDTO findByCondition(@Param("uoId") Long uoId,@Param("typeserviceId") Long typeserviceId);      

}
