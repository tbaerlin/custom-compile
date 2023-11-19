/*
 * OpraWriter.java
 *
 * Created on 10.07.13 12:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.opra;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.VolatileFeedDataRegistry;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldData;
import de.marketmaker.istar.feed.ordered.OrderedFeedDataFactory;
import de.marketmaker.istar.feed.ordered.OrderedFileSnapStore;
import de.marketmaker.istar.feed.ordered.OrderedSnapData;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * Writes opra-symbols and the latest available valuation price/date to a file; can be scheduled
 * to do so periodically; the filename contains a timestamp.
 * @author oflege
 */
@ManagedResource
public class OpraWriter implements InitializingBean {

    public static final String HEADER = "# OpraWriter ";

    public static final String VERSION = "V1.0";

    private static class PriceFactory {
        /**
         * FIELDS[x][0] is an order-ID, array elements are ordered by asc order-ID
         * FIELDS[x][1] is the index in the prices/dates array at which to store the value of the field
         */
        private static final int[][] FIELDS = new int[12][2];

        static {
            int[] tmp = {
                    encode(VwdFieldDescription.ADF_Schluss, 0),
                    encode(VwdFieldDescription.ADF_Schluss_Datum, 0),
                    encode(VwdFieldDescription.ADF_Settlement, 1),
                    encode(VwdFieldDescription.ADF_Settlement_Datum, 1),
                    encode(VwdFieldDescription.ADF_Schluss_Vortag, 2),
                    encode(VwdFieldDescription.ADF_Schluss_Vortagesdatum, 2),
                    encode(VwdFieldDescription.ADF_Settlement_Vortag, 3),
                    encode(VwdFieldDescription.ADF_Settlement_Datum_Vortag, 3),
                    encode(VwdFieldDescription.ADF_Geld_Vortag, 4),
                    encode(VwdFieldDescription.ADF_Vorheriges_Datum, 4),
                    encode(VwdFieldDescription.ADF_Faelligkeits_Datum, 5),
                    encode(VwdFieldDescription.ADF_Kontraktgroesse, 5)
            };
            Arrays.sort(tmp); // ensure FIELDS[0] is ordered by asc order-ID
            for (int i = 0; i < tmp.length; i++) { // and decode the values again
                FIELDS[i][0] = tmp[i] >> 16; // order
                FIELDS[i][1] = tmp[i] & 0xFFFF; // index
            }
        }

        private final boolean valuationPriceOnly;

        private static int encode(VwdFieldDescription.Field f, int index) {
            return (VwdFieldOrder.getOrder(f.id()) << 16) + index;
        }

        final StringBuilder sb = new StringBuilder(40);

        long[] prices = new long[6];

        int[] dates = new int[6];

        private PriceFactory(boolean valuationPriceOnly) {
            this.valuationPriceOnly = valuationPriceOnly;
        }

        void reset() {
            Arrays.fill(this.dates, 0);
            Arrays.fill(this.prices, 0);
        }

        private int getValuationPriceIndex() {
            // TODO: or should we try to find the most recent? even if we use Geld_Vortag?
            for (int j = 0; j < prices.length; j++) {
                if (hasValidPrice(j)) {
                    return j;
                }
            }
            return -1;
        }

        private boolean hasValidPrice(int j) {
            return dates[j] != 0 && (int) (prices[j]) > 0;
        }

        public void appendPrices(PrintWriter pw, FeedData feedData) {
            final OrderedSnapData sd = (OrderedSnapData) feedData.getSnapData(true);
            if (!sd.isInitialized()) {
                return;
            }

            reset();
            collectPricesAndDates(new BufferFieldData(sd.getData(false)));

            sb.setLength(0);
            sb.append(feedData.getVwdcode());
            sb.append(';').append(dates[5]); // Faelligkeits_Datum

            if (this.valuationPriceOnly) {
                appendValuationPrice();
            }
            else {
                appendPrices();
            }

            appendContractSize();

            pw.println(sb);
        }

        private void appendContractSize() {
            sb.append(';').append(prices[5] > 0 ? MdpsFeedUtils.decodePrice(prices[5]).toPlainString() : "");
        }

