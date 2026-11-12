/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service;

import com.google.common.hash.Hashing;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.m2i.omnicanal.presentation.domain.util.CacheObject;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author cda
 */
@Service
public class HazelcastConfigCacheService<T> {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    public void putValue(String key, CacheObject<T> value) {
        IMap<String, CacheObject<T>> map = hazelcastInstance.getMap("spring-distributed-map");
        map.putIfAbsent(key, value);
    }

    public CacheObject<T> getValue(String key) {
        return (CacheObject<T>) hazelcastInstance.getMap("spring-distributed-map").get(key);
    }

    public void deleteValue(String key) {
        IMap<String, CacheObject<T>> map = hazelcastInstance.getMap("spring-distributed-map");
        map.delete(key); // Supprime sans retourner la valeur (plus performant que remove)
    }

    public CacheObject<T> insertInCache(CacheObject<T> value, String key) {
        CacheObject<T> resultat = getValue(key);
        if (resultat != null) {
            return resultat;
        } else {
            putValue(key, value);
            return value;
        }

    }
}
