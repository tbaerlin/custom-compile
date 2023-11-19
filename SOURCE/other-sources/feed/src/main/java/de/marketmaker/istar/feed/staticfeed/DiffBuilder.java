/*
 * DiffWriter.java
 *
 * Created on 19.10.12 11:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.staticfeed;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldData;
import de.marketmaker.istar.feed.ordered.FieldDataUtil;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;
import de.marketmaker.istar.feed.snap.SnapData;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;

import static de.marketmaker.istar.feed.mdps.MdpsTypeMappings.UNKNOWN;

/**
 * Compares incoming records against the current realtime snap and invokes methods on its
 * delegate {@link DiffFormatter} for all new or changed fields.
 * @author oflege
 */
public class DiffBuilder extends StaticBuilder implements OrderedUpdateBuilder {

    private final BufferFieldData fieldData = new BufferFieldData();

    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {

        final char msgType = update.isDelete() ? DiffFormatter.DELETE : DiffFormatter.UPDATE;

        // StaticSnapBuilder#setType stores the mdpsKeyType as vendorkeyType
        final int currentType = data.getVendorkeyType();
        final int updateType = update.getVwdKeyType();
        final int type = (updateType != UNKNOWN) ? updateType : currentType;

        this.formatter.init(msgType, data.getVwdcode(), type, update.getSourceId());

        if (update.getMsgType() == VwdFeedConstants.MESSAGE_TYPE_DELETE_FIELDS) {
            appendDeletedFields(update);
            this.formatter.finish(type != currentType);
            return;
        }

        final SnapData sd = data.getSnapData(true);
        if (sd != null && sd.isInitialized()) {
            try {
                appendUpdate(update.getFieldData(), this.fieldData.reset(sd.getData(false)));
                // we can call finish with false as any new field would trigger a diff write
                // anyway; a change of the type should only matter if it differs from the last
                // type sent from the same source, in which case we would have appended field 17H
                this.formatter.finish(false);
            } catch (Exception e) {
                this.logger.error("<process> failed for " + data.getVwdcode(), e);
                sd.init(null, null);
            }
        }
        else {
            appendAll(update.getFieldData());
            // forward new records even if no fields are present
            this.formatter.finish(data.getState() == FeedData.STATE_NEW);
        }
    }

    private void appendDeletedFields(OrderedUpdate inUpdate) {
        final BufferFieldData update = inUpdate.getFieldData();
        for (int oid = update.readNext(); oid > 0; oid = update.readNext()) {
            this.formatter.fieldDeleted(update.getVwdId());
            update.skipCurrent();
        }
    }

    private void appendUpdate(BufferFieldData update, BufferFieldData existing) {
        int existingId = existing.readNext();
        int updateId = update.readNext();
        while (updateId != 0) {
            if (updateId < existingId || existingId == 0) {
                appendField(update);
                updateId = update.readNext();
            }
            else if (updateId > existingId) {
                existing.skipCurrent();
                existingId = existing.readNext();
            }
            else  {
                appendFieldIfChanged(update, existing);
                updateId = update.readNext();
                existingId = existing.readNext();
            }
        }
    }

    private void appendFieldIfChanged(BufferFieldData update, FieldData existing) {
        switch (update.getType()) {
            case FieldData.TYPE_INT:
                addIntIfChanged(update, existing);
                break;
            case FieldData.TYPE_TIME:
                addTimeIfChanged(update, existing);
                break;
            case FieldData.TYPE_PRICE:
                addPriceIfChanged(update, existing);
                break;
            case FieldData.TYPE_STRING:
                addStringIfChanged(update, existing);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void addStringIfChanged(BufferFieldData update, FieldData existing) {
        if (update.stringEquals(existing.getBytes())) {
            update.skipCurrent();
        }
        else {
            appendField(update);
        }
    }

    private void addPriceIfChanged(BufferFieldData update, FieldData existing) {
        final long updatePrice;
        if ((updatePrice = FieldDataUtil.getPrice(update)) != FieldDataUtil.getPrice(existing)) {
            this.formatter.priceChanged(update.getVwdId(), updatePrice);
        }
    }

    private void addIntIfChanged(BufferFieldData update, FieldData existing) {
        final int updateValue;
        if ((updateValue = update.getInt()) != existing.getInt()) {
            this.formatter.intChanged(update.getVwdId(), updateValue);
        }
    }

    private void addTimeIfChanged(BufferFieldData update, FieldData existing) {
        final int updateValue;
        if ((updateValue = update.getInt()) != existing.getInt()) {
            this.formatter.intChanged(update.getVwdId(), MdpsFeedUtils.decodeTime(updateValue));
        }
    }
}
