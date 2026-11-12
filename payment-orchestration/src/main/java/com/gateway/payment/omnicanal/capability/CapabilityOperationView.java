package com.gateway.payment.omnicanal.capability;

public class CapabilityOperationView {
    private String capabilityCode;
    private String typeMethode;
    private String operationCode;
    private boolean actif;

    public CapabilityOperationView() {}

    public CapabilityOperationView(String capabilityCode, String typeMethode, String operationCode, boolean actif) {
        this.capabilityCode = capabilityCode;
        this.typeMethode = typeMethode;
        this.operationCode = operationCode;
        this.actif = actif;
    }

    public String getCapabilityCode() { return capabilityCode; }
    public void setCapabilityCode(String capabilityCode) { this.capabilityCode = capabilityCode; }
    public String getTypeMethode() { return typeMethode; }
    public void setTypeMethode(String typeMethode) { this.typeMethode = typeMethode; }
    public String getOperationCode() { return operationCode; }
    public void setOperationCode(String operationCode) { this.operationCode = operationCode; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
