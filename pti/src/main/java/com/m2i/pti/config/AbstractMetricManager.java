/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.config;
import org.springframework.beans.factory.annotation.Autowired;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.collections4.map.HashedMap;
/**
 *
 * @author cda
 */
public class AbstractMetricManager {
 
    private MeterRegistry        meterRegistry;
    private Map<String, Counter> counters;

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    public Map<String, Counter> getCounters() {
        return counters;
    }
    
     public AbstractMetricManager()
    {
        this.counters = new HashedMap<>();
    }
    protected String getCounterUniqueName(String counterName, String... params)
    {
        return String.format("%s-%s", counterName, String.join("-", params));
    }
    
    protected Counter registerCounter(String counterName, String... params)
    {
        Counter  counter          = this.meterRegistry.counter(counterName, params);
        String[] couterNameParams = IntStream.range(0, params.length).filter(idx -> (idx % 2) == 0).mapToObj(idx -> params[idx]).collect(Collectors.toList())
                .toArray(new String[0]);
        this.counters.put(getCounterUniqueName(counterName, couterNameParams), counter);
        return counter;
    }
}
