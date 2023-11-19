/*
 * FinderMetaElementList.java
 *
 * Created on 14.08.2008 15:42:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.util.List;
import java.util.ArrayList;

import de.marketmaker.istar.domain.data.LocalizedString;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FinderMetaElementList {
    final List<FinderMetaElement> elements = new ArrayList<>();

    private final LocalizedString name;

    private final String type;

    public FinderMetaElementList(LocalizedString name, String type) {
        this.name = name;
        this.type = type;
    }

    public void add(FinderMetaElement e) {
        this.elements.add(e);
    }

    public List<FinderMetaElement> getElements() {
        return elements;
    }

    public LocalizedString getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return "FinderMetaElementList[" + this.name + ", "  + this.type + ", " + this.elements + "]";
    }
}
