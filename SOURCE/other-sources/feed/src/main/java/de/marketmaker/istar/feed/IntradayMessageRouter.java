/*
 * IntradayMessageRouter.java
 *
 * Created on 02.03.2005 16:28:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import static de.marketmaker.istar.feed.util.FeedMetricsSupport.mayStartSample;
import static de.marketmaker.istar.feed.util.FeedMetricsSupport.mayStopSample;

import com.google.common.collect.ImmutableSet;
import com.rabbitmq.client.ShutdownSignalException;
import de.marketmaker.istar.common.util.ByteString;
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
import de.marketmaker.istar.feed.util.ProtoUtil;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddressImpl;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcConnectionManager;
import de.marketmaker.itools.amqprpc.impl.AmqpRpcClient;
import de.marketmaker.itools.amqprpc.impl.AmqpRpcClientConnectionListener;
import de.marketmaker.itools.amqprpc.impl.AmqpRpcServer;
import de.marketmaker.itools.amqprpc.impl.AmqpRpcServerConnectionListener;
import de.marketmaker.itools.amqprpc.impl.WireLevelServiceProvider;
import dev.infrontfinance.dm.proto.DmChicago.IntIds;
import dev.infrontfinance.dm.proto.DmChicago.SnapFieldsReq;
import dev.infrontfinance.dm.proto.DmChicago.SnapFieldsReq.Builder;
import dev.infrontfinance.dm.proto.DmChicago.SnapFieldsResp;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer.Sample;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.util.SerializationUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class IntradayMessageRouter implements FeedConnector, WireLevelServiceProvider,
    FeedMarketChangeListener, InitializingBean, DisposableBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // the LOCAL intradayServer
    private FeedConnector intradayServer;

    /** Connection manager to handle all custome AMQP connections */
    private AmqpRpcConnectionManager amqpRpcConnectionManager;

    /** Settings for queues this process provides data on */
    private final AmqpRpcServer.Settings marketQueueServerSettings = new AmqpRpcServer.Settings();

    /** Settings for queues this process wants to forward requests to it cannot serve */
    private final AmqpRpcClient.Settings rpcClientSettings = new AmqpRpcClient.Settings();

    /** Used to register a this instance as a change listener and create initial queues after start-up */
    private FeedMarketRepository feedMarketRepository;

    private String fixedLocalMarketsString = null;

    /**
     *  Set of markets that will always be served locally.
     *  This targets mainly markets know to not be available in this environment (i.e. OPRA etc)
     *  and mimicks the behavior of non-cPMQ instances.
     */
    private Set<String> fixedLocalMarkets = ImmutableSet.of();

    /** RWLock to guard the localVendors map */
    private final ReentrantReadWriteLock rwlLocalVendors = new ReentrantReadWriteLock(true);

    /** Map of all queues this process provides data for */
    @GuardedBy("this.rwlLocalVendors")
    private final Map<String, AmqpRpcServerConnectionListener> localVendors = new HashMap<>();

    /** RWLock to guard the remoteVendors map */
    private final ReentrantReadWriteLock rwlRemoteVendors = new ReentrantReadWriteLock(true);

    /** Map of all queues this process has forwarded requests to */
    @GuardedBy("this.rwlRemoteVendors")
    private final Map<String, AmqpRpcClientConnectionListener> remoteVendors = new HashMap<>();

    private MeterRegistry meterRegistry;

    private DistributionSummary snapFieldsRespDist;

    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.snapFieldsRespDist = DistributionSummary
            .builder("snap.fields.resp.size")
            .publishPercentileHistogram()
            .minimumExpectedValue(64D)
            .maximumExpectedValue((double) (1 << 15)) // 32K
            .register(this.meterRegistry);
    }

    public void setIntradayServer(FeedConnector intradayServer) {
        this.intradayServer = intradayServer;
    }

    public void setAmqpRpcConnectionManager(AmqpRpcConnectionManager amqpRpcConnectionManager) {
        this.amqpRpcConnectionManager = amqpRpcConnectionManager;
    }

    public void setFeedMarketRepository(FeedMarketRepository feedMarketRepository) {
        this.feedMarketRepository = feedMarketRepository;
    }

    public void setFixedLocalMarketsString(String fixedLocalMarkets) {
        this.fixedLocalMarketsString = fixedLocalMarkets;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.marketQueueServerSettings.setNumberOfMessagesToPrefetch(1);
        if (this.fixedLocalMarketsString != null) {
            this.fixedLocalMarkets = ImmutableSet.copyOf(this.fixedLocalMarketsString.split(",\\s*"));
        }
        this.serveExistingMarkets();
    }

    /**
     * Create a consumer-connection (i.e. server) for all existing markets and
     * register as a change listener
     */
    @ManagedOperation(description = "Connect Per-Market-Queues to AMQP")
    public void serveExistingMarkets() {
        this.feedMarketRepository.addChangeListener(this);
        this.rwlLocalVendors.writeLock().lock();
        try {
            this.feedMarketRepository.getMarkets()
                    .stream()
                    .map(FeedMarket::getBaseName)
                    .map(ByteString::toString)
                    .forEach(this::addServableMarket);
        } finally {
            this.rwlLocalVendors.writeLock().unlock();
        }
    }

    @Override
    @ManagedOperation(description = "Disconnect Per-Market-Queues from AMQP")
    public void destroy() {
        this.feedMarketRepository.removeChangeListener(this);
        this.rwlLocalVendors.writeLock().lock();
        try {
            this.localVendors.values().forEach(AmqpRpcServerConnectionListener::shutdownRpcServer);
            this.localVendors.clear();
        } finally {
            this.rwlLocalVendors.writeLock().unlock();
        }
    }

    /**
     * Get the appopriate AMQP connection for this vendorkey to serve the request
     * @param vendorkey Vendorkey in the request
     * @return <code>null</code> in case the request should be served locally or an instance
     * of AmqpRpcClientConnectionListener to send the request to
     */
    private AmqpRpcClientConnectionListener getIntradayServer(String vendorkey) {
        return getAmqpRpcClientConnectionListenerForMarket(VendorkeyUtils.getMarketName(vendorkey));
    }

    /**
     * Get the appopriate AMQP connection for this market to serve the request
     * @param market Market the connection is required for
     * @return <code>null</code> in case the request should be served locally or an instance
     * of AmqpRpcClientConnectionListener to send the request to
     */
    private AmqpRpcClientConnectionListener getAmqpRpcClientConnectionListenerForMarket(String market) {
        if (market == null) {
            return null;
        }
        this.rwlLocalVendors.readLock().lock();
        try {
            if (this.localVendors.containsKey(market) || this.fixedLocalMarkets.contains(market)) {

                // We mis-use null as a marker for requests that need to be served locally
                return null;
            }
        } finally {
            this.rwlLocalVendors.readLock().unlock();
        }

        return getOrCreateAmqpRpcClientConnectionListener(market);
    }

    /**
     * Get an existing AMQP connection for this market or create a new connection.
     * This method should preferrably only be used if it has been determined that this request should be forwarded.
     * @param market Market to get or create a connection to
     * @return An instance of AmqpRpcClientConnectionListener that can be requested for data concerning the given market
     */
    private AmqpRpcClientConnectionListener getOrCreateAmqpRpcClientConnectionListener(String market) {

        // Most of the times outgoing connections are already present so it is advantageous to do this extra
        // Map.get() here.
        AmqpRpcClientConnectionListener amqpRpcClientConnectionListener;
        this.rwlRemoteVendors.readLock().lock();
        try {
            if ((amqpRpcClientConnectionListener = this.remoteVendors.get(market)) != null) {
                return amqpRpcClientConnectionListener;
            }
        } finally {
            this.rwlRemoteVendors.readLock().unlock();
        }

        this.rwlRemoteVendors.writeLock().lock();
        try {
            // Use computeIfAbsent for the case where another thread was quicker
            amqpRpcClientConnectionListener = this.remoteVendors.computeIfAbsent(market, m -> {

                AmqpRpcAddressImpl ara = new AmqpRpcAddressImpl();
                ara.setRequestQueue("istar.chicago3.intraday." + market);
                AmqpRpcClientConnectionListener amqpRpcClient = new AmqpRpcClientConnectionListener(ara, this.rpcClientSettings);

                this.amqpRpcConnectionManager.addConnectionListener(amqpRpcClient);
                return amqpRpcClient;
            });
        } finally {
            this.rwlRemoteVendors.writeLock().unlock();
        }

        return amqpRpcClientConnectionListener;
    }

    public IntradayResponse getIntradayData(IntradayRequest request) {
        final Map<AmqpRpcClientConnectionListener, IntradayRequest> requests = toServerSpecificRequests(request);

        final IntradayResponse result = new IntradayResponse();

        requests.entrySet()
                .parallelStream()
                .peek(e -> {
                    if (logger.isDebugEnabled()) {
                        String vendorkey = e.getValue().getItems().get(0).getVendorkey();
                        if (e.getKey() == null) {
                            logger.debug("Serving IntradayRequest for " + vendorkey + " locally");
                        } else {
                            logger.debug("Redirecting IntradayRequest for " + vendorkey + " to RabbitMQ");
                        }
                    }
                })
                .map(e -> {
                    if (e.getKey() == null) {
                        return this.intradayServer.getIntradayData(e.getValue());
                    } else {
                        Object answer = callRemote(e.getKey(), e.getValue());
                        if (answer != null) {
                            return (IntradayResponse) answer;
                        } else {
                            // If we got any error from remote try locally as fallback
                            return this.intradayServer.getIntradayData(e.getValue());
                        }
                    }
                })
                .sequential()
                .forEach(subResult -> this.combine(result, subResult));
        return result;
    }

    private void combine(IntradayResponse base, IntradayResponse mergee) {
        for (IntradayResponse.Item subItem : mergee) {
            IntradayResponse.Item resultItem = base.getItem(subItem.getVendorkey());
            if (resultItem != null) {
                resultItem.merge(subItem);
            }
            else {
                base.add(subItem);
            }
        }
    }

    /**
     * Split an IntradayRequest into markets and map them with an appropriate local/AMQP connection as key
     * @param r Request to split
     * @return Map with local (i.e. <code>null</code>) or remote connection mapping to a sub-request instance
     */
    private Map<AmqpRpcClientConnectionListener, IntradayRequest> toServerSpecificRequests(IntradayRequest r) {
        final Map<AmqpRpcClientConnectionListener, IntradayRequest> result = new IdentityHashMap<>();
        for (IntradayRequest.Item item : r.getItems()) {
            AmqpRpcClientConnectionListener server = getIntradayServer(item.getVendorkey());
            IntradayRequest subRequest = result.computeIfAbsent(server, s -> {
                IntradayRequest ir = new IntradayRequest();
                ir.setTickDataFullAccess(r.isTickDataFullAccess());
                return ir;
            });
            subRequest.add(item);
        }
        return result;
    }

    public PageResponse getPage(PageRequest request) {
        if (this.intradayServer == null) {
            return getInvalidPageResponse();
        }

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Serving PageRequest for " + request.getPagenumber() + " locally");
            }
            final PageResponse page = this.intradayServer.getPage(request);
            if (page != null) {
                ensureTypedKeys(page);
                return page;
            }
        } catch (Exception e) {
            this.logger.warn("<getPage> failed for " + request, e);
        }
        return getInvalidPageResponse();
    }

    public VendorkeyListResponse getVendorkeys(VendorkeyListRequest request) {
        final AmqpRpcClientConnectionListener arc = this.getAmqpRpcClientConnectionListenerForMarket(request.getMarket());

        try {
            if (arc == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Serving VendorkeyListRequest for " + request.getMarket() + " locally");
                }
                return this.intradayServer.getVendorkeys(request);
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Forwarding VendorkeyListRequest for " + request.getMarket() + " to RabbitMQ");
                }
                Object answer = this.callRemote(arc, request);
                if (answer != null) {
                    return (VendorkeyListResponse) answer;
                } else {
                    // If we got any error from remote fall back to trying locally
                    return this.intradayServer.getVendorkeys(request);
                }
            }
        } catch (Exception e) {
            this.logger.warn("<getVendorkeys> failed for " + request, e);
        }
        return VendorkeyListResponse.createInvalid();
    }

    @Override
    public SnapFieldsResp getSnapFields(SnapFieldsReq req) {
        final AmqpRpcClientConnectionListener arc =
            this.getAmqpRpcClientConnectionListenerForMarket(req.getMarket());
        try {
            if (arc == null) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getSnapFields> serving {}, {} locally",
                        req.getInfo().getClientInfo(), req.getMarket());
                }
                return this.intradayServer.getSnapFields(req);
            } else {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getSnapFields> forwarding {}, {}",
                        req.getInfo().getClientInfo(), req.getMarket());
                }
                return (SnapFieldsResp) this.callRemote(arc, req);
            }
        } catch (Exception e) {
            this.logger.warn("<getSnapFields> failed for {}, {}",
                req.getInfo().getClientInfo(), req.getMarket(), e);
        }

        return SnapFieldsResp.newBuilder().setInfo(ProtoUtil.getRespInfo(false)).build();
    }

    @ManagedOperation
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "vwdCode",
            description = "vwd code, e.g. 710000.ETR"),
        @ManagedOperationParameter(name = "orderIds",
            description = "comma separated vwd field order ids, e.g. 29,34,35"),
    })
    public String getSnapFieldsJmx(String vwdCode, String orderIds) {
        try {
            final ByteString vwdcode = new ByteString(vwdCode);
            final VendorkeyVwd vendorKey = VendorkeyVwd.getInstance(vwdcode, 1);
            final Builder builder = SnapFieldsReq.newBuilder();
            builder.setInfo(ProtoUtil.getReqInfo());
            builder.setMarket(vendorKey.getMarketName().toString());

            final ByteBuffer bb = ByteBuffer.allocate(vwdcode.length() + 1);
            vwdcode.writeTo(bb, ByteString.LENGTH_ENCODING_BYTE);
            builder.setVwdCodes(com.google.protobuf.ByteString.copyFrom(bb.array()));
            builder.setOrderList(IntIds.newBuilder()
                .addAllIds(Arrays.stream(orderIds.split(","))
                    .map(Integer::parseInt)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList()))
                .build());
            builder.setRealtime(true);
            return String.valueOf(getSnapFields(builder.build()));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private void ensureTypedKeys(PageResponse page) {
        final Map<String, String> typedKeys = doGetTypedKeys(page.getVendorkeys(), false).getResult();
        if (!typedKeys.isEmpty()) {
            page.replaceVwdcodesWithVendorkeys(typedKeys);
        }
    }

    private TypedVendorkeysResponse doGetTypedKeys(Iterable<String> symbols, boolean mayForward) {
        final Map<AmqpRpcClientConnectionListener, TypedVendorkeysRequest> requests = getTypedVendorkeysRequests(symbols);
        if (requests.isEmpty()) {
            return new TypedVendorkeysResponse(Collections.emptyMap());
        }

        if (requests.size() == 1) {
            Map.Entry<AmqpRpcClientConnectionListener, TypedVendorkeysRequest> entry = requests.entrySet().iterator().next();
            String symbols2 = StreamSupport.stream(symbols.spliterator(), false)
                    .collect(Collectors.joining(","));
            if (entry.getKey() == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Serving TypedVendorkeysRequest for " + symbols2 + " locally");
                }
                return this.intradayServer.getTypesForVwdcodes(entry.getValue());
            }
            else if (mayForward) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Forwarding TypedVendorkeysRequest for " + symbols2 + " to RabbitMQ");
                }
                Object answer = this.callRemote(entry.getKey(), entry.getValue());
                if (answer != null) {
                    return (TypedVendorkeysResponse) answer;
                } else {
                    // If we got any error from remote fall back to trying locally
                    return this.intradayServer.getTypesForVwdcodes(entry.getValue());
                }
            }
        }

        final Map<String, String> typedKeys = new HashMap<>();
        for (Map.Entry<AmqpRpcClientConnectionListener, TypedVendorkeysRequest> entry : requests.entrySet()) {
            TypedVendorkeysResponse response;
            String symbols2 = StreamSupport.stream(symbols.spliterator(), false)
                    .collect(Collectors.joining(","));
            if (entry.getKey() == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Serving TypedVendorkeysRequest for " + symbols2 + " locally");
                }
                response = this.intradayServer.getTypesForVwdcodes(entry.getValue());
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Forwarding TypedVendorkeysRequest for " + symbols2 + " to RabbitMQ");
                }
                response = (TypedVendorkeysResponse) this.callRemote(entry.getKey(), entry.getValue());
                if (response == null) {
                    // If we get any error from remote fall back to trying locally
                    response = this.intradayServer.getTypesForVwdcodes(entry.getValue());
                }
            }
            if (response.isValid()) {
                for (String vwdcode : entry.getValue().getVwdcodes()) {
                    final String typed = response.getTyped(vwdcode);
                    if (typed != null) {
                        typedKeys.put(vwdcode, typed);
                    }
                }
            }
        }
        return new TypedVendorkeysResponse(typedKeys);
    }

    private PageResponse getInvalidPageResponse() {
        final PageResponse result = new PageResponse();
        result.setInvalid();
        return result;
    }

    public SymbolSortResponse getSortedSymbols(SymbolSortRequest request) {
        final Set<AmqpRpcClientConnectionListener> connectors = getServersForSymbolSortRequests(request);
        if (connectors.size() == 1) {
            AmqpRpcClientConnectionListener fc = connectors.iterator().next();
            if (fc == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Serving SymbolSortRequest for " + request.getItems().get(0).get(0).getVendorkey() + " locally");
                }
                return this.intradayServer.getSortedSymbols(request);
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Forwarding SymbolSortRequest for " + request.getItems().get(0).get(0).getVendorkey() + " to RabbitMQ");
                }
                Object answer = this.callRemote(fc, request);
                if (answer != null) {
                    return (SymbolSortResponse) answer;
                } else {
                    // If we get any error from remote fall back to trying locally
                    return this.intradayServer.getSortedSymbols(request);
                }
            }
        }

        SymbolSortResponse result = null;
        for (AmqpRpcClientConnectionListener connector : connectors) {
            SymbolSortResponse tmp;
            try {
                if (connector == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Serving SymbolSortRequest for " + request.getItems().get(0).get(0).getVendorkey() + " locally");
                    }
                    tmp = this.intradayServer.getSortedSymbols(request);
                }
                else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Forwarding SymbolSortRequest for " + request.getItems().get(0).get(0).getVendorkey() + " to RabbitMQ");
                    }
                    Object answer = this.callRemote(connector, request);
                    if (answer != null) {
                        tmp = (SymbolSortResponse) answer;
                    } else {
                        tmp = this.intradayServer.getSortedSymbols(request);
                    }
                }
            } catch (Exception e) {
                this.logger.error("<getSortedSymbols> failed", e);
                return getInvalidSymbolResponse();
            }
            if (!tmp.isValid()) {
                return tmp;
            }
            if (result == null) {
                result = tmp;
            } else {
                result.merge(tmp, request.getComparator());
            }
        }
        return result != null ? result : getInvalidSymbolResponse();
    }

    private Set<AmqpRpcClientConnectionListener> getServersForSymbolSortRequests(SymbolSortRequest request) {
        final Set<AmqpRpcClientConnectionListener> result = new HashSet<>();
        for (List<VendorkeyWithDelay> list : request.getItems()) {
            for (VendorkeyWithDelay vendorkeyWithDelay : list) {
                AmqpRpcClientConnectionListener connector = getIntradayServer(vendorkeyWithDelay.getVendorkey());
                result.add(connector);
            }
        }
        return result;
    }

    public TypedVendorkeysResponse getTypesForVwdcodes(TypedVendorkeysRequest request) {
        return doGetTypedKeys(request.getVwdcodes(), true);
    }

    private Map<AmqpRpcClientConnectionListener, TypedVendorkeysRequest> getTypedVendorkeysRequests(Iterable<String> symbols) {
        final Map<AmqpRpcClientConnectionListener, TypedVendorkeysRequest> result = new IdentityHashMap<>();
        for (String vwdcode : symbols) {
            if (VendorkeyUtils.isWithType(vwdcode)) {
                continue;
            }
            AmqpRpcClientConnectionListener server = getIntradayServer(vwdcode);
            TypedVendorkeysRequest request = result.get(server);
            if (request == null) {
                request = new TypedVendorkeysRequest();
                result.put(server, request);
            }
            request.add(vwdcode);
        }
        return result;
    }

    private SymbolSortResponse getInvalidSymbolResponse() {
        final SymbolSortResponse response = new SymbolSortResponse();
        response.setInvalid();
        return response;
    }

    @Override
    public void onChange(FeedMarket market, ChangeType type) {
        switch (type) {
            case CREATED:
                this.addServableMarket(market.getBaseName().toString());
                break;

            case REMOVED:
                this.removeServeableMarket(market.getBaseName().toString());
                break;
        }
    }

    private void addServableMarket(String market) {
        this.rwlLocalVendors.readLock().lock();
        try {
            AmqpRpcServerConnectionListener marketServer = this.localVendors.get(market);
            if (marketServer != null) {
                if (this.amqpRpcConnectionManager.everythingOk() && marketServer.getRpcServer().get().everythingOk()) {
                    return;
                }

                // Try some repair if possible - since we are messing with connections we get the writeLock
                this.rwlLocalVendors.readLock().unlock();
                this.rwlLocalVendors.writeLock().lock();
                try {
                    this.rwlLocalVendors.readLock().lock();
                    // Basic connection broken
                    if (!this.amqpRpcConnectionManager.everythingOk()) {
                        logger.warn("<addServableMarket> amqpRpcConnectionManager reports not OK. Reconnecting.");
                        // TODO: Better handling failing reconnect?
                        while (true) {
                            try {
                                this.amqpRpcConnectionManager.tryToRecover();
                                return;
                            } catch (Exception e) {
                                logger.error("<addServableMarket> Unable to reconnect: " + e.getMessage() + ": " + e.getCause().getMessage());
                            }
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }
                    // Basic connection is OK, check for market specific queue
                    else if (!marketServer.getRpcServer().get().everythingOk()) {
                        logger.warn("<addServableMarket> RPC server for " + market + " reports not OK. Reconnecting.");
                        marketServer.forceCreateNewRpcServer(this.amqpRpcConnectionManager.getRunningConnection());
                    }
                } finally {
                    this.rwlLocalVendors.writeLock().unlock();
                }
            } else { // No connection for this market in the map
                this.rwlLocalVendors.readLock().unlock();
                this.rwlLocalVendors.writeLock().lock();
                try {
                    this.rwlLocalVendors.readLock().lock();

                    // Use computeIfAbsent if someone was quicker
                    this.localVendors.computeIfAbsent(market, m -> {

                        AmqpRpcAddressImpl ara = new AmqpRpcAddressImpl();
                        ara.getSettings().setQueueAutoDelete(true);
                        ara.setRequestQueue("istar.chicago3.intraday." + market);

                        AmqpRpcServerConnectionListener newMarketServer = new AmqpRpcServerConnectionListener(ara, this.marketQueueServerSettings, this);
                        this.amqpRpcConnectionManager.addConnectionListener(newMarketServer);

                        return newMarketServer;
                    });
                } finally {
                    this.rwlLocalVendors.writeLock().unlock();
                }
            }
        } finally {
            this.rwlLocalVendors.readLock().unlock();
        }
    }

    /**
     * Remove the given market from the list of locally available markets and disconnect from AMQP
     * if there was an existing connection.
     * @param market Market to remove
     */
    private void removeServeableMarket(String market) {
        this.rwlLocalVendors.writeLock().lock();
        try {
            AmqpRpcServerConnectionListener inMap = this.localVendors.remove(market);
            if (inMap != null) {
                logger.info("<removeServeableMarket> FeedMarketRepository notified about removal of " + market + ". Disconnecting.");
                inMap.shutdownRpcServer();
            }
        } finally {
            this.rwlLocalVendors.writeLock().unlock();
        }
    }

    /**
     * Try forwarding a request that cannot be served locally.
     *
     * @param remote Remote connection to forward to
     * @param request Request object to forward
     * @return Response object or <code>null</code> in case of an error
     */
    private Object callRemote(AmqpRpcClientConnectionListener remote, Object request) {
        final Sample sample = mayStartSample(this.meterRegistry);
        try {
            byte[] bytes = remote.getRpcClient().get()
                .sendAndWaitForReply(SerializationUtils.serialize(request), 10000);
            if (this.snapFieldsRespDist != null && request instanceof SnapFieldsReq) {
                this.snapFieldsRespDist.record(bytes.length);
            }
            return SerializationUtils.deserialize(bytes);
        } catch (RemoteAccessException | ShutdownSignalException e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.warn("<callRemote> Exception while forwarding request: {}", request, e);
            }
        } finally {
            mayStopSample(IntradayMessageRouter.class, this.meterRegistry,
                sample, request.getClass());
        }

        return null;
    }

    @Override
    public byte[] call(byte[] bytes) {
        Object req = SerializationUtils.deserialize(bytes);
        Serializable resp;
        if (req instanceof IntradayRequest) {
            if (logger.isDebugEnabled()) {
                logger.debug("Serving IntradayRequest for {} received via RabbitMQ",
                    ((IntradayRequest) req).getItems().get(0).getVendorkey());
            }
            resp = this.intradayServer.getIntradayData((IntradayRequest) req);
        } else if (req instanceof SymbolSortRequest) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Serving SymbolSortRequest for {} received via RabbitMQ",
                    ((SymbolSortRequest) req).getItems().get(0).get(0).getVendorkey());
            }
            resp = this.intradayServer.getSortedSymbols((SymbolSortRequest) req);
        } else if (req instanceof TypedVendorkeysRequest) {
            if (logger.isDebugEnabled()) {
                String symbols = StreamSupport.stream(
                        ((TypedVendorkeysRequest) req).getVwdcodes().spliterator(), false)
                    .collect(Collectors.joining(","));
                logger.debug("Serving TypedVendorkeysRequest for {} received via RabbitMQ",
                    symbols);
            }
            resp = this.intradayServer.getTypesForVwdcodes((TypedVendorkeysRequest) req);
        } else if (req instanceof PageRequest) {
            if (logger.isDebugEnabled()) {
                logger.debug("Serving PageRequest for {} received via RabbitMQ",
                    ((PageRequest) req).getPagenumber());
            }
            resp = this.intradayServer.getPage((PageRequest) req);
        } else if (req instanceof VendorkeyListRequest) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Serving VendorkeyListRequest for {} received via RabbitMQ",
                    ((VendorkeyListRequest) req).getMarket());
            }
            resp = this.intradayServer.getVendorkeys((VendorkeyListRequest) req);
        } else if (req instanceof SnapFieldsReq) {
            final SnapFieldsReq snapFieldsReq = (SnapFieldsReq) req;
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<call> serving SnapFieldsReq {},{}, received via RabbitMQ",
                    snapFieldsReq.getInfo().getClientInfo(), snapFieldsReq.getMarket());
            }

            resp = this.intradayServer.getSnapFields(snapFieldsReq);
        } else {
            throw new UnsupportedOperationException("Invalid request type: " + req.getClass());
        }
        return SerializationUtils.serialize(resp);
    }
}
