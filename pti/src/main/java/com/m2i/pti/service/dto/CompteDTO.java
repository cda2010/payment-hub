package com.m2i.pti.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.enumeration.*;

/**
 * A DTO for the Compte entity.
 */
public class CompteDTO implements Serializable {

    private Long id;

    private String etiquette;

    private String valeurCompte;
    private String libelleCompte;
    private CategorieCompte categorieCompte;
    private ClasseCompte classeCompte;
    private Long comptePincipaleId;

    private FirstCategorieCompte firstCategorieCompte;

    private CompteBanqueDTO compteBanqueDTO;

    private CarteDTO carteDTO;

    private CompteAutreDTO compteAutreDTO;

    private Long compteBanqueId;

    private Long compteAutreId;
    private Long carteId;
    private Long clientId;

    private String codeFse;

    private String clientCodeClient;
    private String numeroCourt;

    private Long typeCompteId;

    private String typeCompteLibelleTypeCompte;

    private Long uniteOrganisationId;

    private String uniteOrganisationLibelleUniteOrganisation;

    private boolean estClient;

    private boolean transversal;

    private boolean estUO;
    private String last4Digit;
    private boolean creerCompte;

    private boolean selectCompte;

    private BigDecimal soldeCompte;
    private EtatCompte etatCompte;
    private StatutCompte statutCompte;
    private String libelleEtiquette;
    private Instant lastModifiedDate;

    private Compte compteDTO;
    private String alias;

    public CompteDTO(Long id, String etiquette, String valeurCompte, String libelleCompte,
            String categorieCompte, String classeCompte, Long comptePincipaleId,
            String firstCategorieCompte, Long compteBanqueId, Long compteAutreId, Long clientId, Long typeCompteId,
            Long uniteOrganisationId, String etatCompte,
            String statutCompte, String numeroCourt, String codeFse) {
        this.id = id;
        this.etiquette = etiquette;
        this.valeurCompte = valeurCompte;
        this.libelleCompte = libelleCompte;

        if (categorieCompte != null) {
            this.categorieCompte = CategorieCompte.valueOf(categorieCompte);
        } else {
            this.categorieCompte = CategorieCompte.AUTRE;
        }

        if (classeCompte != null) {
            this.classeCompte = ClasseCompte.valueOf(classeCompte);
        } else {
            this.classeCompte = ClasseCompte.PRINCIPAL;
        }

        this.comptePincipaleId = comptePincipaleId;
        if (firstCategorieCompte != null) {
            this.firstCategorieCompte = FirstCategorieCompte.valueOf(firstCategorieCompte);
        } else {
            this.firstCategorieCompte = FirstCategorieCompte.COMPTE_INTERNE;
        }

        if (etatCompte != null) {
            this.etatCompte = EtatCompte.valueOf(etatCompte);
        } else {
            this.etatCompte = EtatCompte.OUVERT;
        }

        if (statutCompte != null) {
            this.statutCompte = StatutCompte.valueOf(statutCompte);
        } else {
            this.statutCompte = StatutCompte.ACTIVE;
        }
        this.compteBanqueId = compteBanqueId;
        this.compteAutreId = compteAutreId;
        this.clientId = clientId;
        this.typeCompteId = typeCompteId;
        this.uniteOrganisationId = uniteOrganisationId;

        this.numeroCourt = numeroCourt;

        this.codeFse = codeFse;

    }

    public CompteDTO(Long id, String etiquette, String valeurCompte, String libelleCompte,
            String categorieCompte, 
            Long carteId,Long clientId, Long typeCompteId,
            Long uniteOrganisationId, String etatCompte,
            String statutCompte,  String codeFse, String last4Digit) {
        this.id = id;
        this.etiquette = etiquette;
        this.valeurCompte = valeurCompte;
        this.libelleCompte = libelleCompte;
        
        if (categorieCompte != null) {
            this.categorieCompte = CategorieCompte.valueOf(categorieCompte);
        } else {
            this.categorieCompte = CategorieCompte.AUTRE;
        }

      
        this.carteId = carteId;
        this.comptePincipaleId = comptePincipaleId;
       

        if (etatCompte != null) {
            this.etatCompte = EtatCompte.valueOf(etatCompte);
        } else {
            this.etatCompte = EtatCompte.OUVERT;
        }

        if (statutCompte != null) {
            this.statutCompte = StatutCompte.valueOf(statutCompte);
        } else {
            this.statutCompte = StatutCompte.ACTIVE;
        }
        this.clientId = clientId;
        this.typeCompteId = typeCompteId;
        this.uniteOrganisationId = uniteOrganisationId;
        this.codeFse = codeFse;
        this.last4Digit = last4Digit;
    }

