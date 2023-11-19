/*
 * BalancesReader.java
 *
 * Created on 07.10.2010 12:42:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.convensys;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.data.ReferenceInterval;
import de.marketmaker.istar.domainimpl.data.ReferenceIntervalImpl;

/**
* @author oflege
*/
class BalancesReader extends IstarMdpExportReader<Void> {
    private final Map<Long, Map<String, Map<ReferenceInterval, BigDecimal>>> values
            = new HashMap<>();

    private final Map<Long, String> currencies = new HashMap<>();

    // there are only a couple of dozen intervals, so it pays to cache them
    private final Map<ReferenceIntervalImpl, ReferenceIntervalImpl> riCache
            = new HashMap<>();

    protected Void getResult() {
        return null;
    }

    BalancesReader(boolean limited) {
        super(limited, "CURRENCY", "PATH");
    }

    protected void handleRow() {
        final Long instrumentid = getLong("INSTRUMENTID", "IID");
        final BigDecimal amount = getBigDecimal("AMOUNT");
        if (instrumentid == null || !"y".equals(get("YQ")) || amount == null) {
            return;
        }

        final String currency = get("CURRENCY");
        if (currency != null && !this.currencies.containsKey(instrumentid)) {
            this.currencies.put(instrumentid, currency);
        }

        Map<String, Map<ReferenceInterval, BigDecimal>> intervals = this.values.get(instrumentid);
        if (intervals == null) {
            intervals = new HashMap<>();
            this.values.put(instrumentid, intervals);
        }

        final String path = get("PATH");
        Map<ReferenceInterval, BigDecimal> fs = intervals.get(path);
        if (fs == null) {
            fs = new HashMap<>();
            intervals.put(path, fs);
        }

        final ReferenceInterval ri = createInterval();
        if (fs.containsKey(ri)) {
            this.logger.warn("<handleRow> interval already defined for instrumentid "
                    + instrumentid + " and path " + path + ": " + ri.getInterval());
        }
        fs.put(ri, amount);
    }

    private ReferenceInterval createInterval() {
        final DateTime startdate = getDateTime("START_");
        final DateTime enddate = getDateTime("END_");

//            final DateTime start = new DateTime(this.startdate.getDateTime(), 1, 1, 0, 0, 0, 0);
//            final DateTime end= new DateTime(this.startdate.getDateTime(), 12, 31, 23, 59, 59,999);
        final Interval interval = new Interval(startdate, enddate);
        final ReferenceIntervalImpl ri = new ReferenceIntervalImpl(interval, "T".equals(get("SHORTENED")));
        final ReferenceIntervalImpl existing = riCache.get(ri);
        if (existing != null) {
            return existing;
        }
        riCache.put(ri, ri);
        return ri;
    }

    public Map<Long, Map<String, Map<ReferenceInterval, BigDecimal>>> getValues() {
        return values;
    }

    public Map<Long, String> getCurrencies() {
        return currencies;
    }

    public static void main(String[] args) throws Exception {
        final BalancesReader balancesReader = new BalancesReader(false);
        final File f = new File("d:/produktion/var/data/provider/istar-convensys-balance.xml.gz");
        balancesReader.read(f);
    }
}
