package de.marketmaker.istar.feed.exporttools.boersestg;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedSnapRecord;
import de.marketmaker.istar.feed.ordered.tick.OrderedTickData;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.ordered.OrderedFeedDataFactory.RT_TICKS;
import static de.marketmaker.istar.feed.vwd.VwdFieldOrder.ORDER_ADF_BEZAHLT_UMSATZ;

public class ExporterHelper {

    private static final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    static {
        df.applyLocalizedPattern("0.####");
    }

    public static void collectData(BufferFieldData fd, long[] data, String[] supplements) {
        Arrays.fill(data, Long.MIN_VALUE);
        Arrays.fill(supplements, null);

        for (int oid = fd.readNext(); oid > 0 && oid <= ORDER_ADF_BEZAHLT_UMSATZ; oid = fd.readNext()) {
            switch (oid) {
                // bid
                case VwdFieldOrder.ORDER_ADF_GELD:
                    data[0] = MdpsFeedUtils.encodePrice(fd.getInt(), fd.getByte());
                    break;
                case VwdFieldOrder.ORDER_ADF_GELD_UMSATZ:
                    data[1] = fd.getUnsignedInt();
                    break;
                case VwdFieldOrder.ORDER_ADF_GELD_KURSZUSATZ:
                    supplements[0] = OrderedSnapRecord.toString(fd.getBytes());
                    break;
                // ask
                case VwdFieldOrder.ORDER_ADF_BRIEF:
                    data[2] = MdpsFeedUtils.encodePrice(fd.getInt(), fd.getByte());
                    break;
                case VwdFieldOrder.ORDER_ADF_BRIEF_UMSATZ:
                    data[3] = fd.getUnsignedInt();
                    break;
                case VwdFieldOrder.ORDER_ADF_BRIEF_KURSZUSATZ:
                    supplements[1] = OrderedSnapRecord.toString(fd.getBytes());
                    break;
                // trade
                case VwdFieldOrder.ORDER_ADF_BEZAHLT:
                    data[4] = MdpsFeedUtils.encodePrice(fd.getInt(), fd.getByte());
                    break;
                case VwdFieldOrder.ORDER_ADF_BEZAHLT_UMSATZ:
                    data[5] = fd.getUnsignedInt();
                    break;
                case VwdFieldOrder.ORDER_ADF_BEZAHLT_KURSZUSATZ:
                    supplements[2] = OrderedSnapRecord.toString(fd.getBytes());
                    break;
                default:
                    fd.skipCurrent();
            }
        }
    }

    public static void append(StringBuilder line, long price, long volume, String supplement) {
        if (price != Long.MIN_VALUE) {
            line.append(df.format(MdpsFeedUtils.decodePrice(price)));
            line.append(";");
            if (volume != Long.MIN_VALUE) {
                line.append(volume);
            }
            line.append(";");
            if (supplement != null) {
                line.append(supplement);
            }
            line.append(";");
        }
        else {
            line.append(";;;");
        }
    }

    public static void addItem(List<FeedData> items, ByteString vwdcode, long address, int length, int date) {
        FeedData fd = RT_TICKS.create(VendorkeyVwd.getInstance(vwdcode, 1), null);
        OrderedTickData td = ((OrderedFeedData) fd).getOrderedTickData();
        td.setDate(date);
        td.setStoreAddress(address);
        td.setLength(length);

        items.add(fd);
    }

}
