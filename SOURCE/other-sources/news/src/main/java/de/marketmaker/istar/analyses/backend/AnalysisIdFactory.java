/*
 * AnalysisIdFactory.java
 *
 * Created on 20.06.12 12:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author oflege
 */
class AnalysisIdFactory {
    /**
     * Used to compute a unique id for each analysis based on its contents so that the same
     * id is generated for the same message on all systems.
     */
    private static final MessageDigest DIGESTER;

    static {
        try {
            DIGESTER = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    static Protos.Analysis.Builder addId(Protos.Analysis.Builder builder) {
        if (builder.hasId()) {
            return builder;
        }
        // we may receive the same analysis over the newsfeed more than once, so clear the date
        // (= receive timestamp), before we create the digest to detect duplicate analyses.
        final long id = getDigest(builder.clone().clearDate().build().toByteArray());
        return builder.setId(id);
    }

    // the returned value is always > 0
    static long getDigest(byte[] serialized) {
        final byte[] digest;
        synchronized (DIGESTER) {
            DIGESTER.reset();
            digest = DIGESTER.digest(serialized);
        }
        return Math.abs(ByteBuffer.wrap(digest, 0, 8).getLong());
    }
}
