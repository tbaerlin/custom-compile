/*
 * IntradayProviderImpl.java
 *
 * Created on 07.07.2006 11:30:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import de.marketmaker.istar.ratios.frontend.MatrixMetadataRatioSearchResponse;
import de.marketmaker.istar.ratios.opra.OpraRatioSearchResponse;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.SerializableCollator;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.IstarSearchEngineConnector;
import de.marketmaker.istar.ratios.frontend.PreferIssuerFundQuoteStrategy;
import de.marketmaker.istar.ratios.frontend.QuoteRatios;
import de.marketmaker.istar.ratios.frontend.RatioDataRecordImpl;
import de.marketmaker.istar.ratios.frontend.RatioDataResult;
import de.marketmaker.istar.ratios.frontend.RatioSearchMetaRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchMetaResponse;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatiosProviderImpl implements RatiosProvider {
    private final static SerializableCollator<String> GERMAN_COLLATOR =
            new SerializableCollator<>(Locale.GERMAN);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Ehcache cache;

    private IstarSearchEngineConnector istarSearchEngineConnector;

    public void setIstarSearchEngineConnector(
            IstarSearchEngineConnector istarSearchEngineConnector) {
        this.istarSearchEngineConnector = istarSearchEngineConnector;
    }

    public void setCache(Ehcache cache) {
        this.cache = cache;
    }

    private Map<String, Map<String, Integer>> sortEnumsGroupBy(
            Map<String, Map<String, Integer>> metadata) {
        final Map<String, Map<String, Integer>> m =
                new TreeMap<>(GERMAN_COLLATOR);
        for (Map.Entry<String, Map<String, Integer>> entry : metadata.entrySet()) {
            m.put(entry.getKey(), sort(entry.getValue()));
        }
        return m;
    }

    private Map<String, Integer> sort(Map<String, Integer> value) {
        final TreeMap<String, Integer> result = new TreeMap<>(GERMAN_COLLATOR);
        result.putAll(value);
        return result;
    }

    @Override
    public Map<String, Map<String, Integer>> getMetaData(InstrumentTypeEnum type,
            int groupByFieldid, int selectFieldid, boolean withDetailedSymbol) {
        final Key key = new Key(type, groupByFieldid, selectFieldid);


        final Element ce = this.cache.get(key);
        if (ce != null) {
            //noinspection unchecked
            return (Map<String, Map<String, Integer>>) ce.getValue();
        }

        Map<String, Map<String, Integer>> result = retrieveMetaData(type, key, withDetailedSymbol);
        this.cache.put(new Element(key, result));
        return result;
    }

    private Map<String, Map<String, Integer>> retrieveMetaData(InstrumentTypeEnum type, Key key
            , boolean withDetailedSymbol) {
        final RatioSearchMetaRequest request
                = new RatioSearchMetaRequest(type, key.groupFieldid, key.selectFieldid, withDetailedSymbol);

        final RatioSearchMetaResponse response = this.istarSearchEngineConnector.getMetaData(request);
        return sortEnumsGroupBy(response.getEnumGroupedBy());
    }

    @Override
    public RatioSearchResponse search(RatioSearchRequest request) {
        return this.istarSearchEngineConnector.search(request);
    }

    @Override
    public OpraRatioSearchResponse getOpraItems(RatioSearchRequest request) {
        return this.istarSearchEngineConnector.getOpraItems(request);
    }

    @Override
    public MatrixMetadataRatioSearchResponse getOpraMatrix(RatioSearchRequest request) {
        return this.istarSearchEngineConnector.getOpraMatrix(request);
    }

    @Override
    public RatioSearchMetaResponse getOpraMetaData() {
        return this.istarSearchEngineConnector.getOpraMetaData();
    }

    @Override
    public RatioDataRecord getRatioData(Quote quote,
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {

        return getRatioDatas(Collections.singleton(quote), fields).get(quote.getId());
    }

    @Override
    public Map<Long, RatioDataRecord> getRatioDatas(Collection<Quote> quotes,
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        return getRatioDatas(partitionByType(quotes), fields);
    }

    private Map<Long, RatioDataRecord> getRatioDatas(Map<InstrumentTypeEnum, List<Quote>> quotes,
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        final List<Locale> locales = RequestContextHolder.getRequestContext().getLocales();

        final Map<Long, RatioDataRecord> result = new HashMap<>();

        for (Map.Entry<InstrumentTypeEnum, List<Quote>> e : quotes.entrySet()) {
            final RatioSearchRequest request = new RatioSearchRequest(
                    RequestContextHolder.getRequestContext().getProfile(), locales);
            request.setDataRecordStrategyClass(PreferIssuerFundQuoteStrategy.class);
            request.setType(e.getKey());
            request.setInstrumentIds(getInstrumentIds(e.getValue()));
            request.addParameter("n", Integer.toString(e.getValue().size()));

            final RatioSearchResponse rsr = this.istarSearchEngineConnector.search(request);
            for (Quote quote : e.getValue()) {
                result.put(quote.getId(), getResult(quote, rsr, fields, locales));
            }
        }
        return result;
    }

    private Map<InstrumentTypeEnum, List<Quote>> partitionByType(Collection<Quote> quotes) {
        if (quotes.size() == 1) {
            Quote q = quotes.iterator().next();
            if (q == null) {
                return Collections.emptyMap();
            }
            else {
                return Collections.singletonMap(q.getInstrument().getInstrumentType(),
                                        Collections.singletonList(q));
            }
        }

        final Map<InstrumentTypeEnum, List<Quote>> result = new EnumMap<>(InstrumentTypeEnum.class);
        for (Quote quote : quotes) {
            if (quote != null) {
                InstrumentTypeEnum type = quote.getInstrument().getInstrumentType();
                List<Quote> typeQuotes = result.get(type);
                if (typeQuotes == null) {
                    result.put(type, typeQuotes = new ArrayList<>());
                }
                typeQuotes.add(quote);
            }
        }
        return result;
    }

    private RatioDataRecord getResult(Quote quote, RatioSearchResponse rsr,
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields, List<Locale> locales) {
        if (!rsr.isValid()) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("invalid ratio search result for: " + quote);
            }
            return RatioDataRecord.NULL;
        }

        final RatioDataResult rdr = findResult(quote.getInstrument().getId(),
                (DefaultRatioSearchResponse) rsr);
        if (rdr == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("no ratio search result for: " + quote.getInstrument());
            }
            return RatioDataRecord.NULL;
        }

        for (final QuoteRatios qr : rdr.getQuotes()) {
            if (qr.getId() == quote.getId()) {
                return new RatioDataRecordImpl(qr.getInstrumentRatios(), qr, fields, locales);
            }
        }
        return RatioDataRecord.NULL;
    }

    private RatioDataResult findResult(long iid, DefaultRatioSearchResponse rsr) {
        for (RatioDataResult rdr : rsr.getElements()) {
            if (rdr.getInstrumentid() == iid) {
                return rdr;
            }
        }
        return null;
    }

    private List<Long> getInstrumentIds(List<Quote> quotes) {
        if (quotes.size() == 1) {
            return Collections.singletonList(quotes.get(0).getInstrument().getId());
        }
        return quotes.stream()
                .map(quote -> quote.getInstrument().getId())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static class Key implements Serializable {
        protected static final long serialVersionUID = 1L;

        private final InstrumentTypeEnum type;

        private final int groupFieldid;

        private final int selectFieldid;

        public Key(InstrumentTypeEnum type, int groupFieldid, int selectFieldid) {
            this.type = type;
            this.groupFieldid = groupFieldid;
            this.selectFieldid = selectFieldid;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Key key = (Key) o;

            if (groupFieldid != key.groupFieldid) {
                return false;
            }
            if (selectFieldid != key.selectFieldid) {
                return false;
            }
            return type == key.type;
        }

        public int hashCode() {
            int result;
            result = type.hashCode();
            result = 29 * result + groupFieldid;
            result = 29 * result + selectFieldid;
            return result;
        }
    }
}