    public CompteDTO(Long id, String etiquette, String valeurCompte, String libelleCompte,
            String categorieCompte, String classeCompte, Long comptePincipaleId,
            String firstCategorieCompte, Long compteBanqueId, Long compteAutreId, Long clientId, Long typeCompteId,
            Long uniteOrganisationId, String etatCompte,
            String statutCompte, String numeroCourt, BigDecimal soldeCompte, String codeFse) {
        this.id = id;
        this.etiquette = etiquette;
        this.valeurCompte = valeurCompte;
        this.libelleCompte = libelleCompte;

        if (categorieCompte != null) {
            this.categorieCompte = CategorieCompte.valueOf(categorieCompte);
        } else {
            this.categorieCompte = CategorieCompte.AUTRE;
        }

        if (classeCompte != null) {
            this.classeCompte = ClasseCompte.valueOf(classeCompte);
        } else {
            this.classeCompte = ClasseCompte.PRINCIPAL;
        }

        this.comptePincipaleId = comptePincipaleId;
        if (firstCategorieCompte != null) {
            this.firstCategorieCompte = FirstCategorieCompte.valueOf(firstCategorieCompte);
        } else {
            this.firstCategorieCompte = FirstCategorieCompte.COMPTE_INTERNE;
        }

        if (etatCompte != null) {
            this.etatCompte = EtatCompte.valueOf(etatCompte);
        } else {
            this.etatCompte = EtatCompte.OUVERT;
        }

        if (statutCompte != null) {
            this.statutCompte = StatutCompte.valueOf(statutCompte);
        } else {
            this.statutCompte = StatutCompte.ACTIVE;
        }
        this.compteBanqueId = compteBanqueId;
        this.compteAutreId = compteAutreId;
        this.clientId = clientId;
        this.typeCompteId = typeCompteId;
        this.uniteOrganisationId = uniteOrganisationId;

        this.numeroCourt = numeroCourt;
        this.soldeCompte = soldeCompte;
        this.codeFse = codeFse;

    }

    public CompteDTO(Long id, String etiquette, String valeurCompte, String libelleCompte,
            String categorieCompte, String classeCompte, Long comptePincipaleId,
            String firstCategorieCompte, Long compteBanqueId, Long compteAutreId, Long clientId, Long typeCompteId,
            Long uniteOrganisationId, String etatCompte,
            String statutCompte, String numeroCourt) {
        this.id = id;
        this.etiquette = etiquette;
        this.valeurCompte = valeurCompte;
        this.libelleCompte = libelleCompte;
        if (categorieCompte != null) {
            this.categorieCompte = CategorieCompte.valueOf(categorieCompte);
        } else {
            this.categorieCompte = CategorieCompte.AUTRE;
        }

        if (classeCompte != null) {
            this.classeCompte = ClasseCompte.valueOf(classeCompte);
        } else {
            this.classeCompte = ClasseCompte.PRINCIPAL;
        }

        this.comptePincipaleId = comptePincipaleId;
        if (firstCategorieCompte != null) {
            this.firstCategorieCompte = FirstCategorieCompte.valueOf(firstCategorieCompte);
        } else {
            this.firstCategorieCompte = FirstCategorieCompte.COMPTE_INTERNE;
        }

        this.compteBanqueId = compteBanqueId;
        this.compteAutreId = compteAutreId;
        this.clientId = clientId;
        this.typeCompteId = typeCompteId;
        this.uniteOrganisationId = uniteOrganisationId;

        if (etatCompte != null) {
            this.etatCompte = EtatCompte.valueOf(etatCompte);
        } else {
            this.etatCompte = EtatCompte.OUVERT;
        }

        if (statutCompte != null) {
            this.statutCompte = StatutCompte.valueOf(statutCompte);
        } else {
            this.statutCompte = StatutCompte.DESACTIVE;
        }
        this.numeroCourt = numeroCourt;

    }

