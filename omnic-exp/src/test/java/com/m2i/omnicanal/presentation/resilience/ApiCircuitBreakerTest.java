package com.m2i.omnicanal.presentation.resilience;

import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.outils.restclient.constants.ErreurConstants;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiCircuitBreakerTest {

    @Test
    void opensOneApiWithoutBlockingAnother() {
        ApiCircuitBreaker breaker = breaker();
        Api down = api("0007");
        Api other = api("0114");

        for (int i = 0; i < 20; i++) {
            ApiCircuitBreaker.Guard guard = breaker.guard(down);
            assertFalse(guard.rejected());
            guard.record(failure(ErreurConstants.RESSOURCE_ACCESS_ERROR));
        }

        assertTrue(breaker.guard(down).rejected());
        assertFalse(breaker.guard(other).rejected());
    }

    @Test
    void clientErrorsDoNotOpenTheCircuit() {
        ApiCircuitBreaker breaker = breaker();
        Api api = api("0007");

        for (int i = 0; i < 20; i++) {
            ApiCircuitBreaker.Guard guard = breaker.guard(api);
            assertFalse(guard.rejected());
            guard.record(failure(ErreurConstants.HTTP_CLIENT_STATUS_ERROR));
        }

        assertFalse(breaker.guard(api).rejected());
    }

    private static ApiCircuitBreaker breaker() {
        ApiResilienceProperties properties = new ApiResilienceProperties();
        properties.setFailureThreshold(20);
        properties.setFailureRateThreshold(50f);
        properties.setOpenDurationMs(60_000L);
        properties.setHalfOpenPermittedCalls(3);
        return new ApiCircuitBreaker(properties, (io.micrometer.core.instrument.MeterRegistry) null);
    }

    private static Api api(String code) {
        Api api = new Api();
        api.setCodeApi(code);
        return api;
    }

    private static ResultMessage failure(int code) {
        ResultMessage result = new ResultMessage();
        result.setErrCode(code);
        return result;
    }
}
