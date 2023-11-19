/*
 * TickDirectory.java
 *
 * Created on 01.08.14 09:29
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTimeConstants;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickFiles;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

import static de.marketmaker.istar.feed.FeedMarket.PARTITION_SEPARATOR;
import static de.marketmaker.istar.feed.ordered.tick.FileTickStore.*;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.*;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

/**
 * Entry class for all applications that need to read ticks including corrections.
 * Corrections as handled by this class apply only to compressed tick files (name ends with .tdz).
 * Whereas intraday tick corrections will be integrated into the regular tick data stored in
 * a .td3 file, tick corrections for compressed tick files will be stored separately: Per tick
 * directory <tt>yyyyMMdd</tt>, there may be a file <tt>corrections-yyyyMMdd.tdz</tt> with
 * tick data and an associated index file <tt>corrections-yyyyMMdd.tdx</tt> which stores
 * when corrections have been applied, for which symbols, and where those ticks are stored
 * in the corrections file.
 * <p>
 * The files that contain corrected ticks and the respective index are created/updated by
 * an instance of {@link de.marketmaker.istar.feed.ordered.tick.HistoricTickCorrections}.
 * </p>
 * <p>
 * Also provides support for partitioned markets: For some markets, it makes sense to partition
 * the keys into disjunct sets and create a tick file for each such set, as otherwise the
 * individual tick files would become too large (e.g., +50gb for a single day). Partitioned files
 * are named <code>MARKET^N-yyyyMMdd.suffix</code> where n is the partition number. For a market
 * partitioned into <code>2^i</code> parts, <code>N</code> will be in the range <code>0..2^i - 1~</code>
 * </p>
 *
 * @author oflege
 */
public class TickDirectory implements /* for ehcache: */ Serializable {

    static class CorrectionInfo {
        /**
         * Encoded offset and initial chunk length of the ticks in the corrections file.
         * In contrast to file addresses in regular tick file indexes, this address will not
         * have the {@link de.marketmaker.istar.feed.ordered.tick.TickWriter#FILE_ADDRESS_FLAG}
         * bit set. This allows to distinguish addresses from tick files and addresses from
         * correction files, which is needed by
         * {@link de.marketmaker.istar.feed.ordered.tick.TickFileReader#readTicks(long, int)}
         */
        final long address;

        /**
         * total length of all chunks, i.e., number of tickbytes
         */
        final int length;

        private CorrectionInfo(long address, int length) {
            this.address = address;
            this.length = length;
        }
    }

    private static String toVwdcode(String key, Matcher m) {
        if (m.group(1) == null && m.group(5) == null) {
            return key;
        }
        return m.group(2) + m.group(3) + (m.group(4) != null ? m.group(4) : "");
    }

    protected static final long serialVersionUID = 2L;

    private static final Pattern FILE_NAME
            = Pattern.compile("([0-9A-Z_]+)(\\^\\d+)?-(20[0-9]{6})(\\.[dt]d[3z])");

    private transient Logger logger = LoggerFactory.getLogger(getClass());

    private final File dir;

    private final String suffix;

    /**
     * for each partitioned market in dir, contains the partition mask (i.e., number of partitions -1)
     */
    private final Map<String, Integer> partitions;

    private volatile transient long lastRefresh;

    private volatile transient long lastModifiedCorrections;

    private AbstractTickRecord.TickItem.Encoding encoding;

    /**
     * effectively final, but not declared final as it is transient and has to be set in readObject
     */
    private transient Map<String, CorrectionInfo> corrections = Collections.emptyMap();

    public static TickDirectory open(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return null;
        }

        final Map<String, MutableInt> countsBySuffix = new HashMap<>();
        final Map<String, MutableInt> partitions = new HashMap<>();
        //noinspection ResultOfMethodCallIgnored
        dir.listFiles((d, name) -> {
            Matcher m = FILE_NAME.matcher(name);
            if (m.matches()) {
                MutableInt mi = countsBySuffix.get(m.group(4));
                if (mi == null) {
                    countsBySuffix.put(m.group(4), mi = new MutableInt());
                }
                mi.increment();
                if (m.group(2) != null) {
                    MutableInt pi = partitions.get(m.group(1));
                    if (pi == null) {
                        partitions.put(m.group(1), pi = new MutableInt());
                    }
                    // we need to figure out the number of partitions, which can be computed
                    // by calculating the smallest 2^x that is larger than n, where n is the
                    // file's partition number
                    int i = Math.max(1, Integer.parseInt(m.group(2).substring(1)));
                    pi.setValue(Math.max(pi.intValue(), Integer.highestOneBit(i) << 1));
                }
            }
            return m.matches();
        });
        if (countsBySuffix.isEmpty()) {
            return null;
        }
        if (countsBySuffix.size() > 1) {
            LoggerFactory.getLogger(TickDirectory.class).warn("<open> mixed content in " + dir.getAbsolutePath()
                    + ": " + countsBySuffix);
            return null;
        }

