/*
 * FinderMetaElement.java
 *
 * Created on 17.07.2006 16:11:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import net.jcip.annotations.Immutable;

import de.marketmaker.istar.domain.data.LocalizedString;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class FinderMetaElement {
    private final String key;
    private final LocalizedString name;

    public FinderMetaElement(String key, LocalizedString name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public LocalizedString getName() {
        return name;
    }
}
