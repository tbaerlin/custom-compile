/*
 * NullSnapRecord.java
 *
 * Created on 02.03.2005 14:58:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.util.Collection;
import java.util.Collections;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullSnapRecord implements SnapRecord, Serializable {
    static final long serialVersionUID = 0L;
    
    public final static NullSnapRecord INSTANCE = new NullSnapRecord();

    private NullSnapRecord() {
    }

    private Object readResolve() {
        return INSTANCE;
    }

    public Collection<SnapField> getSnapFields() {
        return Collections.emptyList();
    }

    public SnapField getField(int fieldId) {
        return NullSnapField.INSTANCE;
    }

    public SnapField getField(String fieldname) {
        return NullSnapField.INSTANCE;
    }

    @Override
    public int getNominalDelayInSeconds() {
        return 0;
    }

    public String toString() {
        return "NullSnapRecord[]";
    }

}
