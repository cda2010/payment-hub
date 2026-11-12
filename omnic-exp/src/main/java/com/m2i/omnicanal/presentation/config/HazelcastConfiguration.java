/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author cda
 */
@EnableCaching
@Configuration
public class HazelcastConfiguration {


    @Bean("test")
    public Config hazelcastConfiguration() {
        Config config = new Config();
        //config.setClusterName("spring-cluster");

        config.setInstanceName("spring-distributed-map-test");
        // Network settings
        config.getNetworkConfig()
            .setPort(5701)
            .setPortAutoIncrement(true);

        // Cluster discovery (TCP/IP example)
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig()
            .setEnabled(false)
            .addMember("localhost");
        // Example Map configuration
        MapConfig mapConfig = new MapConfig("spring-distributed-map");
        mapConfig.setTimeToLiveSeconds(3600);
        config.addMapConfig(mapConfig);

        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }
}
