/*
 * Property.java
 *
 * Created on 02.08.2006 16:39:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class Property implements Serializable {
    private final long id;
    private final String key;
    private final String value;

    public Property(long id, String key, String value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public String toString() {
        return this.value;
    }

    public long getId() {
        return this.id;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }
}
