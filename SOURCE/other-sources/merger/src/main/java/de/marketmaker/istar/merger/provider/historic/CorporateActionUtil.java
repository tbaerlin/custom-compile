/*
 * CorporateActionMath.java
 *
 * Created on 12.08.13 10:34
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author zzhao
 */
public final class CorporateActionUtil {

    private CorporateActionUtil() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    public static double getDividendFactor(BigDecimal dividend, InstrumentTypeEnum insType,
            double exPrice) {
        if (Double.isNaN(exPrice)) {
            return 1.0D; // if non-number ex-price, return factor one
        }
        switch (insType) {
            case FND:
                return exPrice / (exPrice + dividend.doubleValue());
            default: // Operation Blanche
                return (exPrice - dividend.doubleValue()) / exPrice;
        }
    }

    public static double findExPrice(InstrumentTypeEnum insType, LocalDate exDay,
            HistoricTimeseries ht) {
        if (null == ht || ht.size() == 0) {
            return Double.NaN;
        }
        final int offset = Math.max(0, ht.getOffset(exDay));
        final double[] values = ht.getValues();
        switch (insType) {
            case FND: // for fond, prefer prices at or after ex-day
                for (int i = offset; i < values.length; i++) {
                    if (!Double.isNaN(values[i])) {
                        return values[i];
                    }
                }
                for (int i = offset; i-- > 0; ) {
                    if (!Double.isNaN(values[i])) {
                        return values[i];
                    }
                }
                return Double.NaN;
            default: // especially for stock, prefer prices before ex-day (Operation Blanche)
                for (int i = offset; i-- > 0; ) {
                    if (!Double.isNaN(values[i]) && values[i] != 0) {
                        return values[i];
                    }
                }
                for (int i = offset; i < values.length; i++) {
                    if (!Double.isNaN(values[i]) && values[i] != 0) {
                        return values[i];
                    }
                }
                return Double.NaN;
        }
    }
}