        final String suffix = countsBySuffix.keySet().iterator().next();
        return new TickDirectory(dir, suffix, toPartitionMap(partitions));
    }

    private static Map<String, Integer> toPartitionMap(Map<String, MutableInt> partitions) {
        if (partitions.isEmpty()) {
            return null;
        }
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, MutableInt> e : partitions.entrySet()) {
            result.put(e.getKey(), e.getValue().intValue() - 1);
        }
        return result;
    }

    private static AbstractTickRecord.TickItem.Encoding getEncoding(String suffix) {
        switch (suffix) {
            case TD3:
                return TICK3;
            case TDZ:
                return TICKZ;
            case DD3:
                return DUMP3;
            case DDZ:
                return DUMPZ;
            default:
                throw new IllegalStateException(suffix);
        }
    }

    TickDirectory(File dir, String suffix, Map<String, Integer> partitions) {
        this.suffix = suffix;
        this.encoding = getEncoding(suffix);
        this.dir = dir;
        this.corrections = readCorrections();
        this.partitions = partitions;
    }

    public File getDir() {
        return dir;
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.logger = LoggerFactory.getLogger(getClass());
        this.corrections = readCorrections();
    }

    public TickDirectory refresh() {
        if (!mayHaveCorrections()) {
            return this;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastRefresh < DateTimeConstants.MILLIS_PER_MINUTE) {
            return this;
        }
        this.lastRefresh = now;
        File f = getCorrectionsIndexFile();
        if (!f.canRead()) {
            if (this.corrections.isEmpty()) {
                return this;
            }
        }
        else if (f.lastModified() == this.lastModifiedCorrections) {
            return this;
        }
        return new TickDirectory(this.dir, this.suffix, this.partitions);
    }

    public boolean isValid() {
        return this.dir.isDirectory();
    }

    private boolean mayHaveCorrections() {
        return TDZ.equals(this.suffix);
    }

    private Map<String, CorrectionInfo> readCorrections() {
        this.lastRefresh = System.currentTimeMillis();
        if (!mayHaveCorrections()) {
            return Collections.emptyMap();
        }
        File f = getCorrectionsIndexFile();
        if (!f.canRead()) {
            return Collections.emptyMap();
        }
        this.lastModifiedCorrections = f.lastModified();
        return readCorrections(f);
    }

    /**
     * The corrections index file is a text file and each line has the format
     * <pre>timestamp;vwdcode;address;length;patchfile</pre>
     * where
     * <dl>
     * <dt>timestamp</dt>
     * <dd>when the correction has been added to this file</dd>
     * <dt>vwdcode</dt>
     * <dd>symbol for which a correction is available</dd>
     * <dt>address</dt>
     * <dd>encoded offset and first chunk length of the corrected ticks, as hex number</dd>
     * <dt>length</dt>
     * <dd>number of tick bytes in the correction file for that vwdcode, as hex number</dd>
     * <dt>patchfile</dt>
     * <dd>name of the patchfile used to write the corrected ticks</dd>
     * </dl>
     * A <code>vwdcode</code> may appear more than once in the index file if multiple corrections
     * have been applied. In this case, the latest entry "wins".
     * @param f corrections index file
     * @return corrections read from <code>f</code>, keyed by vwdcode
     */
    private Map<String, CorrectionInfo> readCorrections(File f) {
        this.logger.info("<readCorrections> from " + f.getAbsolutePath());
        Map<String, CorrectionInfo> result = new HashMap<>();
        try (Scanner sc = new Scanner(f, StandardCharsets.UTF_8.name())) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] values = line.split(";");
                if (values.length > 3) {
                    result.put(values[1], new CorrectionInfo(parseLong(values[2], 16), parseInt(values[3], 16)));
                }
                else {
                    this.logger.warn("<readCorrections> cannot parse: " + line);
                }
            }
            return result;
        } catch (IOException e) {
            this.logger.error("<readCorrections> failed for " + f.getAbsolutePath(), e);
            return Collections.emptyMap();
        }
    }

    public TickFileReader getTickFileReader(String market) throws IOException {
        return getTickFileReader(getFile(market));
    }

    public TickFileReader getTickFileReader(File f) throws IOException {
        if (!this.dir.equals(f.getParentFile()) || !f.getName().endsWith(this.suffix)) {
            throw new IllegalArgumentException(f.getAbsolutePath());
        }
        Map<ByteString, CorrectionInfo> correctionsForMarket = getCorrectionsForMarket(f);
        if (correctionsForMarket.isEmpty()) {
            return new TickFileReader(f, null, null);
        }
        else {
            return new TickFileReader(f, getCorrectionsFile(), correctionsForMarket);
        }
    }

    private Map<ByteString, CorrectionInfo> getCorrectionsForMarket(File f) {
        Map<ByteString, CorrectionInfo> result = Collections.emptyMap();
        Map<String, CorrectionInfo> map = this.corrections;
        if (map.isEmpty()) {
            return result;
        }
        String market = "." + TickFiles.getMarketBaseName(f);
        for (Map.Entry<String, CorrectionInfo> e : map.entrySet()) {
            if (!e.getKey().contains(market)) {
                continue;
            }
            final Matcher m = VendorkeyVwd.KEY_PATTERN.matcher(e.getKey());
            if (!m.matches() || !market.equals(m.group(3))) {
                continue;
            }
            final String key = FileTickStore.toVwdcodeWithoutMarket(m);
            if (result.isEmpty()) {
                result = new HashMap<>();
            }
            result.put(new ByteString(key), e.getValue());
        }
        return result;
    }

    public AbstractTickRecord.TickItem.Encoding getEncoding() {
        return this.encoding;
    }

    /**
     * Reads ticks for vendorkey. If no corrections are available, forwards reading to <code>store</code>,
     * otherwise the corrected ticks are read from the correction file and returned.
     * <p>
     * <b>Important:</b> do not use this methods to read the ticks for all symbols of a given market,
     * as the tickfile would be opened and closed for every symbol. Instead, use
     * {@link #getTickFileReader(java.io.File)} or {@link #getTickFileReader(String)} to obtain a
     * reader that keeps the tick file for a certain market open and thus performs better.
     * </p>
     * @param store provides regular ticks
     * @param vendorkey
     * @return ticks or null if none are available.
     */
    public byte[] readTicks(FileTickStore store, String vendorkey) {
        final Matcher m = VendorkeyVwd.KEY_PATTERN.matcher(vendorkey);
        if (!m.matches()) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<readTicks> invalid key '" + vendorkey + "'");
            }
            return null;
        }

        final String vwdcode = toVwdcode(vendorkey, m);

        Map<String, CorrectionInfo> map = this.corrections;
        if (!map.isEmpty()) {
            CorrectionInfo info = map.get(vwdcode);
            if (info != null) {
                return readCorrectedTicks(vwdcode, info);
            }
        }

        File f = getFile(getMarketFor(m, vwdcode));
        if (!f.canRead()) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<readTicks> no such file: " + f.getAbsolutePath());
            }
            return null;
        }
        try {
            return store.readTicks(f, vendorkey);
        } catch (IOException e) {
            this.logger.error("<readTicks> failed for " + vendorkey + " in " + f.getAbsolutePath(), e);
            return null;
        }
    }

    private String getMarketFor(Matcher m, String vwdcode) {
        String market = m.group(3).substring(1);
        int p = getPartition(market);
        if (p == 0) {
            return market;
        }
        return market + PARTITION_SEPARATOR + (new ByteString(vwdcode).hashCode() & p);
    }

    private int getPartition(String market) {
        if (this.partitions == null) {
            return 0;
        }
        final Integer p = this.partitions.get(market);
        return (p != null) ? p : 0;
    }

    private byte[] readCorrectedTicks(String vwdcode, CorrectionInfo info) {
        ByteBuffer bb = ByteBuffer.allocate(info.length);
        File f = getCorrectionsFile();
        try (RandomAccessFile raf = new RandomAccessFile(f, "r");
             FileChannel fc = raf.getChannel()) {
            FileTickStore.fillData(fc, info.address | TickWriter.FILE_ADDRESS_FLAG, bb);
            return bb.array();
        } catch (IOException e) {
            this.logger.error("<readTicks> failed for " + vwdcode + " in " + f.getAbsolutePath(), e);
            return null;
        }
    }

    File getCorrectionsIndexFile() {
        return getFile("corrections", ".cdx");
    }

    File getCorrectionsFile() {
        return getFile("corrections", ".cdz");
    }

    private File getFile(String market) {
        return getFile(market, this.suffix);
    }

    private File getFile(String name, String fileSuffix) {
        return new File(this.dir, name + "-" + this.dir.getName() + fileSuffix);
    }
}
