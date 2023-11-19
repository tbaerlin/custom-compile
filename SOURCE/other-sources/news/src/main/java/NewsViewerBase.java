/*
 * NewsViewerBase.java
 *
 * Created on 25.10.12 09:46
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.common.util.HexDump;
import de.marketmaker.istar.common.util.NumberUtil;
import de.marketmaker.istar.feed.vwd.SnapFieldVwd;
import de.marketmaker.istar.feed.vwd.SnapFieldVwdFormatter;
import de.marketmaker.istar.feed.vwd.SnapRecordVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;
import de.marketmaker.istar.news.frontend.NewsRecord;

/**
 * @author oflege
 */
class NewsViewerBase {
    protected int textLen = -1;

    protected int rawTextLen = -1;

    protected static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    SnapFieldVwdFormatter f = new SnapFieldVwdFormatter();

    protected void dumpItems(List<NewsRecord> items) {
        for (NewsRecord item : items) {
            dumpItem(item);
        }
    }

    protected void dumpItem(NewsRecord item) {
        NewsRecordImpl record = (NewsRecordImpl) item;
        printId(record);
        System.out.println(HexDump.toHex(record.getSnapRecord().getData()));
        System.out.println(HexDump.toHex(record.getStory()));
    }

    protected void printItems(List<NewsRecord> items) {
        for (NewsRecord nr : items) {
            printItem(nr);
        }
    }

    protected void printItem(NewsRecord nr) {
        NewsRecordImpl record = (NewsRecordImpl) nr;
        printId(record);
        if (nr == null) {
            return;
        }
        SnapRecordVwd sr = record.getSnapRecord();
        final int[] fieldids = sr.getFieldids();
        for (int fieldid : fieldids) {
            if (fieldid < 10000) {
                try {
                    if (fieldid == VwdFieldDescription.NDB_ContentDescriptor.id()) {
                        final SnapFieldVwd field = (SnapFieldVwd) sr.getField(fieldid);
                        printField(fieldid,
                                "0x" + Integer.toHexString(((Number) field.getValue()).intValue())
                                        + " (" + sr.getCharset().displayName()
                                        + ", "
                                        + (nr.isHtml() ? "html" : nr.isNitf() ? "nitf" : "text")
                                        + ")");
                    }
                    else if (fieldid == 1009) {
                        printField(VwdFieldDescription.MMF_Iid_List.id(),
                                record.getAttributes(NewsAttributeEnum.IID));
                    }
                    else if (fieldid == VwdFieldDescription.NDB_Selectors.id()) {
                        final SnapFieldVwd field = (SnapFieldVwd) sr.getField(fieldid);
                        final String selectorStr = field.getValue().toString();
                        printField(fieldid, selectorStr + " // " + formatSelectors(selectorStr));
                    }
                    else if (fieldid == VwdFieldDescription.NDB_dummy_54.id()) {
                        final SnapFieldVwd field = (SnapFieldVwd) sr.getField(fieldid);
                        final String gallery = field.getValue().toString();
                        printField(fieldid, gallery);
                    }
                    else {
                        final SnapFieldVwd field = (SnapFieldVwd) sr.getField(fieldid);
                        printField(fieldid, f.formatValue(field));
                    }
                } catch (Exception e) {
                    System.err.println("no such field: " + fieldid);
                }
            }
        }
        printText(record, false);
        printText(record, true);
    }

    private String formatSelectors(String selectorStr) {
        final String[] selectors = selectorStr.split(",");
        List<String> invalid = null;
        Map<Integer, String> m = new TreeMap<>();
        for (String s: selectors) {
            try {
                m.put(EntitlementsVwd.toValue(s), EntitlementsVwd.normalize(s));
            } catch (IllegalArgumentException e) {
                if (invalid == null) {
                    invalid = new ArrayList<>();
                }
                invalid.add(s);
            }
        }
        if (invalid != null) {
            return m.values().toString() + ", INVALID: " + invalid;
        }
        return m.values().toString();
    }

    private void printText(NewsRecordImpl nr, boolean raw) {
        int len = raw ? rawTextLen : textLen;
        if (len < 0) {
            return;
        }
        String t = raw ? nr.getRawText() : nr.getText();
        if (t == null) {
            System.out.println("-- " + (raw ? "Raw" : "") + "Text is null");
        }
        else {
            System.out.println("-- " + (raw ? "Raw" : "") + "Text:--");
            System.out.println((len == 0) ? t : t.substring(0, Math.min(len, t.length())));
        }
    }

    private static void printId(NewsRecordImpl nr) {
        System.out.println("===================================================");
        if (nr == null) {
            System.out.println(" null");
            return;
        }

        // inlined version of ...news.backend.News2Document#encodeShortid
        // which we avoid as that requires lucene on classpath
        String encodedShortId = Integer.toString(nr.getShortId(), Character.MAX_RADIX);

        System.out.printf(" %14s, short: %8d (%s), %s",
                nr.getId(), nr.getShortId(), encodedShortId, length(nr.getStory()));
        String rawLength = length(nr.getRawStory());
        if (!"n/a".equals(rawLength)) {
            System.out.println(", " + rawLength);
        }
        System.out.println();
        System.out.println("---------------------------------------------------");
    }

    private static String length(byte[] bytes) {
        if (bytes == null) {
            return "n/a";
        }
        return NumberUtil.humanReadableByteCount(bytes.length);
    }

    private static void printField(int id, Object value) {
        System.out.printf("%4d %21s = %s%n", id, VwdFieldDescription.getField(id).name(), value);
    }


}
