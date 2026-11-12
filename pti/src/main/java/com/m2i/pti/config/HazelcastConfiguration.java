/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 *
 * @author cda
 */
@EnableCaching
@Configuration
public class HazelcastConfiguration {

    @Value("${operation.cache.timeToLive}")
    private int timeToLive;
    @Value("${operation.cache.host}")
    private String host;
    @Value("${operation.cache.name}")
    private String name;   
    @Value("${operation.cache.port}")
    private int port;
    @Value("${operation.cache.statut}")
    private boolean statut;

    @Bean("test")
    public Config hazelcastConfiguration() {
        Config config = new Config();
        config.setClusterName(name);
        config.setInstanceName("coris-hazelcast-server");
        config.getNetworkConfig()
                .setPort(port)
                .setPortAutoIncrement(true);

        var join = config.getNetworkConfig().getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getAutoDetectionConfig().setEnabled(false);
        boolean localMember = host == null
                || "127.0.0.1".equals(host)
                || "localhost".equalsIgnoreCase(host);
        if (statut && !localMember) {
            join.getTcpIpConfig().setEnabled(true).addMember(host);
        } else {
            join.getTcpIpConfig().setEnabled(false);
        }
        // Example Map configuration
        MapConfig mapConfig = new MapConfig("spring-distributed-map");
        mapConfig.setTimeToLiveSeconds(timeToLive);
        mapConfig.setEvictionConfig(new EvictionConfig()
                .setSize(2000)
                .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
                .setEvictionPolicy(EvictionPolicy.LRU));
        config.addMapConfig(new MapConfig("SoldeComptef").setTimeToLiveSeconds(300).setBackupCount(1));
        config.addMapConfig(mapConfig);

        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }
    /*

    @Bean("hazelcastCacheManager")
@Order(2)
    public org.springframework.cache.CacheManager hazelcastCacheManager(
            @Qualifier("hazelcastInstance") HazelcastInstance hz) {
        return new com.hazelcast.spring.cache.HazelcastCacheManager(hz);

    }*/
}
