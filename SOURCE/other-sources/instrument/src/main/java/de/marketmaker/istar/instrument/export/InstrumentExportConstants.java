/*
 * InstrumentExportConstants.java
 *
 * Created on 22.04.2010 15:56:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * This class contains instrument export constants associated with export property entries.
 *
 * @author zzhao
 * @since 1.2
 */
public final class InstrumentExportConstants {

    /**
     * Time stamp formatter used by writing out and reading in time stamps.
     */
    public static final DateTimeFormatter EXPORT_DATE_TIME_FORMATTER =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Export property name for the amount of instruments updated totally by incremental updates
     */
    public static final String PN_INSTRUMENT_UPDATE_NUM = "instrument.update.num";

    /**
     * Export property name for the time stamp when the last update is performed.
     */
    public static final String PN_INSTRUMENT_UPDATE_LAST_TS = "instrument.update.last.ts";

    /**
     * Export property name for the amount of instruments updated by the last incremental update
     */
    public static final String PN_INSTRUMENT_UPDATE_LAST_NUM = "instrument.update.last.num";

    /**
     * Export property name for the indicator if the last incremental update is performed
     */
    public static final String PN_INSTRUMENT_UPDATE_LAST_STATUS = "instrument.update.last.perfomed";

    private InstrumentExportConstants() {
        throw new AssertionError("not for instantiation or inheritance");
    }
}
