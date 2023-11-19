/*
 * RatioSearchRequest.java
 *
 * Created on 26.10.2005 12:11:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.common.request.AbstractIstarRequest;

import java.util.Map;
import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatioSearchMetaRequest extends AbstractIstarRequest {
    static final long serialVersionUID = -1156898935437L;

    private InstrumentTypeEnum type;

    private int groupByFieldid;

    private int selectFieldid;

    private boolean withDetailedSymbol;

    public RatioSearchMetaRequest(InstrumentTypeEnum type, int groupByFieldid, int selectFieldid,
            boolean withDetailedSymbol) {
        this.type = type;
        this.groupByFieldid = groupByFieldid;
        this.selectFieldid = selectFieldid;
        this.withDetailedSymbol = withDetailedSymbol;
    }

    public InstrumentTypeEnum getType() {
        return type;
    }

    public void setType(InstrumentTypeEnum type) {
        this.type = type;
    }

    public boolean isWithDetailedSymbol() {
        return this.withDetailedSymbol;
    }

    public int getGroupByFieldid() {
        return this.groupByFieldid;
    }

    public int getSelectFieldid() {
        return this.selectFieldid;
    }
}
