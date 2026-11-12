package com.m2i.pti.certificat;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import jakarta.transaction.Transactional;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Transactional
public class Encryption {

    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes(UTF_8));

        return Base64.getEncoder().encodeToString(cipherText);
    }

    public static String decrypt(String cipherText, PrivateKey privateKey){
        byte[] bytes = Base64.getDecoder().decode(cipherText);
        String str;
        try {
            Cipher decriptCipher = Cipher.getInstance("RSA");
            decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            str = new String(decriptCipher.doFinal(bytes), UTF_8);
        } catch (Exception e) {
            str = "";
        }

        return str;
    }
}
