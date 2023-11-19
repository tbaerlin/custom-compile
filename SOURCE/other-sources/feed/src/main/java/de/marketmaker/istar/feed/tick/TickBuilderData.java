/*
 * TickBuilderData.java
 *
 * Created on 07.01.2005 14:22:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.util.Arrays;

import de.marketmaker.istar.feed.FieldBuilder;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TickBuilderData implements FieldBuilder {
    private static final int INT_NAN = Integer.MIN_VALUE;

    private static final long LONG_NAN = Long.MIN_VALUE;

    private static final int FLAG_SUPPLEMENT = TickCoder.WITH_SUPPLEMENT << 8;

    private static final int FLAG_NOTIERUNGSART = TickCoder.WITH_NOTIERUNGSART << 8;

    private static final int FLAG_MASK = FLAG_SUPPLEMENT | FLAG_NOTIERUNGSART;

    private long price;

    private int volume;

    private byte[] supplement = new byte[4];

    // last byte: notierungsart, last but one: flags
    private int supplementInfo;

    private long bidPrice;

    private int bidVolume;

    private long askPrice;

    private int askVolume;

    private int adfZeitQuotierung;

    private int adfDatum;

    private ParsedRecord parsedRecord;

    private TickParameters tickparams = new TickParameters();

    @Override
    public int getFieldFlags() {
        return VwdFieldDescription.FLAG_DYNAMIC;
    }

    public void reset(ParsedRecord pr) {
        this.parsedRecord = pr;

        this.price = LONG_NAN;
        this.volume = INT_NAN;
        this.supplementInfo = 0;

        this.bidPrice = LONG_NAN;
        this.bidVolume = INT_NAN;

        this.askPrice = LONG_NAN;
        this.askVolume = INT_NAN;

        this.adfDatum = INT_NAN;
        this.adfZeitQuotierung = INT_NAN;
    }


    public String toString() {
        final StringBuilder sb = new StringBuilder(100);
        sb.append("TickBuilderData[");
        sb.append("price=");
        append(sb, this.price, this.volume);
        sb.append(", bid=");
        append(sb, this.bidPrice, this.bidVolume);
        sb.append(", ask=");
        append(sb, this.askPrice, this.askVolume);
        sb.append("]");
        return sb.toString();
    }

    private void append(StringBuilder sb, long p, int vol) {
        sb.append(p != Long.MIN_VALUE ? Long.toString(p) : "-");
        sb.append(" (").append(vol != Integer.MIN_VALUE ? Integer.toString(vol) : "-").append(")");
    }

    public int getTime() {
        if (this.adfZeitQuotierung != INT_NAN) {
            return this.adfZeitQuotierung;
        }

        if (this.parsedRecord.getAdfBoersenzeit() != INT_NAN) {
            return this.parsedRecord.getAdfBoersenzeit();
        }

        if (this.parsedRecord.getAdfZeit() != INT_NAN) {
            return this.parsedRecord.getAdfZeit();
        }

        return this.parsedRecord.getAdfTimeOfArrival();
    }

    public int getDate() {
        if (this.adfDatum != INT_NAN) {
            return this.adfDatum;
        }
        return this.parsedRecord.getAdfDateOfArrival();
    }

    public int getDateOfArrival() {
        return this.parsedRecord.getAdfDateOfArrival();
    }

    public int getTimeOfArrival() {
        return this.parsedRecord.getAdfTimeOfArrival();
    }

    public boolean isDateConsistentWithArrival() {
        return this.adfDatum == INT_NAN || this.adfDatum == getDateOfArrival();
    }

    public long getAskPrice() {
        return askPrice;
    }

    public int getAskVolume() {
        return askVolume;
    }

    public long getBidPrice() {
        return bidPrice;
    }

    public int getBidVolume() {
        return bidVolume;
    }

    public long getPrice() {
        return price;
    }

    public boolean isWithoutSupplement() {
        return (this.supplementInfo & FLAG_MASK) == 0;
    }

    public int getVolume() {
        return volume;
    }

    public TickParameters getTickparams() {
        return this.tickparams;
    }

    public void set(VwdFieldDescription.Field field, int value) {
        if (field == VwdFieldDescription.ADF_Bezahlt_Umsatz) {
            this.volume = value;
        }
        else if (field == VwdFieldDescription.ADF_Brief_Umsatz) {
            this.askVolume = value;
        }
        else if (field == VwdFieldDescription.ADF_Geld_Umsatz) {
            this.bidVolume = value;
        }
        else if (field == VwdFieldDescription.ADF_Datum) {
            this.adfDatum = value;
        }
        else if (field == VwdFieldDescription.ADF_Zeit_Quotierung) {
            this.adfZeitQuotierung = value;
        }
    }

    public void set(VwdFieldDescription.Field field, long value) {
        if (field == VwdFieldDescription.ADF_Bezahlt) {
            this.price = value;
        }
        else if (field == VwdFieldDescription.ADF_Brief) {
            this.askPrice = value;
        }
        else if (field == VwdFieldDescription.ADF_Geld) {
            this.bidPrice = value;
        }
        else if (field == VwdFieldDescription.ADF_Rendite) {
            this.tickparams.setYield(value);
        }
        else if (field == VwdFieldDescription.ADF_Schluss) {
            this.tickparams.setWithClose(true);
        }
        else if (field == VwdFieldDescription.ADF_Kassa) {
            this.tickparams.setWithKassa(true);
        }
    }

    public void set(VwdFieldDescription.Field field, byte[] value, int start, int length) {
        if (field == VwdFieldDescription.ADF_Bezahlt_Kurszusatz) {
            this.supplementInfo |= FLAG_SUPPLEMENT;
            System.arraycopy(value, start, this.supplement, 0, length);
            if (length < 4) {
                this.supplement[length] = 0;
            }
        }
        else if (field == VwdFieldDescription.ADF_Notierungsart) {
            if (value[start] != 0) { // ignore empty string
                this.supplementInfo |= (FLAG_NOTIERUNGSART + ((value[start] & 0xff)));
            }
        }
    }

    byte[] getSupplementIfChanged(byte[] lastSupplement) {
        if (lastSupplement == null) {
            return copySupplement();
        }

        if (isOnlySupplement()) {
            if (checkSupplement(lastSupplement, 0)) {
                return null;
            }
        }
        else if (isOnlyNotierungsart()) {
            if (lastSupplement.length == 2 && prefixMatches(lastSupplement)) {
                return null;
            }
        }
        else if (lastSupplement.length == this.supplement.length + 2) {
            if (prefixMatches(lastSupplement) && checkSupplement(lastSupplement, 2)) {
                return null;
            }
        }
        return copySupplement();
    }

    private boolean prefixMatches(byte[] lastSupplement) {
        return lastSupplement[0] == (this.supplementInfo >> 8)
                && lastSupplement[1] == (this.supplementInfo & 0xff);
    }

    private boolean checkSupplement(byte[] lastSupplement, int offset) {
        for (int i = 0; i < this.supplement.length; i++) {
            final byte last = lastSupplement[i + offset];
            if (supplement[i] == 0 && last == 0) {
                return true;
            }
            if (supplement[i] != last) {
                return false;
            }
        }
        return true;
    }

    private byte[] copySupplement() {
        if (isOnlySupplement()) {
            return Arrays.copyOf(this.supplement, this.supplement.length);
        }
        if (isOnlyNotierungsart()) {
            return new byte[] { TickCoder.WITH_NOTIERUNGSART, (byte) this.supplementInfo };
        }
        return createSupplementWithNotierungsart();
    }

    private byte[] createSupplementWithNotierungsart() {
        // supplement and notierungsart: 1st byte: flags, 2nd byte: noterierungsart, rest: supplement
        final byte[] result = new byte[this.supplement.length + 2];
        result[0] = (byte) (TickCoder.WITH_NOTIERUNGSART | TickCoder.WITH_SUPPLEMENT);
        result[1] = (byte) this.supplementInfo;
        System.arraycopy(this.supplement, 0, result, 2, this.supplement.length);
        return result;
    }

    private boolean isOnlyNotierungsart() {
        return (this.supplementInfo & FLAG_SUPPLEMENT) == 0;
    }

    private boolean isOnlySupplement() {
        return (this.supplementInfo & FLAG_NOTIERUNGSART) == 0;
    }

    public TickBuilderData withSupplement(byte[] supplement) {
        System.arraycopy(supplement, 0, this.supplement, 0, supplement.length);
        this.supplementInfo |= FLAG_SUPPLEMENT;
        return this;
    }

    public TickBuilderData withNotierungsart(byte notierungsart) {
        this.supplementInfo |= (notierungsart & 0xff);
        this.supplementInfo |= FLAG_NOTIERUNGSART;
        return this;
    }
}
