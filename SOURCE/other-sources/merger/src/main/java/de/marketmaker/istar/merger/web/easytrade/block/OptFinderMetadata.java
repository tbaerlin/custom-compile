/*
 * OptFinderMetadata.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * Returns meta data that can be used in {@see OPT_Finder} for searching options.
 *
 * The response consists of option types (call, put), markets identified by their names, markets classified by country,
 * the exercise style, category (weekly, daily, standard), and the vwd code of the underlying performance indexes.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OptFinderMetadata extends AbstractFinderMetadata {
    public OptFinderMetadata() {
        super(InstrumentTypeEnum.OPT, RatioDataRecord.Field.market);
    }
}