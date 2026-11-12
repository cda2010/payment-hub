package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.domain.enumeration.TypePersonne;
import com.m2i.pti.service.dto.PersonneDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NamedNativeQueries({
     @NamedNativeQuery(name = "Personne.findPersonneByPhoneAndCodePaysAndActivated",
            query = "select id, type_personne typePersonne, branche_activite_id brancheActivite , "
            + "personne_morale_id personneMorale, personne_physique_id personnePhysique, "
            + "code_telephone codePaysTelephone ,telephone ,  pays_compte_id paysCompte, email, adresse from  personne   "
            + " where  telephone=:telephone and code_telephone=:codeIndicatif and activated=true",
            resultSetMapping = "Mapping.PersonneDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),
    @NamedNativeQuery(name = "Personne.findPersonneByPhoneAndCodePays",
            query = "select id, type_personne typePersonne, branche_activite_id brancheActivite , "
            + "personne_morale_id personneMorale, personne_physique_id personnePhysique, "
            + "code_telephone codePaysTelephone ,telephone ,  pays_compte_id paysCompte, email, adresse from  personne   "
            + " where  telephone=:telephone and code_telephone=:codeIndicatif",
            resultSetMapping = "Mapping.PersonneDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,
    @NamedNativeQuery(name = "Personne.findOne",
            query = "select p.id, p.type_personne typePersonne, p.branche_activite_id brancheActivite , "
            + "p.personne_morale_id personneMorale, p.personne_physique_id personnePhysique, "
            + "p.code_telephone codePaysTelephone ,p.telephone telephone ,  p.pays_compte_id paysCompte, p.email email, p.adresse adresse from  personne p where  p.id=:id ",
            resultSetMapping = "Mapping.PersonneDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

    @NamedNativeQuery(name = "Personne.findBackofficeOne",
                query = "select p.id, p.type_personne typePersonne, p.branche_activite_id brancheActivite , "
                        + "p.personne_morale_id personneMorale, p.personne_physique_id personnePhysique, "
                        + "p.code_telephone codePaysTelephone ,p.telephone telephone,  p.pays_compte_id paysCompte, p.email email, p.adresse adresse,p.activated activated,p.modifiable modifiable,p.client_salaire clientSalaire,l.id localiteId,l.nom_commune localiteCommune,l.nom_province localiteProvince,l.nom_region localiteRegion,l.nom_ville localiteVille,l.quartier localiteQuartier,l.secteur localiteSecteur from  personne p left join localite l on p.localite_id=l.id   "
                        + " where  p.id=:id",
                resultSetMapping = "Mapping.PersonneDTONew"),

    @NamedNativeQuery(name = "Personne.findOneByBeneficiaireId",
                query = "select p.id, p.type_personne typePersonne, p.branche_activite_id brancheActivite , "
                        + "p.personne_morale_id personneMorale, p.personne_physique_id personnePhysique, "
                        + "p.code_telephone codePaysTelephone ,p.telephone telephone ,  p.pays_compte_id paysCompte, p.email email, p.adresse adresse from  personne p inner join beneficiaire b on b.personne_id=p.id where  b.id=:id ",
                resultSetMapping = "Mapping.PersonneDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

    @NamedNativeQuery(name = "Personne.findAllOptimiser",
                query = "select p.id, p.type_personne typePersonne, p.branche_activite_id brancheActivite , "
                        + "p.personne_morale_id personneMorale, p.personne_physique_id personnePhysique, "
                        + "p.code_telephone codePaysTelephone ,p.telephone telephone ,  p.pays_compte_id paysCompte, p.email email, p.adresse adresse from  personne p where (:hashkey is null or :hashkey='' or p.key_hash=CAST(:hashkey AS TEXT) ) ",
                resultSetMapping = "Mapping.PersonneDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),
    
    @NamedNativeQuery(name = "Personne.findDoublonByTypePiece",
                query = "SELECT p.id, p.key_hash as keyHash, pe.reference_piece as  refPiece, tp.libelle_type_piece as typePiece,p.type_personne as typePersonne,p.telephone,p.adresse" +
                        " FROM personne p JOIN piece pe ON pe.titulaire_id  = p.id JOIN type_piece tp ON tp.id = pe.type_piece_id WHERE pe.type_piece_id=:typePieceId" +
                        " AND pe.reference_piece IN (SELECT reference_piece FROM piece WHERE type_piece_id =:typePieceId and reference_piece is not null and reference_piece !='NULL' and reference_piece !='' GROUP BY reference_piece HAVING COUNT(*) > 1) limit :taille offset :page ",
                resultSetMapping = "Mapping.PersonneDoublonP",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

        @NamedNativeQuery(name = "Personne.findDoublonByHash",
                query = "SELECT p.id, p.key_hash as keyHash,type_personne as typePersonne,telephone,adresse" +
                        " FROM personne p WHERE p.key_hash IN ( SELECT key_hash FROM personne WHERE key_hash IS NOT NULL GROUP BY key_hash HAVING COUNT(*) > 1) limit :taille offset :page",
                resultSetMapping = "Mapping.PersonneDoublonH",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

        @NamedNativeQuery(name = "Personne.findDoublonByTypePieceAndHash",
                query = "SELECT p.id as id, p.key_hash as keyHash,pe.reference_piece as refPiece,tp.libelle_type_piece AS typePiece,p.type_personne as typePersonne,p.telephone,p.adresse" +
                        " FROM personne p JOIN piece pe ON pe.titulaire_id = p.id JOIN type_piece tp ON tp.id = pe.type_piece_id WHERE pe.type_piece_id=:typePieceId AND p.key_hash IS NOT NULL" +
                        " AND (pe.type_piece_id, pe.reference_piece, p.key_hash) IN ( SELECT pe2.type_piece_id, pe2.reference_piece, p2.key_hash FROM personne p2 JOIN piece pe2 ON pe2.titulaire_id = p2.id WHERE pe2.type_piece_id=:typePieceId AND p2.key_hash IS NOT NULL GROUP BY pe2.type_piece_id, pe2.reference_piece, p2.key_hash HAVING COUNT(*) > 1 ) limit :taille offset :page",
                resultSetMapping = "Mapping.PersonneDoublonP",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),


     @NamedNativeQuery(name = "Personne.findAllByTpePieceAndRefPiece",
                query = "select p.id, p.type_personne typePersonne, p.branche_activite_id brancheActivite , "
                        + "p.personne_morale_id personneMorale, p.personne_physique_id personnePhysique, "
                        + "p.code_telephone codePaysTelephone ,p.telephone telephone ,  p.pays_compte_id paysCompte, p.email email, p.adresse adresse from  personne p,piece pi,type_piece tp" +
                        " where pi.titulaire_id=p.id and pi.type_piece_id=tp.id" +
                        " and (:codeTypePiece is null or tp.code_type_piece= CAST (:codeTypePiece AS TEXT) )" +
                        " and (:referencePiece  is null or pi.reference_piece=CAST (:referencePiece AS TEXT) )" +
                        "and (:hash is null or p.key_hash=CAST (:hash AS TEXT) ) ",
                resultSetMapping = "Mapping.PersonneDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),


})

@SqlResultSetMappings({
        @SqlResultSetMapping(name = "Mapping.PersonneDTO",
                classes = @ConstructorResult(targetClass = PersonneDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class)
                                ,
                                @ColumnResult(name = "typePersonne", type = String.class)
                                ,
                                @ColumnResult(name = "brancheActivite", type = Long.class)
                                ,
                                @ColumnResult(name = "personneMorale", type = Long.class)
                                ,
                                @ColumnResult(name = "personnePhysique", type = Long.class)
                                ,
                                @ColumnResult(name = "codePaysTelephone", type = String.class)
                                ,
                                @ColumnResult(name = "telephone", type = String.class)
                                ,
                                @ColumnResult(name = "paysCompte", type = Long.class)
                                ,

                                @ColumnResult(name = "email", type = String.class)
                                ,
                                @ColumnResult(name = "adresse", type = String.class)

                        })),
        @SqlResultSetMapping(name = "Mapping.PersonneDoublonP",
                classes = @ConstructorResult(targetClass = PersonneDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class)
                                ,
                                @ColumnResult(name = "keyHash", type = String.class)
                                ,
                                @ColumnResult(name = "refPiece", type = String.class)
                                ,
                                @ColumnResult(name = "typePiece", type = String.class),
                                @ColumnResult(name = "typePersonne", type = String.class),
                                @ColumnResult(name = "telephone", type = String.class),
                                @ColumnResult(name = "adresse", type = String.class),

                        })),
        @SqlResultSetMapping(name = "Mapping.PersonneDoublonH",
                classes = @ConstructorResult(targetClass = PersonneDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class)
                                ,
                                @ColumnResult(name = "keyHash", type = String.class),
                                @ColumnResult(name = "typePersonne", type = String.class),
                                @ColumnResult(name = "telephone", type = String.class),
                                @ColumnResult(name = "adresse", type = String.class),
                        })),            

        @SqlResultSetMapping(name = "Mapping.PersonneDTONew",
                classes = @ConstructorResult(targetClass = PersonneDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class)
                                ,
                                @ColumnResult(name = "typePersonne", type = String.class)
                                ,
                                @ColumnResult(name = "brancheActivite", type = Long.class)
                                ,
                                @ColumnResult(name = "personneMorale", type = Long.class)
                                ,
                                @ColumnResult(name = "personnePhysique", type = Long.class)
                                ,
                                @ColumnResult(name = "codePaysTelephone", type = String.class)
                                ,
                                @ColumnResult(name = "telephone", type = String.class)
                                ,
                                @ColumnResult(name = "paysCompte", type = Long.class)
                                ,

                                @ColumnResult(name = "email", type = String.class)
                                ,
                                @ColumnResult(name = "adresse", type = String.class),
                                @ColumnResult(name = "activated", type = Boolean.class),
                                @ColumnResult(name = "modifiable", type = Boolean.class),
                                @ColumnResult(name = "clientSalaire", type = BigDecimal.class),
                                @ColumnResult(name = "localiteId", type = Long.class),
                                @ColumnResult(name = "localiteCommune", type = String.class),
                                @ColumnResult(name = "localiteProvince", type = String.class),
                                @ColumnResult(name = "localiteRegion", type = String.class),
                                @ColumnResult(name = "localiteVille", type = String.class),
                                @ColumnResult(name = "localiteQuartier", type = String.class),
                                @ColumnResult(name = "localiteSecteur", type = String.class),



                        }))
})



