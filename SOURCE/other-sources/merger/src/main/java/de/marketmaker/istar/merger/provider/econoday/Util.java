/*
 * Util.java
 *
 * Created on 19.03.12 15:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zzhao
 */
public final class Util {

    private static final MessageDigest hasher = createHasher();

    static MessageDigest createHasher() {
        // commons codec DigestUtil does not reuse MessageDigest instance
        final String alg = "MD5";
        try {
            return MessageDigest.getInstance(alg);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("cannot retrieve digest instance: " + alg);
        }
    }

    private Util() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    public static String getChecksum(String msg) {
        if (StringUtils.isBlank(msg)) {
            return StringUtils.EMPTY;
        }
        hasher.reset();
        hasher.update(msg.getBytes());
        return Hex.encodeHexString(hasher.digest());
    }
}
