/*
 * GisEbrokeragePricesMethod.java
 *
 * Created on 22.08.12 13:54
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.util.StringUtils;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.ProfileAdapter;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;
import de.marketmaker.istar.domainimpl.profile.DelayProfileAdapter;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.domainimpl.profile.VwdProfileAdapter;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.calendar.TradingDay;
import de.marketmaker.istar.merger.provider.calendar.TradingSession;
import de.marketmaker.istar.merger.provider.profile.RealtimeDisclaimerProvider;
import de.marketmaker.istar.merger.web.AuditTrailFilter;
import de.marketmaker.istar.merger.web.NoProfileException;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;

/**
 * @author oflege
 */
class GisEbrokeragePricesMethod {

  private static final VwdFieldDescription.Field[] SELECTOR_FIELDS =
      new VwdFieldDescription.Field[]{
          VwdFieldDescription.ADF_Bezahlt,
          VwdFieldDescription.ADF_Brief,
          VwdFieldDescription.ADF_Geld,
          VwdFieldDescription.ADF_Schluss,
          VwdFieldDescription.ADF_NAV,
          VwdFieldDescription.ADF_Ausgabe,
          VwdFieldDescription.ADF_Ruecknahme
      };

  private static final int APP_ID_DELAYED_PROFILES = 78;

  private static final int APP_ID_REALTIME_PROFILES = 79;

  private static final int APP_ID_VERSION_1 = 50;

  /**
   * this many minutes after the end of the trading session prices are not considered RT any more
   */
  private static final int END_DELAY = 15;

  /**
   * start and end time for realtime prices at a non FSE market: 09:00 - 20:00 so after 20:15 it is
   * guaranteed to be not realtime
   */
  private static final LocalTime[] OTHER_RT_TIMES = new LocalTime[]{
      new LocalTime(8, 0), new LocalTime(20, END_DELAY)
  };

  private static final Map<String, List<String>> MARKET_MAPPINGS =
      new LinkedHashMap<>();

  private static final Set<String> PRICE_ALLOWED_MARKETS = new HashSet<>();

  private static final Set<String> ALLOWED_FOREIGN_MARKETS = new HashSet<>(Arrays.asList(
      "N", "IQ", "Q", "CH", "UK", "UKINT", "FR", "ES", "PT"
      , "WIEN", "IT", "NL", "DK", "FI", "SW", "TK", "TO", "AU"));

  private static final Set<String> FUND_MARKETS = new HashSet<>(Arrays.asList(
      "FFMFO", "FFM", "BLN", "MCH", "STG", "DDF", "HNV", "HBG", "XETF", "ETRI", "TRADE"
  ));

  static {
    MARKET_MAPPINGS.put("FSE", Arrays.asList("FFM", "FFMST", "FFMFO"));
    MARKET_MAPPINGS.put("C48", Collections.singletonList("FFMST"));
    MARKET_MAPPINGS.put("ETR", Arrays.asList("ETR", "EEU", "EUS", "XETF", "ETRI"));
    MARKET_MAPPINGS.put("MUN", Collections.singletonList("MCH"));
    MARKET_MAPPINGS.put("BER", Collections.singletonList("BLN"));
    MARKET_MAPPINGS.put("STU", Arrays.asList("STG", "EUWAX"));
    MARKET_MAPPINGS.put("EWX", Arrays.asList("EUWAX", "STG"));
    MARKET_MAPPINGS.put("DUS", Collections.singletonList("DDF"));
    MARKET_MAPPINGS.put("HAM", Collections.singletonList("HBG"));
    MARKET_MAPPINGS.put("HAN", Collections.singletonList("HNV"));
    MARKET_MAPPINGS.put("TRD", Collections.singletonList("TRADE"));
    MARKET_MAPPINGS.put("BNP", Collections.singletonList("BNP"));
    MARKET_MAPPINGS.put("CIT", Collections.singletonList("CITIF"));
    List<String> dzf = Collections.singletonList("DZF");
    MARKET_MAPPINGS.put("DZF", dzf);
    MARKET_MAPPINGS.put("DZBANK", dzf);
    MARKET_MAPPINGS.put("HVB", Collections.singletonList("HVB"));
    MARKET_MAPPINGS.put("SOC", Collections.singletonList("SCGP"));
    MARKET_MAPPINGS.put("UBS", Collections.singletonList("UBS"));
    MARKET_MAPPINGS.put("QTX", Collections.singletonList("QTX"));
    MARKET_MAPPINGS.put("NYS", Collections.singletonList("N"));
    MARKET_MAPPINGS.values().forEach(PRICE_ALLOWED_MARKETS::addAll);
    PRICE_ALLOWED_MARKETS.addAll(ALLOWED_FOREIGN_MARKETS);
  }

