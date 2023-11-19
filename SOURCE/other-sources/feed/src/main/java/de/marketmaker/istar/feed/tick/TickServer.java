/*
 * TickServer.java
 *
 * Created on 14.12.2004 08:45:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import static de.marketmaker.istar.feed.util.FeedMetricsSupport.mayStartSample;
import static de.marketmaker.istar.feed.util.FeedMetricsSupport.mayStopSample;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.AggregatedTickRecord;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.feed.VendorkeyHistory;
import de.marketmaker.istar.feed.VendorkeyUtils;
import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.feed.api.TickConnector;
import de.marketmaker.istar.feed.history.TickHistoryProvider;
import de.marketmaker.istar.feed.history.TickHistoryRequest;
import de.marketmaker.istar.feed.history.TickHistoryResponse;
import de.marketmaker.istar.feed.ordered.tick.FileTickStore;
import de.marketmaker.istar.feed.ordered.tick.TickDirectory;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer.Sample;
import java.io.File;
import java.util.regex.Matcher;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * A tick server provides history tick records within 100 days for available vendor keys.
 * <p>
 * <b><em>Note that only queries for history tick records are supported. Queries of tick records for
 * the present day must be made against {@link de.marketmaker.istar.feed.IntradayServer}</em></b>
 * <p>
 * TODO: exception if query for present day is made?
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TickServer implements TickConnector, TickServerMBean, InitializingBean {

    private static final int OTC_KEYS_CHANGED = 20070505;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Logger accessLogger;

    private String accessLogName = "[access].[TickServer]";

    private int maxNumDaysBack = 100;

    private MeterRegistry meterRegistry;

    private TickDirectoryProvider directoryProvider;

    private FileTickStore fileTickStore;

    private VendorkeyHistory vendorkeyHistory;

    private TickHistoryProvider tickHistoryProvider;

    private boolean supportToday = false;

    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void setSupportToday(boolean supportToday) {
        this.supportToday = supportToday;
    }

    public void setTickHistoryProvider(TickHistoryProvider tickHistoryProvider) {
        this.tickHistoryProvider = tickHistoryProvider;
    }

    public void setVendorkeyHistory(VendorkeyHistory vendorkeyHistory) {
        this.vendorkeyHistory = vendorkeyHistory;
    }

    public void setFileTickStore(FileTickStore fileTickStore) {
        this.fileTickStore = fileTickStore;
    }

    public void setAccessLogName(String accessLogName) {
        this.accessLogName = accessLogName;
    }

    public void setMaxNumDaysBack(int maxNumDaysBack) {
        this.maxNumDaysBack = maxNumDaysBack;
    }

    public void setDirectoryProvider(TickDirectoryProvider directoryProvider) {
        this.directoryProvider = directoryProvider;
    }

    public void setBaseDirectory(File dir) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("not a directory: " + dir.getAbsolutePath());
        }
        this.directoryProvider = new TickDirectoryProvider() {
            @Override
            public TickDirectory getDirectory(int yyyymmdd) {
                return TickDirectory.open(new File(dir, Integer.toString(yyyymmdd)));
            }

            @Override
            public String toString() {
                return "TickDirectoryProvider{" + dir.getAbsolutePath() + "}";
            }
        };
    }

    public void afterPropertiesSet() throws Exception {
        if (this.fileTickStore == null) {
            throw new IllegalStateException("fileTickStore is null");
        }
        if (this.directoryProvider == null) {
            throw new IllegalStateException("directoryProvider is null");
        }
        this.accessLogger = LoggerFactory.getLogger(this.accessLogName);
    }

    @Override
    public IntradayResponse getIntradayData(IntradayRequest request) {
        final Sample sample = mayStartSample(this.meterRegistry);

        StringBuilder sb = null;
        if (this.accessLogger.isInfoEnabled()) {
            sb = new StringBuilder(request.size() * 40);
            sb.append("Ticks ");
        }

        try {
            IntradayResponse intradayResponse = doGetIntradayData(request, sb);
            if (sb != null) {
                this.accessLogger.info(sb.toString());
            }
            return intradayResponse;
        } catch (Throwable t) {
            this.logger.warn("<getIntradayData> failed", t);
            throw new RuntimeException("<getIntradayData> failed", t);
        } finally {
            mayStopSample(TickServer.class, this.meterRegistry, sample, IntradayRequest.class);
        }
    }

    private IntradayResponse doGetIntradayData(IntradayRequest request, StringBuilder sb) {
        final IntradayResponse response = new IntradayResponse();

        int numTotalBytes = 0;

        final boolean fullAccess = request.isTickDataFullAccess();
        for (final IntradayRequest.Item item : request.getItems()) {
            if (!item.isWithTicks()) {
                continue;
            }

            final String vendorkey = item.getVendorkey();

            final IntradayResponse.Item ri = new IntradayResponse.Item(vendorkey, item.isRealtime());
            response.add(ri);

            final TickRecordImpl tr = new TickRecordImpl();
            ri.setTickRecord(tr);

            final int startDate = fullAccess ? item.getTicksFrom() : Math.max(DateUtil.getDate(-this.maxNumDaysBack), item.getTicksFrom());
            final int endDate = fullAccess ? item.getTicksTo() : Math.min(DateUtil.getDate(this.supportToday ? 0 : -1), item.getTicksTo());

            int date = startDate;
            int numBytes = 0;
            while (date <= endDate) {
                AbstractTickRecord.TickItem ticks = getTickItem(vendorkey, date);

                if (ticks != null) {
                    tr.add(ticks);
                    numBytes += ticks.getData().length;
                }

                final LocalDate ld = DateUtil.yyyyMmDdToLocalDate(date);
                date = DateUtil.toYyyyMmDd(ld.plusDays(1));
            }

            numTotalBytes += numBytes;

            if (sb != null) {
                sb.append(vendorkey).append(" ");
                sb.append(startDate);
                if (endDate != startDate) {
                    sb.append("-").append(endDate);
                }
                sb.append(" #").append(numBytes).append(" ");
            }
        }

        if (sb != null && response.size() > 1) {
            sb.append(" #total=");
            sb.append(numTotalBytes);
        }

        return response;
    }

    @Override
    public TickHistoryResponse getTickHistory(TickHistoryRequest req) {
        final Sample sample = mayStartSample(this.meterRegistry);
        try {
            if (this.tickHistoryProvider != null) {
                if (DateUtil.canUseTickHistory(req.getInterval())) {
                    return this.tickHistoryProvider.query(req);
                }
                this.logger.warn("<getTickHistory> from intraday for " + req);
            }
            return getIntradayTickHistory(req);
        } finally {
            mayStopSample(TickServer.class, this.meterRegistry, sample, TickHistoryRequest.class);
        }
    }

    protected TickHistoryResponse getIntradayTickHistory(TickHistoryRequest req) {
        try {
            final IntradayRequest intradayRequest = new IntradayRequest();
            final IntradayRequest.Item item = new IntradayRequest.Item(req.getVwdCode(), false);
            // if many days, can result in large amount of data
            item.setRetrieveTicks(DateUtil.toYyyyMmDd(req.getInterval().getStart()),
                    DateUtil.toYyyyMmDd(req.getInterval().getEnd()));
            intradayRequest.add(item);
            final IntradayResponse resp = getIntradayData(intradayRequest);
            if (resp.isValid()) {
                final TickRecord tickRecord = resp.getItem(req.getVwdCode()).getTickRecord();
                if (tickRecord != null) {
                    final AggregatedTickRecord atr = tickRecord.aggregate(req.getDuration(), req.getTickType());
                    ReadableInterval interval = atr.getInterval();
                    if (interval != null) {
                        return new TickHistoryResponse(atr, interval.getEnd());
                    }
                }
            }
            this.logger.warn("<getTickHistory> no ticks found for: " + req);
        } catch (Exception e) {
            this.logger.error("<getIntradayTickHistory> failed for " + req, e);
        }
        return TickHistoryResponse.INVALID;
    }

    @Override
    public byte[] getTicks(final String vendorkey, int date) {
        AbstractTickRecord.TickItem item = getTickItem(vendorkey, date);
        return (item != null) ? item.getData() : null;
    }

    @Override
    public AbstractTickRecord.TickItem getTickItem(final String vendorkey, int date) {
        TickDirectory directory = this.directoryProvider.getDirectory(date);
        if (directory == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getTicks> no ticks for " + vendorkey + ", " + date);
            }
            return null;
        }

        String resolvedKey = resolveKey(vendorkey, date);
        if (!resolvedKey.equals(vendorkey)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getTickItem> " + vendorkey + "@" + date + " resolved to " + resolvedKey);
            }
        }
        final byte[] result = directory.readTicks(fileTickStore, resolvedKey);
        if (result == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getTicks> no ticks for " + vendorkey + " on " + date);
            }
            return null;
        }
        return new AbstractTickRecord.TickItem(date, result, directory.getEncoding());
    }

    private String resolveKey(final String vendorkey, int date) {
        if (date < OTC_KEYS_CHANGED) {
            final Matcher m = VendorkeyVwd.KEY_PATTERN.matcher(vendorkey);
            if (m.matches()) {
                VendorkeyVwd vkey = toVendorkey(vendorkey, m.group(1));
                if (VendorkeyUtils.isOTCKey(vkey)) {
                    return VendorkeyUtils.getOldOTCKey(vkey).toString();
                }
            }
        }
        if (this.vendorkeyHistory != null) {
            return this.vendorkeyHistory.getVendorkey(vendorkey, date);
        }
        return vendorkey;
    }

    private VendorkeyVwd toVendorkey(String vendorkey, String typePrefix) {
        return StringUtils.hasText(typePrefix)
                ? VendorkeyVwd.getInstance(vendorkey)         // with type
                : VendorkeyVwd.getInstance("1." + vendorkey); // w/o type, use dummy
    }

}