    public CompteDTO(Long id, String etiquette, String valeurCompte, String libelleCompte, String categorieCompte, String classeCompte, Long comptePincipaleId, String firstCategorieCompte, Long compteBanqueId, Long compteAutreId, Long clientId, String clientCodeClient, Long typeCompteId, Long uniteOrganisationId, String etatCompte, String statutCompte, String numeroCourt) {
        this.id = id;
        this.etiquette = etiquette;
        this.valeurCompte = valeurCompte;
        this.libelleCompte = libelleCompte;
        this.categorieCompte = categorieCompte != null ? CategorieCompte.valueOf(categorieCompte) : null;
        this.classeCompte = classeCompte != null ? ClasseCompte.valueOf(classeCompte) : null;
        this.comptePincipaleId = comptePincipaleId;
        this.firstCategorieCompte = firstCategorieCompte != null ? FirstCategorieCompte.valueOf(firstCategorieCompte) : null;
        this.compteBanqueId = compteBanqueId;
        this.compteAutreId = compteAutreId;
        this.clientId = clientId;
        this.typeCompteId = typeCompteId;
        this.uniteOrganisationId = uniteOrganisationId;
        this.etatCompte = etatCompte != null ? EtatCompte.valueOf(etatCompte) : null;
        this.statutCompte = statutCompte != null ? StatutCompte.valueOf(statutCompte) : null;
        this.numeroCourt = numeroCourt;

    }

    public CompteDTO(Long id, String etiquette, String valeurCompte, String libelleCompte, String categorieCompte, String classeCompte, Long comptePincipaleId, String firstCategorieCompte, Long compteBanqueId, Long compteAutreId, Long clientId, Long typeCompteId, Long uniteOrganisationId, String etatCompte, String statutCompte, String numeroCourt, BigDecimal soldeCompte) {
        this.id = id;
        this.etiquette = etiquette;
        this.valeurCompte = valeurCompte;
        this.libelleCompte = libelleCompte;
        this.categorieCompte = categorieCompte != null ? CategorieCompte.valueOf(categorieCompte) : null;
        this.classeCompte = classeCompte != null ? ClasseCompte.valueOf(classeCompte) : null;
        this.comptePincipaleId = comptePincipaleId;
        this.firstCategorieCompte = firstCategorieCompte != null ? FirstCategorieCompte.valueOf(firstCategorieCompte) : null;
        this.compteBanqueId = compteBanqueId;
        this.compteAutreId = compteAutreId;
        this.clientId = clientId;
        this.typeCompteId = typeCompteId;
        this.uniteOrganisationId = uniteOrganisationId;
        this.etatCompte = etatCompte != null ? EtatCompte.valueOf(etatCompte) : null;
        this.statutCompte = statutCompte != null ? StatutCompte.valueOf(statutCompte) : null;
        this.numeroCourt = numeroCourt;
        this.soldeCompte = soldeCompte;

    }

    public CompteDTO(Long id, String etiquette, String valeurCompte) {
        this.id = id;
        this.etiquette = etiquette;
        this.valeurCompte = valeurCompte;
    }

    public CompteDTO(Long id, String etiquette, String valeurCompte, Long comptePincipaleId, Long compteAutreId, Long clientId, Long uniteOrganisationId) {
        this.id = id;
        this.etiquette = etiquette;
        this.valeurCompte = valeurCompte;
        this.comptePincipaleId = comptePincipaleId;
        this.compteAutreId = compteAutreId;
        this.clientId = clientId;
        this.uniteOrganisationId = uniteOrganisationId;
    }

    public CompteDTO(String valeurCompte, Long clientId, String categorieCompte) {
        this.valeurCompte = valeurCompte;
        this.categorieCompte = categorieCompte != null ? CategorieCompte.valueOf(categorieCompte) : null;
        this.clientId = clientId;
    }

    public CompteDTO(Long id, String valeurCompte, Long uniteOrganisationId, String categorieCompte, Instant lastModifiedDate) {
        this.valeurCompte = valeurCompte;
        this.categorieCompte = categorieCompte != null ? CategorieCompte.valueOf(categorieCompte) : null;
        this.uniteOrganisationId = uniteOrganisationId;
        this.lastModifiedDate = lastModifiedDate;
        this.id = id;
    }

    public CompteDTO(String valeurCompte, Long clientId, String categorieCompte, Instant lastModifiedDate) {
        this.valeurCompte = valeurCompte;
        this.categorieCompte = categorieCompte != null ? CategorieCompte.valueOf(categorieCompte) : null;
        this.clientId = clientId;
        this.lastModifiedDate = lastModifiedDate;
    }

    public CompteDTO() {
    }