  private final Map<String, Object> result = new HashMap<>();

  private final GisEbrokeragePrices.Command cmd;

  private final GisEbrokeragePrices gis;

  private final AuditTrailFilter.Trail audit;

  private Profile delayedProfile;

  private Profile realtimeProfile;

  private RealtimeDisclaimerProvider.Status disclaimerStatus;

  public GisEbrokeragePricesMethod(GisEbrokeragePrices gis,
      GisEbrokeragePrices.Command cmd, AuditTrailFilter.Trail audit) {
    this.gis = gis;
    this.cmd = cmd;
    this.audit = audit;
  }

  private void addAudit(Object o) {
    addAudit(null, o);
  }

  private void addAudit(String s, Object o) {
    this.audit.add(s, o);
  }

  public Map<String, ?> invoke() throws Exception {
    addAudit("Command", this.cmd.toString());

    final ProfileRequest prequest = evaluateAuthentication();
    if (prequest == null) {
      addAudit("no authentication given");
      throw new IllegalStateException("no authentication given for " + cmd);
    }

    addAudit(prequest);

    final ProfileResponse presponse = this.gis.getProfile(prequest);
    if (!presponse.isValid()) {
      addAudit("invalid profile response");
      throw new NoProfileException("no profile for " + cmd);
    }

    addAudit(presponse);

    this.result.put("status", this.disclaimerStatus);

    final Profile profile = presponse.getProfile();

    if (this.cmd.isVersion1()) {
      final boolean isRealtimeProfile = "eBrokerage_rt".equals(prequest.getAuthentication());

      final String complementaryLogin = getLoginV1(!isRealtimeProfile);
      final Profile complementaryProfile =
          this.gis.getProfile(getProfileRequest(APP_ID_VERSION_1, 10, complementaryLogin))
              .getProfile();

      if (isRealtimeProfile) {
        this.realtimeProfile = profile;
        this.delayedProfile = complementaryProfile;
      } else {
        this.realtimeProfile = complementaryProfile;
        this.delayedProfile = profile;
      }
    }

    if (this.cmd.isVersion2()) {
      if (Integer.parseInt(prequest.getApplicationId()) == APP_ID_DELAYED_PROFILES) {
        this.delayedProfile = profile;

        prequest.setApplicationId(Integer.toString(APP_ID_REALTIME_PROFILES));
        this.realtimeProfile = this.gis.getProfile(prequest).getProfile();
      } else {
        this.realtimeProfile = profile;

        prequest.setApplicationId(Integer.toString(APP_ID_DELAYED_PROFILES));
        this.delayedProfile = this.gis.getProfile(prequest).getProfile();
      }
    }

    RequestContextHolder.callWith(profile, () -> {
      handle();
      return null;
    });

    return this.result;
  }

  private ProfileRequest evaluateAuthentication() {
    final boolean hasXun = StringUtils.hasText(this.cmd.getXun());
    if (this.cmd.getSrc() == null && this.cmd.getRealtime() == null) {
      return getProfileRequest(APP_ID_VERSION_1, 10, getLoginV1(hasXun));
    }

    if (isRealtimeRequested()) {
      final Boolean disclaimerforMelNG = this.cmd.getSrc() == null
          ? null
          : this.cmd.isVersion2();
      this.disclaimerStatus = this.gis
          .getDisclaimerStatus(disclaimerforMelNG, this.cmd.getGbr(), this.cmd.getKunde(),
              this.cmd.getXun());
    } else {
      this.disclaimerStatus = RealtimeDisclaimerProvider.INVALID_DEFAULT;
    }

    if (this.cmd.isVersion1()) {
      if (this.cmd.getRealtime() == null) {
        return getProfileRequest(APP_ID_VERSION_1, 10, getLoginV1(hasXun));
      }

      return getProfileRequest(APP_ID_VERSION_1, 10,
          getLoginV1(isRealtimeRequested() && disclaimerStatus.isVerified()));
    }

    final int appId =
        disclaimerStatus.isVerified() ? APP_ID_REALTIME_PROFILES : APP_ID_DELAYED_PROFILES;
    final String login = (cmd.getGbr() != null ? cmd.getGbr() : "")
        + (cmd.getKunde() != null ? cmd.getKunde() : "")
        + (cmd.getXun() != null ? cmd.getXun() : "");
    return getProfileRequest(appId, 10, login);
  }

