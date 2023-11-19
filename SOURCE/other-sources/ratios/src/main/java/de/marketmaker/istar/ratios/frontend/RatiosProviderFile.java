/*
 * PriceProviderFile.java
 *
 * Created on 14.09.2005 `19:48:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.monitor.Resource;
import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.feed.vwd.ParserVwdHelper;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.RatioUpdateable;
import de.marketmaker.istar.ratios.backend.RatiosEncoder;

/**
 * Provides ....
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatiosProviderFile implements InitializingBean {
    private final static DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

    private static final int MAX_NUM_ERRORS_PER_FILE = 100;

    private final Pattern TIMESTAMP_ISO = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}"); // yyyy-MM-dd
    private final Pattern TIMESTAMP_GERMAN = Pattern.compile("[0-9]{2}\\.[0-9]{2}\\.[0-9]{4}"); // dd.MM.yyyy
    private final Pattern TIMESTAMP_CHICAGO = Pattern.compile("[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}"); // yyyy.mm.dd
    private final Pattern TIMESTAMP_NUMBER = Pattern.compile("[0-9]{8}"); // yyyymmdd

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static Map<Integer, RatioFieldDescription.Field[]> VWDFIELDID_TO_RATIOFIELD = new HashMap<>();

    static {
        put(VwdFieldDescription.ADF_Discount_Proz.id(), RatioFieldDescription.mdpsDiscountRelative);
        put(VwdFieldDescription.ADF_Rendite_Max_pa.id(), RatioFieldDescription.mdpsMaximumYieldRelativePerYear);
        put(VwdFieldDescription.ADF_Abst_Partgrenze.id(), RatioFieldDescription.mdpsGapCap);
        put(VwdFieldDescription.ADF_Abst_Partgrenze_Proz.id(), RatioFieldDescription.mdpsGapCapRelative);
        put(VwdFieldDescription.ADF_Discount.id(), RatioFieldDescription.mdpsDiscount);
        put(VwdFieldDescription.ADF_max_Ertrag.id(), RatioFieldDescription.mdpsMaximumYield);
        put(VwdFieldDescription.ADF_Rendite_Max.id(), RatioFieldDescription.mdpsMaximumYieldRelative);
//        put(VwdFieldDescription.ADF_Seitwaertsertrag.id(), RatioFieldDescription.mdpsUnchangedYield);
        put(VwdFieldDescription.ADF_Seitwaertsrendite.id(), RatioFieldDescription.mdpsUnchangedYieldRelative);
        put(VwdFieldDescription.ADF_Seitwaertsrendite_pa.id(), RatioFieldDescription.mdpsUnchangedYieldRelativePerYear);
        put(VwdFieldDescription.ADF_Bonusertrag.id(), RatioFieldDescription.mdpsYield);
        put(VwdFieldDescription.ADF_Bonusrendite.id(), RatioFieldDescription.mdpsYieldRelative);
        put(VwdFieldDescription.ADF_Bonusrendite_pa.id(), RatioFieldDescription.mdpsYieldRelativePerYear);
        put(VwdFieldDescription.ADF_Aufgeld_Abs.id(), RatioFieldDescription.mdpsAgio);
        put(VwdFieldDescription.ADF_Aufgeld_Proz.id(), RatioFieldDescription.mdpsAgioRelative);
        put(VwdFieldDescription.ADF_Aufgeld_PA.id(), RatioFieldDescription.mdpsAgioRelativePerYear);
        put(VwdFieldDescription.ADF_Datum_Kursschwelle_erreicht.id(), RatioFieldDescription.mdpsDateBarrierReached);
        put(VwdFieldDescription.ADF_Abstand_untere_Barriere.id(), RatioFieldDescription.mdpsGapLowerBarrier);
        put(VwdFieldDescription.ADF_Abstand_untere_Barriere_Proz.id(), RatioFieldDescription.mdpsGapLowerBarrierRelative);
        put(VwdFieldDescription.ADF_Abstand_obere_Barriere.id(), RatioFieldDescription.mdpsGapUpperBarrier);
        put(VwdFieldDescription.ADF_Abstand_obere_Barriere_Proz.id(), RatioFieldDescription.mdpsGapUpperBarrierRelative);
        put(VwdFieldDescription.ADF_hist_Performance.id(), RatioFieldDescription.mdpsPerformanceAlltime);
        put(VwdFieldDescription.ADF_notwendige_Performance.id(), RatioFieldDescription.mdpsCapToUnderlyingRelative);
        put(VwdFieldDescription.ADF_Abstand_Strike.id(), RatioFieldDescription.mdpsGapStrike);
        put(VwdFieldDescription.ADF_Abstand_Strike_Proz.id(), RatioFieldDescription.mdpsGapStrikeRelative);
        put(VwdFieldDescription.ADF_Knock_Out_Abstand.id(), RatioFieldDescription.mdpsGapStrike);
        put(VwdFieldDescription.ADF_Knock_Out_Abstand_proz.id(), RatioFieldDescription.mdpsGapStrikeRelative);
        put(VwdFieldDescription.ADF_Abstand_Barriere.id(), RatioFieldDescription.mdpsGapBarrier);
        // no data in input file from vwd Schweinfurt, add to ADF_erlaubter_Rueckgang acc. to R-31418
        // put(VwdFieldDescription.ADF_Abstand_Barriere_Proz.id(), RatioFieldDescription.mdpsGapBarrierRelative);
        put(VwdFieldDescription.ADF_Abst_Bonuslevel.id(), RatioFieldDescription.mdpsGapBonusLevel);
        put(VwdFieldDescription.ADF_Outperformance_Punkt.id(), RatioFieldDescription.mdpsOutperformanceValue);
        put(VwdFieldDescription.ADF_Hebel_Real.id(), RatioFieldDescription.mdpsLeverage);
        // Todo check if this is wrong and should be
        // put(VwdFieldDescription.ADF_Durchschnittspreis_1M.id(), RatioFieldDescription.averagePrice1m);
        // or put(VwdFieldDescription.ADF_Durchschnittsumsatz_1M.id(), RatioFieldDescription.averagePrice1m);
        // It seems that we need to remove that mapping, see DO-32902
        //put(VwdFieldDescription.ADF_Durchschnittspreis_1M.id(), RatioFieldDescription.averageVolume1m);

        // data from one vwd field might be pushed into multiple ratio fields:
        put(VwdFieldDescription.ADF_Abst_Bonuslevel_Proz.id(),
                        RatioFieldDescription.mdpsGapUpperBarrierRelative,
                        RatioFieldDescription.mdpsGapBonusLevelRelative);
        put(VwdFieldDescription.ADF_erlaubter_Rueckgang.id(),
                        RatioFieldDescription.mdpsUnderlyingToCapRelative,
                        RatioFieldDescription.mdpsGapBarrierRelative);
    }

    private static void put(Integer id, RatioFieldDescription.Field... field) {
        VWDFIELDID_TO_RATIOFIELD.put(id, field);
    }

    private ActiveMonitor activeMonitor;

    private RatioUpdateable ratioUpdateable;

    private boolean useGzipFiles = true;

    private final List<InstrumentTypeEnum> types = new ArrayList<>();

    private File baseDir;

    private final RatiosEncoder encoder = new RatiosEncoder();

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setRatioUpdateable(RatioUpdateable ratioUpdateable) {
        this.ratioUpdateable = ratioUpdateable;
    }

    public void setUseGzipFiles(boolean useGzipFiles) {
        this.useGzipFiles = useGzipFiles;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setTypes(String... typesStr) {
        for (final String s : typesStr) {
            this.types.add(InstrumentTypeEnum.valueOf(s));
        }
        this.logger.info("<setTypes> types = " + this.types);
    }

    public void afterPropertiesSet() throws Exception {
        for (final InstrumentTypeEnum type : this.types) {
            final Resource resource = new FileResource(getFile(type));

            this.activeMonitor.addResource(resource);

            resource.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    try {
                        readFile(type);
                    } catch (IOException e) {
                        logger.info("<readFile> failed", e);
                    }
                }
            });
        }
    }

    private File getFile(InstrumentTypeEnum type) {
        return new File(this.baseDir, "mdps-ratios-" + type.name().toLowerCase() + ".csv"
                + (this.useGzipFiles ? ".gz" : ""));
    }

    public void readFile(InstrumentTypeEnum type) throws IOException {
        final TimeTaker tt = new TimeTaker();

        final File file = getFile(type);
        this.logger.info("<readFile> new mdps file, starting to read " + file.getName());

        int numErrors = 0;

        try (InputStream is = file.getName().endsWith(".gz")
                    ? new GZIPInputStream(new FileInputStream(file))
                    : new FileInputStream(file); Scanner scanner = new Scanner(is)) {

            final String header = scanner.nextLine();
            final int[] fields = getFields(header, type);

            final int numRequiredValues = fields.length + 2;

            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                if ("end".equals(line)) {
                    break;
                }
                String[] values = StringUtils.commaDelimitedListToStringArray(line);
                if (values.length != numRequiredValues) {
                    values = Arrays.copyOf(values, numRequiredValues);
                }

                final long instrumentid = Long.parseLong(values[0]);
                final long quoteid = Long.parseLong(values[1]);

                this.encoder.reset(type, instrumentid, quoteid);
                try {
                    readData(fields, values);
                } catch (Exception e) {
                    if (++numErrors <= MAX_NUM_ERRORS_PER_FILE) {
                        this.logger.warn("<readFile> failed for line '" + line + "'", e);
                        continue;
                    }
                    else {
                        this.logger.error("<readFile> more than " + MAX_NUM_ERRORS_PER_FILE
                                + " errors in " + file.getName() +", giving up");
                        return;
                    }
                }
                this.ratioUpdateable.update(ByteBuffer.wrap(this.encoder.getData()));
            }

            this.logger.info("<readFile> read " + file.getName() + " in " + tt
                    + " with " + numErrors + " errors");

        } catch (Exception e) {
            this.logger.error("<readFile> failed ", e);
        }
    }

    private int[] getFields(String header, InstrumentTypeEnum type) {
        final String[] fieldsStr = StringUtils.commaDelimitedListToStringArray(header);
        final int[] fields = new int[fieldsStr.length - 2];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = Integer.parseInt(fieldsStr[i + 2]);
            final RatioFieldDescription.Field[] ratioFields = VWDFIELDID_TO_RATIOFIELD.get(fields[i]);
            if (ratioFields == null) {
                continue;
            }
            for (RatioFieldDescription.Field ratioField : ratioFields) {
                if (ratioField.isStatic()) {
                    this.logger.error("<getFields> trying to set static ratio field {} type {}",
                            ratioField, type);
                }
            }
        }
        return fields;
    }

    void readData(int[] fieldids, String[] values) {
        final Map<RatioFieldDescription.Field, String> mappedValues
                = new HashMap<>(VWDFIELDID_TO_RATIOFIELD.size());

        for (int i = 0; i < fieldids.length; i++) {
            final String str = values[i + 2];
            final boolean empty = !StringUtils.hasText(str);

            final RatioFieldDescription.Field[] ratiofields = VWDFIELDID_TO_RATIOFIELD.get(fieldids[i]);

            if (ratiofields == null) {
//                final VwdFieldDescription.Field f = VwdFieldDescription.getField(fieldids[i]);
//                this.logger.warn("<readData> no ratio field defined for " + f.name() + "/" + f.id());
                continue;
            }

            for (final RatioFieldDescription.Field ratiofield : ratiofields) {
                final String s = mappedValues.get(ratiofield);
                if (s != null) {
                    continue;
                }
                mappedValues.put(ratiofield, empty ? null : str);
            }
        }

        for (final Map.Entry<RatioFieldDescription.Field, String> entry : mappedValues.entrySet()) {
            final RatioFieldDescription.Field ratiofield = entry.getKey();
            final String str = entry.getValue();
            final boolean empty = str == null;
            final byte[] bytes = empty ? null : ByteUtil.toBytes(str);

            try {
                switch (ratiofield.type()) {
                    case DECIMAL:
                        final long price = empty ? Long.MIN_VALUE : parsePrice(bytes);
                        this.encoder.add(ratiofield, empty
                                ? Long.MIN_VALUE
                                : ratiofield.isPercent() ? price / 100 : price);
                        break;
                    case DATE:
                        this.encoder.add(ratiofield, empty
                                ? Integer.MIN_VALUE
                                : getDate(str.trim(), bytes));
                        break;
                    case TIMESTAMP:
                        this.encoder.add(ratiofield, empty
                                ? Long.MIN_VALUE
                                : getTimeStamp(str.trim()));
                        break;
                    default:
                        this.logger.warn("<readData> unknown ratio field type: " + ratiofield.type());
                        break;
                }
            } catch (Exception e) {
                this.logger.warn("<readData> failed for " + str + " for ratio field " + ratiofield.id()
                        + ", fields: " + Arrays.toString(fieldids) + ", values: " + Arrays.toString(values), e);
            }
        }

        patchupSomeTimeAndDateValues(fieldids, values);

    }

    private long parsePrice(byte[] bytes) {
        long encoded = ParserVwdHelper.getPriceAsLong(bytes, 0, bytes.length);
        return PriceCoder.toDefaultEncoding(encoded);
    }

    private void patchupSomeTimeAndDateValues(int[] fieldids, String[] values) {
        int calcTime = Integer.MIN_VALUE;
        int calcDate = Integer.MIN_VALUE;
        int barrTime = Integer.MIN_VALUE;
        int barrDate = Integer.MIN_VALUE;

        for (int i = 0; i < fieldids.length; i++) {
            final int fieldid = fieldids[i];
            final String str = values[i + 2];

            if (!StringUtils.hasText(str)) {
                continue;
            }

            if (fieldid == VwdFieldDescription.ADF_Berechnungszeit.id()) {
                calcTime = ParserVwdHelper.getTimeAsSecondsInDay(ByteUtil.toBytes(str), 0, 8);
            }
            else if (fieldid == VwdFieldDescription.ADF_Berechnungsdatum.id()) {
                calcDate = ParserVwdHelper.getDateChicagoDpAsYyyymmdd(ByteUtil.toBytes(str), 0, 10);
            }
            else if (fieldid == VwdFieldDescription.ADF_Zeit_Barriereberuehrung.id()) {
                barrTime = ParserVwdHelper.getTimeAsSecondsInDay(ByteUtil.toBytes(str), 0, 8);
            }
            else if (fieldid == VwdFieldDescription.ADF_Datum_Barriereberuehrung.id()) {
                barrDate = getDate(str, ByteUtil.toBytes(str));
            }
        }

        if (calcTime >= 0 && calcDate >= 0) {
            this.encoder.add(RatioFieldDescription.externalReferenceTimestamp, calcDate * 100_000L + calcTime);
        }
        if (barrTime >= 0 && barrDate >= 0) {
            this.encoder.add(RatioFieldDescription.mdpsDateBarrierReached, barrDate * 100_000L + barrTime);
        }
    }

    private int getDate(String str, byte[] bytes) {
        if (str.length() != 10) {
            return Integer.MIN_VALUE;
        }
        if (str.charAt(4) == '-' && str.charAt(7) == '-') { // yyyy-MM-dd
            return DateUtil.toYyyyMmDd(ISODateTimeFormat.date().parseDateTime(str));
        }
        if (str.charAt(2) == '.' && str.charAt(5) == '.') { // dd.MM.yyyy
            return DateUtil.toYyyyMmDd(DTF.parseDateTime(str));
        }
        if (str.charAt(4) == '.' && str.charAt(7) == '.') { // yyyy.MM.dd
            return ParserVwdHelper.getDateChicagoDpAsYyyymmdd(bytes, 0, 10);
        }
        this.logger.info("<getDate> parsing failed for '" + str + "', ignoring value ");
        return Integer.MIN_VALUE;
    }

    private long getTimeStamp(String str) {
        if (str.length() > 10) {
            // temporary hack to deal with fields that contain s.th. like "19.06.2014 11:0"
            return getTimeStamp(str.substring(0, 10));
        }
        if (TIMESTAMP_ISO.matcher(str).matches()) {
            return DateUtil.toYyyyMmDd(ISODateTimeFormat.date().parseDateTime(str)) * 100_000L;
        }
        if (TIMESTAMP_GERMAN.matcher(str).matches()) {
            return DateUtil.toYyyyMmDd(DTF.parseDateTime(str)) * 100_000L;
        }
        if (TIMESTAMP_CHICAGO.matcher(str).matches()) {
            return ParserVwdHelper.getDateChicagoDpAsYyyymmdd(str.getBytes(), 0, 10) * 100_000L;
        }
        if (TIMESTAMP_NUMBER.matcher(str).matches()) {
            return Long.valueOf(str) * 100_000L;
        }
        this.logger.info("<getTimeStamp> parsing failed for '" + str + "', ignoring value ");
        return Long.MIN_VALUE;
    }

    public static void main(String[] args) throws Exception {
        final RatiosProviderFile rpf = new RatiosProviderFile();
        rpf.setActiveMonitor(new ActiveMonitor());
        rpf.setBaseDir(new File("/Users/oflege/tmp/"));
        final Consumer updateable = new Consumer();
        rpf.setRatioUpdateable(updateable);
        rpf.setTypes("CER");
        rpf.setUseGzipFiles(true);
        rpf.afterPropertiesSet();

        rpf.readFile(InstrumentTypeEnum.CER);
        updateable.print();
    }

    private static class Consumer implements RatioUpdateable {
        private final Map<RatioFieldDescription.Field, Long> map = new HashMap<>();

        public void update(ByteBuffer buffer) {
            buffer.getInt(); // consume instrument type
            buffer.getLong(); // consume iid
            final long quoteid = buffer.getLong();
            while (buffer.hasRemaining()) {
                final int fieldid = buffer.getShort();
                final RatioFieldDescription.Field field = RatioFieldDescription.getFieldById(fieldid);
                try {
                    switch (field.type()) {
                        case DATE:
                        case TIME:
                            final int intvalue = buffer.getInt();
                            if (intvalue != Integer.MIN_VALUE && intvalue != Integer.MAX_VALUE) {
                                map.put(field, quoteid);
                            }
                            break;
                        case DECIMAL:
                        case NUMBER:
                        case TIMESTAMP:
                            final long longvalue = buffer.getLong();
                            if (longvalue != Long.MIN_VALUE && longvalue != Long.MAX_VALUE) {
                                map.put(field, quoteid);
                                if (field.type() == RatioFieldDescription.Type.TIMESTAMP) {
                                    final int yyyymmdd = (int) (longvalue / 100_000L);
                                    final int secondsInDay = (int) (longvalue % 100_000L);
//                                    System.out.println(quoteid + ": " + DateUtil.toDateTime(yyyymmdd, secondsInDay));
                                }
                            }
                            break;
                    }
                } catch (Exception e) {
                    System.out.println("<update> failed for field " + field);
                    throw new RuntimeException(e);
                }
            }
        }

        public void print() {
            for (final Map.Entry<RatioFieldDescription.Field, Long> entry : map.entrySet()) {
                System.out.println(entry.getKey().name() + ": " + entry.getValue() + ".qid");
            }
        }
    }
}