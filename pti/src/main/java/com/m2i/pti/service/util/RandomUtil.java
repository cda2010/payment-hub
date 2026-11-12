package com.m2i.pti.service.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.common.hash.Hashing;
import com.m2i.pti.security.otp.Totp;
import com.m2i.pti.security.otp.api.Base32;
import com.m2i.pti.security.otp.api.Clock;
import com.m2i.pti.security.otp.api.Digits;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;

/**
 * Utility class for generating random Strings.
 */
public final class RandomUtil {

    private static final int DEF_COUNT = 20;
    private static final int DEF_PASSWRD_COUNT = 8;
      private static final int DEF_PASSWRD_COUNT_NUMERIC = 6;
    private static final int CODE_COUNT = 10;
    public static final Integer NBR_JOURS = 20;
    private static final int DEF_OP = 3;
    private static final int CODE_RETRAIT_SIZE = Digits.NINE.getValue();
    private static final int CODE_INTERNET_SIZE = Digits.FIVE.getValue();
    private static final int TOKEN_SIZE = Digits.SIX.getValue();
 
    private RandomUtil() {
    }

    /**
     * Generate a password.
     *
     * @return the generated password
     */
    public static String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(DEF_PASSWRD_COUNT);
    }
 public static String generatePasswordNumeric() {
        return RandomStringUtils.randomNumeric(DEF_PASSWRD_COUNT_NUMERIC);
    }
    public static String generateCodeOperation() {
        return RandomStringUtils.randomAlphanumeric(DEF_COUNT);
    }
 public static String  encodeHash(String password) {
     String sha256hex = Hashing.sha256()
                   .hashString(password, StandardCharsets.UTF_8)
            .toString();
     return sha256hex;
    }
    /**
     * Generate an activation key.
     *
     * @return the generated activation key
     */
    public static String generateActivationKey() {
        return RandomStringUtils.randomNumeric(DEF_COUNT);
    }

    /**
     * Generate a reset key.
     *
     * @return the generated reset key
     */
    public static String generateResetKey() {
        return RandomStringUtils.randomNumeric(DEF_COUNT);
    }

    public static String generateAlphaNumeric(int count) {
        return RandomStringUtils.randomAlphanumeric(count);
    }

    /*
        Generation d'un otp (code retrait){secret,otp}
     */
    public static String[] generateOtp() {
        Clock customClock = new Clock(604800);//7 jours
        String secret = Base32.random();
        Totp totp = new Totp(secret, customClock);
        String[] datas = {secret, totp.now(CODE_RETRAIT_SIZE, 9)};
        return datas;
    }

    public static String[] generateOtp(int delais) {
        Clock customClock = new Clock(delais);//En seconde
        String secret = Base32.random();
        Totp totp = new Totp(secret, customClock);
        String[] datas = {secret, totp.now(CODE_INTERNET_SIZE, 5)};
        return datas;
    }

    public static String[] generateOtp2(int delai) {

        Clock customClock = new Clock(delai);//7 jours
        String secret = Base32.random();
        Totp totp = new Totp(secret, customClock);
        String[] datas = {secret, totp.now(CODE_RETRAIT_SIZE, 5)};
        return datas;
    }
    
    
    public static String generateToken(String secret) {
        Clock customClock = new Clock(60);
        
        Totp totp = new Totp(secret, customClock);
        return totp.now(TOKEN_SIZE, 6);
    }

    public static String crypt(String code) {
        return BCrypt.withDefaults().hashToString(12, code.toCharArray());
    }

    public static Boolean verify(String password, String bcryptHashString) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), bcryptHashString);
        return result.verified;
    }

    /*
        Vérification du code retrait
     */
    public static boolean verifyOtp(String secret, String otp) {
        Clock customClock = new Clock(604800);
        Totp totp = new Totp(secret, customClock);
        return totp.verify(otp, CODE_RETRAIT_SIZE) == true;
    }

    public static boolean verifyOtp(String secret, String otp,int delais) {
        Clock customClock = new Clock(delais);
        Totp totp = new Totp(secret, customClock);
        return totp.verify(otp, CODE_INTERNET_SIZE) == true;
    }

    public static boolean verifyToken(String secret, String token) {
        Clock customClock = new Clock(40);
        Totp totp = new Totp(secret, customClock);
        return totp.verify(token, TOKEN_SIZE) == true;
    }

    public static String getMd5Code(String code) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(code.getBytes(), 0, code.length());
        String hashedPass = new BigInteger(1, messageDigest.digest()).toString(16);
        if (hashedPass.length() < 32) {
            hashedPass = "0" + hashedPass;
        }
        return hashedPass;
    }

    /**
     * Generation du code de l'operation
     * DateJour+LettreAleatoire+3chiffres random+idOp
     */
    public static String getCodeOperation(ZonedDateTime dateOperation) {
        return dateOperation.getYear() + "" + dateOperation.getMonth().getValue()
                + "" + dateOperation.getDayOfMonth() + "" + dateOperation.getSecond() + "." + RandomStringUtils.randomAlphabetic(1).toUpperCase() + RandomStringUtils.randomNumeric(DEF_OP);
    }
  public static String getCodeOperation(ZonedDateTime dateOperation,String codeUo) {
        return dateOperation.getYear() + "" + dateOperation.getMonth().getValue()
                + "" + dateOperation.getDayOfMonth() + "" + dateOperation.getSecond() + "." + RandomStringUtils.randomAlphabetic(2).toUpperCase() + codeUo+"."+RandomStringUtils.randomNumeric(DEF_OP);
    }
}
