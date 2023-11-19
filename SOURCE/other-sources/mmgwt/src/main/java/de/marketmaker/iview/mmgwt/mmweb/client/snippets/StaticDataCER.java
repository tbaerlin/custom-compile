/*
 * StaticDataCER.java
 *
 * Created on 05.08.2009 16:46:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.CERDetailedStaticData;
import de.marketmaker.iview.dmxml.CERRatioData;
import de.marketmaker.iview.dmxml.EDGData;

/**
 * Combines other objects so it can be used as a generic type
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StaticDataCER {
    private final CERDetailedStaticData data;
    private final CERRatioData ratios;
    private final EDGData edg;

    public StaticDataCER(CERDetailedStaticData data, CERRatioData ratios, EDGData edg) {
        this.data = data;
        this.ratios = ratios;
        this.edg = edg;
    }

    public CERDetailedStaticData getData() {
        return this.data;
    }

    public CERRatioData getRatios() {
        return this.ratios;
    }

    public EDGData getEdg() {
        return this.edg;
    }
}
