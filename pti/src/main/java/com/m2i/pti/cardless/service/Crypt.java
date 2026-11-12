/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.m2i.pti.cardless.service;

import com.gms.security.AESEncryptor;
import com.gms.security.DESEncryptor;
import com.gms.security.DataEncryptor;
import com.gms.security.IEncryptor;
import com.gms.security.TripleDESEncryptor;
import com.gms.utils.common.StringUtil;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author cda
 */

public class Crypt {

    char[] password;
    String key;
    String salt;
    private StringUtil.DataType clearDataType;
    private StringUtil.DataType keyDataType;
    private StringUtil.DataType encryptedDataType;
    private static final Logger LOG = Logger.getLogger(DataEncryptor.class.getName());
    private static final int SINGLE_LENGTH = 16;
    private String str;

    private IEncryptor encryptor;

    public Crypt(String key, String salt, char[] password) {

        //  super(key, salt, password);
        this.clearDataType = StringUtil.DataType.AS_IT_IS_FORMAT;
        this.keyDataType = StringUtil.DataType.AS_IT_IS_FORMAT;
        this.encryptedDataType = StringUtil.DataType.BASE64_FORMAT;
        init(password, salt, key);
    }

    public Crypt(String key, String salt, char[] password, StringUtil.DataType clearDataType, StringUtil.DataType keyDataType, StringUtil.DataType encryptedDataType) {
        // super(key, salt, password, clearDataType, keyDataType, encryptedDataType);
        this.clearDataType = clearDataType;
        this.keyDataType = keyDataType;
        this.encryptedDataType = encryptedDataType;
        init(password, salt, key);
    }

    private void init(char[] password, String salt, String key) {
        AESEncryptor aes = new AESEncryptor();
        aes.setEncryptedDataFormat(StringUtil.DataType.BASE64_FORMAT);
        this.str = aes.decrypt(key(aes, password, salt), key);
        aes = null;

        if ((this.keyDataType == StringUtil.DataType.HEXA_STRING_FORMAT) && (this.str.length() == 32)) {
            this.str += this.str.substring(0, 16);
        }
        this.encryptor = (this.str.length() == 16 ? new DESEncryptor() : new TripleDESEncryptor());
        this.encryptor.setClearDataFormat(this.clearDataType);
        this.encryptor.setEncryptedDataFormat(this.encryptedDataType);
        this.encryptor.setKeyFormat(this.keyDataType);
    }

    private String key(AESEncryptor aes, char[] passPhrase, String dataSalt) {
        String cle = null;
        try {
            cle = aes.generateKey(passPhrase, dataSalt, StringUtil.DataType.BASE64_FORMAT);
        } catch (GeneralSecurityException ex) {
            System.out.println(ex.getMessage());
        }
        return cle;
    }

    public String encrypt(String clearData) {
        return this.encryptor.encrypt(this.str, clearData);
    }

    public String decrypt(String data) {
        return this.encryptor.decrypt(this.str, data);
    }
}
