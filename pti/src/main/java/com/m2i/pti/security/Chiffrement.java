/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.security;

import java.io.InputStream;        


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 *
 * @author cda
 */
public final  class Chiffrement {

    public static String hashFile(InputStream file) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;
            while ((bytesCount = file.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            file.close();
            byte[] bytes = digest.digest();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static String hashString(String originalString) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(
                    originalString.getBytes(StandardCharsets.UTF_8));
             for (int i = 0; i < encodedhash.length; i++) {
                sb.append(Integer.toString((encodedhash[i] & 0xff) + 0x100, 16).substring(1));
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

}
