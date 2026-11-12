package com.pti.emeraude.gateway.domain;




import com.pti.emeraude.gateway.enumeration.StatutOperation;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class EtatParams {
    Long typeServiceId;
    Long uoConnecteId;
    Long uoSourceId;
    Long uoPaysId;
    ZonedDateTime dateDebut;
    ZonedDateTime dateFin;
    Long canalId;
    Long uoAcquereurId;
    BigDecimal montatOp;
    StatutOperation statutOperation;
    Boolean espPaye;
    Long operationId;
    String format;
    String typeClient;
    String codeOperation;
    String login;
    String typeService;
    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Long getUoSourceId() {
        return uoSourceId;
    }

    public void setUoSourceId(Long uoSourceId) {
        this.uoSourceId = uoSourceId;
    }

    public Boolean getEspPaye() {
        return espPaye;
    }

    public void setEspPaye(Boolean espPaye) {
        this.espPaye = espPaye;
    }

    public Long getCanalId() {
        return canalId;
    }

    public void setCanalId(Long canalId) {
        this.canalId = canalId;
    }

    public Long getUoAcquereurId() {
        return uoAcquereurId;
    }

    public void setUoAcquereurId(Long uoAcquereurId) {
        this.uoAcquereurId = uoAcquereurId;
    }

    public BigDecimal getMontatOp() {
        return montatOp;
    }

    public void setMontatOp(BigDecimal montatOp) {
        this.montatOp = montatOp;
    }

    public StatutOperation getStatutOperation() {
        return statutOperation;
    }

    public void setStatutOperation(StatutOperation statutOperation) {
        this.statutOperation = statutOperation;
    }

    public Long getTypeServiceId() {
        return typeServiceId;
    }

    public void setTypeServiceId(Long typeServiceId) {
        this.typeServiceId = typeServiceId;
    }

    public Long getUoConnecteId() {
        return uoConnecteId;
    }

    public void setUoConnecteId(Long uoConnecteId) {
        this.uoConnecteId = uoConnecteId;
    }

    public Long getUoPaysId() {
        return uoPaysId;
    }

    public void setUoPaysId(Long uoPaysId) {
        this.uoPaysId = uoPaysId;
    }

    public ZonedDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(ZonedDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public ZonedDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(ZonedDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getTypeClient() {
        return typeClient;
    }

    public void setTypeClient(String typeClient) {
        this.typeClient = typeClient;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }
}
