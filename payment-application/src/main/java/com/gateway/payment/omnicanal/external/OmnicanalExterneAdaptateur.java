package com.gateway.payment.omnicanal.external;

import com.gateway.payment.omnicanal.canal.OmnicanalExternePort;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Branche le moteur canal sur le client HTTP de l'omnicanal externe. */
@Component
public class OmnicanalExterneAdaptateur implements OmnicanalExternePort {

    private final AccesOmnicanal client;

    public OmnicanalExterneAdaptateur(AccesOmnicanal client) {
        this.client = client;
    }

    @Override
    public boolean enabled() {
        return client.enabled();
    }

    @Override
    public Map<String, Object> boundary(String codeFse, String codeMethode, String otrParametre) {
        return client.boundary(codeFse, codeMethode, otrParametre);
    }
}
