/*
 * StaticBuilder.java
 *
 * Created on 19.04.13 14:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.staticfeed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldData;
import de.marketmaker.istar.feed.ordered.FieldDataUtil;

/**
 * @author oflege
 */
abstract class StaticBuilder {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected DiffFormatter formatter;

    public void setFormatter(DiffFormatter formatter) {
        this.formatter = formatter;
    }

    protected void appendAll(BufferFieldData fd) {
        for (int oid = fd.readNext(); oid != 0; oid = fd.readNext()) {
            appendField(fd);
        }
    }

    /**
     * append only fields with an orderId that is contained in orders
     * @param fd contains fields
     * @param orders defines fields to be written
     */
    protected void appendSome(BufferFieldData fd, int[] orders) {
        if (orders == null || orders.length == 0) {
            return;
        }
        int n = 0;
        for (int oid = fd.readNext(); oid != 0; oid = fd.readNext()) {
            while (oid > orders[n]) {
                if (++n == orders.length) {
                    return;
                }
            }
            if (oid == orders[n]) {
                appendField(fd);
                if (++n == orders.length) {
                    return;
                }
            }
            else {
                fd.skipCurrent();
            }
        }
    }

    protected void appendField(BufferFieldData fd) {
        switch (fd.getType()) {
            case FieldData.TYPE_INT:
                this.formatter.intChanged(fd.getVwdId(), fd.getInt());
                break;
            case FieldData.TYPE_TIME:
                this.formatter.intChanged(fd.getVwdId(), MdpsFeedUtils.decodeTime(fd.getInt()));
                break;
            case FieldData.TYPE_PRICE:
                this.formatter.priceChanged(fd.getVwdId(), FieldDataUtil.getPrice(fd));
                break;
            case FieldData.TYPE_STRING:
                this.formatter.stringChanged(fd.getVwdId(), fd.getBytes());
                break;
            default:
                throw new IllegalStateException();
        }
    }
}
