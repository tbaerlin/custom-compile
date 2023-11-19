/*
 * WntFinderMetadata.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Map;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WntFinderMetadata extends AbstractFinderMetadata {
    public WntFinderMetadata() {
        super(InstrumentTypeEnum.WNT, RatioDataRecord.Field.warrantType,
                RatioDataRecord.Field.issuername);
    }
}