  private String getLoginV1(boolean realtime) {
    return realtime ? "eBrokerage_rt" : "eBrokerage_del";
  }

  private boolean isRealtimeRequested() {
    return "1".equals(cmd.getRealtime());
  }

  private ProfileRequest getProfileRequest(int appId, int clientId, String login) {
    final ProfileRequest pr = new ProfileRequest("vwd-ent:ByLogin", login);
    pr.setApplicationId(Integer.toString(appId));
    pr.setClientId(Integer.toString(clientId));
    return pr;
  }

  private void handle() throws Exception {
    final Instrument instrument;
    try {
      instrument = this.gis.identifyInstrument(cmd.getWkn());
    } catch (UnknownSymbolException e) {
      addAudit("unknown wkn: '" + cmd.getWkn() + "'");
      return;
    }

    addAudit("instrument: " + instrument.getInstrumentType() + " " + instrument);
    handle(instrument);
  }

  private void handle(final Instrument instrument) throws Exception {
    final Profile profile = RequestContextHolder.getRequestContext().getProfile();
    if (profile == null) {
      return;
    }

    final List<Quote> profiledQuotes = ProfiledInstrument.quotesWithPrices(instrument, profile);
    addAudit("profiled quotes: " + profiledQuotes);

    // all the lists that follow have the same size
    final List<Quote> allowedQuotes = filterAllowed(profiledQuotes);
    addAudit("allowed quotes: " + allowedQuotes);

    final List<PriceRecord> allPriceRecords = this.gis.getPriceRecords(allowedQuotes);
    // replace invalid prices with null values
    final List<PriceRecord> priceRecords
        = filterInvalidPrices(instrument, allPriceRecords, allowedQuotes);
    // replace quotes without valid price with null values
    final List<Quote> quotesWithValidPrices = filterQuotesByPrices(allowedQuotes, priceRecords);
    addAudit("quotes with valid prices: " + quotesWithValidPrices);

    final int n = getPriceElementIndex(quotesWithValidPrices, cmd.getBpl());
    if (n == -1) {
      addAudit("no quote with valid prices at requested bpl");
      return;
    }

    final Quote priceQuote = quotesWithValidPrices.get(n);
    final PriceRecord record = priceRecords.get(n);
    final boolean realtime = isRealtime(priceQuote, record);
    final boolean pushRealtimeProfile = isPushRealtimeProfile(profile, priceQuote);
    final boolean countDisclaimer =
        Objects.nonNull(this.disclaimerStatus) && this.disclaimerStatus.isVerified();

    addAudit("price quote: " + priceQuote + ", realtime=" + realtime
        + ", pushRealtimeProfile=" + pushRealtimeProfile
        + ", countDisclaimer=" + countDisclaimer);

    if (realtime && !pushRealtimeProfile && countDisclaimer) {
      final ProfileAdapter adapter = createAdapter(priceQuote);
      final Profile adaptedProfile = adapter.adapt(profile);
      if (adaptedProfile.getPriceQuality(priceQuote) != PriceQuality.REALTIME) {
        addAudit("RT denied");
        if (adapter instanceof VwdProfileAdapter) {
          addAudit(((VwdProfileAdapter) adapter).getCounterServiceResponse());
        } else {
          addAudit("Adapter: " + adapter.getClass().getName() + " " + adapter);
        }

        // todo: is this the right thing to do? what if we use intradayMap
        // todo: for reporting? But we have to clear the cache as otherwise we would
        // todo: end up with rt-Data again.
        RequestContextHolder.getRequestContext().clearIntradayContext();

        // realtime price is not allowed according to accessCounter, so re-request with
        // adapted profile
        RequestContextHolder.callWith(adaptedProfile, () -> {
          addAudit("-WITH-ADAPTED-PROFILE-START-");
          handle(instrument);
          final PriceQuality originalPQ = profile.getPriceQuality(priceQuote);
          final PriceQuality adaptedPQ = adaptedProfile.getPriceQuality(priceQuote);
          boolean tickCountExceeded =
              originalPQ == PriceQuality.REALTIME && adaptedPQ == PriceQuality.DELAYED;
          result.put("tickCountExceeded", tickCountExceeded);
          addAudit("originalPQ= " + originalPQ + ", adaptedPQ=" + adaptedPQ);
          addAudit("tickCountExceeded: " + tickCountExceeded);
          addAudit("-WITH-ADAPTED-PROFILE-END-");
          return null;
        });
        return;
      }
    }
    allowedQuotes.remove(quotesWithValidPrices.get(n));
    priceRecords.remove(record);

    final PriceQuality pq = profile.getPriceQuality(priceQuote);
    final int delayInMinutes = getDelayInMinutes(pq, record);
    addAudit("delayInMinutes for " + pq + ": " + delayInMinutes);

    final boolean realtimeAbfragbar = isRealtimeAbfragbar(priceQuote);
    addAudit("realtimeAbfragbar: " + realtimeAbfragbar);

    result.put("quotes", allowedQuotes);
    result.put("quote", priceQuote);
    result.put("delayInMinutes", Integer.toString(delayInMinutes));
    result.put("realtimeAbfragbar", realtimeAbfragbar);
    result.put("price", record);
    result.put("prices", priceRecords);
    result.put("realtime", realtime);
  }

