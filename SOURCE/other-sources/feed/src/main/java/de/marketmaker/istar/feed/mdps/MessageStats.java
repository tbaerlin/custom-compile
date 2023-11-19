/*
 * FieldStats.java
 *
 * Created on 11.06.14 14:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableLong;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * Captures statistics
 * @author oflege
 */
public class MessageStats {
    private final Map<BitSet, MutableLong> recordCounts = new HashMap<>();

    private final long[] counts = new long[VwdFieldOrder.MAX_ORDER];

    private long numMessages = 0;

    private final BitSet bs = new BitSet();

    private final Map<FeedMarket, BitSet> fieldsPerMarket = new IdentityHashMap<>();

    public void removeCountsSmallerThan(int count) {
        for (Iterator<MutableLong> it = recordCounts.values().iterator(); it.hasNext(); ) {
            if (it.next().longValue() < count) {
                it.remove();
            }
        }
    }

    public BitSet process(FeedData data, OrderedUpdate update) {
        return process(data.getMarket(), update.getFieldData());
    }

    public BitSet process(final FeedMarket market, final BufferFieldData fd) {
        this.numMessages++;

        populateBitSet(fd);
        updateFieldsPerMarket(market);
        incrementCountPerBitSet();

        return this.bs;
    }

    private void populateBitSet(final BufferFieldData fd) {
        this.bs.clear();
        for (int oid = fd.readNext(); oid != 0; oid = fd.readNext()) {
            counts[oid]++;
            bs.set(oid);
            fd.skipCurrent();
        }
    }

    private void incrementCountPerBitSet() {
        MutableLong ml = recordCounts.get(bs);
        if (ml == null) {
            final BitSet key = new BitSet(this.bs.length());
            key.or(this.bs);
            recordCounts.put(key, ml = new MutableLong());
        }
        ml.increment();
    }

    private void updateFieldsPerMarket(final FeedMarket market) {
        BitSet ms = this.fieldsPerMarket.get(market);
        if (ms == null) {
            this.fieldsPerMarket.put(market, ms = new BitSet());
        }
        ms.or(this.bs);
    }


    public void printResult(PrintWriter out, boolean ignoreUnusedFields) {
        out.printf("%11d messages%n", this.numMessages);
        out.println("--------------------------------");
        for (int i = 0; i < counts.length; i++) {
            final int fid = VwdFieldOrder.getFieldId(i);
            if (fid == 0 || !VwdFieldDescription.getField(fid).name().startsWith("ADF")) {
                continue;
            }
            if (ignoreUnusedFields && counts[i] == 0) {
                continue;
            }
            double pct = counts[i] * 100d / numMessages;
            out.printf("%11d  %8.4f %4d %4d %s%n", counts[i], pct, i, fid,
                    VwdFieldDescription.getField(fid).name());
        }
        out.println();
        out.printf("%11d record field combinations%n", this.recordCounts.size());
        out.println("--------------------------------");

        SortedMap<Long, BitSet> sets = new TreeMap<>();
        for (Map.Entry<BitSet, MutableLong> e : recordCounts.entrySet()) {
            sets.put(e.getValue().longValue(), e.getKey());
        }
        for (Map.Entry<Long, BitSet> e : sets.tailMap(this.numMessages / 1000).entrySet()) {
            double pct = e.getKey() * 100d / numMessages;
            out.printf("%11d  %5.2f %s%n", e.getKey(), pct, e.getValue());
        }

        out.println();
        out.println("fields per market");
        out.println("--------------------------------");

        SortedMap<String, int[]> fields = new TreeMap<>();
        for (Map.Entry<FeedMarket, BitSet> e : fieldsPerMarket.entrySet()) {
            fields.put(e.getKey().getName().toString(), toFieldArray(e.getValue()));
        }
        for (Map.Entry<String, int[]> e : fields.entrySet()) {
            out.printf("%-12s (#%4d) %s%n", e.getKey(), e.getValue().length, Arrays.toString(e.getValue()));
        }

        out.println();
        out.println("markets per field");
        out.println("--------------------------------");

        int[] oids = VwdFieldDescription.getFieldIds()
                .stream().map(VwdFieldOrder::getOrder).filter(i -> i > 0).toArray();
        for (int oid : oids) {
            List<String> markets = fieldsPerMarket.entrySet().stream()
                    .filter((e) -> e.getValue().get(oid))
                    .map((e) -> e.getKey().getName().toString())
                    .sorted()
                    .collect(Collectors.toList());
            out.printf("%s used in %s%n", VwdFieldOrder.getField(oid).name(), markets);
        }
    }

    private int[] toFieldArray(BitSet bs) {
        int[] result = new int[bs.cardinality()];
        for (int i = bs.nextSetBit(0), j = 0; i >= 0; i = bs.nextSetBit(i + 1)) {
            result[j++] = VwdFieldOrder.getFieldId(i);
        }
        Arrays.sort(result);
        return result;
    }
}
