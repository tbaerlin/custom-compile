/*
 * NewsRecordEditor.java
 *
 * Created on 28.09.2009 09:32:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.SnapRecordVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.news.data.NewsRecordImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class NewsRecordEditor {
    private SnapRecordVwd snap;

    private NewsRecordImpl newsRecord;

    // For some older records, the iid list is stored in field 1009 instead of 2008
    private static final int OLD_IID_LIST_ID = 1009;

    NewsRecordEditor(NewsRecordImpl newsRecord) {
        this.newsRecord = newsRecord;
        this.snap = newsRecord.getSnapRecord();
    }

    NewsRecordImpl apply(List<NewsRecordUpdate> updates) {
        for (NewsRecordUpdate update : updates) {
            update.apply(this);
        }
        if (this.snap == this.newsRecord.getSnapRecord()) {
            return this.newsRecord;
        }
        return new NewsRecordImpl(computeId(), this.snap, this.newsRecord.getStory(), null);
    }

    String getField(VwdFieldDescription.Field field) {
        return SnapRecordUtils.getString(snap, field.id());
    }

    void replaceField(VwdFieldDescription.Field field, String replacementStr) {
        final int[] fids = snap.getFieldids();
        final int p = getFieldPosition(field, fids);
        if (p < 0) {
            return;
        }

        final int offset = snap.getOffsets()[p];
        final int end = snap.getOffsets()[p + 1];
        final byte[] data = snap.getData();

        byte[] replacement = replacementStr.getBytes(snap.getCharset());

        final int lenDiff = replacement.length - (end - offset);

        final byte[] dataNew = ByteBuffer.allocate(data.length + lenDiff)
                .put(data, 0, offset)
                .put(replacement)
                .put(data, end, data.length - end)
                .array();

        this.snap = new SnapRecordVwd(snap.getFieldids(), adjustOffsets(p, lenDiff), dataNew);
    }

    private int getFieldPosition(VwdFieldDescription.Field field, int[] fids) {
        final int p = Arrays.binarySearch(fids, field.id());
        if (p < 0 && field == VwdFieldDescription.MMF_Iid_List) {
            return Arrays.binarySearch(fids, OLD_IID_LIST_ID);
        }
        return p;
    }

    private String computeId() {
        return NewsRecordBuilder.encodeId(NewsRecordBuilder.decodeId(this.newsRecord.getId()) + 1);
    }

    private int[] adjustOffsets(int p, int diff) {
        if (diff == 0) {
            return snap.getOffsets();
        }
        final int[] result = Arrays.copyOf(snap.getOffsets(), snap.getOffsets().length);
        for (int k = p + 1; k < result.length; k++) {
            result[k] += diff;
        }
        return result;
    }
}
