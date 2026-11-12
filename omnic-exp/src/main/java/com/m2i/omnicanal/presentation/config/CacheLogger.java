/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.config;
import org.ehcache.event.CacheEvent;

import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cda
 */
public class CacheLogger implements CacheEventListener<Object, Object>{
    private final Logger log = LoggerFactory.getLogger(CacheLogger.class);
    @Override
  public void onEvent(CacheEvent<?, ?> cacheEvent) {
    log.debug("Key: {} | EventType: {} | Old value: {} | New value: {}",
             cacheEvent.getKey(), cacheEvent.getType(), cacheEvent.getOldValue(), cacheEvent.getNewValue());
  }
}

