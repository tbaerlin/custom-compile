/*
 * SyntheticTradeWriter.java
 *
 * Created on 13.09.13 12:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.FeedMarketRepository;
import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.mdps.MdpsPriceUtils;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickFiles;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.marketmaker.istar.feed.FeedUpdateFlags.*;
import static de.marketmaker.istar.feed.mdps.MdpsPriceUtils.compare;
import static de.marketmaker.istar.feed.mdps.MdpsPriceUtils.isInvalidAsk;
import static de.marketmaker.istar.feed.ordered.OrderedFeedDataFactory.RT_TICKS;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICK3;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICKZ;

/**
 * Computes daily OHLC prices from tick files and writes them into a csv output file.
 * <br>
 * Optimized for performance. Works for mapped tick files and uses prices in mdps format
 * for ohlc and synthetic trade calculations, which is approx. 7x faster than using BigDecimals.
 * <p>
 * Rules:
 * <ol>
 * <li>If a timeseries contains at least a single trade, ohlc will be computed based on trades only</li>
 * <li>If a timeseries contains ticks with ADF_Mittelkurs, ohlc will be computed based on that</li>
 * <li>If no trade is present, the input values for ohlc computation are taken from (bid+ask)/2</li>
 * <li>except for when no prior bid or ask price is available, in which case the defined ask or bid is used</li>
 * </ol>
 * </p>
 * Each line in the csv-File looks like this:
 * <pre>vwdcode;open;high;low;close(;flag)*</pre>
 * The following flags may appear:
 * <dl>
 * <dt>T</dt><dd>ohlc based on trade ticks</dd>
 * <dt>A</dt><dd>ohlc based on ask ticks</dd>
 * <dt>B</dt><dd>ohlc based on bid ticks</dd>
 * <dt>M</dt><dd>ohlc based on ADF_Mittelkurs</dd>
 * <dt>H</dt><dd>high is larger than 5*low</dd>
 * <dt>X</dt><dd>ohlc based on synthetic trades, no prices added once ask became invalid (e.g., 9999.99 or s.th. like that)</dd>
 * </dl>
 * @author oflege
 */
class SyntheticTradeWriter extends TickFileMapper {
    private static final BigDecimal FIVE = BigDecimal.valueOf(5);

    private static final int TICK_FLAGS = FLAG_WITH_BID + FLAG_WITH_ASK + FLAG_WITH_TRADE;

    private static final ByteString LUS = new ByteString("LUS");

    private static final Set<ByteString> MARKETS_WITH_ASK_CHECK = new HashSet<>(Arrays.asList(
            new ByteString("CITIF"),
            new ByteString("EUWAX"),
            new ByteString("FFM"),
            new ByteString("FFMST"),
            new ByteString("HVB"),
            new ByteString("KLIL"),
            new ByteString("LASW"),
            LUS,
            new ByteString("MS"),
            new ByteString("STG"),
            new ByteString("XETF")
    ));

    static final String HEADER = "VWDCODE;OPEN;HIGH;LOW;CLOSE";

    private static class NegativeValueException extends RuntimeException {
    }

    private static class PriceValidator {
        private final boolean acceptNegative;

        private PriceValidator(boolean acceptNegative) {
            this.acceptNegative = acceptNegative;
        }

        boolean isValid(long value) {
            final int unscaled = (int) value;
            if (this.acceptNegative || unscaled > 0) {
                return true;
            }
            else if (unscaled < 0) {
                throw new NegativeValueException();
            }
            return false;
        }
    }

    private static final PriceValidator POSITIVE = new PriceValidator(false);

    private static final PriceValidator POSITIVE_AND_NEGATIVE = new PriceValidator(true);

    private static final PriceValidator LUS_VALIDATOR = new PriceValidator(true) {
        private final long N_0_001 = MdpsFeedUtils.encodePrice(1, -3);

        @Override
        boolean isValid(long value) {
            return compare(N_0_001, value) != 0;
        }
    };

