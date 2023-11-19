/*
 * PriceProviderFile.java
 *
 * Created on 14.09.2005 19:48:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongList;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.snap.AbstractIndexAndOffsetFactory;
import de.marketmaker.istar.feed.snap.IndexAndOffset;
import de.marketmaker.istar.feed.snap.IndexAndOffsetFactory;
import de.marketmaker.istar.feed.vwd.ParserVwdHelper;
import de.marketmaker.istar.feed.vwd.SnapRecordVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

/**
 * Provides SnapRecord data read from files; each line in a file is a list of comma separated items,
 * the first item is the qid, the latter items represent the values of fields; which fields are
 * supplied is defined in the first line of the file.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceProviderFile implements PriceProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * TODO: elements are never removed from this map, what about expired ones?
     */
    private final Long2ObjectMap<Record> qidToRecord = new Long2ObjectOpenHashMap<>(1 << 20);

    /**
     * Many lines contain values for the same fields, so use this objects to make sure they share the
     * same IndexAndOffset objects.
     */
    private final IndexAndOffsetFactory iaof = new AbstractIndexAndOffsetFactory() {
        protected int getLength(int fieldid) {
            return VwdFieldDescription.getField(fieldid).length();
        }
    };

    /**
     * Data from one line in an input file
     */
    private static class Record {
        private final IndexAndOffset iao;

        private final byte[] data;

        Record(IndexAndOffset iao, byte[] data) {
            this.iao = iao;
            this.data = data;
        }

        synchronized SnapRecord toSnapRecord() {
            // the SnapRecord must never change; since this object can be updated, we have to
            // sync and copy the data
            return new SnapRecordVwd(this.iao.getIndexArray(), this.iao.getOffsetArray(),
                    Arrays.copyOf(this.data, this.data.length), 0);
        }

        synchronized void update(Record source) {
            System.arraycopy(source.data, 0, this.data, 0, this.data.length);
        }

        synchronized boolean hasSameDataAs(Record other) {
            return Arrays.equals(this.data, other.data);
        }

        boolean hasSameFieldsAs(Record other) {
            // can use == because iao comes from the iaof
            return this.iao == other.iao;
        }
    }

    public LongCollection getAllQuoteids() {
        synchronized (this.qidToRecord) {
            return new LongArrayList(this.qidToRecord.keySet());
        }
    }

    public LongCollection read(File file) throws IOException {
        final LongList result = new LongArrayList(4000);

        try (InputStream is = file.getName().endsWith(".gz")
                ? new GZIPInputStream(new FileInputStream(file))
                : new FileInputStream(file);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            final String header = br.readLine();
            final int[] fields = getFields(header);

            final int numRequiredValues = fields.length + 1;

            int lineNo = 1;
            int num = 0;
            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if ("end".equals(line)) {
                    break;
                }
                final String[] values = StringUtils.commaDelimitedListToStringArray(line);
                if (values.length != numRequiredValues) {
                    this.logger.warn("<read> #values/" + values.length + " <> " + numRequiredValues
                            + " in line " + lineNo + ": '" + line + "'");
                    continue;
                }
                num++;

                final long changedQuoteid = addRecord(fields, values);
                if (changedQuoteid != Long.MIN_VALUE) {
                    result.add(changedQuoteid);
                }
            }

            this.logger.info("<read> " + num + " from " + file.getName() + ", "
                    + result.size() + " changed;"
                    + " #records=" + size() + ", #iaof=" + this.iaof.size());
        } catch (Exception e) {
            this.logger.warn("<read> failed ", e);
        }

        return result;
    }

    private int size() {
        synchronized (this.qidToRecord) {
            return this.qidToRecord.size();
        }
    }

    public SnapRecord getSnapRecord(long quoteid) {
        final Record pd = getRecord(quoteid);
        return (pd != null) ? pd.toSnapRecord() : null;
    }

    private int[] getFields(String header) {
        final String[] fieldsStr = StringUtils.commaDelimitedListToStringArray(header);
        final int[] fields = new int[fieldsStr.length - 1];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = Integer.valueOf(fieldsStr[i + 1]);
        }
        return fields;
    }

    private long addRecord(int[] fields, String[] values) {
        final IndexAndOffset iao = getIndexAndOffset(fields, values);

        final Long qid = Long.valueOf(values[0]);

        final Record newRecord = toRecord(iao, fields, values);
        if (newRecord == null) {
            return Long.MIN_VALUE;
        }

        final Record oldRecord = getRecord(qid);

        if (oldRecord != null && oldRecord.hasSameFieldsAs(newRecord)) {
            if (oldRecord.hasSameDataAs(newRecord)) {
                return Long.MIN_VALUE;
            }
            // in-place update = less gc
            oldRecord.update(newRecord);
        }
        else {
            putRecord(qid, newRecord);
        }
        return qid;
    }

    private Record getRecord(long qid) {
        synchronized (this.qidToRecord) {
            return this.qidToRecord.get(qid);
        }
    }

    private void putRecord(long qid, Record r) {
        synchronized (this.qidToRecord) {
            this.qidToRecord.put(qid, r);
        }
    }

    private IndexAndOffset getIndexAndOffset(int[] fields, String[] values) {
        final int[] tmp = new int[fields.length + 1];
        int numFields = 0;
        for (int i = 0, n = fields.length; i < n; i++) {
            if (StringUtils.hasText(values[i + 1])) {
                tmp[numFields++] = fields[i];
            }
        }
        tmp[numFields++] = Integer.MAX_VALUE;
        return this.iaof.getIndexAndOffset(Arrays.copyOf(tmp, numFields));
    }

    private Record toRecord(IndexAndOffset iao, int[] fields, String[] values) {
        final int[] indexedFields = iao.getIndexArray();
        final int[] offsets = iao.getOffsetArray();

        final ByteBuffer bb = ByteBuffer.allocate(iao.getSize());
        for (int i = 0, n = indexedFields.length - 1; i < n; i++) {
            int fid = indexedFields[i];
            String value = values[ArrayUtils.indexOf(fields, fid) + 1];
            if (!StringUtils.hasText(value)) {
                continue;
            }
            bb.position(offsets[i]);
            final VwdFieldDescription.Field f = VwdFieldDescription.getField(fid);

            try {
                final byte[] bytes = ByteUtil.toBytes(value);
                switch (f.type()) {
                    case UINT:
                    case USHORT:
                        final long number = Long.valueOf(value);
                        bb.putInt((int) number);
                        break;
                    case TIME:
                        final int time = ParserVwdHelper.getTimeAsSecondsInDay(bytes, 0, 8);
                        bb.putInt(time);
                        break;
                    case DATE:
                        final int date = ParserVwdHelper.getDateChicagoDpAsYyyymmdd(bytes, 0, 10);
                        bb.putInt(date);
                        break;
                    case PRICE:
                        final long price = ParserVwdHelper.getPriceAsLong(bytes, 0, bytes.length);
                        bb.putLong(price);
                        break;
                    case TIMESTAMP:
                        // ??
                        break;
                    case STRING:
                        final int maxLen = offsets[i + 1] - offsets[i];
                        bb.put(bytes, 0, Math.min(maxLen, bytes.length));
                        if (bytes.length < maxLen) {
                            bb.put((byte) 0);
                        }
                        break;
                }
            } catch (Exception e) {
                this.logger.warn("<toRecord> failed for " + value + " for field " + f
                        + ", fields: " + Arrays.toString(indexedFields) + ", values: " + Arrays.toString(values), e);
                return null;
            }
        }

        return new Record(iao, bb.array());
    }

    public static void main(String[] args) throws IOException {
        final PriceProviderFile ppf = new PriceProviderFile();

/*        final int[] header = ppf.getFields("quote,25,26,28,29,30,31,36,53,63,67,80,81,82,83,109,121,122,123,124,128,130,133,135,212,213,1001,1002,1008");
        final String[] a = StringUtils.commaDelimitedListToStringArray("13953633,2008.05.20,20:54:56,27.31,100,27.28,200,2527000,28.07,27.15,27.58,27.29,100,20:54:55,1910400,,2008.05.19,28.14,,AD,28.82,27.96,28.25,31.40,19.78,20:54:56,2008.05.20,20:54:56,20:54:55,2008.05.20");
        final String[] b = StringUtils.commaDelimitedListToStringArray("13953635,,,0.00,0,0.00,0,,0.00,0.00,0.00,0.00,0,00:00:00,0,,,,,,,,0.00,0.00,18:31:32,2008.05.20,,13:27:17,2008.05.20");

        System.out.println(header.length + " " +a.length+" "+b.length);
        System.exit(0);
  */
        final TimeTaker tt = new TimeTaker();
//        File f = new File("/Users/oflege/tmp/ratio-prices-cer.csv.gz");
        File f = new File(LocalConfigProvider.getProductionBaseDir(), "/var/data/ratiotool-exchange/dp2out/ratio-prices-wnt.csv.gz");
        ppf.read(f);
        tt.stop();
        System.out.println(ppf.getSnapRecord(467801));
        System.out.println(tt);

    }
}
