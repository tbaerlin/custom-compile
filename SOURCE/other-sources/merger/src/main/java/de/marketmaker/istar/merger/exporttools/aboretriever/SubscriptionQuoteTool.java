package de.marketmaker.istar.merger.exporttools.aboretriever;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SubscriptionQuoteTool {
    private Map<String, Map<InstrumentTypeEnum, List<QuoteData>>> byMarket;

    private Map<InstrumentTypeEnum, Map<String, List<QuoteData>>> byType;

    private final Map<Short, List<Integer>> qidBySubscription = new HashMap<>();

    private List<Subscription> subscriptions;

    public SubscriptionQuoteTool(List<Subscription> subscriptions, List<SubscriptionQuote> quotes) {
        this.subscriptions = subscriptions;
        for (final Subscription subscription : this.subscriptions) {
            final ArrayList<Integer> qids = new ArrayList<>();
            this.qidBySubscription.put(subscription.getId(), qids);

            for (final SubscriptionQuote quote : quotes) {
                if (quote.getId() == subscription.getId()) {
                    qids.add(quote.getQuote().getQid());
                }
            }

            qids.sort(null);
        }
    }

    public List<String> getSubscriptions(int qid, String file) {
        final List<String> result = new ArrayList<>();

        for (final Map.Entry<Short, List<Integer>> entry : this.qidBySubscription.entrySet()) {
            if (Collections.binarySearch(entry.getValue(), qid) >= 0) {
                for (final Subscription subscription : this.subscriptions) {
                    if (subscription.getId() == entry.getKey() && !("MM " + file + "-Datei").equals(subscription.getName())) {
                        result.add(subscription.getName());
                    }
                }
            }
        }
        return result;
    }


    public void resetForQuoteData(List<QuoteData> quotes) {
        reset();
        for (final QuoteData quote : quotes) {
            reset(quote);
        }
    }

    public void reset(List<SubscriptionQuote> quotes) {
        reset();
        for (final SubscriptionQuote quote : quotes) {
            try {
                reset(quote.getQuote());
            } catch (Exception e) {
                System.out.println("failed for '" + quote.getQuote().getVwdsymbol()+"'");
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    private void reset(QuoteData quote) {
        final String s = quote.getVwdsymbol();
        final int first = s == null ? -1 : s.indexOf(".");
        final int second = s == null ? -1 : s.indexOf(".", first + 1);
        final int third = s == null ? -1 : s.indexOf(".", second + 1);

        final String market = s == null ? "n/a" : s.substring(second, third < 0 ? s.length() : third);
        final InstrumentTypeEnum type = quote.getType();

        // by market
        Map<InstrumentTypeEnum, List<QuoteData>> typeMap = this.byMarket.get(market);
        if (typeMap == null) {
            typeMap = new EnumMap<>(InstrumentTypeEnum.class);
            this.byMarket.put(market, typeMap);
        }

        List<QuoteData> tl = typeMap.get(type);
        if (tl == null) {
            tl = new ArrayList<>();
            typeMap.put(type, tl);
        }
        tl.add(quote);


        // by type
        Map<String, List<QuoteData>> marketMap = this.byType.get(type);
        if (marketMap == null) {
            marketMap = new HashMap<>();
            this.byType.put(type, marketMap);
        }

        List<QuoteData> ml = marketMap.get(market);
        if (ml == null) {
            ml = new ArrayList<>();
            marketMap.put(market, ml);
        }
        ml.add(quote);
    }

    private void reset() {
        this.byMarket = new HashMap<>();
        this.byType = new EnumMap<>(InstrumentTypeEnum.class);
    }

    public List<String> getMarkets() {
        final List<String> markets = new ArrayList<>();
        markets.addAll(this.byMarket.keySet());
        markets.sort(null);
        return markets;
    }

    public List<String> getMarkets(InstrumentTypeEnum type) {
        final Map<String, List<QuoteData>> map = this.byType.get(type);

        final List<String> markets = new ArrayList<>();
        markets.addAll(map.keySet());
        markets.sort(null);
        return markets;
    }

    public List<InstrumentTypeEnum> getTypes() {
        final List<InstrumentTypeEnum> types = new ArrayList<>();
        types.addAll(this.byType.keySet());
        types.sort(null);
        return types;
    }

    public List<InstrumentTypeEnum> getTypes(String market) {
        final Map<InstrumentTypeEnum, List<QuoteData>> map = this.byMarket.get(market);

        final List<InstrumentTypeEnum> types = new ArrayList<>();
        types.addAll(map.keySet());
        types.sort(null);
        return types;
    }

    public List<QuoteData> getQuotes(String market, InstrumentTypeEnum type) {
        final List<QuoteData> result = this.byMarket.get(market).get(type);
        return result.size() <= 100 ? result : result.subList(0, 100);
    }

    public int getSize(String market, InstrumentTypeEnum type) {
        return this.byMarket.get(market).get(type).size();
    }

    public List<QuoteData> getQuotes(InstrumentTypeEnum type, String market) {
        final List<QuoteData> result = this.byType.get(type).get(market);
        return result.size() <= 100 ? result : result.subList(0, 100);
    }

    public int getSize(InstrumentTypeEnum type, String market) {
        return this.byType.get(type).get(market).size();
    }
}