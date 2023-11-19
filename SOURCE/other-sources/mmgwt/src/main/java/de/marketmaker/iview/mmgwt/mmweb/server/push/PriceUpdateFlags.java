/*
 * PriceUpdateFlags.java
 *
 * Created on 12.03.2010 08:32:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.ThreadSafe;

import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushPrice;

import static de.marketmaker.iview.mmgwt.mmweb.client.prices.Price.UPDATED_BUT_UNCHANGED;

/**
 * Keeps track of fields that have been updated and that should be included in the next pushed
 * price update. For example, the DAX price gets one update per second, regardless of the actual
 * value changed or not. In order to show the unchanged update in the frontend, this class tracks
 * that there has been an update and modifies a pre computed PushPrice in the
 * {@link #postProcessDiff(de.marketmaker.iview.mmgwt.mmweb.client.push.PushPrice, java.util.BitSet)}
 * method.
 * There is no need to cover all possible fields, just fields that are compared to their
 * previous value and are rendered with a background indicating the change are of interest.
 * @author oflege
 */
@ThreadSafe
class PriceUpdateFlags {
    private static int numFlags = 0;

    private static int getNextFlag() {
        return 1 << numFlags++;
    }

    private enum FlagField {
        BID(VwdFieldDescription.ADF_Geld),
        ASK(VwdFieldDescription.ADF_Brief),
        PRICE(VwdFieldDescription.ADF_Bezahlt),
        CHANGE(VwdFieldDescription.ADF_Veraenderung),
        CHANGE_PCT(VwdFieldDescription.ADF_Prozentuale_Veraenderung),
        HIGH(VwdFieldDescription.ADF_Tageshoch),
        LOW(VwdFieldDescription.ADF_Tagestief);

        private final int fieldId;

        private final int flag;

        FlagField(VwdFieldDescription.Field field) {
            this.fieldId = field.id();
            this.flag = getNextFlag();
        }

        boolean isPresentIn(int flags, BitSet allowedFields) {
            return ((flags & this.flag) != 0) && allowedFields.get(this.fieldId);
        }
    }

    private static final int[] FLAGS_BY_OID;

    static {
        int tmp = 0;
        for (FlagField ff : FlagField.values()) {
            tmp = Math.max(tmp, VwdFieldOrder.getOrder(ff.fieldId));
        }
        FLAGS_BY_OID = new int[tmp + 1];

        for (FlagField ff : FlagField.values()) {
            FLAGS_BY_OID[VwdFieldOrder.getOrder(ff.fieldId)] = ff.flag;
        }
    }

    /** has bits set for fields that received updates between calls of {@link #reset()} */
    private final AtomicInteger currentFlags = new AtomicInteger();

    /** value of currentFlags when {@link #reset()} was called */
    private final AtomicInteger previousFlags = new AtomicInteger();

    void postProcessDiff(PushPrice diff, BitSet allowedFields) {
        // Invoked by PeriodicPusher's executor thread after it invoked reset()
        //
        final int flags = this.previousFlags.get();
        if (diff.getPrice() == null && FlagField.PRICE.isPresentIn(flags, allowedFields)) {
            diff.setPrice(UPDATED_BUT_UNCHANGED);
        }
        if (diff.getAsk() == null && FlagField.ASK.isPresentIn(flags, allowedFields)) {
            diff.setAsk(UPDATED_BUT_UNCHANGED);
        }
        if (diff.getBid() == null && FlagField.BID.isPresentIn(flags, allowedFields)) {
            diff.setBid(UPDATED_BUT_UNCHANGED);
        }
        if (diff.getChangeNet() == null && FlagField.CHANGE.isPresentIn(flags, allowedFields)) {
            diff.setChangeNet(UPDATED_BUT_UNCHANGED);
        }
        if (diff.getChangePercent() == null && FlagField.CHANGE_PCT.isPresentIn(flags, allowedFields)) {
            diff.setChangePercent(UPDATED_BUT_UNCHANGED);
        }
        if (diff.getHigh() == null && FlagField.HIGH.isPresentIn(flags, allowedFields)) {
            diff.setHigh(UPDATED_BUT_UNCHANGED);
        }
        if (diff.getLow() == null && FlagField.LOW.isPresentIn(flags, allowedFields)) {
            diff.setLow(UPDATED_BUT_UNCHANGED);
        }
    }

    void reset() {
        // Invoked by PeriodicPusher's executor thread.
        // loop ensures we do not lose an update between getting the value and setting it to 0
        int current;
        do {
            current = this.currentFlags.get();
        }
        while (!this.currentFlags.compareAndSet(current, 0));
        this.previousFlags.set(current);
    }

    void update(OrderedUpdate update) {
        setFlags(computeFlags(update));
    }

    void update(ParsedRecord pr) {
        setFlags(computeFlags(pr));
    }

    private void setFlags(int flags) {
        // Invoked by parser thread.
        // loop ensures we do not miss the reset between getting the value and re-setting it
        int expected;
        do {
            expected = this.currentFlags.get();
        } while (!this.currentFlags.compareAndSet(expected, expected | flags));
    }

    private int computeFlags(ParsedRecord pr) {
        int result = 0;
        for (FlagField field : FlagField.values()) {
            if (pr.isFieldPresent(field.fieldId)) {
                result |= field.flag;
            }
        }
        return result;
    }

    private int computeFlags(OrderedUpdate update) {
        final BufferFieldData fd = update.getFieldData();
        int result = 0;
        for (int oid = fd.readNext(); oid > 0 && oid < FLAGS_BY_OID.length; oid = fd.readNext()) {
            result |= FLAGS_BY_OID[oid];
            fd.skipCurrent();
        }
        return result;
    }
}
