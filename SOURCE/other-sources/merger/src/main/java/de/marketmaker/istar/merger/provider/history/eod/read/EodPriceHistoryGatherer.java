/*
 * EodPriceHistoryGatherer.java
 *
 * Created on 07.01.13 13:33
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.read;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.history.HistoryGatherer;
import de.marketmaker.istar.feed.history.HistoryUnit;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseries;
import de.marketmaker.istar.merger.provider.history.eod.EodPriceHistoryRequest;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

import static de.marketmaker.istar.feed.history.HistoryUnit.*;

/**
 * @author zzhao
 */
@ManagedResource
public class EodPriceHistoryGatherer implements InitializingBean, HistoryGatherer,
        DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Object monitor = new Object();

    private EodHistoryReader monthsReader;

    private EodHistoryReader patchReader;

    private EodHistoryReader restReader;

    private EodHistoryReader[] readers;

    private Interval interval = null;

    private boolean stopped = false;

    public void setMonthsReader(EodHistoryReader monthsReader) {
        this.monthsReader = monthsReader;
    }

    public void setPatchReader(EodHistoryReader patchReader) {
        this.patchReader = patchReader;
    }

    public void setRestReader(EodHistoryReader restReader) {
        this.restReader = restReader;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.restReader, "rest reader required");
        Assert.notNull(this.monthsReader, "months reader required");
        Assert.notNull(this.patchReader, "patch reader required");
        this.readers = new EodHistoryReader[]{this.restReader, this.patchReader, this.monthsReader};
    }

    @Override
    public void updateUnits(File dir, EnumSet<HistoryUnit> units) throws IOException {
        synchronized (this.monitor) {
            if (this.stopped) {
                return;
            }
            for (EodHistoryReader reader : this.readers) {
                updateUnit(reader, units, dir);
            }
            calcInterval();
        }
    }

    private void calcInterval() {
        DateTime from = null;
        DateTime to = null;
        for (EodHistoryReader reader : this.readers) {
            final Interval ri = reader.getInterval();
            if (null != ri) {
                if (null == from || ri.getStart().isBefore(from)) {
                    from = ri.getStart();
                }
            }
            if (null != ri) {
                if (null == to || ri.getEnd().isAfter(to)) {
                    to = ri.getEnd();
                }
            }
        }

        if (null == from && null == to) {
            this.interval = null;
        }
        else {
            this.interval = new Interval(from, to);
        }
    }

    private void updateUnit(EodHistoryReader reader, EnumSet<HistoryUnit> units, File dir)
            throws IOException {
        if (units.contains(reader.getUnit())) {
            reader.setFile(reader.getUnit().getLatestFile(dir));
        }
        else {
            reader.setFile(null);
        }
    }

    @Override
    public void destroy() throws Exception {
        synchronized (this.monitor) {
            this.stopped = true;
        }
        IoUtils.close(this.monthsReader);
        IoUtils.close(this.patchReader);
        IoUtils.close(this.restReader);
    }

    public Map<Integer, HistoricTimeseries> gatherPrices(EodPriceHistoryRequest req)
            throws IOException {
        if (null == this.interval) {
            this.logger.error("<gatherPrices> null interval(no history units updated)," +
                    " cannot gather prices");
            return Collections.emptyMap();
        }
        final TimeTaker tt = new TimeTaker();
        Map<Integer, ByteBuffer> months;
        Map<Integer, ByteBuffer> rest = Collections.emptyMap();
        ByteBuffer patch;

        final Interval val;
        synchronized (this.monitor) {
            if (this.stopped || !req.getInterval().overlaps(this.interval)) {
                return Collections.emptyMap();
            }

            val = this.interval.overlap(req.getInterval());
            if (this.monthsReader.contains(val)) {
                months = this.monthsReader.loadData(req.getQuote(), val, req.getFields());
            }
            else {
                months = this.monthsReader.loadData(req.getQuote(), val, req.getFields());
                rest = this.restReader.loadData(req.getQuote(), val, req.getFields());
            }
            patch = this.patchReader.loadData(req.getQuote(), val);
        }

        final Map<Integer, HistoricTimeseries> ret = extractPrices(val, req.getFields(), rest, months, patch);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<gatherPrices> {},{}", req, tt);
        }
        return ret;
    }

    private Map<Integer, HistoricTimeseries> extractPrices(Interval interval, Set<Integer> fields,
            Map<Integer, ByteBuffer> rest, Map<Integer, ByteBuffer> months, ByteBuffer patch) {
        if (CollectionUtils.isEmpty(months) && CollectionUtils.isEmpty(rest)
                && (!patch.hasRemaining())) {
            return Collections.emptyMap();
        }

        final LocalDate startDate = interval.getStart().toLocalDate();
        final LocalDate endDate = interval.getEnd().toLocalDate();
        final int len = DateUtil.daysBetween(startDate, endDate);

        final int fromYear = interval.getEnd().minusDays(1).getYear();
        final int toYear = interval.getStart().getYear();

        final Map<Integer, double[]> valMap = new HashMap<>();
        for (Integer field : fields) {
            final double[] val = new double[len];
            Arrays.fill(val, Double.NaN);
            fromUnit(val, rest.get(field), fromYear, toYear, startDate, endDate);
            fromUnit(val, months.get(field), fromYear, toYear, startDate, endDate);
            valMap.put(field, val);
        }

        if (patch.hasRemaining()) {
            final EodPriceIterator it = new EodPriceIterator(patch);
            while (it.hasNext()) {
                final EodPrice priceMpc = it.next();
                final int idx = DateUtil.daysBetween(startDate, priceMpc.getLocalDate());
                if (idx < 0) {
                    break;
                }
                if (idx < len) {
                    for (Integer field : fields) {
                        final double[] values = valMap.get(field);
                        if (null != values) {
                            values[idx] = (null == priceMpc.getPrice(field)) ?
                                    Double.NaN : Double.parseDouble(priceMpc.getPrice(field));
                        }
                    }
                }
            }
        }

        final Map<Integer, HistoricTimeseries> ret = new HashMap<>();
        for (Map.Entry<Integer, double[]> entry : valMap.entrySet()) {
            final double[] values = entry.getValue();
            if (anyValidValues(values)) {
                ret.put(entry.getKey(), new HistoricTimeseries(values, startDate));
            }
        }
        return ret;
    }

    private boolean anyValidValues(double[] values) {
        for (double value : values) {
            if (!Double.isNaN(value)) {
                return true;
            }
        }
        return false;
    }

    private void fromUnit(double[] val, ByteBuffer data, int fromYear, int toYear,
            LocalDate startDate, LocalDate endDate) {
        if (null == data || !data.hasRemaining()) {
            return;
        }

        final ByteBuffer bb = getBuffer(data, fromYear, toYear);
        final EodFieldPriceIterator it = new EodFieldPriceIterator(bb);
        while (it.hasNext()) {
            final EodFieldPrice price = it.next();
            final LocalDate date = DateUtil.yyyyMmDdToLocalDate(price.getDate());
            if (!date.isBefore(startDate) && endDate.isAfter(date)) {
                val[DateUtil.daysBetween(startDate, date)] = Double.parseDouble(price.getPrice());
            }
        }
    }

    private ByteBuffer getBuffer(ByteBuffer data, int fromYear, int toYear) {
        if (!data.hasRemaining()) {
            return EodUtil.EMPTY_BB;
        }
        int from = data.position();
        int to = data.limit();
        while (data.hasRemaining()) {
            final int pos = data.position();
            final short year = data.getShort();
            final int lengthMonth = data.getInt();
            final int len = EodUtil.decodeYearLength(lengthMonth);
            if (year == fromYear) {
                from = pos;
            }
            if (year == toYear) {
                to = pos + 2 + 4 + len;
                break;
            }
            data.position(data.position() + len);
        }

        final ByteBuffer ret = data.asReadOnlyBuffer();
        ret.position(from);
        ret.limit(to);
        return ret;
    }

    @ManagedOperation(description = "invoke get EoD prices for a given quote, field and interval")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "quote", description = "a quote id"),
            @ManagedOperationParameter(name = "field", description = "a field id"),
            @ManagedOperationParameter(name = "from", description = "a day yyyyMMdd"),
            @ManagedOperationParameter(name = "to", description = "a day yyyyMMdd")
    })
    public String query(long quote, int field, int from, int to) {
        if (from == 0) {
            from = 18991230;
        }
        if (to == 0) {
            to = DateUtil.toYyyyMmDd(new LocalDate());
        }

        this.logger.info("<query> {} {}", quote + ":" + field, from + " ~ " + to);
        final EodPriceHistoryRequest req = new EodPriceHistoryRequest(quote,
                new Interval(DateUtil.yyyymmddToDateTime(from), DateUtil.yyyymmddToDateTime(to)),
                field);
        try {
            final TimeTaker tt = new TimeTaker();
            final Map<Integer, HistoricTimeseries> map = gatherPrices(req);
            tt.stop();
            if (CollectionUtils.isEmpty(map)) {
                return "no prices found for " + quote + " -> " + field + " for " + from + " ~ " + to;
            }
            final StringBuilder sb = new StringBuilder();
            sb.append(quote).append(" -> ").append(field).append(", ").append(from).append(" ~ ")
                    .append(to);
            sb.append("\n").append("in ").append(tt);
            for (HistoricTimeseries ht : map.values()) {
                final LocalDate startDay = ht.getStartDay();
                final double[] values = ht.getValues();
                for (int i = 0; i < values.length; i++) {
                    sb.append("\n").append(HistoryUtil.DTF_DAY.print(startDay.plusDays(i)))
                            .append(":").append(Double.valueOf(values[i]));
                }
            }

            return sb.toString();
        } catch (Exception e) {
            this.logger.error("<query> {}", quote + ":" + field + " " + from + " ~ " + to, e);
            return "Error: " + e.getMessage();
        }
    }

    @ManagedOperation(description = "invoke to get storage statistic for a given quote")
    @ManagedOperationParameters(
            @ManagedOperationParameter(name = "quote", description = "a quote id")
    )
    public String statistic(long quote) {
        final TimeTaker tt = new TimeTaker();
        this.logger.info("<statistic> {}", quote);
        try {
            final ByteBuffer months;
            final ByteBuffer rest;
            final ByteBuffer patch;
            synchronized (this.monitor) {
                if (this.stopped) {
                    return "gatherer stopped";
                }

                months = this.monthsReader.loadData(quote);
                patch = this.patchReader.loadData(quote);
                rest = this.restReader.loadData(quote);
            }

            int totalLen = months.remaining();
            totalLen += rest.remaining();
            totalLen += patch.remaining();

            if (totalLen > 0) {
                final StringBuilder sb = new StringBuilder();
                sb.append(quote).append(": ").append(totalLen).append(" byte(s)");
                appendUnitStatistic(Rest, rest, sb);
                appendUnitStatistic(Months, months, sb);
                appendUnitStatistic(Patch, patch, sb);
                return sb.append("\n").append("in ").append(tt).toString();
            }
            else {
                return "no data found for: " + quote;
            }
        } catch (Exception e) {
            this.logger.error("<query> {}", quote, e);
            return "Error: " + e.getMessage();
        }
    }

    private void appendUnitStatistic(HistoryUnit unit, ByteBuffer bb, StringBuilder sb) {
        if (!bb.hasRemaining()) {
            return;
        }
        switch (unit) {
            case Rest:
            case Months:
                final int fields = bb.get();
                int totalLen = 0;
                final StringBuilder sbb = new StringBuilder();
                for (int i = 0; i < fields; i++) {
                    final int len24Field8 = bb.getInt();
                    final int len = EodUtil.decodeFieldLength(len24Field8);
                    sbb.append("\n").append("[").append(EodUtil.decodeField(len24Field8))
                            .append("]: ").append(len)
                            .append(" byte(s)");
                    final ByteBuffer slice = bb.slice();
                    slice.limit(len);
                    appendStatisticYear(sbb, slice, "   ");
                    bb.position(bb.position() + len);
                    totalLen += len;
                }

                if (totalLen > 0) {
                    sb.append("\n").append(unit).append(" ").append(fields).append(" field(s)")
                            .append(" ").append(totalLen).append(" byte(s)");
                    sb.append(sbb.toString());
                }
                break;
            case Patch:
                sb.append("\n").append(unit).append(": ").append(bb.remaining()).append(" byte(s)");
                break;
            default:
                throw new UnsupportedOperationException("no support for: " + unit);
        }
    }

    private void appendStatisticYear(StringBuilder sb, ByteBuffer bb, String prefix) {
        while (bb.hasRemaining()) {
            sb.append("\n").append(prefix).append(bb.getShort());
            final int len20Months12 = bb.getInt();
            final int len = EodUtil.decodeYearLength(len20Months12);
            sb.append(" ").append(len).append(" byte(s)");
            bb.position(bb.position() + len);
        }
    }
}