    public CompteDTO(Long id, String etiquette, String valeurCompte, String libelleCompte, String categorieCompte, String classeCompte, Long comptePincipaleId, String firstCategorieCompte, Long compteBanqueId, Long compteAutreId, Long clientId, String clientCodeClient, String numeroCourt, Long typeCompteId, String typeCompteLibelleTypeCompte, Long uniteOrganisationId, String uniteOrganisationLibelleUniteOrganisation, BigDecimal soldeCompte, String etatCompte, String statutCompte) {
        this.id = id;
        this.etiquette = etiquette;
        this.valeurCompte = valeurCompte;
        this.libelleCompte = libelleCompte;
        this.categorieCompte = categorieCompte != null ? CategorieCompte.valueOf(categorieCompte) : null;
        this.classeCompte = classeCompte != null ? ClasseCompte.valueOf(classeCompte) : null;
        this.comptePincipaleId = comptePincipaleId;
        this.firstCategorieCompte = firstCategorieCompte != null ? FirstCategorieCompte.valueOf(firstCategorieCompte) : null;
        this.compteBanqueId = compteBanqueId;
        this.compteAutreId = compteAutreId;
        this.clientId = clientId;
        this.clientCodeClient = clientCodeClient;
        this.numeroCourt = numeroCourt;
        this.typeCompteId = typeCompteId;
        this.typeCompteLibelleTypeCompte = typeCompteLibelleTypeCompte;
        this.uniteOrganisationId = uniteOrganisationId;
        this.uniteOrganisationLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
        this.soldeCompte = soldeCompte;
        this.etatCompte = etatCompte != null ? EtatCompte.valueOf(etatCompte) : null;
        this.statutCompte = statutCompte != null ? StatutCompte.valueOf(statutCompte) : null;
    }

 

    public CompteDTO(Long id, String etiquette, String valeurCompte, String libelleCompte, String categorieCompte, String classeCompte, Long comptePincipaleId, String firstCategorieCompte, Long compteBanqueId, Long compteAutreId, Long clientId, Long typeCompteId, String typeCompteLibelleTypeCompte, Long uniteOrganisationId, String uniteOrganisationLibelleUniteOrganisation, String etatCompte, String statutCompte, String numeroCourt) {
        this.id = id;
        this.etiquette = etiquette;
        this.valeurCompte = valeurCompte;
        this.libelleCompte = libelleCompte;
        this.categorieCompte = categorieCompte != null ? CategorieCompte.valueOf(categorieCompte) : null;
        this.classeCompte = classeCompte != null ? ClasseCompte.valueOf(classeCompte) : null;
        this.comptePincipaleId = comptePincipaleId;
        this.firstCategorieCompte = firstCategorieCompte != null ? FirstCategorieCompte.valueOf(firstCategorieCompte) : null;
        this.compteBanqueId = compteBanqueId;
        this.compteAutreId = compteAutreId;
        this.clientId = clientId;
        this.typeCompteId = typeCompteId;
        this.typeCompteLibelleTypeCompte = typeCompteLibelleTypeCompte;
        this.uniteOrganisationId = uniteOrganisationId;
        this.uniteOrganisationLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
        this.etatCompte = etatCompte != null ? EtatCompte.valueOf(etatCompte) : null;
        this.statutCompte = statutCompte != null ? StatutCompte.valueOf(statutCompte) : null;
        this.numeroCourt = numeroCourt;
    }

    public CompteDTO(Long id, String etiquette, String valeurCompte, String libelleCompte, String categorieCompte, String classeCompte, Long comptePincipaleId, String firstCategorieCompte, Long compteBanqueId, Long compteAutreId, Long clientId, Long typeCompteId, String typeCompteLibelleTypeCompte, Long uniteOrganisationId, String uniteOrganisationLibelleUniteOrganisation, String etatCompte, String statutCompte, String numeroCourt, String alias) {
        this.id = id;
        this.etiquette = etiquette;
        this.valeurCompte = valeurCompte;
        this.libelleCompte = libelleCompte;
        this.categorieCompte = categorieCompte != null ? CategorieCompte.valueOf(categorieCompte) : null;
        this.classeCompte = classeCompte != null ? ClasseCompte.valueOf(classeCompte) : null;
        this.comptePincipaleId = comptePincipaleId;
        this.firstCategorieCompte = firstCategorieCompte != null ? FirstCategorieCompte.valueOf(firstCategorieCompte) : null;
        this.compteBanqueId = compteBanqueId;
        this.compteAutreId = compteAutreId;
        this.clientId = clientId;
        this.typeCompteId = typeCompteId;
        this.typeCompteLibelleTypeCompte = typeCompteLibelleTypeCompte;
        this.uniteOrganisationId = uniteOrganisationId;
        this.uniteOrganisationLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
        this.etatCompte = etatCompte != null ? EtatCompte.valueOf(etatCompte) : null;
        this.statutCompte = statutCompte != null ? StatutCompte.valueOf(statutCompte) : null;
        this.numeroCourt = numeroCourt;
        this.alias = alias;
    }

