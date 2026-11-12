package com.m2i.pti.web.rest.vm;


import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.User;
import com.m2i.pti.service.dto.UserDTO;

import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Set;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;

/**
 * View Model extending the UserDTO, which is meant to be used in the user
 * management UI.
 */
public class ManagedUserVM extends UserDTO {

    public static final int PASSWORD_MIN_LENGTH = 4;

    public static final int PASSWORD_MAX_LENGTH = 100;

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String password;
    public ManagedUserVM() {
        //Empty constructor needed for Jackson.
    }

    public ManagedUserVM(Long id, String login, String password, String firstName, String lastName,
                         String email, boolean activated, String imageUrl, String langKey, String resetKey, String phoneNumber,
                         String createdBy, Instant createdDate, String lastModifiedBy, Instant lastModifiedDate,
                         Set<String> authorities, Long profilId, String profilNom, Long uniteOrganisationId, String uniteOrganisationNom, Long pointVenteId, Integer nbrFailure, ZonedDateTime dateDerniereConnexion,Long clientId, Integer IsBackOffice,String codeIndicatif) {

        super(id, login, firstName, lastName, email, activated, imageUrl, langKey, resetKey, phoneNumber,
                createdBy, createdDate, lastModifiedBy, lastModifiedDate, profilId, profilNom, uniteOrganisationId, uniteOrganisationNom, authorities, pointVenteId, nbrFailure, dateDerniereConnexion, clientId,IsBackOffice,codeIndicatif);
        this.password = password;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "ManagedUserVM{"
                + "} " + super.toString();
    }
}
