/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.service;

import com.m2i.pti.config.*;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.m2i.pti.domain.util.CacheObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author cda
 */
@Service
public class HazelcastConfigCacheService<T>  {
    @Autowired
     private  HazelcastInstance hazelcastInstance;

      
  
    public void putValue(String key, CacheObject<T> value) {
        IMap<String, CacheObject<T>> map = hazelcastInstance.getMap("spring-distributed-map");
        map.putIfAbsent(key, value);
    }

    public CacheObject<T> getValue(String key) {
        return (CacheObject<T>)hazelcastInstance.getMap("spring-distributed-map").get(key);
    }
}
