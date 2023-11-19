/*
 * PriceRecordComparator.java
 *
 * Created on 13.07.2006 14:21:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;
import java.util.Comparator;

import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface PriceRecordComparator extends Comparator<PriceRecord> {
    interface PriceComparator extends PriceRecordComparator {
        default int compare(PriceRecord o1, PriceRecord o2) {
            return getPrice(o1).compareTo(getPrice(o2));
        }

        default BigDecimal getPrice(PriceRecord pr) {
            final BigDecimal result = doGetPrice(pr);
            return (result != null) ? result : BigDecimal.ZERO;
        }

        BigDecimal doGetPrice(PriceRecord pr);
    }

    interface LongComparator extends PriceRecordComparator {
        default int compare(PriceRecord o1, PriceRecord o2) {
            return getLong(o1).compareTo(getLong(o2));
        }

        default Long getLong(PriceRecord pr) {
            final Long result = doGetLong(pr);
            return (result != null) ? result : 0L;
        }

        Long doGetLong(PriceRecord pr);
    }

    interface DateTimeComparator extends PriceRecordComparator {
        DateTime ZERO = new YearMonthDay(2000, 1, 1).toDateTimeAtMidnight();

        default int compare(PriceRecord o1, PriceRecord o2) {
            return getDateTime(o1).compareTo(getDateTime(o2));
        }

        default DateTime getDateTime(PriceRecord pr) {
            final DateTime result = doGetDateTime(pr);
            return (result != null) ? result : ZERO;
        }

        DateTime doGetDateTime(PriceRecord pr);
    }


    Comparator<PriceRecord> BY_PRICE = (PriceComparator) pr -> pr.getPrice().getValue();

    Comparator<PriceRecord> BY_CHANGE_PERCENT = (PriceComparator) PriceRecord::getChangePercent;
    Comparator<PriceRecord> BY_CHANGE_NET = (PriceComparator) PriceRecord::getChangeNet;

    Comparator<PriceRecord> BY_HIGH_DAY = (PriceComparator) pr -> pr.getHighDay().getValue();

    Comparator<PriceRecord> BY_LOW_DAY = (PriceComparator) pr -> pr.getLowDay().getValue();

    Comparator<PriceRecord> BY_HIGH_YEAR = (PriceComparator) pr -> pr.getHighYear().getValue();

    Comparator<PriceRecord> BY_LOW_YEAR = (PriceComparator) pr -> pr.getLowYear().getValue();

    Comparator<PriceRecord> BY_VOLUME_DAY = (LongComparator) PriceRecord::getVolumeDay;

    Comparator<PriceRecord> BY_TURNOVER_DAY = (PriceComparator) PriceRecord::getTurnoverDay;

    Comparator<PriceRecord> BY_NUMBER_OF_TRADES = (LongComparator) PriceRecord::getNumberOfTrades;

    Comparator<PriceRecord> BY_LATEST_TRADE = (DateTimeComparator) pr -> pr.getPrice().getDate();

    Comparator<PriceRecord> BY_ASK_VOLUME = (LongComparator) pr -> pr.getAsk().getVolume();

    Comparator<PriceRecord> BY_BID_VOLUME = (LongComparator) pr -> pr.getBid().getVolume();
}
