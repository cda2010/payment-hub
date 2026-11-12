package com.m2i.pti.service.dto;

import com.m2i.pti.config.Constants;
import com.m2i.pti.domain.Authority;
import com.m2i.pti.domain.User;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO representing a user, with his authorities.
 */
public class UserDTO {

    private Long id;

    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    private String login;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 100)
    private String email;

    private String phoneNumber;

    @Size(max = 256)
    private String imageUrl;

    private boolean activated = false;

    @Size(min = 2, max = 5)
    private String langKey;

    private String resetKey;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private Set<String> authorities;

    private Long profilId;

    private String profilNom;

    private Long uniteOrganisationId;

    private Long pointVenteId;

    private String uniteOrganisationNom;
     private String codeIndicatif;
    private Integer nbrFailure;

    private ZonedDateTime dateDerniereConnexion;
    
      private Long clientId;
 private Integer isBackOffice;
    public UserDTO() {
        // Empty constructor needed for Jackson.
    }

    public UserDTO(User user) {
        this(user.getId(), user.getLogin(), user.getFirstName(), user.getLastName(),
            user.getEmail(), user.getActivated(), user.getImageUrl(), user.getLangKey(),user.getResetKey(),user.getPhoneNumber(),
            user.getCreatedBy(), user.getCreatedDate(), user.getLastModifiedBy(),
                user.getLastModifiedDate(),
                user.getProfil()!=null?user.getProfil().getId():null,
                user.getProfil()!=null?user.getProfil().getNom():null,
                user.getUniteOrganisation()!=null? user.getUniteOrganisation().getId():null,
                user.getUniteOrganisation()!=null?user.getUniteOrganisation().getLibelleUniteOrganisation():null,
            user.getAuthorities().stream().map(Authority::getName)
                .collect(Collectors.toSet()),
            user.getPointVente()!=null? user.getPointVente().getId():null,
            user.getNbrFailure(),
            user.getDateDerniereConnexion(),
            user.getClient()!=null?user.getClient().getId():null,
            user.getIsBackOffice(),user.getCodeIndicatif());
    }

    public UserDTO(Long id, String login, String firstName, String lastName,
                   String email, boolean activated, String imageUrl, String langKey, String resetKey, String phoneNumber,
                   String createdBy, Instant createdDate, String lastModifiedBy, Instant lastModifiedDate, Long profilId, String profilNom, Long uniteOrganisationId, String uniteOrganisationNom,
                   Set<String> authorities, Long pointVenteId, Integer nbrFailure,ZonedDateTime dateDerniereConnexion,Long clientId,Integer isBackOffice ,String codeIndicatif) {
        this.id = id;
        this.login = login;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.activated = activated;
        this.imageUrl = imageUrl;
        this.langKey = langKey;
        this.resetKey = resetKey;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
        this.authorities = authorities;
        this.profilId = profilId;
        this.profilNom = profilNom;
        this.uniteOrganisationId = uniteOrganisationId;
        this.uniteOrganisationNom = uniteOrganisationNom;
        this.pointVenteId=pointVenteId;
        this.nbrFailure=nbrFailure;
         this.clientId=clientId;
        this.dateDerniereConnexion=dateDerniereConnexion;
         this.isBackOffice=isBackOffice;
         this.codeIndicatif=codeIndicatif;
    }
    public UserDTO(Long id, String login, String firstName, String lastName, String email, String phoneNumber, boolean activated, String langKey, String createdBy, Instant createdDate, String lastModifiedBy, Instant lastModifiedDate, Long profilId, String profilNom, Long uniteOrganisationId, Long pointVenteId, String uniteOrganisationNom, String codeIndicatif, Integer nbrFailure, ZonedDateTime dateDerniereConnexion, Integer isBackOffice) {
        this.id = id;
        this.login = login;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.activated = activated;
        this.langKey = langKey;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
        this.profilId = profilId;
        this.profilNom = profilNom;
        this.uniteOrganisationId = uniteOrganisationId;
        this.pointVenteId = pointVenteId;
        this.uniteOrganisationNom = uniteOrganisationNom;
        this.codeIndicatif = codeIndicatif;
        this.nbrFailure = nbrFailure;
        this.dateDerniereConnexion = dateDerniereConnexion;
        this.isBackOffice = isBackOffice;
    }


    public ZonedDateTime getDateDerniereConnexion() {
        return dateDerniereConnexion;
    }

    public void setDateDerniereConnexion(ZonedDateTime dateDerniereConnexion) {
        this.dateDerniereConnexion = dateDerniereConnexion;
    }

    public Integer getNbrFailure() {
        return nbrFailure;
    }

    public void setNbrFailure(Integer nbrFailure) {
        this.nbrFailure = nbrFailure;
    }

    public Long getPointVenteId() {
        return pointVenteId;
    }

    public void setPointVenteId(Long pointVenteId) {
        this.pointVenteId = pointVenteId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isActivated() {
        return activated;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLangKey() {
        return langKey;
    }

    public String getResetKey() {
        return resetKey;
    }

    public void setResetKey(String resetKey) {
        this.resetKey = resetKey;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public Long getProfilId() {
        return profilId;
    }

    public void setProfilId(Long profilId) {
        this.profilId = profilId;
    }

    public String getProfilNom() {
        return profilNom;
    }

    public void setProfilNom(String profilNom) {
        this.profilNom = profilNom;
    }

    public Long getUniteOrganisationId() {
        return uniteOrganisationId;
    }

    public void setUniteOrganisationId(Long uniteOrganisationId) {
        this.uniteOrganisationId = uniteOrganisationId;
    }

    public String getUniteOrganisationNom() {
        return uniteOrganisationNom;
    }

    public void setUniteOrganisationNom(String uniteOrganisationNom) {
        this.uniteOrganisationNom = uniteOrganisationNom;
    }

    public Long getClientId() {
        return clientId;
    }

    public Integer getIsBackOffice() {
        return isBackOffice;
    }

    public void setIsBackOffice(Integer isBackOffice) {
        this.isBackOffice = isBackOffice;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getCodeIndicatif() {
        return codeIndicatif;
    }

    public void setCodeIndicatif(String codeIndicatif) {
        this.codeIndicatif = codeIndicatif;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
            "login='" + login + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            ", activated=" + activated +
            ", langKey='" + langKey + '\'' +
            ", createdBy=" + createdBy +
            ", createdDate=" + createdDate +
            ", lastModifiedBy='" + lastModifiedBy + '\'' +
            ", lastModifiedDate=" + lastModifiedDate +
            ", authorities=" + authorities +
            "}";
    }
}
