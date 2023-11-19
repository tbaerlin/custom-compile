/*
 * MockPushServiceImpl.java
 *
 * Created on 12.12.2016 11:46:16
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import java.math.BigDecimal;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.websocket.Session;

import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.ClassUtils;

import de.marketmaker.istar.domain.data.LiteralSnapField;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecordDefault;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategyFactory;
import de.marketmaker.itools.gwtcomet.comet.server.CometSession;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushChangeRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushChangeResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushData;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushService;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushSessionResponse;
import de.marketmaker.iview.mmgwt.mmweb.server.GwtService;

/**
 * Configure as follows in iview-push web apps applicationConfig.xml file as a replacement for the beans pushService, realtimeRegistrations,
 * neartimeRegistrations, pushBuilder, pushBuilderDelay, periodicPusher, pushRegistry, pushRegistryDelay, entitlementQuoteProvider,
 * entitlementProvider, endOfDayProvider, feedConnector, dataMaintenanceInvoker, dataMaintenanceTrigger.
 *
 * <pre>
 * &lt;bean name=&quot;pushService&quot; class=&quot;de.marketmaker.iview.mmgwt.mmweb.server.push.MockPushServiceImpl&quot;&gt;
 * &lt;property name=&quot;serializationPolicyResolver&quot; ref=&quot;serializationPolicyResolver&quot;/&gt;
 * &lt;property name=&quot;profileProvider" ref="profileProvider&quot;/&gt;
 * &lt;property name=&quot;instrumentProvider" ref="instrumentProvider&quot;/&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * Additionally, replace instrumentProvider (EasytradeInstrumentProvider) with
 * <pre>
 * &lt;bean id=&quot;instrumentProvider&quot;
 * class=&quot;de.marketmaker.istar.merger.provider.InstrumentProviderImpl&quot;&gt;
 * &lt;property name=&quot;instrumentServer&quot; ref=&quot;instrumentServer&quot;/&gt;
 * &lt;property name=&quot;instrumentCache&quot;&gt;
 * &lt;bean class=&quot;org.springframework.cache.ehcache.EhCacheFactoryBean&quot;&gt;
 * &lt;property name=&quot;cacheManager&quot; ref=&quot;cacheMgr&quot;/&gt;
 * &lt;property name=&quot;cacheName&quot; value=&quot;istar.merger.iid2instrument&quot;/&gt;
 * &lt;/bean&gt;
 * &lt;/property&gt;
 * &lt;property name=&quot;symbolCache&quot;&gt;
 * &lt;bean class=&quot;org.springframework.cache.ehcache.EhCacheFactoryBean&quot;&gt;
 * &lt;property name=&quot;cacheManager&quot; ref=&quot;cacheMgr&quot;/&gt;
 * &lt;property name=&quot;cacheName&quot; value=&quot;istar.merger.symbol2iid&quot;/&gt;
 * &lt;/bean&gt;
 * &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * @author mdick
 */
@ManagedResource
@SuppressWarnings({"GwtServiceNotRegistered", "Duplicates"})
public class MockPushServiceImpl extends GwtService implements PushService, CometPushConnect, WebsocketPushConnect, Lifecycle, Runnable {
    private volatile boolean stopped = false;

    private volatile boolean running = false;

    private final Clients clients = new Clients();

    private final Random random = new Random();

    private ProfileProvider profileProvider;

    private InstrumentProvider instrumentProvider;

    private final Map<String, PriceRegistration> symbolToRegistration = Collections.synchronizedMap(new HashMap<>());

