/*
 * MmauthProfileProvider.java
 *
 * Created on 18.07.11 15:01
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.io.File;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domainimpl.profile.PmAuthentcationProvider;
import de.marketmaker.istar.domainimpl.profile.PmAuthenticationRequest;
import de.marketmaker.istar.domainimpl.profile.PmAuthenticationResponse;

/**
 * Creates profiles based on encrypted mmauth keys that are used by PM.
 * @author oflege
 */
public class PmAuthentcationProviderImpl implements PmAuthentcationProvider {
    protected static class Key {
        final String login;
        final String password;
        final String timestamp;

        public Key(String[] tokens) {
            if (tokens.length != 3) {
                throw new IllegalArgumentException(Arrays.toString(tokens));
            }
            this.login = tokens[0];
            this.password = tokens[1];
            this.timestamp = tokens[2];
        }

        protected String getLogin() {
            return this.login;
        }

        protected String getPassword() {
            return this.password;
        }

        protected String getTimestamp() {
            return this.timestamp;
        }
    }

    private static final String MMAUTH_PASSPHRASE = "WennErLangGenugWirdLebenAlleNoeteSichBeheben";

    private static final String MMAUTH_ALGORITHM = "DESede";

    private final SecretKey mmauthSecretKey;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private KukaCustomerProvider kukaCustomerProvider;
    private KukaCustomerProvider wisoCustomerProvider;

    public PmAuthentcationProviderImpl() {
        try {
            final SecretKeyFactory sf = SecretKeyFactory.getInstance(MMAUTH_ALGORITHM);
            final DESedeKeySpec sks = new DESedeKeySpec(MMAUTH_PASSPHRASE.getBytes());
            this.mmauthSecretKey = sf.generateSecret(sks);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setKukaCustomerProvider(KukaCustomerProvider kukaCustomerProvider) {
        this.kukaCustomerProvider = kukaCustomerProvider;
    }

    public void setWisoCustomerProvider(KukaCustomerProvider wisoCustomerProvider) {
        this.wisoCustomerProvider = wisoCustomerProvider;
    }

    @Override
    public PmAuthenticationResponse getPmAuthentication(PmAuthenticationRequest request) {
        final Key key;
        try {
            key = decrypt(request.getCredentials());
        } catch (GeneralSecurityException e) {
            this.logger.warn("<getProfile> invalid mmauth key " + request.getCredentials(), e);
            return PmAuthenticationResponse.invalid();
        }

        final KukaCustomer customer = getCustomer(key.login);
        if (customer == null) {
            this.logger.warn("<getProfile> unknown kuka customer '" + request.getCredentials() + "'");
            return PmAuthenticationResponse.invalid();
        }

        return new PmAuthenticationResponse(customer.getKennung(), customer.getAbos());
    }

    protected KukaCustomer getCustomer(String login) {
        final KukaCustomer kukaCustomer = this.kukaCustomerProvider.getCustomer(login);
        if (kukaCustomer != null) {
            return kukaCustomer;
        }
        return this.wisoCustomerProvider.getCustomer(login);
    }

    protected Key decrypt(String key) throws GeneralSecurityException {
        final String ivStr = key.substring(0, 4);
        final byte[] iv = new byte[8];

        for (int i = 0; i < 4; i++) {
            char c = ivStr.charAt(i);
            iv[i] = (byte) c;
            iv[i + 4] = (byte) c;
        }

        return new Key(decode(key, iv).split(":"));
    }

    private String decode(String key, byte[] iv) throws GeneralSecurityException {
        final AlgorithmParameterSpec ivs = new IvParameterSpec(iv);
        final Cipher decoder = Cipher.getInstance(MMAUTH_ALGORITHM + "/CFB8/NoPadding");
        decoder.init(Cipher.DECRYPT_MODE, this.mmauthSecretKey, ivs);
        return new String(decoder.doFinal(Base64.decodeBase64(key.substring(4))));
    }

    public static void main(String[] args) throws Exception {
        final PmAuthentcationProviderImpl impl = new PmAuthentcationProviderImpl();
        if (args.length == 2) {
            // 1) grep 'search.csv' access.log > pm.txt
            // 2) java ... PmAuthentcationProviderImpl pm.txt xyz
            //    will output all lines where the decrypted authentication contains xyz
            Files.lines(new File(args[0]).toPath())
                    .filter(s -> {
                        final int p = s.indexOf("authentication=");
                        final String encoded = s.substring(p + 15, s.indexOf("&", p + 15));
                        try {
                            final String decoded = URLDecoder.decode(encoded, "UTF-8");
                            final String login = impl.decrypt(decoded).getLogin();
                            return login.contains(args[1]);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .forEach(System.out::println);
            return;
        }

        String mmauth = "B46BZ7beIonV3OqkI9rULBEqP/adLpHD0TWekriXRfUNIKVyXF6EqKUZoRIRVS8bZu+sI+3m7BgaraxxkCnPPLPjAR/MiWagioXDmpBYv/o=";
        Key decrypt = impl.decrypt(mmauth);
        System.out.println("mmath = " + mmauth);
        System.out.println("login = " + decrypt.login);
        System.out.println("pwd   = " + decrypt.password);
        System.out.println("----------------------------------------------------");

        mmauth = "6130TSOVSnO+bVivk1dYlugQded7hMw1qGkmHrRQMBUlGzLZeF1q60XSnao/9d4=";
        decrypt = impl.decrypt(mmauth);
        System.out.println("mmath = " + mmauth);
        System.out.println("login = " + decrypt.login);
        System.out.println("pwd   = " + decrypt.password);
        System.out.println("----------------------------------------------------");

        mmauth = "8592VFmqz5RW2Y/qbDdjXAt6zn4l3nFSszSTH2HgwDN/GoDtbOyYhHCrGtjMYCI=";
        decrypt = impl.decrypt(mmauth);
        System.out.println("mmath = " + mmauth);
        System.out.println("login = " + decrypt.login);
        System.out.println("pwd   = " + decrypt.password);
        System.out.println("----------------------------------------------------");




    }
}