  private boolean isRealtimeAbfragbar(Quote priceQuote) {
    final PriceQuality pqDelayed = getPriceQuality(priceQuote, this.delayedProfile);
    final PriceQuality pqRealtime = getPriceQuality(priceQuote, this.realtimeProfile);

    return (pqDelayed == PriceQuality.DELAYED || pqDelayed == PriceQuality.END_OF_DAY)
        && pqRealtime == PriceQuality.REALTIME;
  }

  private PriceQuality getPriceQuality(Quote priceQuote, final Profile profile) {
    return (profile != null) ? profile.getPriceQuality(priceQuote) : PriceQuality.NONE;
  }

  private boolean isPushRealtimeProfile(Profile profile, Quote quote) {
    return profile.getPushPriceQuality(quote, null) == PriceQuality.REALTIME;
  }

  private int getDelayInMinutes(PriceQuality pq, PriceRecord pr) {
    if (pq == PriceQuality.END_OF_DAY) {
      return 99;
    }
    if (pq == PriceQuality.DELAYED) {
      return 15;
//            return Integer.toString(pr.getNominalDelayInSeconds() / 60);
    }
    return 0;
  }

  private ProfileAdapter createAdapter(Quote priceQuote) {
    final String selector = getSelector(priceQuote);

    final ProfileAdapter result = this.gis.countAccess(this.cmd, selector);
    return (result != null) ? result : new DelayProfileAdapter();
  }

  private String getSelector(Quote priceQuote) {
    for (VwdFieldDescription.Field f : SELECTOR_FIELDS) {
      final int entitlement
          = this.gis.getEntitlement(priceQuote.getSymbolVwdfeed(), f.id());
      if (entitlement > 0) {
        addAudit("getSelector: " + priceQuote.getSymbolVwdfeed() + " "
            + f.toString() + ": " + EntitlementsVwd.toEntitlement(entitlement));
        return EntitlementsVwd.toNumericEntitlement(entitlement);
      }
    }

    final String[] selectors
        = priceQuote.getEntitlement().getEntitlements(KeysystemEnum.VWDFEED);

    addAudit("getSelector: no select field found for " + priceQuote.getSymbolVwdfeed()
        + " using [0] in " + Arrays.toString(selectors));

    return EntitlementsVwd.toNumericSelector(selectors[0]);
  }