    private static final Map<ByteString, PriceValidator> MARKET_VALIDATORS = new HashMap<>();

    static {
        MARKET_VALIDATORS.put(LUS, LUS_VALIDATOR);
    }

    private static class Ohlc {

        final long[] values = new long[4];

        private boolean empty;

        void reset() {
            values[0] = 0;
            empty = true;
        }

        void add(long bd) {
            if (empty) {
                Arrays.fill(values, bd);
                empty = false;
                return;
            }
            if (compare(values[1], bd) < 0) {
                values[1] = bd;
            }
            else if (compare(values[2], bd) > 0) {
                values[2] = bd;

            }
            values[3] = bd;
        }

    }

    private static long getPrice(BufferFieldData fd) {
        return MdpsFeedUtils.encodePrice(fd.getInt(), fd.getByte());
    }

    private static class SyntheticTickBuilder {

        protected static final Logger logger = LoggerFactory.getLogger(SyntheticTickBuilder.class);

        private static final long UNDEFINED = -1L;

        private final boolean withAskCheck;

        private long lastInvalidAsk;

        private long ask = 0;

        private long bid = 0;

        private PriceValidator validator;

        private boolean withNewValue = false;

        private boolean withValidAsk;

        private boolean withValidBid;

        private final Ohlc ohlc = new Ohlc();

        public SyntheticTickBuilder(boolean withAskCheck) {
            this.withAskCheck = withAskCheck;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            return (withValidBid ? format(bid, sb) : "-")
                    + " / " + (withValidAsk ? format(ask, sb) : "-");
        }

        void reset(final PriceValidator priceValidator) {
            this.ask = 0;
            this.bid = 0;
            this.withValidAsk = false;
            this.withValidBid = false;
            this.withNewValue = false;
            this.lastInvalidAsk = UNDEFINED;
            this.validator = priceValidator;
            this.ohlc.reset();
        }

        void updateAsk(long ask) {
            if (this.withAskCheck && !checkAsk(ask)) {
                if (this.withValidAsk) {
                    // TODO: we have already some valid asks and now ask became invalid
                    // setting withNewValue to false will ignore the bid that came with our invalid ask
                    this.withNewValue = false;
                }
                return;
            }
            if (isValid(ask)) {
                this.ask = ask;
                this.withValidAsk = true;
            }
        }

        private boolean checkAsk(long ask) {
            // most of the time, once ask becomes invalid it keeps that value for all ticks
            // so we can avoid costly comparisons by remembering the last invalid value.
            if (ask == this.lastInvalidAsk) {
                return false;
            }
            if (isInvalidAsk(ask, this.withValidBid ? this.bid : 0L)) {
                this.lastInvalidAsk = ask;
                return false;
            }
            this.lastInvalidAsk = UNDEFINED;
            return true;
        }

        boolean isWithInvalidAsk() {
            return this.lastInvalidAsk != UNDEFINED;
        }

        void updateBid(long bid) {
            if (isValid(bid)) {
                this.bid = bid;
                this.withValidBid = true;
            }
        }

        boolean isValid(long value) {
            if (this.validator.isValid(value)) {
                this.withNewValue = true;
                return true;
            }
            return false;
        }

        boolean addSyntheticTrade() {
            if (this.withNewValue) {
                try {
                    addTick(doGetSyntheticPrice());
                    return true;
                } catch (Exception e) {
                    logger.error("<addSyntheticTrade> Unable to add synthetic trade due to {}. [bid={}, ask={})", e.getMessage(), this.bid, this.ask);
                    this.withNewValue = false;
                }
            }
            return false;
        }

        private void addTick(long price) {
            this.ohlc.add(price);
            this.withNewValue = false;
        }