    @SuppressWarnings("unused")
    public void setProfileProvider(ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    @SuppressWarnings("unused")
    public void setInstrumentProvider(InstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "vwdId", description = "vwdId"),
            @ManagedOperationParameter(name = "appId", description = "appId"),
            @ManagedOperationParameter(name = "websocket", description = "websocket")
    })
    public String createSessionJmx(String vwdId, String appId, boolean websocket) {
        final PushSessionResponse response = createSession(vwdId, appId, websocket);
        return response.getState().name() + ": " + response.getSessionId();
    }

    @ManagedOperation(description = "stop push for single client")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "sid", description = "session id")
    })
    public void stopPush(String sid) {
        this.clients.stopPush(sid);
    }

    @ManagedOperation(description = "stop push for all current clients")
    public void stopPush() {
        this.clients.stopPush();
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "sid", description = "session id"),
            @ManagedOperationParameter(name = "vwdcode", description = "vwdcode")
    })
    public String registerKeyJmx(String sid, String vwdcode) {
        final PushChangeRequest request = new PushChangeRequest();
        request.setSessionId(sid);
        request.setToRegister(new HashSet<>(Collections.singletonList(vwdcode)));
        final PushChangeResponse response = modifySession(request);
        return response.getState().name();
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "sid", description = "session id"),
            @ManagedOperationParameter(name = "vwdcode", description = "vwdcode")
    })
    public String unregisterKeyJmx(String sid, String vwdcode) {
        final PushChangeRequest request = new PushChangeRequest();
        request.setSessionId(sid);
        request.setToUnregister(new HashSet<>(Collections.singletonList(vwdcode)));
        final PushChangeResponse response = modifySession(request);
        return response.getState().name();
    }

    @ManagedOperation
    public Collection<String> listSessionsJmx() {
        return this.clients.getClients().stream()
                .map((abstractClient) -> String.format("%s (%s)",
                        abstractClient.getId(),
                        abstractClient.getClass().getSimpleName()))
                .collect(Collectors.toList());
    }

    public PushSessionResponse createSession(String vwdId, String appId, boolean websocket) {
        final String clientId = appId + "_" + vwdId + "@" + Long.toHexString(System.currentTimeMillis());
        final PushSessionResponse result = new PushSessionResponse();

        final ProfileRequest request = new ProfileRequest("vwd-ent:ByVwdId", vwdId);
        request.setApplicationId(appId);
        final ProfileResponse response = this.profileProvider.getProfile(request);

        if (!response.isValid()) {
            this.logger.warn("<createSession> no valid profile for " + clientId);
            return result.withState(PushSessionResponse.State.INTERNAL_ERROR);
        }
        final Profile profile = response.getProfile();
        if (!(profile instanceof VwdProfile)) {
            this.logger.error("<createSession> not a VwdProfile for " + clientId);
            return result.withState(PushSessionResponse.State.INTERNAL_ERROR);
        }
        final VwdProfile vwdProfile = (VwdProfile) profile;
        if (!vwdProfile.isWithPush()) {
            this.logger.info("<createSession> no push allowed for but mocking it anyway " + clientId);
        }
        final AbstractClient client = this.clients.createClient(clientId, vwdProfile, websocket);
        client.setPushService(this);
        return result.withSession(client.getId());
    }

    public PushChangeResponse modifySession(PushChangeRequest request) {
        final PushChangeResponse result = new PushChangeResponse();
        final AbstractClient client = this.clients.getClient(request.getSessionId());
        if (client == null) {
            return result.withState(PushChangeResponse.State.NO_SESSION);
        }

        register(client, request, result);
        unregister(client, request);

        return result;
    }

    private void register(AbstractClient client, PushChangeRequest request,
            PushChangeResponse result) {
        final HashSet<String> symbols = request.getToRegister();
        if (symbols == null || symbols.isEmpty()) {
            return;
        }
        final VwdProfile profile = client.getProfile();
        RequestContextHolder.setRequestContext(new RequestContext(profile, MarketStrategyFactory.defaultStrategy()));

        final HashSet<String> invalid = new HashSet<>(symbols);
        final HashSet<String> registered = new HashSet<>();

        final Map<String, Quote> quotes = getQuotes(symbols);
        for (String symbol : symbols) {
            final String vwdcode = isOrderbookSymbol(symbol) ? symbol.substring(1) : symbol;
            Quote quote = quotes.get(vwdcode);
            if (quote == null) {
                continue;
            }

            invalid.remove(symbol);
            registered.add(symbol);
            registerQuote(client, symbol, quote);
        }

        if (!invalid.isEmpty()) {
            this.logger.debug("<register> invalid symbols for " + client.getId() + ": " + invalid);
        }
        if (!registered.isEmpty()) {
            this.logger.debug("<register> for " + client.getId() + ": " + registered);
        }

        result.setInvalid(invalid.isEmpty() ? null : invalid);
        result.setRegistered(registered.isEmpty() ? null : registered);
    }

    private boolean isOrderbookSymbol(String symbol) {
        return symbol.startsWith(PushChangeRequest.ORDERBOOK_PREFIX);
    }

    private void registerQuote(AbstractClient client, String symbol, Quote quote) {
        if (client.isRegistered(symbol)) {
            return;
        }
        client.add(getRegistration(quote), getAllowedFields());
    }

    private BitSet getAllowedFields() {
        final BitSet bitSet = new BitSet();
        bitSet.set(0, VwdFieldDescription.length(), true);
        return bitSet;
    }

    private Registration getRegistration(Quote q) {
        final String key = q.getSymbolVwdcode();
        synchronized (this.symbolToRegistration) {
            final PriceRegistration existing = this.symbolToRegistration.get(key);
            if (existing != null) {
                return existing;
            }

            final PriceRegistration value = new PriceRegistration(q);
            this.symbolToRegistration.put(key, value);
            return value;
        }
    }

    private Map<String, Quote> getQuotes(HashSet<String> symbols) {
        return symbols.stream()
                .map(vwdcode -> instrumentProvider.identifyByVwdcode(vwdcode))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Quote::getSymbolVwdcode, quote -> quote));
    }

    private void unregister(AbstractClient client, PushChangeRequest request) {
        final HashSet<String> symbols = request.getToUnregister();
        if (symbols == null) {
            return;
        }
        for (String symbol : symbols) {
            unregister(client, symbol);
        }
    }

    private void unregister(AbstractClient client, String symbol) {
        client.unregister(symbol);
    }

    public void closeSession(String id) {
        this.clients.removeClient(id);
        this.logger.info("<closeSession> closed " + id);
    }

    public void connect(String sid, CometSession cometSession) {
        final AbstractClient client = getClient(sid);
        if (client == null) {
            throw new IllegalArgumentException("no such client " + sid);
        }
        if (!(client instanceof CometClient)) {
            throw new IllegalStateException("client must be instanceof CometClient! Is " + client.getClass().getSimpleName());
        }
        ((CometClient) client).setSession(cometSession);
    }

    public void connect(String sid, Session session) {
        final AbstractClient client = getClient(sid);
        if (client == null) {
            throw new IllegalArgumentException("no such client " + sid);
        }
        if (!(client instanceof WebsocketClient)) {
            throw new IllegalArgumentException("client must be instanceof WebsocketClient! Is " + client.getClass().getSimpleName());
        }
        ((WebsocketClient) client).setSession(session);
    }

    public void disconnect(String sid) {
        final AbstractClient client = getClient(sid);
        if (client != null) {
            client.nullSession();
        }
    }

    @Override
    public PushData getPushData() {
        return new PushData();
    }

    private AbstractClient getClient(String sid) {
        return this.clients.getClient(sid);
    }

    List<AbstractClient> getClientsWithData() {
        return this.clients.getClientsWithData();
    }

    private final ScheduledExecutorService es = Executors.newScheduledThreadPool(1,
            r -> new Thread(r, ClassUtils.getShortName(MockPushServiceImpl.this.getClass())));

    @Override
    public void start() {
        this.es.schedule(this, 1, TimeUnit.SECONDS);
        this.running = true;
        this.logger.info("<start> done");
    }

    @Override
    public void stop() {
        this.stopped = true;
        this.es.shutdown();
        this.logger.info("<stop> done");
    }

    @Override
    public void run() {
        if (this.stopped) {
            return;
        }
        final long then = System.currentTimeMillis();
        long took = 0;
        try {
            final int numUpdates = push();
            if (numUpdates > 0) {
                flush();
            }
            took = System.currentTimeMillis() - then;

            if (numUpdates > 0 && this.logger.isDebugEnabled()) {
                this.logger.debug("<run> finished in " + took + "ms, #updates=" + numUpdates);
            }
        } catch (Throwable t) {
            this.logger.error("<run> failed", t);
        } finally {
            if (!this.stopped) {
                this.es.schedule(this, Math.max(100, 1000 - took), TimeUnit.MILLISECONDS);
            }
        }
    }

    private void flush() {
        for (AbstractClient client : getClientsWithData()) {
            client.flush();
        }
    }

    private int push() {
        final Collection<? extends Registration> registrations = this.symbolToRegistration.values();
        if (registrations.isEmpty()) {
            return 0;
        }
        for (final Registration r : registrations) {
            r.clearUpdated();
            final BigDecimal price = new BigDecimal(((double) this.random.nextInt(200)) + this.random.nextDouble());

            SnapField priceField = LiteralSnapField.createPrice(VwdFieldDescription.ADF_Bezahlt.id(), price);
            SnapRecordDefault record = new SnapRecordDefault(Collections.singletonList(priceField));
            r.pushUpdate(record);
        }

        return registrations.size();
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }
}