package com.gateway.payment.payment.repository;

import java.util.List;

public interface ProviderExecutionSummary {
    String getProviderCode();
    String getOperationCode();
    long getSuccessCount();
    long getFailureCount();
    Double getAvgDurationMs();
}
