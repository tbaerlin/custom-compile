/*
 * FinderMetaElement.java
 *
 * Created on 17.07.2006 16:11:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class FinderMetaItem implements Serializable {
    private final String key;

    private final String name;

    private final int count;

    public FinderMetaItem(String key, String name, int count) {
        this.key = key;
        this.name = name;
        this.count = count;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }
}