    public CompteDTO(Long id, String valeurCompte, Long compteBanqueId, Long compteAutreId, Long clientId, Long uniteOrganisationId) {
        this.id = id;
        this.valeurCompte = valeurCompte;
        this.compteBanqueId = compteBanqueId;
        this.compteAutreId = compteAutreId;
        this.clientId = clientId;
        this.uniteOrganisationId = uniteOrganisationId;
    }

    public CompteDTO(Long id, String etiquette, String valeurCompte, String libelleCompte, String categorieCompte, String classeCompte, Long comptePincipaleId, String firstCategorieCompte, Long compteBanqueId, Long compteAutreId, Long typeCompteId, Long uniteOrganisationId, String etatCompte, String statutCompte, String numeroCourt) {
        this.id = id;
        this.etiquette = etiquette;
        this.valeurCompte = valeurCompte;
        this.libelleCompte = libelleCompte;
        this.categorieCompte = categorieCompte != null ? CategorieCompte.valueOf(categorieCompte) : null;
        this.classeCompte = classeCompte != null ? ClasseCompte.valueOf(classeCompte) : null;
        this.comptePincipaleId = comptePincipaleId;
        this.firstCategorieCompte = firstCategorieCompte != null ? FirstCategorieCompte.valueOf(firstCategorieCompte) : null;
        this.compteBanqueId = compteBanqueId;
        this.compteAutreId = compteAutreId;
        this.typeCompteId = typeCompteId;
        this.uniteOrganisationId = uniteOrganisationId;
        this.etatCompte = etatCompte != null ? EtatCompte.valueOf(etatCompte) : null;
        this.statutCompte = statutCompte != null ? StatutCompte.valueOf(statutCompte) : null;
        this.numeroCourt = numeroCourt;

    }

    public CompteDTO(String valeurCompte, Long clientId, String categorieCompte, String etatCompte, String statutCompte) {
        this.valeurCompte = valeurCompte;
        this.clientId = clientId;
        this.categorieCompte = categorieCompte != null ? CategorieCompte.valueOf(categorieCompte) : null;
        this.etatCompte = etatCompte != null ? EtatCompte.valueOf(etatCompte) : null;
        this.statutCompte = statutCompte != null ? StatutCompte.valueOf(statutCompte) : null;
    }

    public void setNumeroCourt(String numeroCourt) {
        this.numeroCourt = numeroCourt;
    }

    public String getNumeroCourt() {
        return numeroCourt;
    }

    public String getLibelleCompte() {
        return libelleCompte;
    }

    public void setLibelleCompte(String libelleCompte) {
        this.libelleCompte = libelleCompte;
    }

