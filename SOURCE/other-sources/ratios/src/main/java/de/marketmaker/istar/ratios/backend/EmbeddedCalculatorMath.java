/*
 * EmbeddedCalculatorMath.java
 *
 * Created on 21.10.2005 09:08:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import java.util.Arrays;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecordDefault;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.SnapFieldVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EmbeddedCalculatorMath {

    private EmbeddedCalculatorMath() {
    }

    private static boolean isValidPrice(long price) {
        return price != Long.MIN_VALUE && price != 0;
    }

    private static long getAskEscaped(SnapWrapper sw) {
        final long ask = sw.getAsk();
        return ask != Long.MIN_VALUE
                ? ask
                : sw.getLastPrice();
    }

    private static long getNormalizedCap(StaticData sd, double currencyConvertFactor) {
        return (long) (sd.getCap() * sd.getSubscriptionratio() / (currencyConvertFactor * Constants.SCALE_FOR_DECIMAL));
    }

    private static long getPriceUnderlyingNormalized(StaticData sd, SnapWrapper swUnderlying) {
        final Number priceUnderlying = swUnderlying.getLastPrice();
        return priceUnderlying.longValue() * sd.getSubscriptionratio() / Constants.SCALE_FOR_DECIMAL;
    }

    public static long spreadRelative(CalcData calcData) {
        final SnapField bid = calcData.getSnap().getField(VwdFieldDescription.ADF_Geld.id());
        final SnapField ask = calcData.getSnap().getField(VwdFieldDescription.ADF_Brief.id());
        final long bidprice = SnapRecordUtils.getLong(bid);
        final long askprice = SnapRecordUtils.getLong(ask);

        return isValidPrice(bidprice) && isValidPrice(askprice)
                ? (askprice - bidprice) * Constants.SCALE_FOR_DECIMAL / askprice
                : Long.MIN_VALUE;
    }

    public static long spread(CalcData calcData) {
        final SnapField bid = calcData.getSnap().getField(VwdFieldDescription.ADF_Geld.id());
        final SnapField ask = calcData.getSnap().getField(VwdFieldDescription.ADF_Brief.id());
        final long bidprice = SnapRecordUtils.getLong(bid);
        final long askprice = SnapRecordUtils.getLong(ask);

        return isValidPrice(bidprice) && isValidPrice(askprice)
                ? askprice - bidprice
                : Long.MIN_VALUE;
    }

    public static long lastPrice(SnapWrapper sw) {
        if (sw.isKagFonds()) {
            final long price = SnapRecordUtils.getLong(sw.getSnapRecord().getField(VwdFieldDescription.ADF_Ruecknahme.id()));
            return price != 0
                    ? price
                    : SnapRecordUtils.getLong(sw.getSnapRecord().getField(VwdFieldDescription.ADF_Ruecknahme_Vortag.id()));
        }
        else {
            return sw.getLastPrice();
        }
    }

    public static int lastDate(SnapWrapper sw) {
        if (sw.isKagFonds()) {
            final int value = SnapRecordUtils.getInt(sw.getSnapRecord().getField(VwdFieldDescription.ADF_Handelsdatum.id()));
            if (value > 0) {
                return value;
            }
            else {
                return SnapRecordUtils.getInt(sw.getSnapRecord().getField(VwdFieldDescription.ADF_Schluss_Vortagesdatum.id()));
            }
        }
        else {
            return sw.getPriceDate();
        }
    }

    public static long discount(SnapWrapper sw, SnapWrapper swu, StaticData staticData) {
        final long priceUnderlying = getPriceUnderlyingNormalized(staticData, swu);
        return Math.max(0, priceUnderlying - getAskEscaped(sw));
    }

    public static long discountRelative(SnapWrapper sw, SnapWrapper swu, StaticData staticData) {
        final long priceUnderlying = getPriceUnderlyingNormalized(staticData, swu);
        if (priceUnderlying > 0) {
            return Math.max(0,
                (priceUnderlying - getAskEscaped(sw)) * Constants.SCALE_FOR_DECIMAL / priceUnderlying);
        } else {
            return 0;
        }
    }

    public static long discountRelativePerYear(SnapWrapper sw, SnapWrapper swu,
            StaticData staticData) {
        final long discountRelative = discountRelative(sw, swu, staticData);
        final int diffDays = DateUtil.getDaysToToday(staticData.getExpires());
        return (long) (discountRelative * 365d / diffDays);
    }

    public static long unchangedEarning(SnapWrapper sw, SnapWrapper swu, StaticData staticData,
            double currencyConvertFactor) {
        final long priceUnderlying = getPriceUnderlyingNormalized(staticData, swu);
        final long base = Math.min(getNormalizedCap(staticData, currencyConvertFactor), priceUnderlying);
        return Math.max(0, base - getAskEscaped(sw));
    }

    public static long unchangedEarningRelative(SnapWrapper sw, SnapWrapper swu,
            StaticData staticData, double currencyConvertFactor) {
        final long priceUnderlying = getPriceUnderlyingNormalized(staticData, swu);
        final long base = Math.min(getNormalizedCap(staticData, currencyConvertFactor), priceUnderlying);
        final long ask = getAskEscaped(sw);
        return ask == 0L ? Long.MIN_VALUE : Math.max(0, (base - ask) * Constants.SCALE_FOR_DECIMAL / ask);
    }

    public static long unchangedEarningRelativePerYear(SnapWrapper sw, SnapWrapper swu,
            StaticData staticData, double currencyConvertFactor) {
        final long priceUnderlying = getPriceUnderlyingNormalized(staticData, swu);
        final double base = Math.min(getNormalizedCap(staticData, currencyConvertFactor), priceUnderlying);
        final int diffDays = DateUtil.getDaysToToday(staticData.getExpires());

        final double unchangedearningpa = Math.pow(base / getAskEscaped(sw), 365d / diffDays) - 1;
        return (long) (unchangedearningpa * Constants.SCALE_FOR_DECIMAL);
    }

    public static long capToUnderlying(SnapWrapper swu, StaticData staticData,
            double currencyConvertFactor) {
        final long priceUnderlying = getPriceUnderlyingNormalized(staticData, swu);
        return getNormalizedCap(staticData, currencyConvertFactor) - priceUnderlying;
    }

    public static long capToUnderlyingRelative(SnapWrapper swu, StaticData staticData,
            double currencyConvertFactor) {
        final long priceUnderlying = getPriceUnderlyingNormalized(staticData, swu);
        return (getNormalizedCap(staticData, currencyConvertFactor) - priceUnderlying) * Constants.SCALE_FOR_DECIMAL / priceUnderlying;
    }

    public static long underlyingToCap(SnapWrapper swu, StaticData staticData,
            double currencyConvertFactor) {
        final long priceUnderlying = getPriceUnderlyingNormalized(staticData, swu);
        return priceUnderlying - getNormalizedCap(staticData, currencyConvertFactor);
    }

    public static long underlyingToCapRelative(SnapWrapper swu, StaticData staticData,
            double currencyConvertFactor) {
        final long priceUnderlying = getPriceUnderlyingNormalized(staticData, swu);
        return (priceUnderlying - getNormalizedCap(staticData, currencyConvertFactor)) * Constants.SCALE_FOR_DECIMAL / priceUnderlying;
    }

    public static long issuePrice(SnapWrapper sw) {
        if (sw.isKagFonds()) {
            final long issueprice = SnapRecordUtils.getLong(sw.getSnapRecord().getField(VwdFieldDescription.ADF_Ausgabe.id()));
            return issueprice > 0
                    ? issueprice
                    : SnapRecordUtils.getLong(sw.getSnapRecord().getField(VwdFieldDescription.ADF_Ausgabe_Vortag.id()));
        }
        else {
            return sw.getLastPrice();
        }
    }

    public static long capLevel(SnapWrapper swu, StaticData staticData,
            double currencyConvertFactor) {
        final double priceUnderlying = getPriceUnderlyingNormalized(staticData, swu);
        final double normalizedCap = getNormalizedCap(staticData, currencyConvertFactor);
        return (long) (normalizedCap / priceUnderlying * Constants.SCALE_FOR_DECIMAL);
    }

    static long percentValueRatio(long value) {
        if (value > Long.MIN_VALUE) {
            return value / 100L;
        }
        return Long.MIN_VALUE;
    }

    static long priceValueRatio(long price, long value) {
        if (price == Long.MIN_VALUE || value == Long.MIN_VALUE || value == 0) {
            return Long.MIN_VALUE;
        }

        final long result = (long) ((double) price * Constants.SCALE_FOR_DECIMAL / value);
        return (result >= 0) ? result : Long.MIN_VALUE;
    }

    static long getMarketCapitalization(SnapWrapper sw, StaticData staticData) {
        return calcMarketCapitalization(staticData, sw.getLastPrice());
    }

    static long getMarketCapitalizationPreviousDay(SnapWrapper sw, StaticData staticData) {
        return calcMarketCapitalization(staticData, sw.getPreviousClose());
    }

    // result is A NUMBER (see RatioFieldDescription Type for Field marketCapitalization)
    private static long calcMarketCapitalization(StaticData staticData, long price) {
        if (price == Long.MIN_VALUE) {
            return Long.MIN_VALUE;
        }

        if (staticData == null) {
            return Long.MIN_VALUE;
        }

        // the plain count value, scaled with SCALE_FOR_DECIMAL
        final long numberOfEquities = staticData.getWmNumberOfIssuedEquities();

        if (numberOfEquities < 0) {
            return Long.MIN_VALUE;
        }
        return  (long) (
                ((double) price / Constants.SCALE_FOR_DECIMAL)
                        * ((double) numberOfEquities / Constants.SCALE_FOR_DECIMAL));
    }

    public static void main(String[] args) {
        long num = PriceCoder.encode("918478694");
        long price = PriceCoder.encode("78.18");
        long mc = (long) (
                ((double) price / Constants.SCALE_FOR_DECIMAL) * ((double) num / Constants.SCALE_FOR_DECIMAL)
                        * ((double)Constants.SCALE_FOR_DECIMAL / 1_000_000) );
        System.out.println(PriceCoder.decode(mc));
        System.out.println((long) (((Number) price).doubleValue() / Constants.SCALE_FOR_DECIMAL * num / Constants.SCALE_FOR_DECIMAL));
        if (num != 0) {
            return;
        }

        final SnapWrapper wrapper = SnapWrapper.create(new SnapRecordDefault(Arrays.<SnapField>asList(
                new SnapFieldVwd(VwdFieldDescription.ADF_Brief.id(), 715000)
        )), null);
        final SnapWrapper uwrapper = SnapWrapper.create(new SnapRecordDefault(Arrays.<SnapField>asList(
                new SnapFieldVwd(VwdFieldDescription.ADF_Bezahlt.id(), 938000)
        )), null);
        final StaticDataCER staticdata = new StaticDataCER(18 * Constants.SCALE_FOR_DECIMAL, Constants.SCALE_FOR_DECIMAL, "CERT_DISCOUNT", 200101010, "USD");
        System.out.println("discount: " + getPrice(EmbeddedCalculatorMath.discount(wrapper, uwrapper, staticdata)));
    }

    private static double getPrice(long l) {
        return l * 1d / Constants.SCALE_FOR_DECIMAL;
    }
}