  private boolean isRealtime(Quote quote, PriceRecord record) {
    if (record.getPriceQuality() != PriceQuality.REALTIME) {
      return false;
    }
    final Interval mt = getTimesForMarket(quote);
    final DateTime now = new DateTime();
    final boolean result = mt.contains(now);
    addAudit("isRealtime: now=" + now + ", marketTime=" + mt + ", result=" + result);
    return result;
  }

  private Interval getTimesForMarket(Quote quote) {
    final TradingDay tradingDay = this.gis.getTradingDay(quote);
    if (tradingDay == null || tradingDay.sessions() == null) {
      return new Interval(OTHER_RT_TIMES[0].toDateTimeToday(), OTHER_RT_TIMES[1].toDateTimeToday());
    }
    final TradingSession[] sessions = tradingDay.sessions();
    return new Interval(
        sessions[0].sessionInterval().getStart(),
        sessions[sessions.length - 1].sessionInterval().getEnd().plusMinutes(END_DELAY)
    );
  }

  private List<Quote> filterQuotesByPrices(List<Quote> quotes, List<PriceRecord> prices) {
    final List<Quote> result = new ArrayList<>();
    for (int i = 0; i < quotes.size(); i++) {
      result.add(prices.get(i) != null ? quotes.get(i) : null);
    }
    return result;
  }

  public int getPriceElementIndex(List<Quote> quotes, String market) {
    if (ALLOWED_FOREIGN_MARKETS.contains(market)) {
      for (int i = 0; i < quotes.size(); i++) {
        final Quote quote = quotes.get(i);
        if (quote != null && market.equals(quote.getSymbolVwdfeedMarket())) {
          return i;
        }
      }
    }

    final List<List<String>> vwdMarketsLists =
        StringUtils.hasText(market) && MARKET_MAPPINGS.containsKey(market)
            ? Collections.singletonList(MARKET_MAPPINGS.get(market))
            : new ArrayList<>(MARKET_MAPPINGS.values());

    for (List<String> vwdMarkets : vwdMarketsLists) {
      for (int i = 0; i < quotes.size(); i++) {
        final Quote quote = quotes.get(i);
        if (quote != null && vwdMarkets.contains(quote.getSymbolVwdfeedMarket())) {
          return i;
        }
      }
    }

    return -1;
  }

  private List<PriceRecord> filterInvalidPrices(Instrument instrument,
      List<PriceRecord> priceRecords, List<Quote> quotes) {
    final int numDays = getNumDays(instrument);
    final DateTime minAge = new LocalDate().minusDays(numDays).toDateTimeAtStartOfDay();

    final List<PriceRecord> result = new ArrayList<>();
    for (int i = 0; i < priceRecords.size(); i++) {
      final PriceRecord record = priceRecords.get(i);
      if (record == NullPriceRecord.INSTANCE) {
        result.add(null);
      } else if (isTooOld(record, minAge)) {
        addAudit("too old: " + quotes.get(i) + ", " + minAge + ", " + record);
        result.add(null);
      } else {
        result.add(record);
      }
    }
    return result;
  }

  private boolean isTooOld(PriceRecord record, DateTime acceptable) {
    final DateTime date = record.getDate();
    return (date == null || date.isBefore(acceptable));
  }

  private boolean isAllowed(Quote q) {
    return getAllowedMarkets(q).contains(q.getSymbolVwdfeedMarket());
  }

  private Set<String> getAllowedMarkets(Quote q) {
    return q.getInstrument().getInstrumentType() == InstrumentTypeEnum.FND
        ? FUND_MARKETS : PRICE_ALLOWED_MARKETS;
  }

  private List<Quote> filterAllowed(List<Quote> quotes) {
    final List<Quote> result = new ArrayList<>(quotes.size());
    result.addAll(quotes.stream().filter(this::isAllowed).collect(Collectors.toList()));
    return result;
  }

  private int getNumDays(Instrument instrument) {
    switch (instrument.getInstrumentType()) {
      case BND:
        return 14;
      case FND:
        return 7;
      default:
        return 20;
    }
  }
}
