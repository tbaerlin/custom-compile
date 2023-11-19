package de.marketmaker.istar.feed.ordered;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.ordered.tick.TickBuilder;
import de.marketmaker.istar.feed.ordered.tick.TickCli;
import de.marketmaker.istar.feed.vwd.MarketTickFields;
import de.marketmaker.istar.feed.vwd.TickTypeCheckerVwd;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.FeedUpdateFlags.FLAG_WITH_SPECIAL_TICK_FIELDS;
import static de.marketmaker.istar.feed.vwd.VwdFieldOrder.*;

/**
 * Converter from DPH to TickCli-importable format.
 *
 * <ul>
 *     <li>Data in DPH files cannot be directly converted into format in TDZ files due to missing FeedData instances.</li>
 *     <li>Importing data into existing TDZ files is implemented by {@link TickCli}.</li>
 *     <li>Few DPH file items qualify as ticks according to the following criteria {@link TickTypeCheckerVwd#DEFAULT}.</li>
 * </ul>
 *
 * @see TickTypeCheckerVwd#DEFAULT
 * @author Manuel Coenen
 */
public class HistoricToTickConverter extends FieldDataBuilder {

    private int tickTime;

    public HistoricToTickConverter(int bufferSize) {
        super(bufferSize);
    }

    public BufferFieldData convert(OrderedUpdate update) {
        this.bb.clear();
        final BufferFieldData fd = update.getFieldData();

        this.tickTime = TickBuilder.getTime(fd);
        // the first field is always time, its id/type is implicit, so we do NOT put it first
        bb.putInt(this.tickTime);

        this.lastOrder = 0;
        addTickFields(update, fd);

        if (fd.getId() > 0 && update.hasFlag(FLAG_WITH_SPECIAL_TICK_FIELDS)) {
            addAdditionalFields(fd, MarketTickFields.getTickOrderIds(update.getVendorkey().getMarketName()));
        }

        return new BufferFieldData(this.asArray());
    }

    private void addTickFields(OrderedUpdate update, BufferFieldData fd) {
        for (int oid = fd.getId(); oid != 0 && oid < VwdFieldOrder.FIRST_NON_TICK; oid = fd.readNext()) {
            switch (oid) {
                case ORDER_MMF_BEZAHLT_DATUM:
                case ORDER_MMF_BOERSENZEIT:
                case ORDER_ADF_PROZENTUALE_VERAENDERUNG:
                case ORDER_ADF_VERAENDERUNG:
                case ORDER_ADF_BRIEF_QUELLE:
                case ORDER_ADF_GELD_QUELLE:
                case ORDER_ADF_BEZAHLT_DATUM:
                case ORDER_ADF_DATUM_QUOTIERUNG:
                case ORDER_ADF_DATUM:
                case ORDER_ADF_HANDELSDATUM:
                    fd.skipCurrent();
                    break;
                case ORDER_ADF_ZEIT_QUOTIERUNG:
                case ORDER_ADF_GELD_ZEIT:
                case ORDER_ADF_BRIEF_ZEIT:
                case ORDER_ADF_BEZAHLT_ZEIT:
                    addTime(oid, fd.getInt());
                    break;
                case ORDER_ADF_QUELLE:
                    addQuelle(update, fd);
                    break;
                default:
                    addFieldToBuffer(fd);
                    break;
            }
        }
    }

    private void addAdditionalFields(BufferFieldData fd, BitSet additionalOids) {
        if (additionalOids == null) {
            return;
        }
        final int max = additionalOids.size();
        for (int oid = fd.getId(); oid != 0 && oid < max; oid = fd.readNext()) {
            if (additionalOids.get(oid)) {
                addFieldToBuffer(fd);
            } else {
                fd.skipCurrent();
            }
        }
    }

    private void addQuelle(OrderedUpdate update, BufferFieldData fd) {
        if (update.hasFlag(FeedUpdateFlags.FLAG_WITH_QUELLE)) {
            addFieldToBuffer(fd);
        } else {
            fd.skipCurrent();
        }
    }

    private void addTime(int id, final int time) {
        if (time != this.tickTime) {
            putTimeFid(id);
            this.bb.putInt(time);
        }
    }

