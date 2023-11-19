/*
 * IncrementalInstrumentExporter.java
 *
 * Created on 10.08.2010 10:44:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.util.Properties;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.util.Assert;

import de.marketmaker.istar.common.util.IoUtils;

import static de.marketmaker.istar.instrument.export.InstrumentExportConstants.*;
import static org.joda.time.DateTimeConstants.SECONDS_PER_HOUR;

/**
 * An instrument exporter which exports only instruments updated during a specific time period. The
 * produced instruments data file contains only instruments. The FAST template and domain context
 * are not stored, since they are considered the same between instrument updates and always readable
 * from the instrument data file produced by a thorough export.
 * <p>
 * The time period is calculated based on the export properties persisted after exports, but would be
 * at least one hour.
 * <p>
 * An incremental instruments update export can only be performed if a given gate number(defaults to
 * 10K, can be configured otherwise) of updated instruments is not exceeded. Note that the check
 * against this number is made both before the update attempt and after the number of instruments to
 * be updated is available. Anyway the sum would be persisted as export properties so that the
 * following situations can be avoided:
 * <ol>
 * <li>One update with exceeded number of instruments</li>
 * <li>After several updates, the last update causing the total number of updates exceed the limit</li>
 * <li>After one not-performed updates, no further updates would be performed to reserve data consistency</li>
 * </ol>
 *
 * @author zzhao
 * @since 1.2
 */
public class IncrementalInstrumentExporter extends AbstractInstrumentExporter {

    private static final String TMP_DIR_UPDATE = "tmp_ins_dir_update";

    private File completeInstrumentDir;

    private int maxNumUpdatedInstruments = 10000;

    public void setCompleteInstrumentDir(File completeInstrumentDir) {
        this.completeInstrumentDir = completeInstrumentDir;
    }

    public void setMaxNumUpdatedInstruments(int maxNumUpdatedInstruments) {
        this.maxNumUpdatedInstruments = maxNumUpdatedInstruments;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Assert.notNull(this.completeInstrumentDir, "Last complete instrument dir is required");
        InstrumentSystemUtil.validateDir(this.completeInstrumentDir);
    }

    @Override
    protected ExporterResult exportInstruments(File dataDir, File tmpDir, Properties prop)
            throws Exception {
        int totalUpdatedNum = getInstrumentUpdateNumber(prop);
        if (totalUpdatedNum > this.maxNumUpdatedInstruments) {
            this.logger.warn("<exportInstruments> cumulated updated instruments overrun:" +
                    " " + totalUpdatedNum + " > " + this.maxNumUpdatedInstruments);
            prop.put(PN_INSTRUMENT_UPDATE_LAST_STATUS, "false");
            return new ExporterResult(null).withSkipRestSteps(true);
        }

        final DateTime dateTime = new DateTime();
        setForLastHours(getUpdateHours(prop, dateTime));
        ExporterResult ret = performExport(dataDir, tmpDir);

        prop.put(PN_INSTRUMENT_UPDATE_LAST_TS, EXPORT_DATE_TIME_FORMATTER.print(dateTime));
        prop.put(PN_INSTRUMENT_UPDATE_LAST_NUM, Integer.toString(ret.numberExported));

        if (ret.numberExported == 0) {
            this.logger.info("<exportInstruments> no instruments to update");
            prop.put(PN_INSTRUMENT_UPDATE_LAST_STATUS, "false");
            return ret.withSkipRestSteps(true);
        }

        totalUpdatedNum += ret.numberExported;
        prop.put(PN_INSTRUMENT_UPDATE_NUM, Integer.toString(totalUpdatedNum));
        if (totalUpdatedNum >= this.maxNumUpdatedInstruments) {
            this.logger.warn("<exportInstruments> cumulated updated instruments overrun:"
                    + " " + totalUpdatedNum + "(" + ret.numberExported + ") > "
                    + this.maxNumUpdatedInstruments);
            prop.put(PN_INSTRUMENT_UPDATE_LAST_STATUS, "false");
            return ret.withSkipRestSteps(true);
        }

        prop.put(PN_INSTRUMENT_UPDATE_LAST_STATUS, "true");
        return ret.withSkipRestSteps(false);
    }

    private ExporterResult performExport(File dataDir, File tmpDir)
            throws Exception {
        final ExporterResult ret = readLatestInstrumentContext();
        this.parameters.setDomainContext(ret.domainContext);

        final File tmpDataDir = InstrumentSystemUtil.getDir(tmpDir, TMP_DIR_UPDATE, true);

        try (InstrumentDirWriter writer = InstrumentDirWriter.createUpdateWriter(tmpDataDir,
                getCensorDir())) {
            int numWritten = exportWith(writer, (e, eh) -> e.export(eh, this.parameters));
            ret.withNumberExported(numWritten);
        }

        InstrumentSystemUtil.replace(dataDir, tmpDataDir);
        return ret;
    }

    private int getUpdateHours(Properties properties, DateTime now) {
        final String prop = properties.getProperty(PN_INSTRUMENT_UPDATE_LAST_TS);
        final DateTime lastExportAt = EXPORT_DATE_TIME_FORMATTER.parseDateTime(prop);

        final int seconds = Seconds.secondsBetween(lastExportAt, now).getSeconds();

        if (seconds <= 0) {
            this.logger.warn("<getUpdateHours> inconsistent instrument update property <"
                    + "lastExportAt: " + prop + ", now: " + EXPORT_DATE_TIME_FORMATTER.print(now)
                    + "> update hours set to 2");
            return 2;
        }

        return (seconds + 2 * SECONDS_PER_HOUR - 1) / SECONDS_PER_HOUR;
    }

    private ExporterResult readLatestInstrumentContext() throws Exception {
        InstrumentDirDao dao = null;
        try {
            dao = new InstrumentDirDao(this.completeInstrumentDir);
            return new ExporterResult(dao.getDomainContext());
        } finally {
            IoUtils.close(dao);
        }
    }

    private int getInstrumentUpdateNumber(Properties prop) {
        return Integer.parseInt(prop.getProperty(PN_INSTRUMENT_UPDATE_NUM, "0"));
    }
}
