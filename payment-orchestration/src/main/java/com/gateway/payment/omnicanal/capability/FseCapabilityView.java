package com.gateway.payment.omnicanal.capability;

import java.util.ArrayList;
import java.util.List;

public class FseCapabilityView {
    private String codeFse;
    private String providerCode;
    private String providerLibelle;
    private List<CapabilityOperationView> operations = new ArrayList<>();

    public String getCodeFse() { return codeFse; }
    public void setCodeFse(String codeFse) { this.codeFse = codeFse; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderLibelle() { return providerLibelle; }
    public void setProviderLibelle(String providerLibelle) { this.providerLibelle = providerLibelle; }
    public List<CapabilityOperationView> getOperations() { return operations; }
    public void setOperations(List<CapabilityOperationView> operations) {
        this.operations = operations != null ? operations : new ArrayList<>();
    }
}
