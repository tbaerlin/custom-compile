/*
 * SnapRecord.java
 *
 * Created on 01.03.2005 15:32:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface SnapRecord {
    Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;

    Charset UTF_8 = StandardCharsets.UTF_8;

    Charset US_ASCII = StandardCharsets.US_ASCII;

    Charset CP_1252 = Charset.forName("windows-1252");

    Charset DEFAULT_CHARSET = CP_1252;

    Collection<SnapField> getSnapFields();

    SnapField getField(int fieldId);

    SnapField getField(String fieldname);

    /**
     * @return nominal delay of the data in this record, 0 for realtime; in contrast to actual
     * delay, the nominal delay does not include feed latency or processing delays.
     */
    int getNominalDelayInSeconds();
}
