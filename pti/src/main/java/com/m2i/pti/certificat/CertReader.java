package com.m2i.pti.certificat;


import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CertReader {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CertReader.class);



    public static PrivateKey getPrivateKey(String keyPath) {
        DataInputStream dis = null;
        try {
            // read private key DER file
            File privKeyFile = new File(keyPath);
            //dis = new DataInputStream(new FileInputStream(keyPath));
            dis = new DataInputStream(new FileInputStream(keyPath));
            byte[] privKeyBytes = new byte[(int)privKeyFile.length()];
            dis.read(privKeyBytes);
            dis.close();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privKeyBytes);
            RSAPrivateKey privKey = (RSAPrivateKey) keyFactory.generatePrivate(privSpec);
            return privKey;
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            try {
                dis.close();
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
        }
        return null;
    }

}
