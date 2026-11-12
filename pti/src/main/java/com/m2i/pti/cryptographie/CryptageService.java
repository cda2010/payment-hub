package com.m2i.pti.cryptographie;

import com.m2i.pti.domain.Machine;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.repository.MachineRepository;
import com.m2i.pti.service.dto.MachineDTO;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import jakarta.transaction.Transactional;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Transactional
public class CryptageService {

    private final Logger log = LoggerFactory.getLogger(CryptageService.class);

    @Value("${m2i.security.pti.scripts.genkeys.windows}")
    private String genKeysScriptsWindows;

    @Value("${m2i.security.pti.ca.file.windows}")
    private String caFileWindows;

    @Value("${m2i.security.pti.pem.file.windows}")
    private String pemFileWindows;


    @Value("${m2i.security.pti.ca.file.linux}")
    private String caFileLinux;

    @Value("${m2i.security.pti.pem.file.linux}")
    private String pemFileLinux;

    @Value("${m2i.security.pti.scripts.genkeys.linux}")
    private String genKeysScriptsLinux;

    @Value("${m2i.security.pti.keys.path.windows}")
    private String keysPathWindows;

    @Value("${m2i.security.pti.keys.path.linux}")
    private String keysPathLinux;

    @Value("${m2i.security.pti.publickeys.extension}")
    private String pubKeysExtension;

    private final MachineRepository machineRepository;

