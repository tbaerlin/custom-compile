/*
 * RawTick.java
 *
 * Created on 15.11.2004 15:51:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.TickEvent;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RawTick implements Serializable, Cloneable, TickEvent {
    static final long serialVersionUID = -5136072341654802584L;

    private final boolean mdpsPriceFormat;

    private int type;

    private byte header;

    private int time;

    private int millis = 0;

    private long price = Long.MIN_VALUE;

    private long volume = Long.MIN_VALUE;

    private long bidPrice = Long.MIN_VALUE;

    private long bidVolume = Long.MIN_VALUE;

    private long askPrice = Long.MIN_VALUE;

    private long askVolume = Long.MIN_VALUE;

    private String supplement;

    private String notierungsart;

    private byte tradeHeader;

    private boolean pricePresent = true;

    private boolean bidPresent = true;

    private boolean askPresent = true;

    private boolean withClose;

    private boolean withKassa;

    private long yield = Long.MIN_VALUE;

    private List<SnapField> additionalFields;

    public RawTick() {
        this(false);
    }

    public RawTick(boolean mdpsPriceFormat) {
        this.mdpsPriceFormat = mdpsPriceFormat;
    }

    public boolean isMdpsPriceFormat() {
        return this.mdpsPriceFormat;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getHeader() {
        return header;
    }

    public void setHeader(byte header) {
        this.header = header;
    }

    public boolean isPricePresent() {
        return pricePresent;
    }

    public void setPricePresent(boolean pricePresent) {
        this.pricePresent = pricePresent;
    }

    public boolean isBidPresent() {
        return bidPresent;
    }

    public void setBidPresent(boolean bidPresent) {
        this.bidPresent = bidPresent;
    }

    public boolean isAskPresent() {
        return askPresent;
    }

    public void setAskPresent(boolean askPresent) {
        this.askPresent = askPresent;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isTrade() {
        return (this.type & TickCoder.TYPE_TRADE) != 0;
    }

    public boolean isBid() {
        return (this.type & TickCoder.TYPE_BID) != 0;
    }

    public boolean isAsk() {
        return (this.type & TickCoder.TYPE_ASK) != 0;
    }

    public boolean isSuspendStart() {
        return (this.type & TickCoder.TICK_SUSPEND_START) != 0;
    }

    public boolean isSuspendEnd() {
        return (this.type & TickCoder.TICK_SUSPEND_END) != 0;
    }

    public int getTime() {
        return time;
    }

    public void setMdpsTime(int mdpsTime) {
        this.millis = MdpsFeedUtils.decodeTimeMillis(mdpsTime);
        this.time = MdpsFeedUtils.decodeTime(mdpsTime);
    }

    public int getMillis() {
        return millis;
    }

    public void setTime(int time) {
        this.time = time;
        this.millis = 0;
    }

    public String getTimeStr() {
        return TimeFormatter.formatSecondsInDay(this.time);
    }

    public long getPrice() {
        return this.price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public boolean isVolumePresent() {
        return this.volume != Long.MIN_VALUE;
    }

    public boolean isAskVolumePresent() {
        return this.askVolume != Long.MIN_VALUE;
    }

    public boolean isBidVolumePresent() {
        return this.bidVolume != Long.MIN_VALUE;
    }

    public String getSupplement() {
        return supplement;
    }

    public void setSupplement(String supplement) {
        this.supplement = supplement;
    }

    public String getTradeIdentifier() {
        return this.notierungsart;
    }

    @Override
    public List<SnapField> getAdditionalFields() {
        return this.additionalFields;
    }

    public void setAdditionalFields(List<SnapField> additionalFields) {
        this.additionalFields = additionalFields;
    }

    public void setNotierungsart(String notierungsart) {
        this.notierungsart = notierungsart;
    }

    public long getBidPrice() {
        return this.bidPrice;
    }

    public void setBidPrice(long bidPrice) {
        this.bidPrice = bidPrice;
    }

    public long getBidVolume() {
        return bidVolume;
    }

    public void setBidVolume(long bidVolume) {
        this.bidVolume = bidVolume;
    }

    public long getAskPrice() {
        return this.askPrice;
    }

    public void setAskPrice(long askPrice) {
        this.askPrice = askPrice;
    }

    public long getAskVolume() {
        return askVolume;
    }

    public void setAskVolume(long askVolume) {
        this.askVolume = askVolume;
    }

    public String toString() {
        return "RawTick[header=" + getHeaderStr()
                + ", time=" + getTimeStr()
                + getTradeStr() + getBidStr() + getAskStr()
                + getWithStr()
                + "]";
    }

    private String formatPrice(long p) {
        BigDecimal bd = toBigDecimal(p);
        return (bd != null) ? bd.toPlainString() : "n/a";
    }

    private String getAskStr() {
        if (!isAsk()) {
            return "";
        }
        return ", ask=" + formatPrice(this.askPrice)
                + (isAskPresent() ? "" : "*")
                + " (" + (askVolume == Long.MIN_VALUE ? "n/a" : askVolume + "") + ")";
    }

    private String getBidStr() {
        if (!isBid()) {
            return "";
        }
        return ", bid=" + formatPrice(this.bidPrice)
                + (isBidPresent() ? "" : "*")
                + " (" + (bidVolume == Long.MIN_VALUE ? "n/a" : bidVolume + "") + ")";
    }

    private String getTradeStr() {
        if (!isTrade()) {
            return "";
        }
        return ", trade=" + formatPrice(this.price)
                + (isPricePresent() ? "" : "*")
                + " '" + (supplement == null ? "" : supplement) + "'"
                + (notierungsart == null ? "" : ("[" + notierungsart + "]"))
                + " (" + (volume == Long.MIN_VALUE ? "n/a" : volume + "") + ")";
    }

    private String getWithStr() {
        if (!this.withClose && !this.withKassa && this.yield == Long.MIN_VALUE) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();

        sb.append(this.withClose ? "Close" : "");
        sb.append(this.withKassa ? "Kassa" : "");
        sb.append(this.yield != Long.MIN_VALUE ? "Yield=" + formatPrice(this.yield) : "");

        return sb.toString();

    }

    private String getHeaderStr() {
        final StringBuilder sb = new StringBuilder();

        sb.append(isTrade() ? 'T' : '-');
        sb.append(isBid() ? 'B' : '-');
        sb.append(isAsk() ? 'A' : '-');

        return sb.toString();
    }

    public void setTradeHeader(byte tradeHeader) {
        this.tradeHeader = tradeHeader;
    }

    public byte getTradeHeader() {
        return tradeHeader;
    }

    public void setWithClose(boolean withClose) {
        this.withClose = withClose;
    }

    public void setWithKassa(boolean withKassa) {
        this.withKassa = withKassa;
    }

    public boolean isWithClose() {
        return withClose;
    }

    public boolean isWithKassa() {
        return withKassa;
    }

    public long getYield() {
        return yield;
    }

    public boolean isWithYield() {
        return this.yield != Long.MIN_VALUE;
    }

    public void setYield(long yield) {
        this.yield = yield;
    }

    private BigDecimal toBigDecimal(long value) {
        if (value == Long.MIN_VALUE) {
            return null;
        }
        return this.mdpsPriceFormat ? MdpsFeedUtils.decodePrice(value) : PriceCoder.decode(value);
    }

    boolean isRequiredType(final TickType type) {
        switch (type) {
            case TRADE:
                return isTrade();
            case BID:
                return isBid();
            case ASK:
                return isAsk();
            case SUSPEND_START:
                return isSuspendStart();
            case SUSPEND_END:
                return isSuspendEnd();
            default:
                return false;
        }
    }

    public void reset() {
        setAskPresent(false);
        setBidPresent(false);
        setPricePresent(false);
        setAskVolume(Long.MIN_VALUE);
        setBidVolume(Long.MIN_VALUE);
        setVolume(Long.MIN_VALUE);
        setYield(Long.MIN_VALUE);
        setWithKassa(false);
        setSupplement(null);
        setNotierungsart(null);
        setAdditionalFields(null);
    }
}