        private void appendPrices() {
            for (int i = 0; i < 5; i++) {
                if (hasValidPrice(i)) {
                    sb.append(';').append(MdpsFeedUtils.decodePrice(this.prices[i]).toPlainString())
                            .append(';').append(this.dates[i]);
                }
                else {
                    sb.append(";;");
                }
            }
        }

        private void appendValuationPrice() {
            final int i = getValuationPriceIndex();
            if (i >= 0) {
                sb.append(';').append(MdpsFeedUtils.decodePrice(this.prices[i]).toPlainString())
                        .append(';').append(this.dates[i]);
            } else {
                sb.append(";;");
            }
        }

        private void collectPricesAndDates(BufferFieldData fd) {
            int n = 0;
            for (int oid = fd.readNext(); oid > 0; oid = fd.readNext()) {
                while (oid > FIELDS[n][0]) {
                    if (++n == FIELDS.length) {
                        return;
                    }
                }
                if (oid < FIELDS[n][0]) {
                    fd.skipCurrent();
                    continue;
                }
                final int idx = FIELDS[n][1];
                if (fd.getType() == FieldData.TYPE_PRICE) {
                    int base = fd.getInt();
                    int exp = fd.getByte();
                    if (!MdpsFeedUtils.hasZeroBit(exp)) {
                        this.prices[idx] = MdpsFeedUtils.encodePrice(base, exp);
                    }
                }
                else {
                    this.dates[idx] = fd.getInt();
                }
            }
        }

    }

    private static OutputStream createOutputStream(File f) throws IOException {
        final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(f, false), 1 << 16);
        return (f.getName().endsWith(".gz")) ? new GZIPOutputStream(os, 8192) : os;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeedDataRepository repository;

    private boolean valuationPriceOnly = true;

    private File outDir;

    public void setValuationPriceOnly(boolean valuationPriceOnly) {
        this.valuationPriceOnly = valuationPriceOnly;
    }

    public void setOutDir(File outDir) {
        this.outDir = outDir;
    }

    public void setRepository(FeedDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.outDir == null) {
            throw new IllegalStateException("outDir is null");
        }
    }

    @ManagedOperation
    public void write() {
        doWrite(new File(this.outDir, "opra-" + new DateTime().toString("yyyy-MM-dd_HHmmss") + ".csv.gz"),
                this.valuationPriceOnly);
    }

    @ManagedOperation
    public void write(String filename, final boolean valuationPriceOnly) {
        doWrite(new File(filename), valuationPriceOnly);
    }

    private void doWrite(File f, final boolean priceOnly) {
        PriceFactory factory = new PriceFactory(priceOnly);
        try (PrintWriter pw = getPrintWriter(f)) {
            writeHeader(pw);
            final List<FeedMarket> markets = repository.getMarkets();
            for(FeedMarket market: markets){
                writeMarket(pw, market, factory);
            }
        } catch (IOException e) {
            this.logger.error("<write> failed", e);
        }
    }

    private static void writeHeader(PrintWriter pw) {
        pw.append(HEADER).println(VERSION);
    }

    private void writeMarket(PrintWriter pw, FeedMarket market, PriceFactory factory) {
        for (FeedData feedData : market.getElements(true)) {
            synchronized (feedData) {
                if (!feedData.isDeleted()) {
                    factory.appendPrices(pw, feedData);
                }
            }
        }
    }

    private static PrintWriter getPrintWriter(File f) throws IOException {
        return new PrintWriter(new OutputStreamWriter(createOutputStream(f), StandardCharsets.UTF_8));
    }

    public static void main(String[] args) throws IOException {
        final VolatileFeedDataRegistry registry = new VolatileFeedDataRegistry();
        registry.setDataFactory(OrderedFeedDataFactory.RT);

        final PriceFactory f = new PriceFactory(true);
        try (PrintWriter pw = getPrintWriter(new File(args[1]))) {
            writeHeader(pw);
            OrderedFileSnapStore ofss = new OrderedFileSnapStore();
            ofss.setRegistry(registry);
            ofss.setSnapFile(new File(args[0]));
            ofss.restore(new OrderedFileSnapStore.RestoreCallback() {
                @Override
                public void restored(FeedData data, long offset) {
                    f.appendPrices(pw, data);
                }
            });
        }
    }
}
