/*
 * PushServiceImpl.java
 *
 * Created on 10.02.2010 11:46:16
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.feed.vwd.EntitlementProvider;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.EntitlementQuoteProvider;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategyFactory;
import de.marketmaker.itools.gwtcomet.comet.server.CometSession;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushChangeRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushChangeResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushData;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushService;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushSessionResponse;
import de.marketmaker.iview.mmgwt.mmweb.server.GwtService;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.websocket.Session;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author oflege
 */
@ManagedResource
@SuppressWarnings("GwtServiceNotRegistered")
public class PushServiceImpl extends GwtService implements PushService, CometPushConnect, WebsocketPushConnect {
    private static final int FID_BEST_BID_1 = VwdFieldDescription.ADF_Best_Bid_1.id();

    private static final int FID_BEZAHLT = VwdFieldDescription.ADF_Bezahlt.id();

    private static final String MARKET_DEPTH_SUFFIX = "MT";

    private static final String MARKET_DEPTH_TYPE = "12.";

    private final Clients clients = new Clients();

    private Registrations realtimeRegistrations;

    private Registrations neartimeRegistrations;

    private ProfileProvider profileProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private EntitlementQuoteProvider entitlementQuoteProvider;

    private EntitlementProvider entitlementProvider;

    @SuppressWarnings("unused")
    public void setProfileProvider(ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    @SuppressWarnings("unused")
    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    @SuppressWarnings("unused")
    public void setEntitlementProvider(EntitlementProvider entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    @SuppressWarnings("unused")
    public void setEntitlementQuoteProvider(EntitlementQuoteProvider entitlementQuoteProvider) {
        this.entitlementQuoteProvider = entitlementQuoteProvider;
    }

    @SuppressWarnings("unused")
    public void setRealtimeRegistrations(Registrations realtimeRegistrations) {
        this.realtimeRegistrations = realtimeRegistrations;
    }

    @SuppressWarnings("unused")
    public void setNeartimeRegistrations(Registrations neartimeRegistrations) {
        this.neartimeRegistrations = neartimeRegistrations;
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
            this.logger.info("<createSession> no push allowed for " + clientId);
            return result.withState(PushSessionResponse.State.PUSH_NOT_ALLOWED);
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

            final PriceQuality quality = profile.getPushPriceQuality(quote, getEntitlement(quote, symbol));
            if (quality == PriceQuality.REALTIME || quality == PriceQuality.DELAYED) {
                invalid.remove(symbol);
                registered.add(symbol);
                registerQuote(client, symbol, quote, quality == PriceQuality.REALTIME);
            }
            else {
                this.logger.warn("<register> " + client.getId() + " " + symbol + " " + quality + "?!");
            }
        }

        if (!invalid.isEmpty() && this.logger.isDebugEnabled()) {
            this.logger.debug("<register> invalid symbols for " + client.getId() + ": " + invalid);
        }
        if (!registered.isEmpty() && this.logger.isDebugEnabled()) {
            this.logger.debug("<register> for " + client.getId() + ": " + registered);
        }

        result.setInvalid(invalid.isEmpty() ? null : invalid);
        result.setRegistered(registered.isEmpty() ? null : registered);

        if (!registered.isEmpty()) {
            this.realtimeRegistrations.completeRegistrations();
            this.neartimeRegistrations.completeRegistrations();
        }
    }

    private boolean isOrderbookSymbol(String symbol) {
        return symbol.startsWith(PushChangeRequest.ORDERBOOK_PREFIX);
    }

    private String getEntitlement(Quote quote, String symbol) {
        final int fid = isOrderbookSymbol(symbol) ? FID_BEST_BID_1 : FID_BEZAHLT;
        final int e = this.entitlementProvider.getEntitlement(quote.getSymbolVwdfeed(), fid);
        return (e > 0) ? String.valueOf(e) : null;
    }

    private void registerQuote(AbstractClient client, String symbol, Quote quote,
            boolean realtime) {
        if (client.isRegistered(symbol)) {
            return;
        }
        final Registrations registrations = realtime
                ? this.realtimeRegistrations : this.neartimeRegistrations;

        final BitSet allowedFields
                = entitlementProvider.getAllowedFields(quote, client.getProfile());
        if (isOrderbookSymbol(symbol)) {
            allowedFields.and(PushOrderbookFactory.ALLOWED_FIELDS);
        }
        else {
            allowedFields.andNot(PushOrderbookFactory.BEST_FIELDS);
        }

        registrations.addClientFor(quote, client, allowedFields);
    }

    private Map<String, Quote> getQuotes(HashSet<String> symbols) {
        final HashMap<String, Quote> result = new HashMap<>();

        final ArrayList<String> codes = new ArrayList<>(symbols.size());
        for (String symbol : symbols) {
            if (isOrderbookSymbol(symbol)) {
                final String vwdcode = symbol.substring(1);
                if (vwdcode.endsWith(MARKET_DEPTH_SUFFIX)) {
                    result.put(vwdcode, getOrderbookQuote(vwdcode));
                }
                else {
                    codes.add(vwdcode);
                }
            }
            else {
                codes.add(symbol);
            }
        }

        this.instrumentProvider.identifyQuotes(codes, SymbolStrategyEnum.VWDCODE, null, null)
                .stream()
                .filter(Objects::nonNull)
                .forEach(quote -> result.put(quote.getSymbolVwdcode(), quote));

        return result;
    }

    private Quote getOrderbookQuote(String vwdcode) {
        return this.entitlementQuoteProvider.getQuote(MARKET_DEPTH_TYPE + vwdcode);
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

    @Override
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

    @Override
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

    @Override
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

    // invoked by external scheduler
    @SuppressWarnings("unused")
    public void dataMaintenance() {
        final int numRt = this.realtimeRegistrations.evictIdleRegistrations();
        final int numNt = this.neartimeRegistrations.evictIdleRegistrations();
        final int sizeRt = this.realtimeRegistrations.size();
        final int sizeNt = this.neartimeRegistrations.size();
        this.logger.info("<dataMaintenance> #rt=" + sizeRt + diff(numRt)
                + ", #nt=" + sizeNt + diff(numNt));
    }

    private String diff(int num) {
        return num == 0 ? "" : ("(-" + num + ")");
    }

    List<AbstractClient> getClientsWithData() {
        return this.clients.getClientsWithData();
    }
}