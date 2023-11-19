package de.marketmaker.istar.merger.web.xmlmarket;

import java.util.List;
import java.util.ArrayList;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.DESedeKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles encryption and decryption of ukeys.
 *
 * @author umaurer
 */
public class CryptUkey {
    private Logger logger = LoggerFactory.getLogger(CryptUkey.class);
    public static final String COMPONENTNAME = "CryptUkey";

    private static final int MAX_STORED_CIPHER_COUNT = 20;
    private static final String CRYPT_ALGORITHM = "DESede";
    private static final String CIPHER_ALGORITHM = CRYPT_ALGORITHM + "/ECB/PKCS5Padding";
    private static final String CRYPT_PASSPHRASE = "EisIstAuchImHeissenSommerKalt";
    public static final String UKEY_DELIMITER = "\000";

    private static final CryptUkey INSTANCE = new CryptUkey();

    private SecretKey secretKey = null;
    private List ciphersForEncryption = new ArrayList();
    private List ciphersForDecryption = new ArrayList();

    private SecretKey createSecretKey() throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        final SecretKeyFactory sf = SecretKeyFactory.getInstance(CRYPT_ALGORITHM);
        final DESedeKeySpec sks = new DESedeKeySpec(CRYPT_PASSPHRASE.getBytes());
        return sf.generateSecret(sks);
    }

    private CryptUkey() {
        try {
            secretKey = createSecretKey();
        }
        catch (Exception e) {
            this.logger.error("cannot initialize secret key", e);
        }
    }

    public static CryptUkey getInstance() {
        if (INSTANCE.secretKey == null) {
            throw new NullPointerException("key is not initialized");
        }
        return INSTANCE;
    }

    private Cipher getCipher(int mode) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        List ciphers;
        if (mode == Cipher.DECRYPT_MODE) {
            ciphers = ciphersForDecryption;
        }
        else if (mode == Cipher.ENCRYPT_MODE) {
            ciphers = ciphersForEncryption;
        }
        else {
            throw new IllegalArgumentException("unknown mode: " + mode);
        }
        synchronized (ciphers) {
            if (!ciphers.isEmpty()) {
                return (Cipher)ciphers.remove(ciphers.size() - 1);
            }
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("new cipher created for " + (mode == Cipher.DECRYPT_MODE ? "decryption" : "encryption"));
        }
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(mode, secretKey);
        return cipher;
    }

    private void returnCipher(int mode, Cipher cipher) {
        List ciphers;
        if (mode == Cipher.DECRYPT_MODE) {
            ciphers = ciphersForDecryption;
        }
        else if (mode == Cipher.ENCRYPT_MODE) {
            ciphers = ciphersForEncryption;
        }
        else {
            throw new IllegalArgumentException("unknown mode: " + mode);
        }
        synchronized (ciphers) {
            if (ciphers.size() < MAX_STORED_CIPHER_COUNT) {
                ciphers.add(cipher);
                return;
            }
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("cipher for " + (mode == Cipher.DECRYPT_MODE ? "decryption" : "encryption") + " discarded");
        }
    }

    private void returnCipherAfterError(int mode, Cipher cipher) throws InvalidKeyException {
        cipher.init(mode, secretKey);
        returnCipher(mode, cipher);
    }

    public String encrypt(String toEncrypt) throws Exception {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
        try {
            byte[] encrypted = cipher.doFinal(toEncrypt.getBytes());
            returnCipher(Cipher.ENCRYPT_MODE, cipher);
            return hexEncode(encrypted);
        }
        catch (Exception exception) {
            returnCipherAfterError(Cipher.ENCRYPT_MODE, cipher);
            throw exception;
        }
    }


    public String decrypt(String toDecrypt) throws Exception {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
        try {
            final byte[] tmp = hexDecode(toDecrypt);
            String decrypted = new String(cipher.doFinal(tmp));
            returnCipher(Cipher.DECRYPT_MODE, cipher);
            return decrypted;
        }
        catch (Exception exception) {
            returnCipherAfterError(Cipher.DECRYPT_MODE, cipher);
            throw exception;
        }

    }

    private String hexEncode(byte[] buffer) {
        StringBuffer sb = new StringBuffer(buffer.length * 2);
        for (int i = 0; i < buffer.length; i++) {
            int b = buffer[i] & 0xff;
            String hex = Integer.toHexString(b);
            sb.append(hex.length() == 1 ? "0" + hex : hex);
        }
        return sb.toString();
    }

    private byte[] hexDecode(String hexEncoded) {
        byte[] buffer = new byte[hexEncoded.length() / 2];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte)Integer.parseInt(hexEncoded.substring(i * 2, (i + 1) * 2), 16);
        }
        return buffer;
    }
}