/**
 * A Personne.
 */
@Entity
@Table(name = "personne")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(value = {EntityAuditEventListener.class})
@Cacheable(value = false)
public class Personne extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_personne")
    private TypePersonne typePersonne;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "code_telephone")
    private String codePaysTelephone;
    @Column
    private boolean activated = true;

    @Column(name = "email")
    private String email;

    @Column(name = "adresse")
    private String adresse;
    @Column(name = "key_hash")
    private String keyHash;

    @Column(name = "modifiable")
    private boolean modifiable = false;

    @Column(name = "lien_absolu")
    private Long lienAbsolu;

    @Column(name = "client_salaire")
    private BigDecimal clientSalaire;
    
    @Column(name="memo")
    private String memo;

    @ManyToOne
    private UniteOrganisation paysCompte;

    @OneToOne
    @JoinColumn(unique = true)
    private PersonnePhysique personnePhysique;

    @OneToOne
    @JoinColumn(unique = true)
    private PersonneMorale personneMorale;

    @OneToMany(mappedBy = "titulaire",fetch = FetchType.LAZY)
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Piece> pieces = new HashSet<>();

   

  /*  @OneToMany(mappedBy = "personne",fetch = FetchType.LAZY)
    @OneToMany(mappedBy = "personne",fetch = FetchType.LAZY)

    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Beneficiaire> beneficiaires = new HashSet<>();
*/
    /*@OneToMany(mappedBy = "representant",fetch = FetchType.LAZY)
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<UniteOrganisation> uniteOrganisations = new HashSet<>();
*/
    @ManyToOne
    private BrancheActivite brancheActivite;

    @OneToOne
    private Localite localite;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypePersonne getTypePersonne() {
        return typePersonne;
    }

    public Personne typePersonne(TypePersonne typePersonne) {
        this.typePersonne = typePersonne;
        return this;
    }

    public void setTypePersonne(TypePersonne typePersonne) {
        this.typePersonne = typePersonne;
    }

    public String getTelephone() {
        return telephone;
    }

    public Personne telephone(String telephone) {
        this.telephone = telephone;
        return this;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCodePaysTelephone() {
        return codePaysTelephone;
    }

    public void setCodePaysTelephone(String codePaysTelephone) {
        this.codePaysTelephone = codePaysTelephone;
    }

    public PersonnePhysique getPersonnePhysique() {
        return personnePhysique;
    }

    public Personne personnePhysique(PersonnePhysique personnePhysique) {
        this.personnePhysique = personnePhysique;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Personne email(String email) {
        this.email = email;
        return this;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Personne adresse(String adresse) {
        this.adresse = adresse;
        return this;
    }

    public void setPersonnePhysique(PersonnePhysique personnePhysique) {
        this.personnePhysique = personnePhysique;
    }

    public PersonneMorale getPersonneMorale() {
        return personneMorale;
    }

    public Personne personneMorale(PersonneMorale personneMorale) {
        this.personneMorale = personneMorale;
        return this;
    }

    public void setPersonneMorale(PersonneMorale personneMorale) {
        this.personneMorale = personneMorale;
    }

    public Set<Piece> getPieces() {
        return pieces;
    }

    public BigDecimal getClientSalaire() {
        return clientSalaire;
    }

    public void setClientSalaire(BigDecimal clientSalaire) {
        this.clientSalaire = clientSalaire;
    }

    public Personne pieces(Set<Piece> pieces) {
        this.pieces = pieces;
        return this;
    }

    public Personne addPiece(Piece piece) {
        this.pieces.add(piece);
        piece.setTitulaire(this);
        return this;
    }

    public Personne removePiece(Piece piece) {
        this.pieces.remove(piece);
        piece.setTitulaire(null);
        return this;
    }

    public BrancheActivite getBrancheActivite() {
        return brancheActivite;
    }

    public void setBrancheActivite(BrancheActivite brancheActivite) {
        this.brancheActivite = brancheActivite;
    }

    public Personne brancheActivite(BrancheActivite brancheActivite) {
        this.brancheActivite = brancheActivite;
        return this;
    }

    public void setPieces(Set<Piece> pieces) {
        this.pieces = pieces;
    }

  

    public UniteOrganisation getPaysCompte() {
        return paysCompte;
    }

    public void setPaysCompte(UniteOrganisation paysCompte) {
        this.paysCompte = paysCompte;
    }

    

   /* public Set<Beneficiaire> getBeneficiaires() {
        return beneficiaires;
    }

    public Personne beneficiaires(Set<Beneficiaire> beneficiaires) {
        this.beneficiaires = beneficiaires;
        return this;
    }

    public Personne addBeneficiaire(Beneficiaire beneficiaire) {
        this.beneficiaires.add(beneficiaire);
        beneficiaire.setPersonne(this);
        return this;
    }

    public Personne removeBeneficiaire(Beneficiaire beneficiaire) {
        this.beneficiaires.remove(beneficiaire);
        beneficiaire.setPersonne(null);
        return this;
    }

    public void setBeneficiaires(Set<Beneficiaire> beneficiaires) {
        this.beneficiaires = beneficiaires;
    }

    public Set<UniteOrganisation> getUniteOrganisations() {
        return uniteOrganisations;
    }

    public Personne uniteOrganisations(Set<UniteOrganisation> uniteOrganisations) {
        this.uniteOrganisations = uniteOrganisations;
        return this;
    }

    public Personne addUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisations.add(uniteOrganisation);
        uniteOrganisation.setRepresentant(this);
        return this;
    }

    public Personne removeUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisations.remove(uniteOrganisation);
        uniteOrganisation.setRepresentant(null);
        return this;
    }

    public void setUniteOrganisations(Set<UniteOrganisation> uniteOrganisations) {
        this.uniteOrganisations = uniteOrganisations;
    }*/
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public String getKeyHash() {
        return keyHash;
    }

    public boolean isModifiable() {
        return modifiable;
    }

    public void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
    }

    public Long getLienAbsolu() {
        return lienAbsolu;
    }

    public void setLienAbsolu(Long lienAbsolu) {
        this.lienAbsolu = lienAbsolu;
    }

    public Localite getLocalite() {
        return localite;
    }

    public void setLocalite(Localite localite) {
        this.localite = localite;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Personne personne = (Personne) o;
        if (personne.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), personne.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Personne{"
                + "id=" + getId()
                + ", typePersonne='" + getTypePersonne() + "'"
                + ", telephone='" + getTelephone() + "'"
                + ", adresse='" + getAdresse() + "'"
                + "}";
    }
}
