/*
 * AbstractTicksCommand.java
 *
 * Created on 02.11.2009 15:28:28
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import java.util.BitSet;

import org.joda.time.DateTimeConstants;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * @author oflege
 */
public abstract class AbstractTicksCommand extends AbstractFeedAdminCommand {
    private String key;

    private String fields;

    private int from = 0;

    private int to = DateTimeConstants.SECONDS_PER_DAY;

    private boolean reverseOrder = true;

    protected boolean onlyTrades;

    public String getQueryString() {
        return "name=" + getName() + "&key=" + getKey() + "&view=ticks"
                + (StringUtils.hasText(this.fields) ? ("&fields=" + this.fields) : "");
    }

    public String getKey() {
        return (key != null) ? key.toUpperCase() : null;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMdpsKey() {
        if (!StringUtils.hasText(key)) {
            return "";
        }
        VendorkeyVwd vkey = VendorkeyVwd.getInstance(key);
        if (vkey == VendorkeyVwd.ERROR) {
            return "";
        }
        return vkey.toMdpsKey().toString();
    }

    public String getFields() {
        return fields;
    }

    public BitSet getFieldOrders() {
        if (!StringUtils.hasText(this.fields)) {
            return null;
        }
        BitSet result = new BitSet();
        for (String s : StringUtils.commaDelimitedListToStringArray(this.fields)) {
            VwdFieldDescription.Field f = getField(s.trim());
            if (f != null) {
                result.set(VwdFieldOrder.getOrder(f.id()));
            }
        }
        return result;
    }

    private static VwdFieldDescription.Field getField(String val) {
        return Character.isDigit(val.charAt(0))
                ? VwdFieldDescription.getField(Integer.parseInt(val))
                : VwdFieldDescription.getFieldByName(val);
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public boolean isReverseOrder() {
        return reverseOrder;
    }

    public void setReverseOrder(boolean reverseOrder) {
        this.reverseOrder = reverseOrder;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public boolean isOnlyTrades() {
        return onlyTrades;
    }

    public void setOnlyTrades(boolean onlyTrades) {
        this.onlyTrades = onlyTrades;
    }

    public abstract int getDay();
}
