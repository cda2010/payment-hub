package com.gateway.payment.edge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EdgeMetricsTest {

    @Test
    void classeLissueSansLeChemin() {
        assertEquals("success", EdgeMetrics.outcome(200, false, false));
        assertEquals("client_error", EdgeMetrics.outcome(401, false, false));
        assertEquals("error", EdgeMetrics.outcome(502, false, false));
        assertEquals("error", EdgeMetrics.outcome(0, true, false));
        assertEquals("cancelled", EdgeMetrics.outcome(200, false, true));
    }
}
