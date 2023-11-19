/*
 * NewsSymbolIdentifierImpl.java
 *
 * Created on 23.03.2007 21:04:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.util.IsinUtil;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.instrument.IndexConstants;
import de.marketmaker.istar.instrument.InstrumentRequest;
import de.marketmaker.istar.instrument.InstrumentResponse;
import de.marketmaker.istar.instrument.InstrumentServer;
import de.marketmaker.istar.instrument.search.SearchRequestResultType;
import de.marketmaker.istar.instrument.search.SearchRequestStringBased;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;

/**
 * Identifies the instruments associated with a NewsRecord based on the symbols that are
 * contained in the news feed record by asking an {@link de.marketmaker.istar.instrument.InstrumentServer}.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsSymbolIdentifierImpl implements NewsSymbolIdentifier, InitializingBean {

    private static final String[] ISIN_FIELDS = new String[]{
            KeysystemEnum.ISIN.name().toLowerCase(),
            IndexConstants.FIELDNAME_ALIAS
    };

    private static final String[] WKN_FIELDS = new String[]{
            KeysystemEnum.WKN.name().toLowerCase(),
            KeysystemEnum.VWDSYMBOL.name().toLowerCase()
    };

    private static final EnumSet<InstrumentTypeEnum> EMPTY_COUNT_TYPES
            = EnumSet.noneOf(InstrumentTypeEnum.class);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private InstrumentServer instrumentServer;

    /**
     * Number of instruments that will be cached, use &lt; 0 for unlimited cache, =0 for no cache.
     * a limited cache will use an LRU strategy to remove elements when it is full and a new
     * element will be added; even an unlimited cache will not cause an OoME, as the cache is
     * referenced only using a SoftReference.
     */
    private int instrumentCacheSize = 1000;

    private final Object cacheMutex = new Object();

    /**
     * Cache instruments but ensure we do not run into OutOfMemory if too many
     * are cached. Using a single SoftReference is better than storing each Instrument
     * as a SoftReference in a Map as the garbage collector has to keep track of all
     * SoftReferences during gc.
     */
    private volatile SoftReference<Map<String, Instrument>> cacheRef;

    public Map<Long, Instrument> identify(Set<String> symbols) {
        if (symbols.isEmpty()) {
            return null;
        }

        final Map<Long, Instrument> result = new HashMap<>();
        for (String s : symbols) {
            final Instrument instrument = getInstrument(s);
            if (instrument == null) {
                continue;
            }
            result.put(instrument.getId(), instrument);
        }
        return result;
    }

    public void assignInstrumentsTo(NewsRecordImpl newsRecord) {
        final Set<String> iids = newsRecord.getAttributes(NewsAttributeEnum.IID);
        if (iids.isEmpty()) {
            return;
        }

        final InstrumentRequest ir = new InstrumentRequest();
        ir.addItems(iids, InstrumentRequest.KeyType.IID);

        try {
            final InstrumentResponse response = this.instrumentServer.identify(ir);
            final List<Instrument> instruments = response.getInstruments();
            newsRecord.setInstruments(instruments);
        } catch (Exception e) {
            this.logger.warn("<assignInstrumentsTo> failed for " + newsRecord.getId()
                    + ", iids " + iids, e);
        }
    }

    public void setInstrumentServer(InstrumentServer instrumentServer) {
        this.instrumentServer = instrumentServer;
    }


    public void setInstrumentCacheSize(int instrumentCacheSize) {
        this.instrumentCacheSize = instrumentCacheSize;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.instrumentServer == null) {
            throw new IllegalStateException("instrumentServer is null");
        }
        if (this.instrumentCacheSize != 0) {
            this.cacheRef = new SoftReference<>(createInstrumentCacheMap());
        }
    }

    public void clearCache() {
        if (this.cacheRef == null) {
            this.logger.warn("<clearCache> called w/o cache");
            return;
        }
        synchronized (this.cacheMutex) {
            Map<String, Instrument> m = this.cacheRef.get();
            if (m == null) {
                this.logger.info("<clearCache> for null cache");
                return;
            }
            final int size = m.size();
            m.clear();
            this.logger.info("<clearCache> removed " + size + " instruments");
        }
    }

    private HashMap<String, Instrument> createInstrumentCacheMap() {
        if (this.instrumentCacheSize <= 0) {
            this.logger.info("<createInstrumentCacheMap> unlimited");
            return new HashMap<>();
        }
        this.logger.info("<createInstrumentCacheMap> limit: " + this.instrumentCacheSize);
        return new LinkedHashMap<String, Instrument>(this.instrumentCacheSize * 4 / 3 + 1, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, Instrument> eldest) {
                return size() > NewsSymbolIdentifierImpl.this.instrumentCacheSize;
            }
        };
    }

    private Instrument getInstrument(String symbol) {
        final String key = symbol.toLowerCase();

        if (this.cacheRef == null) {
            return identify(key);
        }

        final Instrument result;
        synchronized (this.cacheMutex) {
            Map<String, Instrument> m = this.cacheRef.get();
            if (m == null) { // gc removed our map, create a new one
                this.cacheRef = new SoftReference<>(m = createInstrumentCacheMap());
            }
            else if (m.containsKey(key)) {
                return m.get(key);
            }

            result = identify(key);
            m.put(key, result);
        }
        return result;
    }

    private Instrument identify(String symbol) {
        final String[] fields = getFields(symbol);

        for (String field : fields) {
            String se = field + ":" + symbol;
            try {
                final Instrument result = search(se);
                if (result != null) {
                    return result;
                }
            } catch (Exception e) {
                this.logger.error("<identify> failed for '" + se + "'", e);
            }
        }

        this.logger.info("<identify> no result for " + symbol + " in " + Arrays.toString(fields));
        return null;
    }

    private Instrument search(String se) throws Exception {
        final SearchRequestStringBased request = createRequest(se);
        final SearchResponse response = this.instrumentServer.search(request);
        final List<Instrument> instruments = response.getInstruments();
        if (instruments != null && !instruments.isEmpty()) {
            return instruments.get(0);
        }
        return null;
    }

    private SearchRequestStringBased createRequest(String se) {
        final SearchRequestStringBased result = new SearchRequestStringBased();
        result.setProfile(ProfileFactory.valueOf(true));
        result.setUsePaging(true);
        result.setMaxNumResults(1);
        result.setPagingCount(1);
        result.setResultType(SearchRequestResultType.QUOTE_WITH_VWDSYMBOL);
        result.setCountInstrumentResults(true);
        result.setCountTypes(EMPTY_COUNT_TYPES);
        result.setSearchExpression(se);
        return result;
    }


    private String[] getFields(String symbol) {
        return IsinUtil.isIsin(symbol) ? ISIN_FIELDS : WKN_FIELDS;
    }
}
