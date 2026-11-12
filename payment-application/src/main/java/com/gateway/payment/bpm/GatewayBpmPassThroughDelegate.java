package com.gateway.payment.bpm;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/** ServiceTask sans effet (attente / placeholder redirect). */
@Component("gatewayBpmPassThroughDelegate")
public class GatewayBpmPassThroughDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // no-op
    }
}
