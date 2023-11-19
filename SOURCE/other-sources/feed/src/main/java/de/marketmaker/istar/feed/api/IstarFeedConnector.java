/*
 * IstarFeedConnector.java
 *
 * Created on 17.02.2005 13:44:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;


import dev.infrontfinance.dm.proto.DmChicago.SnapFieldsReq;
import dev.infrontfinance.dm.proto.DmChicago.SnapFieldsResp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteLookupFailureException;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.feed.history.TickHistoryRequest;
import de.marketmaker.istar.feed.history.TickHistoryResponse;
import de.marketmaker.istar.feed.tick.TickRecordImpl;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.instrument.InstrumentUtil;

import static de.marketmaker.istar.common.util.DateUtil.*;

/**
 * Provider methods to request feed-related information such as intraday snap data, tick data,
 * and also vwd pages.
 * <p>
 * Querying tick records for several days or months may result in a huge response message.
 * This connector splits multiday tick requests into several daily tick requests,
 * so that the rmi transfer objects do not become too large.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IstarFeedConnector implements FeedAndTickConnector, InitializingBean {
    /**
     * Yesterday's ticks will be written at 00:15 and are accessible by the IntradayServer until
     * 00:45, so this is a good time for when the TickServer should take over:
     */
    private static final LocalTime HALF_PAST_MIDNIGHT = new LocalTime(0, 30);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // TODO: implement caching for opra data
    private FeedConnector intradayOpraServer;   // current day

    private FeedConnector intradayServer;       // current day

    private TickConnector tickServer;    // history backend

    public IstarFeedConnector() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // HACK: dummy request to check if the queue is available...
            if (intradayOpraServer != null) {
                logger.info("checking Opra availability...");
                intradayOpraServer.getTypesForVwdcodes(new TypedVendorkeysRequest());
                logger.info("...found queue");
            }
        } catch (RemoteLookupFailureException ex) {
            logger.info("...queue not available");
            intradayOpraServer = null;
        }
    }

    public void setChicagoOpraServer(FeedConnector intradayOpraServer) {
        this.intradayOpraServer = intradayOpraServer;
    }

    public void setChicagoServer(FeedConnector intradayServer) {
        this.intradayServer = intradayServer;
    }

    public void setIntradayServer(FeedConnector intradayServer) {
        this.intradayServer = intradayServer;
    }

    public void setTickServer(TickConnector tickServer) {
        this.tickServer = tickServer;
    }

    public IntradayResponse getIntradayData(IntradayRequest request) {
        final int latestTickserverDay
                = DateUtil.getDate(new LocalTime().isBefore(HALF_PAST_MIDNIGHT) ? -2 : -1);
        final int[] ticksFromTo = getTicksFromTo(request);

        final boolean withIntradayTicks = ticksFromTo[1] > latestTickserverDay;
        final boolean withTickServerTicks = ticksFromTo[0] <= latestTickserverDay;

        IntradayResponse response = getIntradayResponse(request, withIntradayTicks);

        if (!withTickServerTicks || (response != null && !response.isValid())) {
            return response;
        }

        final IntradayResponse tickResponse = getTickResponse(request);
        if (response == null || !tickResponse.isValid()) {
            return tickResponse;
        }

        response.mergeTicksFrom(tickResponse);
        return response;
    }

    private int[] getTicksFromTo(IntradayRequest request) {
        final int[] result = new int[] { Integer.MAX_VALUE, 0};
        for (IntradayRequest.Item item : request.getItems()) {
            if (item.isWithTicks()) {
                result[0] = Math.min(result[0], item.getTicksFrom());
                result[1] = Math.max(result[1], item.getTicksTo());
            }
        }
        return result;
    }

    // return true if this is a single request for opra and opra backend is available
    private boolean isForOpraBackend(IntradayRequest request) {
        if (intradayOpraServer == null) {
            return false;
        }
        if (request.size() != 1) {
            return false;
        }
        final List<IntradayRequest.Item> items = request.getItems();
        final VendorkeyVwd vendorkey = VendorkeyVwd.getInstance(items.get(0).getVendorkey());
        return InstrumentUtil.isOPRAMarket(vendorkey.getMarketName().toString());
    }

    private IntradayResponse getIntradayResponse(IntradayRequest request, boolean withTicks) {
        // HACK:
        // re-route single opra requests to the opra intraday backend
        if (isForOpraBackend(request)) {
            return this.intradayOpraServer.getIntradayData(request);
        }

        if (this.intradayServer == null) {
            return getInvalidIntradayResponse();
        }

        try {
            if (withTicks && request.size() > 1) {
                return getIntradayWithTicks(request);
            }
            else {
                return this.intradayServer.getIntradayData(request);
            }
        }
        catch (Exception e) {
            this.logger.error("<getIntradayResponse> failed", e);
            return getInvalidIntradayResponse();
        }
    }

    private IntradayResponse getIntradayWithTicks(IntradayRequest request) {
        final IntradayResponse response = new IntradayResponse();

        for (final IntradayRequest.Item item : request.getItems()) {
            final IntradayRequest singleKeyRequest = new IntradayRequest();
            singleKeyRequest.setTickDataFullAccess(request.isTickDataFullAccess());
            singleKeyRequest.add(item);

            final IntradayResponse singleKeyResponse = this.intradayServer.getIntradayData(singleKeyRequest);
            final IntradayResponse.Item result = singleKeyResponse.getItem(item.getVendorkey());

            if (result != null) {
                response.add(result);
            }
        }

        return response;
    }

    private IntradayResponse getTickResponse(IntradayRequest request) {
        if (this.tickServer == null) {
            return getInvalidIntradayResponse();
        }

        try {
            final IntradayResponse response = new IntradayResponse();

            for (final IntradayRequest.Item item : request.getItems()) {
                final TickRecordImpl tr = getTicks(tickServer, item, request.isTickDataFullAccess());
                if (!tr.getItems().isEmpty()) {
                    response.add(createResponseItem(item, tr));
                }
            }

            return response;
        }
        catch (Exception e) {
            this.logger.error("<getIntradayResponse> failed", e);
            return getInvalidIntradayResponse();
        }
    }

    private TickRecordImpl getTicks(BaseFeedConnector tickServer, IntradayRequest.Item item, final boolean isFullAccess) {
        final LocalDate to = toDate(item);
        LocalDate date = fromDate(item);

        TickRecordImpl result = new TickRecordImpl();

        while (!date.isAfter(to)) {
            final IntradayResponse.Item responseItem
                    = requestTicks(tickServer, item.getVendorkey(), toYyyyMmDd(date), isFullAccess);

            if (responseItem != null) {
                result = (TickRecordImpl) result.merge(responseItem.getTickRecord());
            }

            date = date.plusDays(1);
        }
        return result;
    }

    private LocalDate toDate(IntradayRequest.Item item) {
        return min(yyyyMmDdToLocalDate(item.getTicksTo()), new LocalDate().minusDays(1));
    }

    private LocalDate fromDate(IntradayRequest.Item item) {
        return yyyyMmDdToLocalDate(item.getTicksFrom());
    }

    private IntradayResponse.Item requestTicks(BaseFeedConnector tickServer,
            final String vendorkey, int day, final boolean isFullAccess) {
        final IntradayRequest request = new IntradayRequest();
        request.setTickDataFullAccess(isFullAccess);

        final IntradayRequest.Item item = new IntradayRequest.Item(vendorkey, true);
        request.add(item);
        item.setRetrieveTicks(day);

        final IntradayResponse response = tickServer.getIntradayData(request);
        return response.getItem(vendorkey);
    }

    private IntradayResponse.Item createResponseItem(IntradayRequest.Item item, TickRecordImpl tr) {
        final IntradayResponse.Item result
                = new IntradayResponse.Item(item.getVendorkey(), item.isRealtime());
        result.setTickRecord(tr);
        return result;
    }

    private IntradayResponse getInvalidIntradayResponse() {
        final IntradayResponse result = new IntradayResponse();
        result.setInvalid();
        return result;
    }

    public PageResponse getPage(PageRequest request) {
        if (this.intradayServer == null) {
            return getInvalidPageResponse();
        }

        try {
            return this.intradayServer.getPage(request);
        }
        catch (Exception e) {
            this.logger.error("<getPage> failed", e);
            return getInvalidPageResponse();
        }
    }

    private PageResponse getInvalidPageResponse() {
        final PageResponse result = new PageResponse();
        result.setInvalid();
        return result;
    }

    public SymbolSortResponse getSortedSymbols(SymbolSortRequest request) {
        if (this.intradayServer == null) {
            return getInvalidSymbolResponse();
        }

        try {
            return this.intradayServer.getSortedSymbols(request);
        }
        catch (Exception e) {
            this.logger.error("<getSortedSymbols> failed", e);
            return getInvalidSymbolResponse();
        }
    }

    @Override
    public TypedVendorkeysResponse getTypesForVwdcodes(TypedVendorkeysRequest request) {
        TypedVendorkeysResponse response = this.intradayServer.getTypesForVwdcodes(request);
        /*
        // TODO: this is a hack and needs refactoring so we route opra to the correct intradayServer
        if (intradayOpraServer != null
                && (!response.isValid() || response.getResult().isEmpty())) {
            response = this.intradayOpraServer.getTypesForVwdcodes(request);
        }
        */
        return response;
    }

    @Override
    public VendorkeyListResponse getVendorkeys(VendorkeyListRequest request) {
        return this.intradayServer.getVendorkeys(request);
    }

    @Override
    public SnapFieldsResp getSnapFields(SnapFieldsReq req) {
        return this.intradayServer.getSnapFields(req);
    }

    @Override
    public TickHistoryResponse getTickHistory(TickHistoryRequest req) {
        return this.tickServer.getTickHistory(req);
    }

    private SymbolSortResponse getInvalidSymbolResponse() {
        final SymbolSortResponse response = new SymbolSortResponse();
        response.setInvalid();
        return response;
    }

}