    public CryptageService(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    /**
     * Hash une chaine avec SHA256
     *
     * @param msg : la chaine à hasher
     * @return : La chaine hashée ou la chaine en hexadécimale si le hash echoue
     */
    private static String hash256OrReturnHex(String msg) {
        MessageDigest digest;
        byte[] hash;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(msg.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            hash = msg.getBytes(StandardCharsets.UTF_8);
            e.printStackTrace();
        }
        return String.format("%040x", new BigInteger(1, hash));
    }

    /**
     * Encrypt un message donné
     *
     * @param message   : Le message à encrypter
     * @param publicKey : La clé publique
     * @return : Le message crypté
     * @throws Exception
     */
    public String encrypt(String message, PublicKey publicKey) throws Exception {
        Cipher encryptCipher = Cipher.getInstance(CrypConstants.CIPHER_NAME);
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherText = encryptCipher.doFinal(message.getBytes(UTF_8));
        return Base64.getEncoder().encodeToString(cipherText);
    }

    /**
     * Décrypte un message donnée, le message est supposé être crypté
     *
     * @param cryptedMessage : Le message cryptéz
     * @param privateKey     : La clé privée
     * @return : Le message en clair qui avait été crypté
     */
    public String decrypt(String cryptedMessage, PrivateKey privateKey) {
        byte[] bytes = Base64.getDecoder().decode(cryptedMessage);
        log.debug("\n\n---Nombre de bytes du message crypté : {} ---\n\n", bytes.length);
        String str;
        try {
            Cipher decriptCipher = Cipher.getInstance("RSA");
            decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            str = new String(decriptCipher.doFinal(bytes), UTF_8);
        } catch (Exception e) {
            log.error("Impossible de décrypter le message : " + cryptedMessage, e);
            str = "";
        }
        return str;
    }

    /**
     * Récupère la clé privée à partir d'un fichier
     *
     * @param path : Le chemin d'accès du fichier
     * @return : La clé privée
     */
    public PrivateKey fetchPrivateKey(String path) {
        DataInputStream dis = null;
        try {
            log.debug("\n---Fetching the private key from {} ", path);
            // read private key DER file
            File privKeyFile = new File(path);
            dis = new DataInputStream(new FileInputStream(path));
            byte[] privKeyBytes = new byte[(int) privKeyFile.length()];
            final int read = dis.read(privKeyBytes);
            log.debug("\n\n---Nombre de bytes du fichier : {}, Nombre de bytes lus : {} ", privKeyFile.length(), read);
            dis.close();
            KeyFactory keyFactory = KeyFactory.getInstance(CrypConstants.CIPHER_NAME);
            PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privKeyBytes);
            return keyFactory.generatePrivate(privSpec);
        } catch (Exception ex) {
            log.error("Impossible de construire la clé privée.", ex);
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException ex) {
                log.error("Impossible de ferme le flux du fichier contenant la clé privée", ex);
            }
        }
        return null;
    }

    private boolean generateKeys(String pubKeyFileName, String prvtKeyFileName, String commonName, String crtFileName, String pass, String p12FileName, String p12Password) throws IOException {
        log.debug("f%" + new File(crtFileName).exists() + new File(p12FileName).exists());
        if (new File(crtFileName).exists() && new File(p12FileName).exists()) {
            log.debug("\n\n-- Un des fichiers du même nom existe déjà: {}, {}--\n", pubKeyFileName, prvtKeyFileName);
            return true;
        }

        if (SystemUtils.IS_OS_LINUX) {
            try {
                String cmd = String.format("%s %s %s %s %s %s %s %s %s %s", genKeysScriptsLinux, prvtKeyFileName, pubKeyFileName, commonName, crtFileName, pass, p12FileName, p12Password, caFileLinux, pemFileLinux);
                log.debug("\n---------------------------------------\n");
                log.debug("\n--Commande a executer : {} --\n", cmd);
                log.debug("\n---------------------------------------\n");
                Process p = new ProcessBuilder("/bin/bash", "-c", cmd).start();
                log.debug("\n------------------ERROR WHILE RUNNING CERTIFICATE SCRIPT 1---------------------\n");
                new BufferedReader(new InputStreamReader(p.getErrorStream())).lines().peek(log::error).collect(Collectors.toList());
                log.debug("\n------------------ERROR WHILE RUNNING CERTIFICATE SCRIPT 1---------------------\n");
                p.waitFor();
                log.info("\n--Generation du certificat : exitValue ={}", p.exitValue());
                return p.exitValue() == 0;
            } catch (Exception e) {
                log.error("\n--La génération du certificat a échoué !", e);
                return false;
            }
        } else {
            log.debug("\n\n---Running script on windows OS...");
            try {
                String cmd = String.format("cmd /c start /b %s \"key=%s\" \"csr=%s\" \"cn=%s\" \"crt=%s\" \"pass=%s\" \"p12=%s\" \"p12Pass=%s\" \"caFile=%s\" \"pemFile=%s\"",
                        genKeysScriptsWindows, prvtKeyFileName, pubKeyFileName,
                        commonName, crtFileName, pass, p12FileName,
                        p12Password, caFileWindows, pemFileWindows);
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
                log.debug("\n--Generation du certificat : exitValue ={}", p.exitValue());
                return p.exitValue() == 0;
            } catch (Exception e) {
                log.error("\n--La génération du certificat a échoué !", e);
                return false;
            }
        }
    }

    @Transactional
    public File generateKeys(UniteOrganisation organisation, MachineDTO machine, String p12Pass) throws IOException {
        // Si la génération du certificat reussit, on active le compte

        log.debug("%time%" + ZonedDateTime.now());
        final String hostId = machine.getHostId();
        String libelle = organisation.getLibelleUniteOrganisation().replaceAll(" ", "_");
        final String keyDirectory = (SystemUtils.IS_OS_LINUX ? keysPathLinux : keysPathWindows);
        final String newFilesDirectory = keyDirectory + File.separator + libelle;

        if (!new File(keyDirectory).exists()) {
            new File(keyDirectory).mkdir();
        }
        if (!new File(newFilesDirectory).exists()) {
            new File(newFilesDirectory).mkdir();
        }

        if (machine.getId() != null) {
            Machine machineExist = machineRepository.findById(machine.getId()).orElse(null);
            libelle = libelle + "_" + new String(Base64.getDecoder().decode(machineExist.getCurrentUser()));
            log.info("Infos ||" + libelle);
        }


        final String prvKeyFile = newFilesDirectory + File.separator + libelle.toLowerCase() + ".key";
        final String pubKeyFile = newFilesDirectory + File.separator + libelle.toLowerCase() + ".csr";
        final String crtFile = newFilesDirectory + File.separator + libelle.toLowerCase() + ".crt";
        final String pass = machine.getCle();//code du ca

        final String p12File = newFilesDirectory + File.separator + libelle.toLowerCase() + ".p12";
        if (this.generateKeys(pubKeyFile, prvKeyFile, machine.getUuid(), crtFile, pass, p12File, p12Pass)) {
            final File file = new File(p12File);
            if (file.exists()) {
                return file;
            } else {
                return null;
            }
        } else {
            throw new CustomParameterizedException("Impossible d'activer le poste en question. La génération de votre certificat a échoué !");
        }
    }

    /**
     * @param libelle
     * @param pass
     * @param p12Pass
     * @param uid
     * @return
     * @throws IOException
     */

    @Transactional
    public File generateCaAndKeys(String libelle, String pass, String p12Pass, String uid) throws IOException {
        // Si la génération du certificat reussit, on active le compte

        log.debug("%time%" + ZonedDateTime.now());
        final String keyDirectory = (SystemUtils.IS_OS_LINUX ? keysPathLinux : keysPathWindows);
        final String newFilesDirectory = keyDirectory + File.separator + libelle;

        if (!new File(keyDirectory).exists()) {
            new File(keyDirectory).mkdir();
        }
        if (!new File(newFilesDirectory).exists()) {
            new File(newFilesDirectory).mkdir();
        }
        final String prvKeyFile = newFilesDirectory + File.separator + libelle.toLowerCase() + ".key";
        final String pubKeyFile = newFilesDirectory + File.separator + libelle.toLowerCase() + ".csr";
        final String crtFile = newFilesDirectory + File.separator + libelle.toLowerCase() + ".crt";

        final String p12File = newFilesDirectory + File.separator + libelle.toLowerCase() + ".p12";
        if (this.generateKeys(pubKeyFile, prvKeyFile, uid, crtFile, pass, p12File, p12Pass)) {
            final File file = new File(p12File);
            if (file.exists()) {
                return file;
            } else {
                return null;
            }
        } else {
            throw new CustomParameterizedException("Impossible d'activer le poste en question. La génération de votre certificat a échoué !");
        }
    }
}
