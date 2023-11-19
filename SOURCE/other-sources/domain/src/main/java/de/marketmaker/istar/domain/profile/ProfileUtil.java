/*
 * ProfileUtil.java
 *
 * Created on 12.05.11 13:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.profile;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;

import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.domainimpl.profile.DefaultProfile;
import de.marketmaker.istar.domainimpl.profile.ResourceProfile;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;

/**
 * @author Thomas Kiesgen
 */
public class ProfileUtil {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final int XOR_VALUE = 132;

    private static final String SEP = "=";

    private ProfileUtil() {
    }

    public static String encodeCredential(String authentication, String authenticationType) {
        return encode((authentication + SEP + authenticationType).getBytes(UTF_8));
    }

    public static String decodeAuthentication(String credentials) {
        return decodeCredential(credentials)[0];
    }

    public static String decodeAuthenticationType(String credentials) {
        return decodeCredential(credentials)[1];
    }

    private static String[] decodeCredential(String credentials) {
        final byte[] bytes = decode(credentials);
        return new String(bytes, UTF_8).split(SEP, 2);
    }

    public static String encode(byte[] bytes) {
        final byte[] xored = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            xored[i] = (byte) (bytes[i] ^ XOR_VALUE);
        }
        return ByteUtil.toBase64String(xored);
    }

    public static byte[] decode(String str) {
        final byte[] xored = Base64.decodeBase64(str);
        final byte[] bytes = new byte[xored.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (xored[i] ^ XOR_VALUE);
        }
        return bytes;
    }

    public static String getSimpleName(Profile p) {
        StringBuilder sb = new StringBuilder(40).append(p.getClass().getSimpleName()).append('[');
        if (p instanceof VwdProfile) {
            VwdProfile vwdProfile = (VwdProfile) p;
            sb.append(vwdProfile.getAppId()).append(':').append(vwdProfile.getVwdId());
        } else {
            sb.append(p.getName());
        }
        return sb.append(']').toString();
    }
}
