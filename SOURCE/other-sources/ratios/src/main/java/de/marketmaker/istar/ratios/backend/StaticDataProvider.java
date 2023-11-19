/*
 * StaticDataProvider.java
 *
 * Created on 16.09.2005 14:09:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import java.util.Iterator;
import java.util.Map;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StaticDataProvider implements StaticDataCallback {

    private static final int MAX_AGE = 5;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Long2ObjectMap<StaticData> map = new Long2ObjectOpenHashMap<>();

    public StaticData get(long iid) {
        synchronized (this.map) {
            return this.map.get(iid);
        }
    }

    public void process(InstrumentTypeEnum type, long instrumentid, Map<Integer, Object> fields) {
        final StaticData data = createFor(type, fields);
        if (data != null) {
            synchronized (this.map) {
                this.map.put(instrumentid, data);
            }
        }
    }

    /**
     * expected to be called by external scheduler
     */
    public void gc() {
        int n = 0;
        synchronized (this.map) {
            for (Iterator<StaticData> it = this.map.values().iterator(); it.hasNext(); ) {
                if (((AbstractStaticData) it.next()).incrementAndGetAge() > MAX_AGE) {
                    it.remove();
                    n++;
                }
            }
        }
        this.logger.info("<gc> removed " + n);
    }

    private AbstractStaticData createFor(InstrumentTypeEnum type, Map<Integer, Object> fields) {
        switch (type) {
            case CER:
                return createCer(fields);
            case STK:
                return createStk(fields);
            case FND:
                return createFnd(fields);
            case BND:
                return createBnd(fields);
            case GNS:
                return createGns(fields);
            default:
                return null;
        }
    }

    private static StaticDataCER createCer(Map<Integer, Object> fields) {
        return new StaticDataCER(
                asLong(fields.get(RatioFieldDescription.cap.id()), 0L),
                asLong(fields.get(RatioFieldDescription.subscriptionRatio.id()), 0L),
                (String) fields.get(RatioFieldDescription.typeKey.id()),
                asInt(fields.get(RatioFieldDescription.expires.id()), 0),
                (String) fields.get(RatioFieldDescription.currencyStrike.id())
        );
    }

    private static StaticDataGNS createGns(Map<Integer, Object> fields) {
        return new StaticDataGNS(
                asLong(fields.get(RatioFieldDescription.wmIssueVolume.id())),
                asLong(fields.get(RatioFieldDescription.wmNumberOfIssuedEquities.id())),
                asLong(fields.get(RatioFieldDescription.wmDividend.id())),
                asLong(fields.get(RatioFieldDescription.wmDividendLastYear.id())),
                (String) fields.get(RatioFieldDescription.wmDividendCurrency.id())
        );
    }

    private static StaticDataSTK createStk(Map<Integer, Object> fields) {
        return new StaticDataSTK(
                asLong(fields.get(RatioFieldDescription.factsetBookValue1Y.id())),
                asLong(fields.get(RatioFieldDescription.factsetBookValue2Y.id())),
                asLong(fields.get(RatioFieldDescription.factsetCashflow1Y.id())),
                asLong(fields.get(RatioFieldDescription.factsetCashflow2Y.id())),
                asLong(fields.get(RatioFieldDescription.factsetSales1Y.id())),
                asLong(fields.get(RatioFieldDescription.factsetSales2Y.id())),
                asLong(fields.get(RatioFieldDescription.factsetEps1Y.id())),
                asLong(fields.get(RatioFieldDescription.factsetEps2Y.id())),
                (String) fields.get(RatioFieldDescription.factsetCurrency.id()),
                asLong(fields.get(RatioFieldDescription.trBookValue1Y.id())),
                asLong(fields.get(RatioFieldDescription.trBookValue2Y.id())),
                asLong(fields.get(RatioFieldDescription.trCashflow1Y.id())),
                asLong(fields.get(RatioFieldDescription.trCashflow2Y.id())),
                asLong(fields.get(RatioFieldDescription.trSales1Y.id())),
                asLong(fields.get(RatioFieldDescription.trSales2Y.id())),
                asLong(fields.get(RatioFieldDescription.trEps1Y.id())),
                asLong(fields.get(RatioFieldDescription.trEps2Y.id())),
                (String) fields.get(RatioFieldDescription.trCurrency.id()),
                asLong(fields.get(RatioFieldDescription.wmDividend.id())),
                asLong(fields.get(RatioFieldDescription.wmDividendLastYear.id())),
                (String) fields.get(RatioFieldDescription.wmDividendCurrency.id()),
                asLong(fields.get(RatioFieldDescription.wmIssueVolume.id())),
                asLong(fields.get(RatioFieldDescription.wmNumberOfIssuedEquities.id()))
        );
    }

    private static StaticDataFND createFnd(Map<Integer, Object> fields) {
        return new StaticDataFND(
                (Long) fields.get(RatioFieldDescription.vwdBenchmarkQid.id()),
                (Long) fields.get(RatioFieldDescription.vwdbenlBenchmarkQid.id())
        );
    }

    private static StaticDataBND createBnd(Map<Integer, Object> fields) {
        return new StaticDataBND(asLong(fields.get(RatioFieldDescription.wmRedemptionPrice.id())));
    }

    private static long asLong(Object o) {
        return asLong(o, Long.MIN_VALUE);
    }

    private static long asLong(Object o, long defaultValue) {
        return (o == null) ? defaultValue : ((Number) o).longValue();
    }

    private static int asInt(Object o, int defaultValue) {
        return (o == null) ? defaultValue : ((Number) o).intValue();
    }
}
