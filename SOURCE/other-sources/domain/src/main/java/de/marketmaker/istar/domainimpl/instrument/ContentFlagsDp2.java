/*
 * ContentFlagsImpl.java
 *
 * Created on 10.10.11 11:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import java.util.BitSet;
import java.util.Iterator;

import org.apache.commons.codec.binary.Base64;

import de.marketmaker.istar.domain.instrument.ContentFlags;

/**
 * @author zzhao
 */
public class ContentFlagsDp2 implements ContentFlags {

    private static final int SHIFTS = 6;

    private static final int BITS = 1 << SHIFTS;

    public static final ContentFlags NO_FLAGS_SET = new ContentFlagsDp2(new long[0]);

    private final BitSet bitSet;

    public ContentFlagsDp2(BitSet bs) {
        this.bitSet = bs.get(0, Flag.values().length);
    }

    /**
     * Create from base64 encoded flags bitset as is used in the mdps feed
     * @param s encoded flags
     */
    public ContentFlagsDp2(String s) {
        this.bitSet = new BitSet();
        final byte[] bytes = Base64.decodeBase64(s);
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[i / 8] & (1 << (i % 8))) > 0) {
                this.bitSet.set(i);
            }
        }
    }

    public ContentFlagsDp2(long[] flags) {
        this.bitSet = new BitSet(flags.length * BITS);
        for (int i = 0, max = Math.min(Flag.values().length, flags.length * BITS); i < max; i++) {
            if ((flags[i >> SHIFTS] & (1L << (i & 63))) != 0) {
                this.bitSet.set(i);
            }
        }
    }

    public BitSet asBitSet() {
        return (BitSet) this.bitSet.clone();
    }

    private boolean isFlagSet(Flag flag) {
        return this.bitSet.get(flag.ordinal());
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ContentFlagsDp2) && this.bitSet.equals(((ContentFlagsDp2) o).bitSet);
    }

    @Override
    public int hashCode() {
        return bitSet.hashCode();
    }

    /**
     * @return an iterator over all flag enums present in this object
     */
    public Iterable<Flag> flags() {
        return new Iterable<Flag>() {
            @Override
            public Iterator<Flag> iterator() {
                return new Iterator<Flag>() {
                    private final Flag[] flags = ContentFlags.Flag.values();

                    private int i = bitSet.nextSetBit(1); // ignore Null

                    @Override
                    public boolean hasNext() {
                        return this.i >= 0 && this.i < this.flags.length;
                    }

                    @Override
                    public Flag next() {
                        if (!hasNext()) {
                            throw new IllegalStateException();
                        }
                        final Flag result = this.flags[this.i];
                        this.i = bitSet.nextSetBit(this.i + 1);
                        return result;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Flag flag : this.flags()) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(flag.name());
        }

        return sb.toString();
    }

    @Override
    public boolean hasFlag(Flag f) {
        return isFlagSet(f);
    }

    @Override
    public boolean isConvensys() {
        return isFlagSet(Flag.Convensys);
    }

    @Override
    public boolean isEstimatesReuters() { return isFlagSet(Flag.EstimatesReuters); }

    @Override
    public boolean isScreener() {
        return isFlagSet(Flag.Screener);
    }

    @Override
    public boolean isEdg() {
        return isFlagSet(Flag.Edg);
    }

    @Override
    public boolean isStockselectionFndReport() {
        return isFlagSet(Flag.StockselectionFndReport);
    }

    @Override
    public boolean isStockselectionCerReport() {
        return isFlagSet(Flag.StockselectionCerReport);
    }

    @Override
    public boolean isSsatFndReport() {
        return isFlagSet(Flag.SsatFndReport);
    }

    @Override
    public boolean isFactset() {
        return isFlagSet(Flag.Factset);
    }

    @Override
    public boolean isVwdbenlFundamentalData() {
        return isFlagSet(Flag.VwdbenlFundamentalData);
    }

    @Override
    public boolean isFunddataMorningstar() {
        return isFlagSet(Flag.FunddataMorningstar);
    }

    @Override
    public boolean isFunddataVwdBeNl() {
        return isFlagSet(Flag.FunddataVwdBeNl);
    }

    @Override
    public boolean isCerUnderlying() {
        return isFlagSet(Flag.CerUnderlying);
    }

    @Override
    public boolean isWntUnderlying() {
        return isFlagSet(Flag.WntUnderlying);
    }

    @Override
    public boolean isCerUnderlyingDzbank() {
        return isFlagSet(Flag.CerUnderlyingDzbank);
    }

    @Override
    public boolean isCerUnderlyingWgzbank() {
        return isFlagSet(Flag.CerUnderlyingWgzbank);
    }

    @Override
    public boolean isWntUnderlyingDzbank() {
        return isFlagSet(Flag.WntUnderlyingDzbank);
    }

    @Override
    public boolean isCerDzbank() {
        return isFlagSet(Flag.CerDzbank);
    }

    @Override
    public boolean isCerWgzbank() {
        return isFlagSet(Flag.CerWgzbank);
    }

    @Override
    public boolean isWntDzbank() {
        return isFlagSet(Flag.WntDzbank);
    }

    @Override
    public boolean isOptUnderlying() {
        return isFlagSet(Flag.OptUnderlying);
    }

    @Override
    public boolean isFutUnderlying() {
        return isFlagSet(Flag.FutUnderlying);
    }

    @Override
    public boolean isVRPIF() {
        return isFlagSet(Flag.VRPIF);
    }

    @Override
    public boolean isHistoricaTimeseriesData() {
        return isFlagSet(Flag.HistoricaTimeseriesData);
    }

    @Override
    public boolean isPibDz() {
        return isFlagSet(Flag.PibDz);
    }

    @Override
    public boolean isLeverageProductUnderlyingDzbank() { return isFlagSet(Flag.LeverageProductUnderlyingDzbank); }

    @Override
    public boolean isOfferteDzbank() {
        return isFlagSet(Flag.OfferteDzbank);
    }

    @Override
    public boolean isIlSole24OreAmf() {
        return isFlagSet(Flag.IlSole24OreAmf);
    }

    @Override
    public boolean isTopproduktDzbank() {
        return isFlagSet(Flag.TopproduktDzbank);
    }

    @Override
    public boolean isKapitalmarktfavoritDzbank() { return isFlagSet(Flag.KapitalmarktfavoritDzbank); }

    @Override
    public boolean isOfferteUnderlyingDzbank() {
        return isFlagSet(Flag.OfferteUnderlyingDzbank);
    }

    @Override
    public boolean isDzMarginDialogRequired() {
        return isFlagSet(Flag.DzMarginDialogRequired);
    }

    @Override
    public boolean isIndexWithConstituents() {
        return isFlagSet(Flag.IndexWithConstituents);
    }

    @Override
    public boolean isLMEComposite() {
        return isFlagSet(Flag.LMEComposite);
    }

    @Override
    public boolean isResearchDzHM1() {
        return isFlagSet(Flag.ResearchDzHM1);
    }

    @Override
    public boolean isResearchDzHM2() {
        return isFlagSet(Flag.ResearchDzHM2);
    }

    @Override
    public boolean isResearchDzHM3() {
        return isFlagSet(Flag.ResearchDzHM3);
    }

    @Override
    public boolean isResearchDzFP4() {
        return isFlagSet(Flag.ResearchDzFP4);
    }

    @Override
    public boolean isKursfortschreibung() {
        return isFlagSet(Flag.Kursfortschreibung);
    }

    @Override
    public boolean isLMEWarehouse() {
        return isFlagSet(Flag.LMEWarehouse);
    }

    @Override
     public boolean isResearchLBBW() {
        return isFlagSet(Flag.ResearchLBBW);
    }

    @Override
    public boolean hasLEIEquity() {
        return isFlagSet(Flag.HasLEIEquity);
    }

    @Override
    public boolean hasLEIBonds() {
        return isFlagSet(Flag.HasLEIBonds);
    }

    @Override
    public boolean hasLEIIssuerRatings() {
        return isFlagSet(Flag.HasLEIIssuerRatings);
    }

    public static void main(String[] args) {
        ContentFlagsDp2 contentFlagsDp2 = new ContentFlagsDp2("8fAAAHAB");
        System.out.println("contentFlagsDp2 = " + contentFlagsDp2);

        contentFlagsDp2 = new ContentFlagsDp2("8fAAAHA=");
        System.out.println("contentFlagsDp2 = " + contentFlagsDp2);

        contentFlagsDp2 = new ContentFlagsDp2("AQAAAAAM");
        System.out.println("contentFlagsDp2 AQAAAAAM = " + contentFlagsDp2);
    }
}
