/*
 * SnapFileConverter.java
 *
 * Created on 12.12.12 10:55
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Exchanger;

import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataImpl;
import de.marketmaker.istar.feed.FeedDataRegistry;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.VolatileFeedDataRegistry;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.snap.FileSnapRestoreMethod;
import de.marketmaker.istar.feed.snap.SnapData;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.IndexAndOffsetFactoryVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

/**
 * Converts a chicago2 snap file (snap.sda) into a chicago3 snap file (snap.sd3).
 * <pre>
 * Usage: SnapFileConverter input-file [output-file]
 * </pre>
 * @author oflege
 */
public class SnapFileConverter {

    private final File snapFile;

    private FeedDataImpl next;

    private File outFile;

    private final FieldDataBuilder builder;


    public static void main(String[] args) throws Exception {
        File in = new File(args[0]);
        File out;
        if (args.length > 1) {
            out = new File(args[1]);
        }
        else {
            out = new File(in.getParentFile(), in.getName().replace(".sda", ".sd3"));
        }
        new SnapFileConverter(in, out).convert();
    }

    private FeedDataRegistry registry = new VolatileFeedDataRegistry() {
        @Override
        public FeedData register(Vendorkey vkey) {
            return createFeedData(vkey, getMarket(vkey));
        }
    };

    private FeedDataImpl createFeedData(Vendorkey vkey, FeedMarket market) {
        this.next = new FeedDataImpl(vkey, market);
        return next;
    }

    public SnapFileConverter(File in, File out) {
        snapFile = in;
        outFile = out;
        builder = new FieldDataBuilder(4096);
    }

    private OrderedFeedData convertFeedData() {
        OrderedFeedData result = OrderedFeedDataFactory.RT_NT
                .create(this.next.getVendorkey(), this.next.getMarket());
        SnapData rt = this.next.getSnapData(true);
        if (rt.isInitialized()) {
            SnapRecord srt = rt.toSnapRecord(0);
            OrderedSnapData osrt = result.getSnapData(true);
            osrt.setLastUpdateTimestamp(getTimestamp(srt));
            osrt.init(null, convertSnap(srt));
        }
        SnapData nt = this.next.getSnapData(false);
        if (nt.isInitialized()) {
            SnapRecord snt = nt.toSnapRecord(0);
            OrderedSnapData osnt = result.getSnapData(false);
            osnt.setLastUpdateTimestamp(getTimestamp(snt));
            osnt.init(null, convertSnap(snt));
        }
        return result;
    }

    private byte[] convertSnap(SnapRecord snapRecord) {
        final ArrayList<SnapField> fields = SnapRecordUtils.toOrderedFields(snapRecord);
        this.builder.reset();
        for (SnapField field : fields) {
            int id = field.getId();
            int order = SnapRecordUtils.order(field);
            if (id == 212 || id == 213 || order == 0 || field.getValue() == null) {
                continue;
            }
            VwdFieldDescription.Field vf = VwdFieldDescription.getField(id);
            switch (field.getType()) {
                case PRICE:
                    builder.putPriceFid(order);
                    builder.putPrice(MdpsFeedUtils.encodePrice(field.getPrice()));
                    break;
                case TIME:
                    // intentional fall-through
                case DATE:
                    // intentional fall-through
                case NUMBER:
                    if (vf.type() == VwdFieldDescription.Type.TIME) {
                        builder.putTimeFid(order);
                        builder.putInt(MdpsFeedUtils.encodeTime(((Number)field.getValue()).intValue()));
                    }
                    else {
                        builder.putIntFid(order);
                        builder.putInt(((Number)field.getValue()).intValue());
                    }
                    break;
                case STRING:
                    builder.putStringFid(order);
                    builder.putString(String.valueOf(field.getValue()).getBytes(OrderedSnapRecord.DEFAULT_CHARSET));
                    break;
            }
        }
        return this.builder.asArray();
    }

    private int getTimestamp(SnapRecord sr) {
        SnapField toa = sr.getField(VwdFieldDescription.ADF_TIMEOFARR.id());
        if (!toa.isDefined()) {
            return 0;
        }
        SnapField doa = sr.getField(VwdFieldDescription.ADF_DATEOFARR.id());
        if (!doa.isDefined()) {
            return 0;
        }
        return DateTimeProvider.Timestamp.encodeTimestamp(((Number) doa.getValue()).intValue(),
                ((Number) toa.getValue()).intValue());
    }

    private void convert() throws Exception {
        final Exchanger<OrderedFeedData> exchanger = new Exchanger<>();

        Thread t = new Thread(() -> {
            try {
                store(exchanger);
            } catch (Exception e) {
                System.err.println("Store thread failed");
                e.printStackTrace();
            }
        });
        t.start();

        final FileSnapRestoreMethod m =
                new FileSnapRestoreMethod(snapFile, this.registry, new IndexAndOffsetFactoryVwd());
        m.setCallback(new FileSnapRestoreMethod.Callback() {
            int n = 0;
            @Override
            public void ackRestore() {
                if ((++n & 0xFFFF) == 0) System.out.println(n);
                try {
                    exchanger.exchange(convertFeedData());
                } catch (InterruptedException e) {
                    System.err.println("interrupted?!");
                }
            }
        });
        m.restore(true);
        exchanger.exchange(null);
        t.join();
    }

    private void store(final Exchanger<OrderedFeedData> exchanger) throws Exception {
        OrderedFileSnapStore store = new OrderedFileSnapStore();
        store.store(outFile, true, true, new Iterable<FeedData>() {
            @Override
            public Iterator<FeedData> iterator() {
                return new Iterator<FeedData>() {
                    boolean stopped = false;

                    OrderedFeedData next;

                    @Override
                    public boolean hasNext() {
                        if (this.next == null && !this.stopped) {
                            try {
                                this.next = exchanger.exchange(null);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            this.stopped = this.next == null;
                        }
                        return this.next != null;
                    }

                    @Override
                    public FeedData next() {
                        OrderedFeedData result = this.next;
                        this.next = null;
                        return result;
                    }

                    @Override
                    public void remove() {
                    }
                };
            }
        });
    }
}
