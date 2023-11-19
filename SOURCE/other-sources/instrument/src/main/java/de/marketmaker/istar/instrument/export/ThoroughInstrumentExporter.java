/*
 * InstrumentExporterImpl.java
 *
 * Created on 10.08.2010 09:43:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.util.Properties;

import org.joda.time.DateTime;

import static de.marketmaker.istar.instrument.export.InstrumentExportConstants.*;

/**
 * An instrument exporter which exports complete set of instruments available. Besides instrument data,
 * the produced data file also contains the FAST template and domain context.
 * <p>
 * A complete export is considered to be successful only if a pre-defined minimal number of instruments
 * are exported. It has a default value for 1M, and can be configured for other values.
 * <p>
 * After a successful complete export some export related properties are reset.
 *
 * @author zzhao
 * @since 1.2
 */
public class ThoroughInstrumentExporter extends AbstractInstrumentExporter {

    private static final String TMP_INS_DIR = "tmp_ins_dir";

    private int minimumInstrumentNumber = 1000000;

    public void setMinimumInstrumentNumber(int minimumInstrumentNumber) {
        this.minimumInstrumentNumber = minimumInstrumentNumber;
    }

    @Override
    protected ExporterResult exportInstruments(File dataDir, File tmpDir, Properties prop)
            throws Exception {
        final File tmpInsDir = InstrumentSystemUtil.getDir(tmpDir, TMP_INS_DIR, true);
        ExporterResult ret;

        final DateTime dateTime = new DateTime();
        try (InstrumentDirWriter writer = InstrumentDirWriter.create(tmpInsDir, getCensorDir())) {
            int numWritten = exportWith(writer, (e, eh) -> e.export(eh, this.parameters));
            ret = new ExporterResult(null).withNumberExported(numWritten);
        }

        if (ret.numberExported < this.minimumInstrumentNumber) {
            this.logger.error("<exportInstruments> not enough instruments: " + ret.numberExported
                    + " exported, will stop NOW");
            throw new IllegalStateException("read too few instruments");
        }

        InstrumentSystemUtil.replace(dataDir, tmpInsDir);
        resetExportProperties(prop, dateTime);

        return ret;
    }

    private void resetExportProperties(Properties prop, DateTime dateTime) {
        prop.put(PN_INSTRUMENT_UPDATE_NUM, "0");
        prop.put(PN_INSTRUMENT_UPDATE_LAST_TS, EXPORT_DATE_TIME_FORMATTER.print(dateTime));
        prop.remove(PN_INSTRUMENT_UPDATE_LAST_NUM);
        prop.remove(PN_INSTRUMENT_UPDATE_LAST_STATUS);
    }
}