        private long doGetSyntheticPrice() {
            if (this.ask == 0 && !this.withValidAsk) {
                return this.bid;
            }
            if (this.bid == 0 && !this.withValidBid) {
                return this.ask;
            }
            return MdpsPriceUtils.getSyntheticPrice(this.ask, this.bid);
        }

        // HACK for DEKA, where the first bid/ask is the same as Rendite_Brief:
        // e.g. 159067.DEKA on 20131023:
        // ---X;05:08:00;578=1.016684
        // -BA-;08:05:30;30=0.93;28=0.93;395=0.93
        // -BA-;09:10:03;30=113.65;28=114.05;395=0.93
        boolean isWithRenditeInBidAndAsk(long price) {
            if (this.bid == price && this.ask == price) {
                this.withNewValue = false;
                return true;
            }
            return false;
        }
    }

    private final FeedMarket market;

    private final DefaultIndexHandler handler;

    private final Set<ByteString> symbols;

    private final AbstractTickRecord.TickItem.Encoding encoding;

    private final int date;

    private final Ohlc tradeOhlc = new Ohlc();

    private final Ohlc mittelkursOhlc = new Ohlc();

    private final SyntheticTickBuilder stb;

    private final StringBuilder sb = new StringBuilder(16);

    private PriceValidator validator;

    private final PriceValidator extraValidator;

    private int numOutOfOrder = 0;

    private int numLines = 0;

    public SyntheticTradeWriter(File f, Set<ByteString> symbols) {
        super(ByteOrder.LITTLE_ENDIAN, f);

        this.symbols = symbols;
        this.encoding = file.getName().endsWith(".td3") ? TICK3 : TICKZ;
        this.market = new FeedMarketRepository(true)
                .getMarket(new ByteString(TickFiles.getMarketName(f)));

        this.handler = new DefaultIndexHandler(file) {
            @Override
            protected void doHandle(ByteString vwdcode, long position, int length) {
                addItem(vwdcode, position, length);
            }
        };

        this.stb = new SyntheticTickBuilder(MARKETS_WITH_ASK_CHECK.contains(market.getName()));
        this.extraValidator = MARKET_VALIDATORS.get(market.getName());

        this.date = TickFile.getDay(this.file);
    }

    void process(File out) throws IOException {
        TimeTaker tt = new TimeTaker();
        final File tmp = File.createTempFile("synth", ".tmp", out.getParentFile());
        boolean success = false;

        try (FileChannel chIn = new RandomAccessFile(this.file, "r").getChannel();
             PrintWriter pw = new PrintWriter(tmp)) {
            process(chIn, pw);
            success = true;
        } catch (Throwable t) {
            this.logger.error("FATAL error for " + this.file.getName(), t);
        } finally {
            unmapBuffers();
        }

        if (success && this.numLines > 0) {
            if (!tmp.renameTo(out)) {
                this.logger.error("<process> rename " + tmp.getName() + " to "
                        + out.getAbsolutePath() + " failed?!");
            }
            else {
                this.logger.info("<process> wrote " + out.getName()
                        + " with " + this.numLines + " entries"
                        + (numOutOfOrder > 0 ? (", #outOfOrder=" + numOutOfOrder) : "")
                        + ", took " + tt);
            }
        }
        else if (!tmp.delete()) {
            this.logger.error("<process> failed to delete " + tmp.getAbsolutePath());
        }
    }


    protected void readItems(FileChannel ch) throws IOException {
        TickFileIndexReader.readEntries(ch, this.handler);
    }

    protected void addItem(ByteString vendorkey, long address, int length) {
        if (this.symbols != null && !this.symbols.contains(vendorkey)) {
            return;
        }
        FeedData fd = RT_TICKS.create(VendorkeyVwd.getInstance(vendorkey, 1), market);
        OrderedTickData td = ((OrderedFeedData) fd).getOrderedTickData();
        td.setDate(this.date);
        td.setStoreAddress(address);
        td.setLength(length);
        market.add(fd);
    }

