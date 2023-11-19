/*
 * OrderedSnapBuilder.java
 *
 * Created on 29.08.12 15:05
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import net.jcip.annotations.NotThreadSafe;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.snap.SnapData;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;

import static de.marketmaker.istar.feed.FeedUpdateFlags.*;

/**
 * @author oflege
 */
@NotThreadSafe
public class OrderedSnapBuilder implements OrderedUpdateBuilder {

    private static final boolean ALWAYS_FILTER_NT_SNAP = Boolean.getBoolean("istar.snap.nt.filter.always");

    private static final int STATIC_RECAP_TYPE = toBit(VwdFeedConstants.MESSAGE_TYPE_STATIC_RECAP);

    private static final int FIELDSAVE_FLAGS =
            FLAG_WITH_CLOSE_DATE_YESTERDAY + FLAG_WITH_OLD_HANDELSDATUM;

    private boolean realtime = true;

    private final FieldDataMerger merger = new FieldDataMerger();

    private final BufferFieldData existing = new BufferFieldData();

    private final UnsafeFieldData existingU = new UnsafeFieldData();

    private final DynamicFieldDataFilter filter;

    private final int messageTypeBits = toBit(VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE)
            + toBit(VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_RECAP)
            + toBit(VwdFeedConstants.MESSAGE_TYPE_STATIC_UPDATE)
            + STATIC_RECAP_TYPE
            + toBit(VwdFeedConstants.MESSAGE_TYPE_RATIOS);

    public OrderedSnapBuilder() {
        this(false);
    }

    public OrderedSnapBuilder(boolean withFilter) {
        this.filter = withFilter ? new DynamicFieldDataFilter() : null;
    }

    private static int toBit(final byte type) {
        return 1 << (type - 64);
    }

    public void setRealtime(boolean realtime) {
        this.realtime = realtime;
    }

    @Override
    public void process(OrderedFeedData fd, OrderedUpdate update) {
        final int updateType = this.messageTypeBits & toBit(update.getMsgType());
        if (updateType == 0) {
            // these messages are so rare we don't include them in messageTypeBits
            if (update.getMsgType() == VwdFeedConstants.MESSAGE_TYPE_DELETE_FIELDS) {
                removeFields(fd, update);
            }
            return;
        }
        if (updateType == STATIC_RECAP_TYPE) {
            doUpdateType(fd, update);
        }

        final OrderedSnapData sd = fd.getSnapData(this.realtime);
        if (sd == null) {
            return;
        }

        final OrderedUpdate filtered = applyFilter(update);
        if (filtered != null) {
            process(sd, filtered);
        }
    }

    protected void process(OrderedSnapData sd, OrderedUpdate update) {
        sd.setLastUpdateTimestamp(update.getTimestamp());

        final byte[] newData = merge(sd, update);
        if (newData != null) {
            sd.init(null, newData);
        }
    }

    protected void doUpdateType(FeedData fd, OrderedUpdate update) {
        fd.setVendorkeyType(update.getVwdKeyType());
    }

    private FieldData asFieldData(SnapData sd) {
        return (sd instanceof UnsafeSnapData)
                ? ((UnsafeSnapData) sd).reset(this.existingU)
                : this.existing.reset(sd.getData(false));
    }

    private void removeFields(FeedData fd, OrderedUpdate update) {
        final OrderedSnapData sd = (OrderedSnapData) fd.getSnapData(this.realtime);
        if (sd == null || !sd.isInitialized()) {
            return;
        }
        final byte[] newData = this.merger.remove(asFieldData(sd), update.getFieldData());
        if (newData != null) {
            sd.init(null, newData.length > 0 ? newData : null);
        }
    }

    private byte[] merge(SnapData sd, OrderedUpdate update) {
        if (sd.isInitialized()) { // common case
            return merge(asFieldData(sd), update);
        }
        final byte[] initial = update.getFieldData().getAsByteArray();
        return (initial.length != 0) ? initial : null;
    }

    private byte[] merge(FieldData data, OrderedUpdate update) {
        if (update.hasFlag(FIELDSAVE_FLAGS)) {
            final FieldData save = update.hasFlag(FLAG_WITH_CLOSE_DATE_YESTERDAY)
                    ? FieldSaveClose.create(data, update.getFieldData())
                    : FieldSaveBezahlt.create(data, update.getFieldData());

            return merge(data, update, save);
        }
        return this.merger.merge(data, update.getFieldData());
    }

    private byte[] merge(FieldData data, OrderedUpdate update, FieldData save) {
        final byte[] merged = this.merger.merge(data.rewind(), update.getFieldData());
        if (save == null) {
            return merged;
        }
        final FieldData toBeMerged = (merged != null) ? asExisting(merged) : data.rewind();
        final byte[] saved = this.merger.merge(toBeMerged, save);
        return (saved != null) ? saved : (merged != null) ? merged : null;
    }

    private OrderedUpdate applyFilter(OrderedUpdate update) {
        if (this.filter != null && (update.hasFlag(FLAG_WITH_NON_DYNAMIC_FIELD) || ALWAYS_FILTER_NT_SNAP)) {
            return this.filter.applyTo(update);
        }
        return update;
    }

    private FieldData asExisting(byte[] fields) {
        return this.existing.reset(fields);
    }
}