    public BigDecimal getSoldeCompte() {
        return soldeCompte;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public void setSoldeCompte(BigDecimal soldeCompte) {
        this.soldeCompte = soldeCompte;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEtiquette() {
        return etiquette;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    public String getValeurCompte() {
        return valeurCompte;
    }

    public void setValeurCompte(String valeurCompte) {
        this.valeurCompte = valeurCompte;
    }

    public CategorieCompte getCategorieCompte() {
        return categorieCompte;
    }

    public void setCategorieCompte(CategorieCompte categorieCompte) {
        this.categorieCompte = categorieCompte;
    }

    public Long getComptePincipaleId() {
        return comptePincipaleId;
    }

    public void setComptePincipaleId(Long comptePincipaleId) {
        this.comptePincipaleId = comptePincipaleId;
    }

    public FirstCategorieCompte getFirstCategorieCompte() {
        return firstCategorieCompte;
    }

    public void setFirstCategorieCompte(FirstCategorieCompte firstCategorieCompte) {
        this.firstCategorieCompte = firstCategorieCompte;
    }

    public Compte getCompteDTO() {
        return compteDTO;
    }

    public void setCompteDTO(Compte compteDTO) {
        this.compteDTO = compteDTO;
    }

    public CompteBanqueDTO getCompteBanqueDTO() {
        return compteBanqueDTO;
    }

    public void setCompteBanqueDTO(CompteBanqueDTO compteBanqueDTO) {
        this.compteBanqueDTO = compteBanqueDTO;
    }

    public CompteAutreDTO getCompteAutreDTO() {
        return compteAutreDTO;
    }

    public void setCompteAutreDTO(CompteAutreDTO compteAutreDTO) {
        this.compteAutreDTO = compteAutreDTO;
    }

    public Long getCompteBanqueId() {
        return compteBanqueId;
    }

    public void setCompteBanqueId(Long compteBanqueId) {
        this.compteBanqueId = compteBanqueId;
    }

    public Long getCompteAutreId() {
        return compteAutreId;
    }

    public void setCompteAutreId(Long compteAutreId) {
        this.compteAutreId = compteAutreId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientCodeClient() {
        return clientCodeClient;
    }

    public void setClientCodeClient(String clientCodeClient) {
        this.clientCodeClient = clientCodeClient;
    }

    public Long getTypeCompteId() {
        return typeCompteId;
    }

    public void setTypeCompteId(Long typeCompteId) {
        this.typeCompteId = typeCompteId;
    }

    public String getTypeCompteLibelleTypeCompte() {
        return typeCompteLibelleTypeCompte;
    }

    public void setTypeCompteLibelleTypeCompte(String typeCompteLibelleTypeCompte) {
        this.typeCompteLibelleTypeCompte = typeCompteLibelleTypeCompte;
    }

    public Long getUniteOrganisationId() {
        return uniteOrganisationId;
    }

    public void setUniteOrganisationId(Long uniteOrganisationId) {
        this.uniteOrganisationId = uniteOrganisationId;
    }

    public String getUniteOrganisationLibelleUniteOrganisation() {
        return uniteOrganisationLibelleUniteOrganisation;
    }

    public void setUniteOrganisationLibelleUniteOrganisation(String uniteOrganisationLibelleUniteOrganisation) {
        this.uniteOrganisationLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
    }

    public boolean isEstClient() {
        return estClient;
    }

    public void setEstClient(boolean estClient) {
        this.estClient = estClient;
    }

    public boolean isEstUO() {
        return estUO;
    }

    public void setEstUO(boolean estUO) {
        this.estUO = estUO;
    }

    public boolean isCreerCompte() {
        return creerCompte;
    }

    public void setCreerCompte(boolean creerCompte) {
        this.creerCompte = creerCompte;
    }

    public boolean isSelectCompte() {
        return selectCompte;
    }

    public void setSelectCompte(boolean selectCompte) {
        this.selectCompte = selectCompte;
    }

    public boolean isTransversal() {
        return transversal;
    }

    public void setTransversal(boolean transversal) {
        this.transversal = transversal;
    }

    public ClasseCompte getClasseCompte() {
        return classeCompte;
    }

    public void setClasseCompte(ClasseCompte classeCompte) {
        this.classeCompte = classeCompte;
    }

    public EtatCompte getEtatCompte() {
        return etatCompte;
    }

    public void setEtatCompte(EtatCompte etatCompte) {
        this.etatCompte = etatCompte;
    }

    public StatutCompte getStatutCompte() {
        return statutCompte;
    }

    public void setStatutCompte(StatutCompte statutCompte) {
        this.statutCompte = statutCompte;
    }

    public String getLibelleEtiquette() {
        return libelleEtiquette;
    }

    public void setLibelleEtiquette(String libelleEtiquette) {
        this.libelleEtiquette = libelleEtiquette;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public CarteDTO getCarteDTO() {
        return carteDTO;
    }

    public void setCarteDTO(CarteDTO carteDTO) {
        this.carteDTO = carteDTO;
    }

    public Long getCarteId() {
        return carteId;
    }

    public void setCarteId(Long carteId) {
        this.carteId = carteId;
    }

    public String getLast4Digit() {
        return last4Digit;
    }

    public void setLast4Digit(String last4Digit) {
        this.last4Digit = last4Digit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompteDTO compteDTO = (CompteDTO) o;
        if (compteDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), compteDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CompteDTO{"
                + "id=" + getId()
                + ", etiquette='" + getEtiquette() + "'"
                + ", valeurCompte='" + getValeurCompte() + "'"
                + ", categorieCompte='" + getCategorieCompte() + "'"
                + ",classeCompte='" + getClasseCompte() + "'"
                + ",selectCompte='" + getSoldeCompte() + "'"
                + ",creerCompte='" + isCreerCompte() + "'"
                + "}";
    }
}
