/*
 * DynamicFieldDataFilter.java
 *
 * Created on 11.10.12 17:28
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.util.BitSet;

import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * A filter for {@link OrderedUpdate}s that removes all fields for which a bit is set in
 * {@link VwdFieldOrder#getNonDynamicOrders()}.
 * @author oflege
 */
class DynamicFieldDataFilter extends FieldDataBuilder {

    private static final int MAX_DYNAMIC_ORDER = VwdFieldOrder.FIRST_NON_DYNAMIC_ORDER - 1;

    private final BitSet nonDynamicFieldOrders = VwdFieldOrder.getNonDynamicOrders();

    private final OrderedUpdate update = new OrderedUpdate();

    protected DynamicFieldDataFilter() {
        super(4096);
    }

    /**
     * @param other to be filtered
     * @return other, if it contains only dynamic fields; a filtered version of other with only
     * dynamic fields, if other contains at least one dynamic field; null if other does not contain
     * any dynamic fields.<br>
     * The filtered version will always be returned in the
     * same object, so clients should discard the objects after processing and not keep a
     * reference to it for later processing (as the next invocation of this method will change
     * the state of that object again).
     */
    OrderedUpdate applyTo(OrderedUpdate other) {
        final BufferFieldData fd = other.getFieldData();
        this.lastOrder = 0;
        for (int orderId = fd.readNext(); orderId != 0; orderId = fd.readNext()) {
            if (orderId > MAX_DYNAMIC_ORDER // faster than BitSet lookup
                    && this.nonDynamicFieldOrders.get(orderId)) {
                return createFiltered(other, fd);
            }
            fd.skipCurrent();
            this.lastOrder = orderId;
        }
        return other;
    }

    private OrderedUpdate createFiltered(OrderedUpdate other, BufferFieldData fd) {
        this.bb.clear().position(2);
        other.putHeader(this.bb);
        final int posBeforeFields = this.bb.position();
        fd.putFieldsBeforeCurrent(this.bb);
        fd.skipCurrent();
        for (int orderId = fd.readNext(); orderId != 0; orderId = fd.readNext()) {
            if (this.nonDynamicFieldOrders.get(orderId)) {
                fd.skipCurrent();
                continue;
            }
            addFieldToBuffer(fd);
        }
        if (bb.position() == posBeforeFields) {
            return null;
        }
        this.bb.flip();
        this.bb.putShort((short) bb.remaining());
        this.update.reset(this.bb);
        return this.update;
    }
}
