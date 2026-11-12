package com.m2i.pti.domain;

import java.util.Objects;
import java.io.Serializable;

import jakarta.persistence.*;

import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.service.dto.ClientDTO;
import com.m2i.pti.service.dto.DetailsLotDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


@SqlResultSetMapping(name = "Mapping.DetailsLotDTO",
        classes = @ConstructorResult(targetClass = DetailsLotDTO.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                        @ColumnResult(name = "lotId", type = Long.class)
                        ,
                        @ColumnResult(name = "clientId", type = Long.class)
                        ,@ColumnResult(name = "ligneChargementFichierId", type = Long.class)


        }))
@NamedNativeQueries({
        @NamedNativeQuery(name = "DetailsLot.findDetailsLotByLotIdOptimiser",
                query = "select d.id, c.id as clientId,l.id as lotId,lc.id as ligneChargementFichierId"
                        + " FROM details_lot d"
                        + " LEFT JOIN client c on d.client_id=c.id INNER JOIN lot l on d.lot_id=l.id"
                        + " LEFT JOIN ligne_chargement_fichier lc  on d.ligne_chargement_fichier_id=lc.id"
                        + " WHERE ("
                        + "  d.lot_id=:lotId) order by d.id desc ",
                resultSetMapping = "Mapping.DetailsLotDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})

})
@Entity
@Table(name="details_lot")
@Cacheable(value = true)
@EntityListeners(value = {EntityAuditEventListener.class})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DetailsLot extends AbstractAuditingEntity implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Lot lot;

    @OneToOne
    private Client client;
    
    @OneToOne
    private LigneChargementFichier ligneChargementFichier;

    private byte[] qrcode;

    public DetailsLot() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lot getLot() {
        return lot;
    }

    public void setLot(Lot lot) {
        this.lot = lot;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public byte[] getQrCode(){
        return qrcode;
    }

    public void setQrCode(byte[] qrcode){
        this.qrcode = qrcode;
    }

    public LigneChargementFichier getLigneChargementFichier() {
        return ligneChargementFichier;
    }

    public void setLigneChargementFichier(LigneChargementFichier ligneChargementFichier) {
        this.ligneChargementFichier = ligneChargementFichier;
    }
    

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DetailsLot dLot = (DetailsLot) o;
        if (dLot.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), dLot.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "DetailsLot{" +
                "id=" + getId() +
                "}";
    }
}
