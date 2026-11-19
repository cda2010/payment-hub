package com.gateway.payment.omnicanal.canal;

import java.util.Map;

/**
 * Appel unique vers l'omnicanal externe.
 * Le moteur canal l'utilise ; les services appelants ne contactent pas l'omnicanal eux-mêmes.
 */
public interface OmnicanalExternePort {

    boolean enabled();

    Map<String, Object> boundary(String codeFse, String codeMethode, String otrParametre);
}
