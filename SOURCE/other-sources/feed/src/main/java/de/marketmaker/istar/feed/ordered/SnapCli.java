/*
 * SnapCli.java
 *
 * Created on 07.05.13 13:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Exchanger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.HexDump;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapFieldComparators;
import de.marketmaker.istar.domain.instrument.ContentFlags;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;
import de.marketmaker.istar.feed.VolatileFeedDataRegistry;
import de.marketmaker.istar.feed.delay.DelayProviderImpl;
import de.marketmaker.istar.feed.snap.SnapData;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp.encodeTimestamp;
import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp.toDateTime;
import static org.joda.time.format.ISODateTimeFormat.dateHourMinuteSecond;
import static org.joda.time.format.ISODateTimeFormat.dateOptionalTimeParser;

/**
 * Sample usage:
 * <dl>
 * <dt><tt>SnapCli /path/to/snap.sd3 filter 620200.FFM 620200.ffm.sd3</tt></dt>
 * <dd>Takes snap.sd3 and creates a new snap file that only contains data for 620200.FFM</dd>
 * </dl>
 * @author oflege
 */
public class SnapCli {
    private static final Boolean WITH_TYPE = Boolean.getBoolean("withType");

    // this allows us to write a snap file while reading another, so we don't need a 10g VM
    private static class FeedDataExchangingCallback implements OrderedFileSnapStore.RestoreCallback,
            Iterator<FeedData> {
        private final Exchanger<FeedData> e = new Exchanger<>();

        private boolean done = false;

        protected FeedData next;

        private FeedData getNext() {
            try {
                return e.exchange(null);
            } catch (InterruptedException e1) {
                throw new RuntimeException(e1);
            }
        }

        @Override
        public boolean hasNext() {
            while (this.next == null && !done) {
                if ((this.next = getNext()) != null) {
                    if (!isNextAcceptable()) {
                        this.next = null;
                    }
                }
                else {
                    done = true;
                }
            }
            return next != null;
        }

        protected boolean isNextAcceptable() {
            return true;
        }

        protected OrderedSnapData getOrderedSnapData() {
            return (OrderedSnapData) this.next.getSnapData(true);
        }

        @Override
        public FeedData next() {
            onNext();
            final FeedData data = this.next;
            this.next = null;
            return data;
        }

        protected void onNext() {
        }

        @Override
        public void remove() {
        }

        @Override
        public void restored(FeedData data, long offset) {
            try {
                e.exchange(data);
            } catch (InterruptedException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    private static class FieldUpdateCallback extends FeedDataExchangingCallback {
        private final FieldDataMerger merger = new FieldDataMerger();

        private final int timestamp;

        private final BufferFieldData update;

        private final BufferFieldData existing = new BufferFieldData();

        public FieldUpdateCallback(int timestamp, BufferFieldData update) {
            this.timestamp = timestamp;
            this.update = update;
        }

        @Override
        protected boolean isNextAcceptable() {
            return this.timestamp == 0 || getOrderedSnapData().getLastUpdateTimestamp() > this.timestamp;
        }

        @Override
        protected void onNext() {
            if (this.update == null) {
                return;
            }
            OrderedSnapData osd = getOrderedSnapData();
            if (osd.isInitialized()) {
                this.update.rewind();
                existing.reset(osd.getData(false));
                byte[] merged = this.merger.merge(existing, this.update);
                if (merged != null) {
                    osd.init(null, merged);
                }
            }
        }
    }


    private static class Validator implements OrderedFileSnapStore.RestoreCallback {
        private final BufferFieldData fieldData = new BufferFieldData();

        private final StringBuilder sb = new StringBuilder(40);

        private final boolean rt;

        private final boolean nt;

        public Validator(Boolean rtNt) {
            this.rt = rtNt == null || rtNt;
            this.nt = rtNt == null || !rtNt;
        }

        @Override
        public void restored(FeedData data, long offset) {
            if (this.rt) {
                validate(data, true);
            }
            if (this.nt) {
                validate(data, false);
            }
        }

        private void validate(FeedData data, boolean rt) {
            final SnapData snapData = data.getSnapData(rt);
            if (snapData == null || !snapData.isInitialized()) {
                return;
            }
            final byte[] bytes = snapData.getData(false);
            sb.setLength(0);
            fieldData.reset(bytes);
            try {
                for (int oid = fieldData.readNext(); oid > 0; oid = fieldData.readNext()) {
                    switch (fieldData.getType()) {
                        case FieldData.TYPE_INT:
                            int value = fieldData.getInt();
                            if (value != 0 && VwdFieldOrder.getField(oid).type() == VwdFieldDescription.Type.DATE) {
                                if (!isValidDate(value)) {
                                    if (sb.length() == 0) {
                                        sb.append("F;").append(data.getVendorkey().toVwdcode());
                                    }
                                    sb.append(";").append(VwdFieldOrder.getFieldId(oid)).append("=").append(value);
                                }
                            }
                            break;
                        default:
                            fieldData.skipCurrent();
                            break;
                    }
                }
                if (sb.length() > 0) {
                    System.out.println(sb);
                }
            } catch (Exception e) {
                System.err.println("Invalid data in " + data.getVendorkey() + " " + (rt ? "RT" : "NT"));
                HexDump.writeHex(System.err, bytes);
                System.err.println();
            }
        }

        private boolean isValidDate(int value) {
            int year = value / 10000;
            int month = (value % 10000) / 100;
            int day = value % 100;
            if (year >= 1900 && year <= 2200 && month > 0 && month < 13 && day > 0 && day < 29) {
                return true;
            }
            try {
                DateUtil.yyyyMmDdToLocalDate(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private static abstract class RestoreFilter implements OrderedFileSnapStore.RestoreCallback,
            InitializingBean {
        int cmin = Integer.MIN_VALUE;

        int cmax = Integer.MAX_VALUE;

        protected boolean withCreated;

        int umin = Integer.MIN_VALUE;

        int umax = Integer.MAX_VALUE;

        protected boolean withUpdated;

        protected int created = 0;

        protected int updated = 0;

        boolean withType = false;

        PrintWriter pw = new PrintWriter(System.out);

        @Override
        public void afterPropertiesSet() throws Exception {
            this.withCreated = (this.cmin != Integer.MIN_VALUE || this.cmax != Integer.MAX_VALUE);
            this.withUpdated = (this.umin != Integer.MIN_VALUE || this.umax != Integer.MAX_VALUE);
        }

        @Override
        public final void restored(FeedData data, long offset) {
            this.created = 0;
            OrderedFeedData ofd = (OrderedFeedData) data;
            if (this.withCreated) {
                this.created = ofd.getCreatedTimestamp();
                if (this.created < this.cmin || this.created >= this.cmax) {
                    return;
                }
            }
            this.updated = 0;
            OrderedSnapData osd = (OrderedSnapData) data.getSnapData(true);
            if (this.withUpdated) {
                this.updated = osd.getLastUpdateTimestamp();
                if (this.updated < this.umin || this.updated >= this.umax) {
                    return;
                }
            }
            restored(ofd, osd);
        }

        protected abstract void restored(OrderedFeedData ofd, OrderedSnapData osd);

        protected String formatTimestamp(int ts) {
            return dateHourMinuteSecond().print(toDateTime(ts));
        }

        protected ByteString getKey(OrderedFeedData data) {
            return this.withType ? data.getVendorkey().toByteString() : data.getVendorkey().toVwdcode();
        }
    }

    private static class GrepFields extends RestoreFilter {
        private final StringBuilder sb = new StringBuilder();

        private int[] oids;

        private String[] values;

        private int[] indexes;

        private boolean requireAll;

        public void setOids(int[] oids) {
            this.oids = oids.clone(); // create copy to retain original order
            Arrays.sort(this.oids);

            this.indexes = new int[oids.length];
            for (int i = 0; i < oids.length; i++) {
                indexes[i] = ArrayUtils.indexOf(this.oids, oids[i]);
            }

            this.values = new String[oids.length];
            for (int i = 0; i < values.length; i++) {
                values[indexes[i]] = VwdFieldOrder.getField(oids[i]).name();
            }
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            super.afterPropertiesSet();
            print(withType ? "VWDKEY" : "VWDCODE");
        }

        private void print(Object key) {
            sb.setLength(0);
            sb.append(key);
            if (withCreated) {
                sb.append(';').append(created == 0 ? "CREATED" : formatTimestamp(this.created));
            }
            if (withUpdated) {
                sb.append(';').append(updated == 0 ? "UPDATED" : formatTimestamp(this.updated));
            }
            for (int i = 0; i < values.length; i++) {
                sb.append(";").append(values[indexes[i]]);
            }
            pw.println(sb);
        }

        @Override
        protected void restored(OrderedFeedData data, OrderedSnapData osd) {
            if (!osd.isInitialized()) {
                return;
            }
            Arrays.fill(this.values, "");
            BufferFieldData bfd = new BufferFieldData(osd.getData(false));
            int n = 0;
            LOOP:
            for (int oid = bfd.readNext(); oid > 0; oid = bfd.readNext()) {
                while (oid > oids[n]) {
                    if (requireAll) {
                        return;
                    }
                    if (++n == oids.length) {
                        break LOOP;
                    }
                }
                if (oid < oids[n]) {
                    bfd.skipCurrent();
                }
                else {
                    values[n] = formatField(bfd);
                    if (++n == oids.length) {
                        break;
                    }
                }
            }
            if (n >= (requireAll ? oids.length : 1)) {
                print(getKey(data));
            }
        }

        private String formatField(BufferFieldData bfd) {
            if (bfd.getType() == FieldData.TYPE_PRICE) {
                return formatPrice(bfd);
            }
            return String.valueOf(OrderedSnapRecord.getFieldValue(bfd));
        }

        private String formatPrice(BufferFieldData bfd) {
            int base = bfd.getInt();
            int exp = bfd.getByte();
            BigDecimal real = OrderedSnapRecord.getDecimal(base, exp, false);
            String result = real.toPlainString();
            if (BigDecimal.ZERO.compareTo(real) == 0) {
                BigDecimal escaped = OrderedSnapRecord.getDecimal(base, exp, true);
                if (BigDecimal.ZERO.compareTo(escaped) != 0) {
                    return result + "/" + escaped.toPlainString();
                }
            }
            return result;
        }
    }

    private static class GrepSymbols extends RestoreFilter {
        @Override
        protected void restored(OrderedFeedData data, OrderedSnapData osd) {
            pw.println(getKey(data));
        }

    }

    private static class Find extends RestoreFilter {

        private static final int CONTENT_FLAGS_OID
                = VwdFieldOrder.getOrder(VwdFieldDescription.ADF_ContentFlags.id());

        private BitSet required = new BitSet();

        private BitSet forbidden = new BitSet();

        private BitSet contentFlags = null;

        private boolean findAll;

        private int maxOrder;

        private int numRequired;

        public void setRequired(int[] required) {
            this.required = createBitSet(required);
        }

        public void setForbidden(int[] forbidden) {
            this.forbidden = createBitSet(forbidden);
        }

        public void setFlags(String[] flags) {
            this.contentFlags = new BitSet();
            for (String flag : flags) {
                ContentFlags.Flag cf = ContentFlags.Flag.valueOf(flag);
                this.contentFlags.set(cf.ordinal());
            }
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            super.afterPropertiesSet();
            if (this.contentFlags != null) {
                this.required.set(CONTENT_FLAGS_OID);
            }
            this.maxOrder = Math.max(this.required.length(), this.forbidden.length());
            this.findAll = maxOrder == 0;
            this.numRequired = this.required.cardinality();
        }

        private BitSet createBitSet(int[] ints) {
            if (ints == null || ints.length == 0) {
                return new BitSet();
            }
            final BitSet result = new BitSet(ints[ints.length - 1]);
            for (int i : ints) {
                result.set(i);
            }
            return result;
        }

        @Override
        protected void restored(OrderedFeedData data, OrderedSnapData osd) {
            if (find(osd)) {
                ByteString value = WITH_TYPE ? data.getVendorkey().toByteString() : data.getVwdcode();
                if (this.created != 0) {
                    System.out.println(formatTimestamp(this.created) + " " + value);
                }
                else {
                    System.out.println(value);
                }
            }
        }

        private boolean find(OrderedSnapData osd) {
            if (this.findAll) {
                return true;
            }
            BufferFieldData bfd = new BufferFieldData(osd.getData(false));
            int n = 0;
            int id = bfd.readNext();
            String cf = null;
            while (id > 0 && id < maxOrder) {
                if (numRequired != 0 && required.get(id)) {
                    n++;
                }
                if (this.forbidden.get(id)) {
                    return false;
                }
                if (id == CONTENT_FLAGS_OID) {
                    cf = new String(bfd.getBytes(), StandardCharsets.US_ASCII);
                }
                else {
                    bfd.skipCurrent();
                }
                id = bfd.readNext();
            }
            if (this.contentFlags != null) {
                if ((cf == null) || !decode(cf).intersects(this.contentFlags)) {
                    return false;
                }
            }
            return n == numRequired;
        }

        private BitSet decode(String s) {
            final byte[] bytes = Base64.decodeBase64(s);
            final BitSet result = new BitSet();
            for (int i = 0; i < bytes.length * 8; i++) {
                if ((bytes[i / 8] & (1 << (i % 8))) > 0) {
                    result.set(i);
                }
            }
            return result;
        }
    }

    private static class MinMaxExponent implements OrderedFileSnapStore.RestoreCallback {
        private final int threshold;

        public MinMaxExponent(int threshold) {
            this.threshold = threshold;
        }

        @Override
        public void restored(FeedData data, long offset) {
            OrderedSnapData osd = (OrderedSnapData) data.getSnapData(true);
            BufferFieldData bfd = new BufferFieldData(osd.getData(false));
            for (int id = bfd.readNext(); id != 0; id = bfd.readNext()) {
                if (bfd.getType() == FieldData.TYPE_PRICE) {
                    bfd.getInt();
                    int exp = bfd.getByte();
                    if (Math.abs(exp) > threshold) {
                        System.out.printf("%4d %+3d %s%n", VwdFieldOrder.getFieldId(id), exp, data.getVwdcode());
                    }
                }
                else {
                    bfd.skipCurrent();
                }
            }
        }
    }

    private static class NegativePrices implements OrderedFileSnapStore.RestoreCallback {
        StringBuilder sb = new StringBuilder(100);

        private static final BitSet IGNORE = new BitSet(80);

        static {
            IGNORE.set(VwdFieldOrder.ORDER_ADF_PROZENTUALE_VERAENDERUNG);
            IGNORE.set(VwdFieldOrder.ORDER_ADF_VERAENDERUNG);
            IGNORE.set(VwdFieldOrder.ORDER_ADF_NAV);
            IGNORE.set(VwdFieldOrder.ORDER_ADF_RENDITE);
            IGNORE.set(VwdFieldOrder.ORDER_ADF_RENDITE_BRIEF);
            IGNORE.set(VwdFieldOrder.ORDER_ADF_RENDITE_GELD);
            IGNORE.set(VwdFieldOrder.ORDER_ADF_RENDITE_ISMA);
        }

        @Override
        public void restored(FeedData data, long offset) {
            sb.setLength(0);
            OrderedSnapData osd = (OrderedSnapData) data.getSnapData(true);
            BufferFieldData bfd = new BufferFieldData(osd.getData(false));
            for (int id = bfd.readNext(); id != 0 && id < VwdFieldOrder.FIRST_NON_TICK; id = bfd.readNext()) {
                if (bfd.getType() == FieldData.TYPE_PRICE && !IGNORE.get(id)) {
                    int base = bfd.getInt();
                    int exp = bfd.getByte();
                    if (base < 0) {
                        if (sb.length() == 0) {
                            sb.append(data.getVendorkey().toByteString());
                        }
                        sb.append(";").append(VwdFieldOrder.getFieldId(id)).append('=')
                                .append(base).append('E').append(exp);
                    }
                }
                else {
                    bfd.skipCurrent();
                }
            }
            if (sb.length() > 0) {
                System.out.println(sb);
            }
        }
    }


    private static class EnumFinder implements OrderedFileSnapStore.RestoreCallback {

        private final int oid;

        private Map<String, MutableInt> counts = new HashMap<>();

        private EnumFinder(int fid) {
            if (VwdFieldDescription.getField(fid).type() != VwdFieldDescription.Type.STRING) {
                throw new IllegalArgumentException("not a string field");
            }
            this.oid = VwdFieldOrder.getOrder(fid);
        }

        @Override
        public void restored(FeedData data, long offset) {
            OrderedSnapData osd = (OrderedSnapData) data.getSnapData(true);
            BufferFieldData bfd = new BufferFieldData(osd.getData(false));
            for (int id = bfd.readNext(); id != 0; id = bfd.readNext()) {
                if (id < this.oid) {
                    bfd.skipCurrent();
                    continue;
                }
                if (id == oid) {
                    String s = OrderedSnapRecord.toString(bfd.getBytes());
                    MutableInt count = counts.get(s);
                    if (count == null) {
                        counts.put(s, count = new MutableInt());
                    }
                    count.add(1);
                }
                break;
            }
        }
    }

    private static class FieldCounter implements OrderedFileSnapStore.RestoreCallback {

        private final int[] countsByOid = new int[VwdFieldOrder.MAX_ORDER];

        @Override
        public void restored(FeedData data, long offset) {
            OrderedSnapData osd = (OrderedSnapData) data.getSnapData(true);
            if (!osd.isInitialized()) {
                return;
            }
            BufferFieldData bfd = new BufferFieldData(osd.getData(false));
            for (int oid = bfd.readNext(); oid != 0; oid = bfd.readNext()) {
                countsByOid[oid]++;
                bfd.skipCurrent();
            }
        }
    }

    private static class Dumper implements OrderedFileSnapStore.RestoreCallback, VendorkeyFilter {

        private final Set<ByteString> vwdcodes = new HashSet<>();

        public Dumper(String[] codes) {
            for (String code : codes) {
                vwdcodes.add(new ByteString(code));
            }
        }

        @Override
        public void restored(FeedData data, long offset) {
            System.out.println("==================");
            System.out.println(data.getVendorkey());
            OrderedSnapData osd = (OrderedSnapData) data.getSnapData(true);
            if (!osd.isInitialized()) {
                return;
            }
            OrderedSnapRecord sr = new OrderedSnapRecord(osd.getData(false), osd.getLastUpdateTimestamp());
            TreeSet<SnapField> ts = new TreeSet<>(SnapFieldComparators.BY_ID);
            ts.addAll(sr.getSnapFields());
            for (SnapField sf : ts) {
                System.out.printf("%4d %4d %s%n", sf.getId(), VwdFieldOrder.getOrder(sf.getId()), sf.getValue());
            }
        }

        @Override
        public boolean test(Vendorkey vkey) {
            return this.vwdcodes.contains(vkey.toVwdcode());
        }
    }

    private static class FindUnused implements OrderedFileSnapStore.RestoreCallback {
        private boolean[] used = new boolean[VwdFieldOrder.MAX_ORDER + 1];

        @Override
        public void restored(FeedData data, long offset) {
            OrderedSnapData osd = (OrderedSnapData) data.getSnapData(true);
            BufferFieldData bfd = new BufferFieldData(osd.getData(false));
            for (int oid = bfd.readNext(); oid > 0; oid = bfd.readNext()) {
                used[oid] = true;
                bfd.skipCurrent();
            }
        }

        public void printResult() {
            Set<VwdFieldDescription.Field> unused = new HashSet<>();
            for (int i = 0; i < used.length; i++) {
                if (!used[i]) {
                    VwdFieldDescription.Field f = VwdFieldOrder.getField(i);
                    if (f != null && !used[VwdFieldOrder.getOrder(f.id())]) {
                        unused.add(f);
                    }
                }
            }
            ArrayList<VwdFieldDescription.Field> result = new ArrayList<>(unused);
            result.sort(null);
            for (VwdFieldDescription.Field field : result) {
                System.out.println(field);
            }
        }
    }

    private static class FindOld implements OrderedFileSnapStore.RestoreCallback {
        private final int yyyymmdd;

        private static final int[] ALL_DATE_OIDS = VwdFieldDescription.getFieldIds().stream()
                .filter(i -> VwdFieldDescription.getField(i).type() == VwdFieldDescription.Type.DATE)
                .map(VwdFieldOrder::getOrder).sorted().toArray();

        private static final int[] OIDS = new int[]{
                VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Bezahlt_Datum.id()),
                // ADF_Datum is sent for IID/QID messages, ignore it...
//                VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Datum.id()),
                VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Datum_Quotierung.id()),
                VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Faelligkeits_Datum.id()),
                VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Handelsdatum.id()),
                VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Kassa_Datum.id()),
                VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Letzter_Handelstag.id()),
                VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Schluss_Datum.id()),
                VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Schluss_Vortagesdatum.id()),
                VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Settlement_Datum.id()),
                VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Vorheriges_Datum.id()),
                VwdFieldOrder.getOrder(VwdFieldDescription.MMF_Bezahlt_Datum.id())
        };

        static {
            Arrays.sort(OIDS);
        }

        private final int[] oids;

        private final int[] values;

        private FindOld(int yyyymmdd, boolean allDateFields) {
            this.yyyymmdd = yyyymmdd;
            if (allDateFields) {
                this.oids = ALL_DATE_OIDS;
                this.values = null;
            }
            else {
                this.oids = OIDS;
                this.values = new int[oids.length];
                System.out.println(Arrays.toString(this.oids) + " / " + this.yyyymmdd);
            }
        }

        @Override
        public void restored(FeedData data, long offset) {
            if (data.getVendorkeyType() == 12 || data.getVendorkeyType() == 16) {
                return;
            }
            OrderedSnapData osd = (OrderedSnapData) data.getSnapData(true);
            BufferFieldData bfd = new BufferFieldData(osd.getData(false));
            int n = 0;
            if (this.values != null) {
                Arrays.fill(values, -1);
            }
            LOOP:
            for (int id = bfd.readNext(); id != 0; id = bfd.readNext()) {
                while (id > oids[n]) {
                    if (++n == oids.length) {
                        break LOOP;
                    }
                }
                if (id == oids[n]) {
                    final int date = bfd.getInt();
                    if (date > yyyymmdd) {
                        return;
                    }
                    if (this.values != null) {
                        values[n] = date;
                    }
                }
                else {
                    bfd.skipCurrent();
                }
            }
            if (this.values != null) {
                System.out.println(data.getVwdcode() + " " + Arrays.toString(values));
            }
            else {
                System.out.println(data.getVwdcode());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            return;
        }

        final File snapFile = new File(args[0]);
        if (!snapFile.canRead()) {
            System.err.println("cannot read " + snapFile.getAbsolutePath());
            return;
        }

        final VolatileFeedDataRegistry vr = createVolatileRegistry();

        final OrderedFileSnapStore ofss = new OrderedFileSnapStore();
        ofss.setSnapFile(snapFile);
        ofss.setStoreDelayed(false);
        ofss.setRegistry(vr);

        if ("minmax".equals(args[1])) {
            final int threshold = (args.length > 2) ? Integer.parseInt(args[2]) : 48;
            ofss.restore(new MinMaxExponent(threshold));
        }
        else if ("negative".equals(args[1])) {
            ofss.restore(new NegativePrices());
        }
        else if ("transform".equals(args[1])) {
            int i = 2;
            boolean withRt = true;
            boolean withNt = true;
            int timestamp = 0;
            FieldDataBuilder b = new FieldDataBuilder(256);

            while (i < args.length && args[i].startsWith("-")) {
                if ("-rt".equals(args[i])) {
                    withRt = false;
                }
                else if ("-nt".equals(args[i])) {
                    withNt = false;
                }
                else if ("-f".equals(args[i])) {
                    ofss.setVendorkeyFilter(toFilter(args[++i]));
                }
                else if ("-dr".equals(args[i])) {
                    DelayProviderImpl dp = new DelayProviderImpl();
                    dp.setDelayRules(new FileSystemResource(new File(args[++i])));
                    dp.afterPropertiesSet();
                    ofss.setDelayProvider(dp);
                    ofss.setStoreDelayed(true);
                    vr.setDataFactory(OrderedFeedDataFactory.RT_NT);
                }
                else if ("-d".equals(args[i])) {
                    DateTime dt = dateOptionalTimeParser().parseDateTime(args[++i]);
                    timestamp = new DateTimeProvider.Timestamp(dt).feedTimestamp;
                }
                else if ("-i".equals(args[i])) {
                    b.putIntFid(VwdFieldOrder.getOrder(getFieldId(args[++i])));
                    b.putInt(0);
                }
                else if ("-p".equals(args[i])) {
                    b.putPriceFid(VwdFieldOrder.getOrder(getFieldId(args[++i])));
                    b.putPrice(0L);
                }
                i++;
            }

            final BufferFieldData update = b.length() > 0 ? new BufferFieldData(b.asArray()) : null;
            transform(ofss, (i < args.length) ? new File(args[i]) : null,
                    withRt, withNt, new FieldUpdateCallback(timestamp, update));
        }
        else if ("find".equals(args[1])) {
            int r = 0;
            int f = 0;
            int[] required = new int[args.length];
            int[] forbidden = new int[args.length];
            Find callback = new Find();

            for (int i = 2; i < args.length; i++) {
                if ("-cmin".equals(args[i])) {
                    callback.cmin = parseTimestamp(args[++i]);
                    continue;
                }
                if ("-cmax".equals(args[i])) {
                    callback.cmax = parseTimestamp(args[++i]);
                    continue;
                }
                if ("-umin".equals(args[i])) {
                    callback.umin = parseTimestamp(args[++i]);
                    continue;
                }
                if ("-umax".equals(args[i])) {
                    callback.umax = parseTimestamp(args[++i]);
                    continue;
                }
                if ("-flags".equals(args[i])) {
                    callback.setFlags(StringUtils.commaDelimitedListToStringArray(args[++i]));
                    continue;
                }
                String val = args[i].startsWith("-") ? args[i].substring(1) : args[i];
                int fid = getFieldId(val);
                if (val == args[i]) {
                    required[r++] = VwdFieldOrder.getOrder(fid);
                    System.err.println("required field: " + VwdFieldDescription.getField(fid));
                }
                else {
                    forbidden[f++] = VwdFieldOrder.getOrder(fid);
                    System.err.println("forbidden field: " + VwdFieldDescription.getField(fid));
                }
            }

            callback.setRequired(Arrays.copyOf(required, r));
            callback.setForbidden(Arrays.copyOf(forbidden, r));
            callback.afterPropertiesSet();
            ofss.restore(callback);
        }
        else if ("grep".equals(args[1])) {
            GrepFields grep = new GrepFields();
            int i = 2;
            while (i < args.length && args[i].startsWith("-")) {
                switch (args[i]) {
                    case "-a":
                        grep.requireAll = true;
                        break;
                    case "-f":
                        ofss.setVendorkeyFilter(toFilter(args[++i]));
                        break;
                    case "-o":
                        grep.pw = new PrintWriter(new File(args[++i]));
                        break;
                    case "-t":
                        grep.withType = true;
                        break;
                    case "-cmin":
                        grep.cmin = parseTimestamp(args[++i]);
                        break;
                    case "-cmax":
                        grep.cmax = parseTimestamp(args[++i]);
                        break;
                    case "-umin":
                        grep.umin = parseTimestamp(args[++i]);
                        break;
                    case "-umax":
                        grep.umax = parseTimestamp(args[++i]);
                        break;
                    default:
                        System.err.println("invalid option " + args[i]);
                        System.exit(1);
                }
                i++;
            }
            int base = i;
            int[] fields = new int[args.length - base];

            for (; i < args.length; i++) {
                String val = args[i];
                fields[i - base] = VwdFieldOrder.getOrder(getFieldId(val));
            }

            grep.setOids(fields);
            grep.afterPropertiesSet();
            ofss.restore(grep);
            grep.pw.close();
        }
        else if ("symbols".equals(args[1])) {
            GrepSymbols callback = new GrepSymbols();
            int i = 2;
            while (i < args.length && args[i].startsWith("-")) {
                switch (args[i]) {
                    case "-o":
                        callback.pw = new PrintWriter(new File(args[++i]));
                        break;
                    case "-t":
                        callback.withType = true;
                        break;
                    case "-f":
                        ofss.setVendorkeyFilter(toFilter(args[++i]));
                        break;
                    default:
                        System.err.println("invalid option " + args[i]);
                        System.exit(1);
                }
                i++;
            }
            ofss.restore(callback);
            callback.pw.close();
        }
        else if ("old".equals(args[1])) {
            boolean all = false;
            int i = 2;
            if (i < args.length && args[i].equals("-a")) {
                all = true;
                i++;
            }
            ofss.restore(new FindOld(Integer.parseInt(args[i]), all));
        }
        else if ("unused".equals(args[1])) {
            FindUnused unused = new FindUnused();
            ofss.restore(unused);
            unused.printResult();
        }
        else if ("dump".equals(args[1])) {
            Dumper d = new Dumper(Arrays.copyOfRange(args, 2, args.length));
            ofss.setVendorkeyFilter(d);
            ofss.restore(d);
        }
        else if ("enum".equals(args[1])) {
            EnumFinder ef = new EnumFinder(getFieldId(args[2]));
            ofss.restore(ef);
            TreeMap<String, MutableInt> tm = new TreeMap<>(ef.counts);
            for (Map.Entry<String, MutableInt> e : tm.entrySet()) {
                System.out.printf("%8d %s%n", e.getValue().intValue(), e.getKey());
            }
        }
        else if ("count".equals(args[1])) {
            boolean verbose = args.length > 2 && "-v".equals(args[2]);
            FieldCounter fc = new FieldCounter();
            ofss.restore(fc);
            for (int oid = 0; oid < VwdFieldOrder.MAX_ORDER; oid++) {
                if (fc.countsByOid[oid] > 0 || verbose) {
                    VwdFieldDescription.Field f = VwdFieldOrder.getField(oid);
                    if (f != null) {
                        System.out.printf("%8d %4d %4d %s%n", fc.countsByOid[oid], oid,
                                f.id(), f.name());
                    }
                }
            }
        }
        else if ("validate".equals(args[1])) {
            ofss.restore(new Validator(args.length > 2 ? Boolean.valueOf(args[2]) : null));
        }
    }

    static VendorkeyFilter toFilter(String spec) {
        File f = new File(spec);
        if (f.canRead()) {
            try {
                return VendorkeyFilterFactory.create(f);
            } catch (FileNotFoundException e) {
                // cannot happen as canRead returned true
            }
        }
        return VendorkeyFilterFactory.create(spec);
    }

    private static int parseTimestamp(String ts) {
        if (ts.matches("-?[0-9]+")) {
            return Integer.parseInt(ts);
        }
        return encodeTimestamp(dateOptionalTimeParser().parseMillis(ts));
    }

    private static int getFieldId(String val) {
        return Character.isDigit(val.charAt(0))
                ? Integer.parseInt(val) : VwdFieldDescription.getFieldByName(val).id();
    }

    private static void transform(final OrderedFileSnapStore ofss, File out,
            boolean withRt, boolean withNt,
            final FeedDataExchangingCallback callback) throws Exception {

        final Thread t = new Thread(() -> {
            try {
                ofss.restore(callback);
            } catch (IOException e) {
                e.printStackTrace();
            }
            callback.restored(null, 0);
        });
        t.start();

        if (out != null) {
            OrderedFileSnapStore store = new OrderedFileSnapStore();
            store.setStoreRecordsWithoutData(true);
            store.store(out, withRt, withNt, () -> callback);
        }
        else {
            long rtBytes = 0;
            long ntBytes = 0;
            int num = 0;
            while (callback.hasNext()) {
                num++;
                OrderedFeedData fd = (OrderedFeedData) callback.next();
                rtBytes += getLength(fd.getSnapData(true));
                ntBytes += getLength(fd.getSnapData(false));
            }
            System.out.printf("num = " + num + ", #rt = " + rtBytes + " #nt = " + ntBytes);
        }
        t.join();
    }

    private static int getLength(OrderedSnapData sr) {
        return (sr != null && sr.isInitialized()) ? sr.getData(false).length : 0;
    }

    private static VolatileFeedDataRegistry createVolatileRegistry() {
        final VolatileFeedDataRegistry r = new VolatileFeedDataRegistry();
        r.setDataFactory(OrderedFeedDataFactory.RT);
        return r;
    }

    private static void usage() {
        System.err.println("Usage: SnapCli <snapFile> <command> <args>");
        System.err.println("available commands");
        System.err.println("transform [-rt] [-nt] [-dr <delay-rules-file>] [-f <filter-expr>] [-d <date>] [<out-file>]");
        System.err.println("  reads records from snapFile and write them to out-file");
        System.err.println("  if no out-file is specified, writes stats about restored snap records");
        System.err.println("  -rt to exclude realtime snap records");
        System.err.println("  -nt to exclude neartime snap records");
        System.err.println("  -dr restores nt snap from rt data for delayed data as defined in delay-rules-file");
        System.err.println("  -f only include records that match the filter");
        System.err.println("  -d only include records that have received an update after date");
        System.err.println();
        System.err.println("enum <fieldName|fieldId>?");
        System.err.println("  print distinct values and respective count for the given field");
        System.err.println();
        System.err.println("find [-(cmax|cmin|umin|umax) <timestamp>]* [-flags name[,name]*] <fieldName|fieldId>?");
        System.err.println("  print vwdcode for all symbols (that have the requested snap field, if specified)");
        System.err.println("  -cmin limit to vwdcodes created after timestamp (e.g. 2014-01-01T23:00:00, time is optional)");
        System.err.println("  -cmax limit to vwdcodes created before timestamp");
        System.err.println("  -umin limit to vwdcodes updated after timestamp (e.g. 2014-01-01T23:00:00, time is optional)");
        System.err.println("  -umax limit to vwdcodes updated before timestamp");
        System.err.println("  -flags name(s) of content flags, at least one has to be present");
        System.err.println();
        System.err.println("grep [-a] [-t] [-f <filter>] [-o <outfile>] <fieldName|fieldId>?");
        System.err.println("  print fields for all items (that match the filter)");
        System.err.println("  -a require that all fields actually occur in a snap record");
        System.err.println("  -t to print vwd keys with type prefix");
        System.err.println();
        System.err.println("symbols [-t] [-f <filter>] [-o <outfile>]");
        System.err.println("  print all symbols (that match the filter)");
        System.err.println("  -t to print vwd keys with type prefix");
        System.err.println();
        System.err.println("old [-a] <yyyymmdd>");
        System.err.println("  print vwdcode for all symbols that have no date field later than yyyymmdd");
        System.err.println("  -a evaluate all date fields instead of just the default date fields");
        System.err.println();
        System.err.println("validate");
        System.err.println("  validates data in the given snap file");
        System.err.println();
        System.err.println("unused");
        System.err.println("  prints all vwd fields that do not occur in any snap record");
        System.err.println();
        System.err.println("count [-v]");
        System.err.println("  prints count for each field, -v prints fields with 0 count as well");
        System.err.println();
        System.err.println("dump <vwdcode>+");
        System.err.println("  prints all fields for all given vwdcodes");
        System.err.println();
        System.err.println("minmax [threshold]");
        System.err.println("  prints all price fields/vendorkeys with abs(exponent) > threshold ");
        System.err.println("  threshold default is 48");
        System.err.println();
        System.err.println("negative");
        System.err.println("  prints fields/vendorkeys with negative prices");
    }
}