    private void process(FileChannel ch, PrintWriter pw) throws IOException {
        readItems(ch);
        pw.println(HEADER);

        if (market.size() == 0) {
            return;
        }

        mapFile(ch);

        for (FeedData item : market.getElements(false)) {
            OrderedTickData td = ((OrderedFeedData) item).getOrderedTickData();
            byte[] ticks;
            try {
                ticks = addTicks(td);
                try {
                    write(item, ticks, POSITIVE, pw);
                } catch (NegativeValueException e) {
                    write(item, ticks, POSITIVE_AND_NEGATIVE, pw);
                }
            } catch (Throwable e) {
                this.logger.error(file.getName() + " process failed for " + item.getVwdcode(), e);
            }
        }
    }

    /**
     * calc synthetic trades for item in ticks and write ohlc to pw
     * @param item
     * @param ticks
     * @param priceValidator
     * @param pw
     * @throws NegativeValueException iff acceptNegative is true and a negative bid/ask is
     * encountered; in that case, this method will be called again with acceptNegative = true.
     * The problem is that a value of 0 must be ignored when positive bids/asks are processed but
     * it must be included if negative values occur as well. Since we do not know in advance whether
     * negative values will occur, an exception is used.
     */
    private void write(FeedData item, byte[] ticks, final PriceValidator priceValidator,
            PrintWriter pw) throws NegativeValueException {
        TickDecompressor td = new TickDecompressor(ticks, this.encoding);
        this.validator = priceValidator;

        reset();

        int lastTime = 0;
        int tickFlags = TICK_FLAGS;

        for (TickDecompressor.Element e : td) {
            if (!e.hasFlag(tickFlags)) {
                continue;
            }

            BufferFieldData fd = e.getData();
            final int time = getTime(fd);
            if (time < lastTime) {
                numOutOfOrder++;
                continue;
            }
            lastTime = time;

            if (this.tradeOhlc.empty) {
                if (processFields(fd)) {
                    // we found a trade, continue with trades only
                    tickFlags = FeedUpdateFlags.FLAG_WITH_TRADE;
                }
            }
            else {
                processTrade(fd);
            }
        }
        appendOhlc(item, pw);
    }

    private void reset() {
        this.stb.reset(this.validator);
        this.tradeOhlc.reset();
        this.mittelkursOhlc.reset();
    }

    private void appendOhlc(FeedData item, PrintWriter pw) {
        final Ohlc ohlc = getOhlc();
        if (ohlc.empty /*&& (stb.overflow || this.ohlc.isWithPositiveScale())*/) {
            return;
        }
        pw.append(item.getVwdcode().toString());
        for (long bd : ohlc.values) {
            pw.append(';').append(format(bd));
        }

        appendTypeIndicator(pw, ohlc);
        appendAwarenessIndicator(pw, ohlc);

        pw.println();
        this.numLines++;
    }

    private Ohlc getOhlc() {
        if (!this.tradeOhlc.empty) {
            return this.tradeOhlc;
        }
        if (!this.mittelkursOhlc.empty) {
            return this.mittelkursOhlc;
        }
        return this.stb.ohlc;
    }

    private void appendAwarenessIndicator(PrintWriter pw, Ohlc ohlc) {
        if (this.validator == POSITIVE) {
            if (MdpsFeedUtils.decodePrice(ohlc.values[1])
                    .divide(FIVE, RoundingMode.DOWN)
                    .compareTo(MdpsFeedUtils.decodePrice(ohlc.values[2])) > 0) {
                pw.append(";H");
            }
        }
    }

    private void appendTypeIndicator(PrintWriter pw, Ohlc ohlc) {
        if (ohlc == this.tradeOhlc) {
            pw.append(";T");
        }
        else if (ohlc == this.mittelkursOhlc) {
            pw.append(";M");
        }
        else {
            if (!stb.withValidAsk) {
                pw.append(";B");
            }
            else {
                if (!stb.withValidBid) {
                    pw.append(";A");
                }
                if (stb.isWithInvalidAsk()) {
                    pw.append(";X");
                }
            }
        }
    }

