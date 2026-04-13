package com.medicalreports.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility per la crittografia e la sicurezza dei dati sensibili
 */
public class SecurityUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    /**
     * Genera un hash SHA-256 del contenuto per verificare l'integrità
     */
    public static String generaHash(String contenuto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(contenuto.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.error("Errore durante la generazione dell'hash", e);
            return null;
        }
    }
    
    /**
     * Verifica l'integrità del contenuto confrontando gli hash
     */
    public static boolean verificaIntegrita(String contenuto, String hashAtteso) {
        String hashCalcolato = generaHash(contenuto);
        return hashAtteso != null && hashAtteso.equals(hashCalcolato);
    }
    
    /**
     * Cifra un testo utilizzando AES-GCM
     */
    public static String cifraTesto(String testo, SecretKey chiave) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, chiave, parameterSpec);
            
            byte[] testoCifrato = cipher.doFinal(testo.getBytes("UTF-8"));
            
            // Combina IV e testo cifrato
            byte[] combinato = new byte[iv.length + testoCifrato.length];
            System.arraycopy(iv, 0, combinato, 0, iv.length);
            System.arraycopy(testoCifrato, 0, combinato, iv.length, testoCifrato.length);
            
            return Base64.getEncoder().encodeToString(combinato);
        } catch (Exception e) {
            logger.error("Errore durante la cifratura", e);
            return null;
        }
    }
    
    /**
     * Decifra un testo utilizzando AES-GCM
     */
    public static String decifraTesto(String testoCifratoBase64, SecretKey chiave) {
        try {
            byte[] combinato = Base64.getDecoder().decode(testoCifratoBase64);
            
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] testoCifrato = new byte[combinato.length - GCM_IV_LENGTH];
            
            System.arraycopy(combinato, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combinato, GCM_IV_LENGTH, testoCifrato, 0, testoCifrato.length);
            
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, chiave, parameterSpec);
            
            byte[] testoDecifrato = cipher.doFinal(testoCifrato);
            return new String(testoDecifrato, "UTF-8");
        } catch (Exception e) {
            logger.error("Errore durante la decifratura", e);
            return null;
        }
    }
    
    /**
     * Genera una nuova chiave AES casuale
     */
    public static SecretKey generaChiaveAES() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Errore durante la generazione della chiave", e);
            return null;
        }
    }
    
    /**
     * Genera una password casuale sicura
     */
    public static String generaPasswordSicura(int lunghezza) {
        String CARATTERI = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(lunghezza);
        
        for (int i = 0; i < lunghezza; i++) {
            int index = random.nextInt(CARATTERI.length());
            password.append(CARATTERI.charAt(index));
        }
        
        return password.toString();
    }
    
    /**
     * Offusca un codice fiscale per la visualizzazione parziale
     */
    public static String offuscaCodiceFiscale(String codiceFiscale) {
        if (codiceFiscale == null || codiceFiscale.length() < 8) {
            return "*******";
        }
        return codiceFiscale.substring(0, 6) + "****" + codiceFiscale.substring(codiceFiscale.length() - 2);
    }
    
    /**
     * Offusca un nome per la privacy
     */
    public static String offuscaNome(String nome) {
        if (nome == null || nome.isEmpty()) {
            return "***";
        }
        if (nome.length() < 3) {
            return "*";
        }
        return nome.charAt(0) + "***" + nome.charAt(nome.length() - 1);
    }
}
