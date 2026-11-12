/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.m2i.pti.domain.TypeServiceReseauOrganisation;
import com.m2i.pti.service.dto.TypeServiceReseauOrganisationDTO;

/**
 *
 * @author cda
 */
@SuppressWarnings("unused")
@Repository
public interface TypeServiceReseauOrganisationRepository extends JpaRepository<TypeServiceReseauOrganisation, Long> {

    @Query(value = "SELECT typ FROM TypeServiceReseauOrganisation typ where typ.codePays=?1  and typ.typeService.code=?2")
    List<TypeServiceReseauOrganisation> getListTypeServiceReseau(String codePays, String typeServiceCode);

    @Query(name = "TypeServiceReseauOrganisation.findAllTypeSerResOrg", nativeQuery = true)
    List<TypeServiceReseauOrganisationDTO> findAllTypeSerResOrg();
}