    private void processTrade(BufferFieldData fd) {
        for (int oid = fd.readNext(); oid > 0; oid = fd.readNext()) {
            switch (oid) {
                case VwdFieldOrder.ORDER_ADF_GELD:
                case VwdFieldOrder.ORDER_ADF_BRIEF:
                    // detect negative bids/asks that occur after first trade
                    validatePrice(getPrice(fd));
                    break;
                case VwdFieldOrder.ORDER_ADF_BEZAHLT:
                    long trade = getPrice(fd);
                    if (validatePrice(trade)) {
                        addTrade(trade);
                        return;
                    }
                    break;
                default:
                    fd.skipCurrent();
            }
        }
    }

    private boolean processFields(BufferFieldData fd) {
        for (int oid = fd.readNext(); oid > 0; oid = fd.readNext()) {
            switch (oid) {
                case VwdFieldOrder.ORDER_ADF_GELD:
                    stb.updateBid(getPrice(fd));
                    break;
                case VwdFieldOrder.ORDER_ADF_BRIEF:
                    stb.updateAsk(getPrice(fd));
                    break;
                case VwdFieldOrder.ORDER_ADF_BEZAHLT:
                    long trade = getPrice(fd);
                    if (validatePrice(trade)) {
                        addTrade(trade);
                        return true;
                    }
                    break;
                case VwdFieldOrder.ORDER_ADF_MITTELKURS:
                    long mittelkurs = getPrice(fd);
                    if (validatePrice(mittelkurs)) {
                        addMittelkurs(mittelkurs);
                        return false;
                    }
                    break;
                case VwdFieldOrder.ORDER_ADF_RENDITE_BRIEF:
                    if (stb.isWithRenditeInBidAndAsk(getPrice(fd))) {
                        return false;
                    }
                    break;
                default:
                    fd.skipCurrent();
                    break;
            }
        }
        stb.addSyntheticTrade();
        return false;
    }

    private boolean validatePrice(long value) {
        return this.validator.isValid(value)
                && (this.extraValidator == null || this.extraValidator.isValid(value));
    }

    private void addTrade(long trade) {
        this.tradeOhlc.add(trade);
    }

    private void addMittelkurs(long mittelkurs) {
        this.mittelkursOhlc.add(mittelkurs);
    }

    private String format(long bd) {
        return format(bd, this.sb);
    }

    static String format(long bd, StringBuilder sb) {
        sb.setLength(0);
        sb.append((int) bd);
        final int minusOffset = sb.charAt(0) == '-' ? 1 : 0;

        int scale = MdpsFeedUtils.getMdpsPriceScale(bd);
        if (scale < 0) {
            final int offset = sb.length() + scale;
            if (offset > minusOffset) {
                sb.insert(offset, '.');
                int n = sb.length();
                //noinspection StatementWithEmptyBody
                while (n-- > offset && (sb.charAt(n) == '0' || sb.charAt(n) == '.')) ;
                sb.setLength(n + 1);
            }
            else {
                sb.insert(minusOffset, "0.000000000000000", 0, -offset + 2 + minusOffset);
            }
        }
        else {
            while (scale-- > 0) {
                sb.append('0');
            }
        }
        return sb.toString();
    }

    private int getTime(BufferFieldData fd) {
        final int mdpsTime = fd.getInt();
        return MdpsFeedUtils.decodeTime(mdpsTime) * 1000 + MdpsFeedUtils.decodeTimeMillis(mdpsTime);
    }

    private byte[] addTicks(OrderedTickData td) {
        byte[] ticks = new byte[td.getLength()];
        fillFileTickStoreTicks(td.getStoreAddress(), ticks);
        return ticks;
    }
}