    public static void main(String[] args) {
        DpHistoricUpdateBuilder dphub = new DpHistoricUpdateBuilder();
        TickCli.LineBuilder b = new TickCli.LineBuilder();
        HistoricToTickConverter httc = new HistoricToTickConverter(1024*1024*10);

        // Map Date -> Market -> Symbol -> Entries
        Map<Integer, Map<ByteString, Map<Vendorkey, List<String>>>> result = new HashMap<>();

        Iterator<OrderedUpdate> iterator = dphub.read(new File(args[0]), args[1]);
        while (iterator.hasNext()) {
            OrderedUpdate orderedUpdate = iterator.next();
            BufferFieldData fd = orderedUpdate.getFieldData();
            int date = getDate(fd);
            int flags = getTickFlags(fd) | orderedUpdate.getFlags();
            if (flags != 0) {
                VendorkeyVwd vendorkey = orderedUpdate.getVendorkey();
                ByteString marketName = vendorkey.getMarketName();

                BufferFieldData fdTick = httc.convert(orderedUpdate);
                String tickLine = b.build(orderedUpdate.getFlags() | flags, fdTick);
                result.computeIfAbsent(date, d -> new HashMap<>())
                        .computeIfAbsent(marketName, m -> new HashMap<>())
                        .computeIfAbsent(vendorkey, v -> new ArrayList<>())
                        .add(tickLine);
            }
        }

        result.forEach((date, markets) -> {
            markets.forEach((market, symbols) -> {
                File output = new File("/home/manuel/tmp/dtt", market + "-" + date + ".txt");
                try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(output)));) {
                    symbols.forEach((symbol, ticks) -> {
                        out.println("# " + symbol);
                        ticks.sort((o1, o2) -> {
                            LocalTime lt1 = LocalTime.parse(o1.split(";")[1]);
                            LocalTime lt2 = LocalTime.parse(o2.split(";")[1]);

                            return lt1.compareTo(lt2);
                        });
                        ticks.forEach(out::println);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private static int getTickFlags(BufferFieldData fd) {
        // the first field is always time, its id/type is implicit and we have to skip it like this
        TickBuilder.getTime(fd);
        int flags = 0;
        boolean trade = false;
        for (int oid = fd.getId(); oid != 0 && oid < VwdFieldOrder.FIRST_NON_TICK; oid = fd.readNext()) {
            switch (oid) {
                case ORDER_ADF_BEZAHLT:
                    final BigDecimal bezahlt = BigDecimal.valueOf(fd.getInt(), -fd.getByte());
                    trade = (bezahlt.compareTo(BigDecimal.ZERO) > 0); // bezahlt > 0
                    break;
                case ORDER_ADF_RENDITE: // Intentional fallthrough
                case ORDER_ADF_SCHLUSS:
                    trade = false;
                    fd.skipCurrent();
                    break;
                case ORDER_ADF_BRIEF:
                    final BigDecimal brief = BigDecimal.valueOf(fd.getInt(), -fd.getByte());
                    if (brief.compareTo(BigDecimal.ZERO) > 0) {
                        flags |= FeedUpdateFlags.FLAG_WITH_ASK;
                    }
                    break;
                case ORDER_ADF_GELD:
                    final BigDecimal geld = BigDecimal.valueOf(fd.getInt(), -fd.getByte());
                    if (geld.compareTo(BigDecimal.ZERO) > 0) {
                        flags |= FeedUpdateFlags.FLAG_WITH_BID;
                    }
                    break;
                default:
                    fd.skipCurrent();
            }
        }
        if (trade) {
            flags |= FeedUpdateFlags.FLAG_WITH_TRADE;
        }
        return flags;
    }

    private static int getDate(BufferFieldData fd) {
        // the first field is always time, its id/type is implicit and we have to skip it like this
        TickBuilder.getTime(fd);
        try {
            for (int oid = fd.getId(); oid != 0 && oid < VwdFieldOrder.FIRST_NON_TICK; oid = fd.readNext()) {
                if (oid == ORDER_ADF_HANDELSDATUM) {
                    return fd.getInt();
                }
                fd.skipCurrent();
            }
        } finally {
            // We need to rewind for the next steps
            fd.rewind();
        }
        return 0;
    }
}
