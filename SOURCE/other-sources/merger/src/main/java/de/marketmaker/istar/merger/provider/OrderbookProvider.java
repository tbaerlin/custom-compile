/*
 * OrderbookProvider.java
 *
 * Created on 14.09.2009 10:24:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.NullOrderbookData;
import de.marketmaker.istar.domain.data.NullSnapRecord;
import de.marketmaker.istar.domain.data.OrderbookData;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.OrderbookDataImpl;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.EntitlementProvider;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.context.RequestContextHolder;

import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.getFieldByName;

/**
 * A delegate used by IntradayProviderImpl to process orderbook related requests.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OrderbookProvider {
    /**
     * field used to check orderbook permissions; the Best_.._1 field is sometimes used as a flag
     * field to signal a condition for ADF_Geld/Brief, so it may be entitled even if orderbook
     * access is not granted... (see R-70445).
     */
    private static final int FID_BEST_BID_2 = VwdFieldDescription.ADF_Best_Bid_2.id();

    private IntradayProvider intradayProvider;

    private EntitlementQuoteProvider entitlementQuoteProvider;

    private EntitlementProvider entitlementProvider;

    private Ehcache orderbookInfoCache;

    private static final int BEST_COUNT = 20;

    private static final int[] BEST_BID_IDS = new int[BEST_COUNT];

    private static final int[] BEST_BID_SIZE_IDS = new int[BEST_COUNT];

    private static final int[] BEST_ASK_IDS = new int[BEST_COUNT];

    private static final int[] BEST_ASK_SIZE_IDS = new int[BEST_COUNT];

    static {
        for (int i = 0; i < BEST_COUNT; i++) {
            final int j = i + 1;
            BEST_ASK_IDS[i] = getFieldByName("ADF_Best_Ask_" + j).id();
            BEST_ASK_SIZE_IDS[i] = getFieldByName("ADF_Best_Ask_" + j + "_Size").id();
            BEST_BID_IDS[i] = getFieldByName("ADF_Best_Bid_" + j).id();
            BEST_BID_SIZE_IDS[i] = getFieldByName("ADF_Best_Bid_" + j + "_Size").id();
        }
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setEntitlementQuoteProvider(EntitlementQuoteProvider entitlementQuoteProvider) {
        this.entitlementQuoteProvider = entitlementQuoteProvider;
    }

    public void setEntitlementProvider(EntitlementProvider entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public void setOrderbookInfoCache(Ehcache orderbookInfoCache) {
        this.orderbookInfoCache = orderbookInfoCache;
    }

    public OrderbookData getOrderbook(Quote quote) {
        return getOrderbook(quote, getOrderbookInfoFromCache(quote));
    }

    private Quote getOrderbookQuote(Quote quote, String cachedSymbol) {
        final String symbol = (cachedSymbol != null) ? cachedSymbol : getMtReferenz(quote);

        if (!StringUtils.hasText(symbol)) {
            if (cachedSymbol == null) {
                addOrderbookInfoToCache(quote, "");
            }
            return null;
        }

        return symbol.equals(quote.getSymbolVwdcode()) ? quote : getOrderbookQuote(symbol);
    }

    private OrderbookData getOrderbook(Quote quote, String cachedSymbol) {
        final Quote mdQuote = getOrderbookQuote(quote, cachedSymbol);
        if (mdQuote == null) {
            return NullOrderbookData.INSTANCE;
        }

        final String entitlement = getBestBid2Entitlement(mdQuote);
        final boolean bestBid1Allowed = isEntitlementAllowed(entitlement);

        final PriceQuality priceQuality = getProfile().getPriceQuality(mdQuote);
        if (!(bestBid1Allowed && isIntradayAllowed(priceQuality))) {
            if (cachedSymbol == null) {
                addOrderbookInfoToCache(quote, mdQuote.getSymbolVwdcode()); // might be wrong if no data is available (1)
            }
            return NullOrderbookData.INSTANCE;
        }

        final SnapRecord sr = getPriceSnap(mdQuote);
        if (sr == NullSnapRecord.INSTANCE) {
            addOrderbookInfoToCache(quote, ""); // corrects wrong add for (1)
            return NullOrderbookData.INSTANCE;
        }

        if (cachedSymbol == null) {
            addOrderbookInfoToCache(quote, mdQuote.getSymbolVwdcode());
        }


        final List<OrderbookData.Item> bids = buildOrderbookList(true, sr);
        final List<OrderbookData.Item> asks = buildOrderbookList(false, sr);

        final boolean pushAllowed = isPushAllowed(mdQuote, entitlement);
        return new OrderbookDataImpl(mdQuote, priceQuality, pushAllowed, bids, asks, getDate(sr));
    }

    private boolean isPushAllowed(Quote mdQuote, String entitlement) {
        return isIntradayAllowed(getProfile().getPushPriceQuality(mdQuote, entitlement));
    }

    private boolean isBestBid2Allowed(Quote quote) {
        return isEntitlementAllowed(getBestBid2Entitlement(quote));
    }

    private boolean isEntitlementAllowed(String entitlement) {
        return (entitlement != null) && getProfile().isAllowed(Profile.Aspect.PRICE, entitlement);
    }

    private String getBestBid2Entitlement(Quote quote) {
        final int e = this.entitlementProvider.getEntitlement(quote.getSymbolVwdfeed(), FID_BEST_BID_2);
        return (e > 0) ? String.valueOf(e) : null;
    }

    private Profile getProfile() {
        return RequestContextHolder.getRequestContext().getProfile();
    }

    private DateTime getDate(SnapRecord sr) {
        final int yyyymmdd = getInt(sr, VwdFieldDescription.ADF_DATEOFARR);
        final int seconds = getInt(sr, VwdFieldDescription.ADF_TIMEOFARR);
        if (yyyymmdd >= 0 && seconds >= 0) {
            return DateUtil.toDateTime(yyyymmdd, seconds);
        }
        return null;
    }

    private int getInt(SnapRecord sr, final VwdFieldDescription.Field field) {
        return SnapRecordUtils.getInt(sr.getField(field.id()));
    }

    public boolean isWithOrderbook(Quote quote) {
        final String cachedSymbol = getOrderbookInfoFromCache(quote);
        if (cachedSymbol == null) {
            return getOrderbook(quote, null) != NullOrderbookData.INSTANCE;
        }

        if ("".equals(cachedSymbol)) {
            return false;
        }

        return isOrderBookAllowed(getOrderbookQuote(quote, cachedSymbol));
    }

    private String getOrderbookInfoFromCache(Quote quote) {
        final Element element = this.orderbookInfoCache.get(quote.getId());
        if (element != null) {
            return (String) element.getValue();
        }
        return null;
    }

    private void addOrderbookInfoToCache(Quote quote, String symbol) {
        this.orderbookInfoCache.put(new Element(quote.getId(), symbol));
    }

    private String getMtReferenz(Quote quote) {
        final SnapRecord psr = getPriceSnap(quote);

        // first check for dedicated orderbook symbol (R-70445)
        final SnapField field = psr.getField(VwdFieldDescription.ADF_MT_Referenz.id());
        if (field.isDefined()) {
            final String mtReferenz = (String) field.getValue();
            if (StringUtils.hasText(mtReferenz)) {
                return mtReferenz;
            }
        }

        // check if oderbook fields are inlined
        if (psr.getField(FID_BEST_BID_2).isDefined()) {
            // mt data is in same snap record as all the other data
            return quote.getSymbolVwdcode();
        }
        return null;
    }

    private Quote getOrderbookQuote(String symbol) {
        return this.entitlementQuoteProvider.getQuote("12." + symbol);
    }

    private List<OrderbookData.Item> buildOrderbookList(boolean bid, SnapRecord sr) {
        final List<OrderbookData.Item> result = new ArrayList<>(BEST_COUNT);

        final int[] prices = bid ? BEST_BID_IDS : BEST_ASK_IDS;
        final int[] sizes = bid ? BEST_BID_SIZE_IDS : BEST_ASK_SIZE_IDS;

        for (int i = 0; i < BEST_COUNT; i++) {
            final SnapField pf = sr.getField(prices[i]);
            final SnapField vf = sr.getField(sizes[i]);
            if (!pf.isDefined() || !vf.isDefined()) {
                break;
            }

            final BigDecimal price = pf.getPrice();
            final Number volume = (Number) vf.getValue();
            if (price.signum() == 0 || volume.longValue() == 0) {
                break;
            }

            result.add(new OrderbookDataImpl.ItemImpl(price, volume.longValue()));
        }

        return result;
    }

    private SnapRecord getPriceSnap(Quote q) {
        return intradayProvider.getIntradayData(q, null).getSnap();
    }

    private boolean isOrderBookAllowed(Quote quote) {
        return isBestBid2Allowed(quote) && isIntradayAllowed(quote);
    }

    private boolean isIntradayAllowed(Quote quote) {
        return isIntradayAllowed(getProfile().getPriceQuality(quote));
    }

    private boolean isIntradayAllowed(PriceQuality priceQuality) {
        return priceQuality == PriceQuality.DELAYED || priceQuality == PriceQuality.REALTIME;
    }
}
