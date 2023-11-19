/*
 * IntradayServer.java
 *
 * Created on 02.03.2005 16:28:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import static de.marketmaker.istar.feed.ordered.FieldDataUtil.getFieldsUpTo;
import static de.marketmaker.istar.feed.util.FeedMetricsSupport.mayStartSample;
import static de.marketmaker.istar.feed.util.FeedMetricsSupport.mayStopSample;
import static de.marketmaker.istar.feed.util.ProtoUtil.getRespInfo;
import static de.marketmaker.istar.feed.util.ProtoUtil.getSnapFieldValues;
import static de.marketmaker.istar.feed.util.ProtoUtil.getVwdFieldOrderIds;

import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.api.FeedConnector;
import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.feed.api.PageRequest;
import de.marketmaker.istar.feed.api.PageResponse;
import de.marketmaker.istar.feed.api.SymbolSortRequest;
import de.marketmaker.istar.feed.api.SymbolSortResponse;
import de.marketmaker.istar.feed.api.TypedVendorkeysRequest;
import de.marketmaker.istar.feed.api.TypedVendorkeysResponse;
import de.marketmaker.istar.feed.api.VendorkeyListRequest;
import de.marketmaker.istar.feed.api.VendorkeyListResponse;
import de.marketmaker.istar.feed.api.VendorkeyWithDelay;
import de.marketmaker.istar.feed.delay.DelayProvider;
import de.marketmaker.istar.feed.delay.DelayProviderUtil;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldDataIterator;
import de.marketmaker.istar.feed.ordered.FieldDataMerger;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedSnapData;
import de.marketmaker.istar.feed.ordered.OrderedSnapRecord;
import de.marketmaker.istar.feed.pages.PageDao;
import de.marketmaker.istar.feed.pages.PageData;
import de.marketmaker.istar.feed.snap.SnapData;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickProvider;
import de.marketmaker.istar.feed.tick.TickRecordImpl;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;
import dev.infrontfinance.dm.proto.DmChicago.SnapFieldValues;
import dev.infrontfinance.dm.proto.DmChicago.SnapFieldsReq;
import dev.infrontfinance.dm.proto.DmChicago.SnapFieldsResp;
import dev.infrontfinance.dm.proto.DmChicago.SnapFieldsResp.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer.Sample;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * An intraday server provides tick records of the present day for available vendor keys.
 * <p/>
 * <b><em>Note that only queries of tick records for the present day is supported. Queries of history
 * tick records must be made against {@link de.marketmaker.istar.feed.tick.TickServer} instead</em></b>
 * <p/>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IntradayServer implements FeedConnector, IntradayServerMBean, InitializingBean {

    private static byte[] copySnapBytes(OrderedSnapData sd, int maxOid) {
        return doGetSnapBytes(sd, maxOid, true);
    }

    private static byte[] getSnapBytes(OrderedSnapData sd, int maxOid) {
        return doGetSnapBytes(sd, maxOid, false);
    }

    private static byte[] doGetSnapBytes(OrderedSnapData sd, int maxOid, boolean copy) {
        if (maxOid == 0) {
            return sd.getData(copy);
        }
        return getFieldsUpTo(new BufferFieldData(sd.getData(false)), maxOid);
    }

    private static OrderedSnapRecord createSnapRecord(OrderedSnapData sd, int maxOid,
            int delayInSeconds) {
        return new OrderedSnapRecord(copySnapBytes(sd, maxOid), sd.getLastUpdateTimestamp(), delayInSeconds);
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Logger accessLogger;

    private String accessLogName = "[access].[IntradayServer]";

    private FeedDataRepository feedDataRepository;

    private TickProvider tickProvider;

    private PageDao pageDao;

    private DelayProvider delayProvider;

    private final FieldDataMerger fieldDataMerger = new FieldDataMerger();

    private MeterRegistry meterRegistry;

    public IntradayServer() {
    }

    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void setDelayProvider(DelayProvider delayProvider) {
        this.delayProvider = delayProvider;
    }

    public void setFeedDataRepository(FeedDataRepository feedDataRepository) {
        this.feedDataRepository = feedDataRepository;
    }

    public void setTickProvider(TickProvider tickProvider) {
        this.tickProvider = tickProvider;
    }

    public void setPageDao(PageDao pageDao) {
        this.pageDao = pageDao;
    }

    public void setAccessLogName(String accessLogName) {
        this.accessLogName = accessLogName;
    }

    public void afterPropertiesSet() throws Exception {
        this.accessLogger = LoggerFactory.getLogger(this.accessLogName);
    }

    private StringBuilder getBuilderForAccessLog(String name, int size, String clientInfo) {
        if (!this.accessLogger.isInfoEnabled()) {
            return null;
        }
        // clientInfo = pid@host.domain,timestamp,requestId -- we only need pid@host
        final int p = clientInfo.indexOf('.');
        return new StringBuilder(size).append(name)
                .append(";").append(clientInfo, 0, p > 0 ? p : clientInfo.length());
    }

    private void logAccess(final StringBuilder sb) {
        if (sb != null) {
            this.accessLogger.info(sb.toString());
        }
    }

    @Override
    public IntradayResponse getIntradayData(IntradayRequest request) {
        final Sample sample = mayStartSample(this.meterRegistry);
        final StringBuilder sb = getBuilderForAccessLog(request.isWithTicks() ? "Tick" : "Snap",
            request.size() * 40, request.getClientInfo());
        final long then = System.currentTimeMillis();

        try {
            IntradayResponse intradayResponse = doGetIntradayData(request, sb);

            if (sb != null) {
                sb.append(";").append(System.currentTimeMillis() - then);
                logAccess(sb);
            }

            return intradayResponse;
        } catch (Throwable t) {
            this.logger.error("<getIntradayData> failed", t);
            throw new RuntimeException("<getIntradayData> failed", t);
        } finally {
            mayStopSample(IntradayServer.class, this.meterRegistry, sample, IntradayRequest.class);
        }
    }

    @Override
    public IntradayResponse getIntradayDataJmx(IntradayRequest request) {
        final Sample sample = mayStartSample(this.meterRegistry);
        try {
            return doGetIntradayData(request, null);
        } catch (Exception e) {
            this.logger.warn("<getIntradayDataJmx> failed for " + request, e);
            return null;
        } finally {
            mayStopSample(IntradayServer.class, this.meterRegistry, sample, IntradayRequest.class);
        }
    }

    @Override
    public String getTypedVendorkey(String vkeyWithoutType) {
        return resolveKey(vkeyWithoutType);
    }

    @Override
    public TypedVendorkeysResponse getTypesForVwdcodes(TypedVendorkeysRequest request) {
        final Sample sample = mayStartSample(this.meterRegistry);
        try {
            TypedVendorkeysResponse result = new TypedVendorkeysResponse();
            for (String vwdcode : request.getVwdcodes()) {
                String s = getTypedVendorkey(vwdcode);
                if (s != null) {
                    result.add(vwdcode, s);
                }
            }
            return result;
        } finally {
            mayStopSample(IntradayServer.class, this.meterRegistry,
                sample, TypedVendorkeysRequest.class);
        }
    }

    @Override
    public byte[] getTicks(String vendorkey, int date) {
        AbstractTickRecord.TickItem item = getTickItem(vendorkey, date);
        return (item != null) ? item.getData() : null;
    }

    @Override
    public AbstractTickRecord.TickItem getTickItem(final String vendorkey, int date) {
        final FeedData fd = getFeedData(vendorkey);
        if (fd == null || this.tickProvider == null) {
            return null;
        }
        final TickProvider.Result result;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (fd) {
            result = getTicks(fd, date, null);
        }
        if (result == null) {
            return null;
        }

        final TickRecordImpl tr = new TickRecordImpl();
        tr.add(date, result.getTicks(), result.getEncoding());

        return tr.getItem(date);
    }

    private IntradayResponse doGetIntradayData(IntradayRequest request, StringBuilder sb) {
        final boolean withTicks = request.isWithTicks();
        if (sb != null && !request.isWithTicks()) {
            sb.append(";").append(request.size());
        }
        final IntradayResponse response = new IntradayResponse();
        final int today = DateUtil.dateToYyyyMmDd();
        final int yesterday = DateUtil.getDate(-1);

        int[] numSnaps = new int[2];

        for (final IntradayRequest.Item item : request.getItems()) {
            final String vendorkey = item.getVendorkey();
            if (response.getItem(vendorkey) != null) {
                continue;
            }

            final FeedData fd = getFeedData(vendorkey);
            if (fd == null) {
                if (withTicks && sb != null) {
                    sb.append(";").append(vendorkey).append(";X");
                }
                continue;
            }

            final IntradayResponse.Item ri = new IntradayResponse.Item(vendorkey, item.isRealtime());

            final boolean requestTodaysTicks = requestTicks(item, today, fd, request.getUpdatedSince());
            final boolean requestYesterdaysTicks = requestTicks(item, yesterday, fd, 0);

            SnapRecord snap = null;
            TickProvider.Result todaysResult = null;
            TickProvider.Result yesterdaysResult = null;
            final int[] tsi = item.getTickStorageInfo();

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (fd) {
                if (item.isRealtimeAndDelay()) {
                    addSnap(fd, ri, request.getUpdatedSince(), request.getMaxFieldOrderId());
                }
                else {
                    snap = getSnap(fd, item.isRealtime(), request.getUpdatedSince(), request.getMaxFieldOrderId());
                    if (snap == null) {
                        continue;
                    }
                }
                if (requestTodaysTicks) {
                    todaysResult = getTicks(fd, today, tsi);
                }
                if (requestYesterdaysTicks) {
                    yesterdaysResult = getTicks(fd, yesterday, tsi);
                }
            }

            // don't add earlier as we might hit continue in the sync block above
            response.add(ri);
            ri.setVendorkeyType(fd.getVendorkeyType());
            if (fd instanceof OrderedFeedData) {
                ri.setCreatedTimestamp(((OrderedFeedData) fd).getCreatedTimestamp());
            }

            if (!item.isRealtimeAndDelay()) {
                ri.setPriceSnapRecord(snap);
                numSnaps[item.isRealtime() ? 0 : 1]++;
            }

            final byte[] todaysTicks = getTicks(todaysResult);
            final byte[] yesterdaysTicks = getTicks(yesterdaysResult);

            if (todaysResult != null || yesterdaysResult != null) {
                final TickRecordImpl tr = new TickRecordImpl();
                int numTickBytes = 0;

                if (snap != null && snap.getNominalDelayInSeconds() > 0
                        && !item.isRealtime() && !item.isRealtimeAndDelay()) {
                    final int doa = SnapRecordUtils.getDateOfArrival(snap);
                    if (doa != 0) {
                        // mdps-delayed data's TOA is the time when the delayed data is received,
                        // so we cannot use getTimeOfArrival here
                        final SnapField sf = snap.getField(VwdFieldDescription.ADF_Zeit.id());
                        final int toa = sf.isDefined() ? SnapRecordUtils.getInt(sf) : SnapRecordUtils.getTime(snap);
                        tr.setLast(DateUtil.toDateTime(doa, toa), snap);
                    }
                }

                if (todaysResult != null) {
                    if (todaysTicks != null) {
                        tr.add(today, todaysTicks, todaysResult.getEncoding());
                        numTickBytes += todaysTicks.length;
                    }
                    ri.setTickStorageInfo(todaysResult.getStorageInfo());
                }
                if (yesterdaysResult != null) {
                    tr.add(yesterday, yesterdaysTicks, yesterdaysResult.getEncoding());
                    numTickBytes += yesterdaysTicks.length;
                }
                ri.setTickRecord(tr);

                if (sb != null) {
                    sb.append(";").append(vendorkey)
                            .append(";").append(item.isRealtimeAndDelay() ? '*' : (item.isRealtime() ? 'R' : 'D'))
                            .append(";").append(requestTodaysTicks ? 'T' : 'Y')
                            .append(";").append(numTickBytes)
                            .append(";").append(tsi != null ? ArraysUtil.sum(tsi) : numTickBytes); //
                }
            }
        }

        if (sb != null && !withTicks) {
            sb.append(';').append(numSnaps[0] + numSnaps[1]);
            sb.append(';').append(numSnaps[0]).append(';').append(numSnaps[1]);
        }

        return response;
    }

    private TickProvider.Result getTicks(FeedData fd, int day, final int[] storageInfo) {
        return this.tickProvider.getTicks(fd, day, storageInfo);
    }

    private byte[] getTicks(TickProvider.Result result) {
        return (result != null) ? result.getTicks() : null;
    }

    private boolean requestTicks(final IntradayRequest.Item item, final int day, FeedData fd,
            int updatedSince) {
        if (updatedSince != 0) {
            final OrderedSnapData osd = (OrderedSnapData) fd.getSnapData(true);
            if (osd.getLastUpdateTimestamp() < updatedSince) {
                return false;
            }
        }
        return this.tickProvider != null
                && item.isWithTicks()
                && day >= item.getTicksFrom() && day <= item.getTicksTo();
    }

    private FeedData getFeedData(final String vkStr) {
        final VendorkeyVwd vkey = VendorkeyVwd.getInstance(vkStr);
        if (vkey == VendorkeyVwd.ERROR) {
            // happens for "type-less" vkStr
            return this.feedDataRepository.get(new ByteString(vkStr));
        }
        return this.feedDataRepository.get(vkey.toVwdcode());
    }

    @Override
    public PageResponse getPage(PageRequest request) {
        if (this.pageDao == null) {
            throw new RuntimeException("<getPage> no pageDao set");
        }

        final Sample sample = mayStartSample(this.meterRegistry);
        final StringBuilder sb = getBuilderForAccessLog("Page", 60, request.getClientInfo());

        try {
            final PageResponse pageResponse = doGetPage(request, sb);

            logAccess(sb);

            return pageResponse;
        } catch (Throwable t) {
            this.logger.error("<getPage> failed", t);
            throw new RuntimeException("<getPage> failed", t);
        } finally {
            mayStopSample(IntradayServer.class, this.meterRegistry, sample, PageRequest.class);
        }
    }

    private PageResponse doGetPage(PageRequest request, StringBuilder sb) {
        this.logger.info("DO-29829: doGetPage");
        if (sb != null) {
            sb.append(";").append(request.getPagenumber());
            if (request.isPreferGermanText()) {
                sb.append("g");
            }
        }

        final PageData pageData = this.pageDao.getPageData(request.getPagenumber());
        if (pageData == null) {
            return null;
        }

        String text = pageData.getText();
        if (request.isPreferGermanText() && pageData.getTextg() != null) {
            text = pageData.getTextg();
        }

        if (sb != null) {
            sb.append(";").append(text == null ? 0 : text.length());
            sb.append(";").append(pageData.getKeys().size());
        }

        pageData.setKeys(resolveKeys(pageData.getKeys()));
        PageDao.Neighborhood neighbors = this.pageDao.getNeighborhood(request.getPagenumber());
        return new PageResponse(text, pageData.getKeys(), pageData.getSelectors(),
                pageData.isDynamic(), pageData.getTimestamp(),
                neighbors.getNextPagenumber() == null ? null :
                        Integer.toString(neighbors.getNextPagenumber()),
                neighbors.getPreviousPagenumber() == null ? null :
                        Integer.toString(neighbors.getPreviousPagenumber()));
    }

    @Override
    public VendorkeyListResponse getVendorkeys(VendorkeyListRequest request) {
        final Sample sample = mayStartSample(this.meterRegistry);
        try {
            final StringBuilder sb = getBuilderForAccessLog("VList", 24, request.getClientInfo());

            final VendorkeyListResponse response = doGetVendorkeys(request);
            if (sb != null) {
                sb.append(";").append(request.getMarket())
                    .append(";").append(response.getVendorkeys().size());
                logAccess(sb);
            }
            return response;
        } finally {
            mayStopSample(IntradayServer.class, this.meterRegistry,
                sample, VendorkeyListRequest.class);
        }
    }

    private VendorkeyListResponse doGetVendorkeys(VendorkeyListRequest request) {
        final String marketName = request.getMarket();

        final FeedMarket market = this.feedDataRepository.getMarket(new ByteString(marketName));
        if (market == null) {
            return new VendorkeyListResponse(marketName, 0);
        }

        final VendorkeyListResponse result = new VendorkeyListResponse(marketName, market.size());
        for (FeedData feedData : market.getElements(false)) {
            synchronized (feedData) {
                if (!feedData.isDeleted()) {
                    result.add(feedData.getVendorkey().toByteString().toString());
                }
            }
        }

        return result;
    }

    private List<String> resolveKeys(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> result = new ArrayList<>(keys.size());
        for (String s : keys) {
            final String resolvedKey = resolveKey(s);
            result.add(resolvedKey != null ? resolvedKey : s);
        }
        return result;
    }

    private String resolveKey(String s) {
        final FeedData feedData = this.feedDataRepository.get(new ByteString(s));
        if (feedData == null) {
            return null;
        }
        return feedData.getVendorkey().toByteString().toString();
    }

    @Override
    public SnapFieldsResp getSnapFields(SnapFieldsReq req) {
        final Sample sample = mayStartSample(this.meterRegistry);
        try {
            return getSnapFieldsInternal(req, req.getForExport());
        } finally {
            mayStopSample(IntradayServer.class, this.meterRegistry, sample, SnapFieldsReq.class);
        }
    }

    private SnapFieldsResp getSnapFieldsInternal(SnapFieldsReq req, boolean forExport) {
        final Builder resp = SnapFieldsResp.newBuilder();
        try {
            final FieldDataIterator fdi = FieldDataIterator.create(getVwdFieldOrderIds(req));
            resp.setInfo(getRespInfo(true));
            final ByteBuffer bb = ByteBuffer.wrap(req.getVwdCodes().toByteArray());
            final BufferFieldData bfd = new BufferFieldData();
            while (bb.hasRemaining()) {
                final ByteString vwdCode = ByteString.readFrom(bb);
                try {
                    final FeedData fd = this.feedDataRepository.get(vwdCode);
                    if (fd == null) {
                        resp.addValues(SnapFieldValues.getDefaultInstance());
                        continue;
                    }
                    final byte[] data;
                    final int[] timestamp = new int[1];
                    if (forExport) {
                        data = getFieldDataForExport((OrderedFeedData) fd, req, timestamp);
                    } else {
                        data = getFieldData((OrderedFeedData) fd, req, timestamp);
                    }
                    if (data == null) {
                        resp.addValues(SnapFieldValues.getDefaultInstance());
                    } else {
                        resp.addValues(getSnapFieldValues(timestamp[0], fdi, data, bfd, fd.getVendorkeyType()));
                    }
                } catch (Exception e) {
                    this.logger.warn("<getSnapFieldsInternal> failed for {}", vwdCode, e);
                    resp.addValues(SnapFieldValues.getDefaultInstance());
                }
            }
        } catch (Exception e) {
            this.logger.warn("<getSnapFieldsInternal> failed for {}, {}",
                req.getInfo().getClientInfo(), req.getMarket(), e);
            resp.setInfo(getRespInfo(false));
        }

        return resp.build();
    }

    /**
     * Transitioned from DpPricesController.getFieldData.
     * We use this method only for dp prices snap fields request (from dm-dpman service).
     */
    private byte[] getFieldData(OrderedFeedData fd, SnapFieldsReq req, int[] timestamp) {
        synchronized (fd) {
            final OrderedSnapData sd = fd.getSnapData(req.getRealtime());
            if (!sd.isInitialized()) {
                return null;
            }
            timestamp[0] = sd.getLastUpdateTimestamp();
            final byte[] data = sd.getData(true);
            if (!req.getRealtime() && req.getWithDynamicFields() && req.getWithNonDynamicFields()) {
                final SnapData sdRt = fd.getSnapData(true);
                if (!sdRt.isInitialized()) {
                    return null;
                }
                return merge(sdRt.getData(true), data);
            }
            return data;
        }
    }

    /**
     * Transitioned from OrderedDpWriter.initSnap.
     * We use this method for dp manager export snap field requests (from dm-dpman service).
     */
    private byte[] getFieldDataForExport(OrderedFeedData fd, SnapFieldsReq req, int[] timestamp) {
        synchronized (fd) {
            if (!req.getRealtime()) {
                final OrderedSnapData ntSnap = fd.getSnapData(false);
                if (ntSnap != null && ntSnap.isInitialized()) {
                    timestamp[0] = ntSnap.getLastUpdateTimestamp();
                    final OrderedSnapData rtSnap = fd.getSnapData(true);
                    return (rtSnap != null && rtSnap.isInitialized()) ?
                        merge(rtSnap.getData(true), ntSnap.getData(true)) : ntSnap.getData(true);
                }
            }
            final OrderedSnapData rtSnap = fd.getSnapData(true);
            if (rtSnap != null && rtSnap.isInitialized()) {
                timestamp[0] = rtSnap.getLastUpdateTimestamp();
                return rtSnap.getData(true);
            }

            return null;
        }
    }

    private byte[] merge(byte[] existing, byte[] update) {
        synchronized (this.fieldDataMerger) {
            final byte[] merged = this.fieldDataMerger.merge(new BufferFieldData(existing),
                new BufferFieldData(update));
            return (merged != null) ? merged : existing;
        }
    }

    @Override
    public byte[] getRawSnap(String vwdcode, boolean realtime) {
        final FeedData fd = this.feedDataRepository.get(new ByteString(vwdcode));
        if (fd == null) {
            return null;
        }
        synchronized (fd) {
            final SnapData sd = fd.getSnapData(realtime);
            if (sd == null || !sd.isInitialized()) {
                return null;
            }
            if (!(sd instanceof OrderedSnapData)) {
                return sd.getData(true);
            }

            final byte[] data = sd.getData(false);
            byte[] result = new byte[data.length + 4];
            final ByteBuffer bb = BufferFieldData.asBuffer(result);
            bb.putInt(((OrderedSnapData) sd).getLastUpdateTimestamp()).put(data);
            return result;
        }
    }

    /**
     * Add realtime and delayed (if defined) snap record to <tt>item</tt>.
     */
    private void addSnap(FeedData feedData, IntradayResponse.Item item, int updatedSince,
            int maxOid) {
        final OrderedSnapData rt = (OrderedSnapData) feedData.getSnapData(true);
        if (!rt.isInitialized() || rt.getLastUpdateTimestamp() < updatedSince) {
            return;
        }
        item.setPriceSnapRecord(createSnapRecord(rt, maxOid, 0));

        final OrderedSnapData nt = (OrderedSnapData) feedData.getSnapData(false);
        if (nt != null && nt.isInitialized() && this.delayProvider != null) {
            final int delayInSeconds = this.delayProvider.getDelayInSeconds(feedData);
            if (delayInSeconds > 0) {
                item.setDelaySnapRecord(createSnapRecord(nt, maxOid, delayInSeconds));
            }
        }
    }

    /**
     * Returns a SnapRecord for the given feedData, either realtime or delayed. The caller
     * has to ensure that it is synchronized on feedData.
     */
    private SnapRecord getSnap(FeedData feedData, boolean realtime, int updatedSince, int maxOid) {
        final Boolean isRealtime = isRealtime(feedData, realtime);
        if (isRealtime == null) {
            return null;
        }

        final int delayInSeconds = realtime ? 0 : this.delayProvider.getDelayInSeconds(feedData);

        if (feedData instanceof OrderedFeedData) {
            final OrderedSnapData rt = (OrderedSnapData) feedData.getSnapData(true);
            if (!rt.isInitialized() || rt.getLastUpdateTimestamp() < updatedSince) {
                return null;
            }
            if (isRealtime) {
                return createSnapRecord(rt, maxOid, 0);
            }
            final OrderedSnapData nt = (OrderedSnapData) feedData.getSnapData(false);
            if (!nt.isInitialized()) {  // nt is null when no snap backend is configured
                return null;
            }
            return new OrderedSnapRecord(merge(rt, nt, maxOid), nt.getLastUpdateTimestamp(), delayInSeconds);
        }
        else {
            final SnapData snapData = feedData.getSnapData(isRealtime);
            return snapData.toSnapRecord(delayInSeconds);
        }
    }

    private byte[] merge(OrderedSnapData rt, OrderedSnapData nt, int maxOid) {
        if (maxOid > 0 && maxOid < VwdFieldOrder.FIRST_NON_DYNAMIC_ORDER) {
            // we only request non-dynamic snap fields which are all in nt
            return copySnapBytes(nt, maxOid);
        }
        // need to copy rt since fields will be overridden from ntBytes
        final byte[] rtCopy = copySnapBytes(rt, maxOid);
        // nt is only the copy source and will not be modified during merge, so no need to copy
        final byte[] ntBytes = getSnapBytes(nt, maxOid);
        synchronized (this.fieldDataMerger) {
            final byte[] merged = this.fieldDataMerger.merge(
                    new BufferFieldData(rtCopy), new BufferFieldData(ntBytes)
            );
            return (merged != null) ? merged : rtCopy;
        }
    }

    /**
     * If this component has a reference to a delayProvider and the delay for data is 0, then
     * the delayed data is stored in the realtime snap as well, so we return true regardless of
     * the realtime parameter. Otherwise, we return realtime.
     */
    private Boolean isRealtime(FeedData data, boolean realtime) {
        return DelayProviderUtil.isRealtime(this.delayProvider, data, realtime);
    }

    @Override
    public SymbolSortResponse getSortedSymbols(SymbolSortRequest request) {
        final Sample sample = mayStartSample(this.meterRegistry);
        final StringBuilder sb = getBuilderForAccessLog("Sort", 60, request.getClientInfo());

        try {
            SymbolSortResponse symbolSortResponse = doGetSortedSymbols(request);

            if (sb != null) {
                sb.append(";").append(request.getItems().size());
                logAccess(sb);
            }

            return symbolSortResponse;
        } catch (Throwable t) {
            this.logger.error("<getSortedSymbols> failed", t);
            throw new RuntimeException("<getSortedSymbols> failed", t);
        } finally {
            mayStopSample(IntradayServer.class, this.meterRegistry,
                sample, SymbolSortRequest.class);
        }
    }

    private SymbolSortResponse doGetSortedSymbols(SymbolSortRequest request) {
        final List<SymbolSortResponse.Item> result
                = new ArrayList<>(request.getItems().size());

        for (final List<VendorkeyWithDelay> vkeylist : request.getItems()) {
            final SymbolSortResponse.Builder builder
                    = new SymbolSortResponse.Builder(request.getComparator());

            for (final VendorkeyWithDelay vd : vkeylist) {
                final SnapRecord snap = getSnap(vd.getVendorkey(), vd.isRealtime());
                if (snap != null) {
                    builder.update(vd.getVendorkey(), snap);
                }
            }

            result.add(builder.build());
        }

        return new SymbolSortResponse(result);
    }

    private SnapRecord getSnap(String vendorkey, boolean realtime) {
        final FeedData feedData = getFeedData(vendorkey);
        if (feedData == null) {
            return null;
        }
        synchronized (feedData) {
            return getSnap(feedData, realtime, 0, 0);
        }
    }
}
