/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain.util;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author cda
 */
public class CacheObject<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    T content;
    private String hashKey;

    public CacheObject(T content, String hashKey) {
        this.content = content;
        this.hashKey = hashKey;

    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

}
