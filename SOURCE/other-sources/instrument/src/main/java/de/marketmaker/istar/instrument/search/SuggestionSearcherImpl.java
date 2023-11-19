/*
 * SuggestionSearcherImpl.java
 *
 * Created on 17.06.2009 12:14:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.util.FileCopyUtils;
import org.xerial.snappy.Snappy;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.SuggestedInstrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domainimpl.data.SuggestedInstrumentImpl;
import de.marketmaker.istar.instrument.IndexConstants;
import de.marketmaker.istar.instrument.protobuf.InstrumentProtos;

import static de.marketmaker.istar.domain.KeysystemEnum.WM_WP_NAME_KURZ;
import static de.marketmaker.istar.instrument.SuggestIndexConstants.*;

/**
 * Searches an index that is used to provide suggestions to users that type a few characters
 * in a SuggestBox. Uses a lucene index for searching and then data serialized in a small
 * file to retrieve the data for the hits. Index and file have been created by a
 * {@link de.marketmaker.istar.instrument.export.SuggestionExporter}, so whenever that class
 * changes how data is serialized, this class will have to be adapted as well.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SuggestionSearcherImpl implements SuggestionSearcher {
    private static final Charset CS = Charset.forName("UTF-8");

    private static final String[] OTHER_FIELDS = new String[]{
            FIELDNAME_ISIN, FIELDNAME_WKN
    };

    private static final Map<String, KeysystemEnum> NAME_TO_KEYSYSTEM = new HashMap<>();

    static {
        NAME_TO_KEYSYSTEM.put(FIELDNAME_NAME, KeysystemEnum.DUMMY);
        NAME_TO_KEYSYSTEM.put(FIELDNAME_WM_WP_NAME_KURZ, WM_WP_NAME_KURZ);
        NAME_TO_KEYSYSTEM.put(FIELDNAME_NAME_COST, KeysystemEnum.PM_INSTRUMENT_NAME);
        NAME_TO_KEYSYSTEM.put(FIELDNAME_NAME_FREE, KeysystemEnum.PM_INSTRUMENT_NAME_FREE);
    }

    private static final int MAX_VERSION = 2;

    private static FieldCache.IntParser OFFSET_PARSER = new FieldCache.IntParser() {
        @Override
        public int parseInt(String s) {
            return Integer.parseInt(s, Character.MAX_RADIX);
        }
    };

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ByteBuffer buffer;

    private final IndexReader indexReader;

    private final IndexSearcher indexSearcher;

    private final SearchFilterFactory filterFactory;

    private final String[] orderStrategies;

    private final int[] offsets;

    private final boolean withWmWpNameKurz;

    private final String constraints;

    private final int version;

    public static SuggestionSearcherImpl create(File baseDir, String constraints) {
        final Logger log = LoggerFactory.getLogger(SuggestionSearcherImpl.class);

        final File dataDir = new File(baseDir, "data");
        final File indexDir = new File(baseDir, "index-suggest");
        if (!dataDir.isDirectory()) {
            log.warn("<create> no such directory " + dataDir.getAbsolutePath());
            return null;
        }
        if (!indexDir.isDirectory()) {
            log.warn("<create> no such directory " + indexDir.getAbsolutePath());
            return null;
        }

        try {
            return new SuggestionSearcherImpl(dataDir, indexDir, constraints);
        } catch (IOException e) {
            log.error("<create> failed", e);
        }
        return null;
    }

    private SuggestionSearcherImpl(File dataDir, File indexDir, String constraints)
            throws IOException {
        this.buffer = mapData(dataDir);
        this.logger.info("<SuggestionSearcherImpl> mapped " + this.buffer.remaining() + " bytes");
        this.buffer.position(0);

        if (this.buffer.get() == 0) {
            // version > 0: first two bytes are 0x00 0x.., the second being the version
            version = this.buffer.get() & 0xFF;
        }
        else {
            // version == 0: file starts with number of strategies (always != 0)
            version = 0;
            this.buffer.position(0);
        }
        if (version > MAX_VERSION) {
            throw new IllegalStateException("cannot handle version " + version);
        }
        this.withWmWpNameKurz = version > 0;

        final int numOrders = this.buffer.get();
        this.orderStrategies = new String[numOrders];
        for (int i = 0; i < numOrders; i++) {
            this.orderStrategies[i] = readString(this.buffer);
        }
        this.logger.info("orderStrategies = " + Arrays.toString(orderStrategies));

        final Directory dir = FSDirectory.open(indexDir);
        this.indexReader = IndexReader.open(dir, true);
        this.indexSearcher = new IndexSearcher(indexReader);

        this.offsets = FieldCache.DEFAULT.getInts(indexReader, FIELDNAME_OFFSET, OFFSET_PARSER);
        this.logger.info("<SuggestionSearcherImpl> read " + this.offsets.length + " offsets");

        this.filterFactory = new SearchFilterFactory(this.indexReader);
        this.constraints = constraints;
    }

    private int getStrategyIndex(String strategy) {
        if (strategy == null) {
            return 0;
        }
        for (int i = 0; i < this.orderStrategies.length; i++) {
            if (this.orderStrategies[i].equals(strategy)) {
                return i;
            }
        }
        this.logger.warn("<getOrderIndex> invalid strategy: '" + strategy + "'");
        return 0;
    }


    public List<SuggestedInstrument> query(SuggestRequest request) throws Exception {
        TimeTaker tt = new TimeTaker();
        final int limit = request.getLimit();

        final int strategyIndex = getStrategyIndex(request.getStrategy());
        final KeysystemEnum nameKeysystem = NAME_TO_KEYSYSTEM.get(request.getNameField());

        final Filter f = this.filterFactory.createFilter(request, this.constraints);
        final Sort sort = new Sort(new SortField(this.orderStrategies[strategyIndex], SortField.SHORT));

        Query typeQuery = createTypeQuery(request);

        Query query = createQuery(request.getNameField(), request.getQuery(), typeQuery);
        TopDocs docs = indexSearcher.search(query, f, limit, sort);

        final List<SuggestedInstrument> result = getItems(docs, limit, strategyIndex, nameKeysystem);

        if (docs.totalHits < limit) {
            // not enough hits on name, query isin/wkn as well
            final Query bq = and(symbolQuery(request), typeQuery);
            final int num = limit - result.size();
            docs = this.indexSearcher.search(bq, f, num, sort);
            result.addAll(getItems(docs, num, strategyIndex, nameKeysystem));
        }
        result.sort(null);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<query> for " + request + " took " + tt);
        }

        return result;
    }

    private BooleanQuery symbolQuery(SuggestRequest request) {
        final BooleanQuery result = new BooleanQuery();
        for (String field : OTHER_FIELDS) {
            result.add(termQuery(field, request.getQuery().toLowerCase()), BooleanClause.Occur.SHOULD);
        }
        return result;
    }

    private Query createTypeQuery(SuggestRequest request) {
        if (request.getType() == null) {
            return null;
        }
        return termQuery(IndexConstants.FIELDNAME_TYPE, request.getType().name());
    }

    private Query createQuery(String field, String value, Query typeQuery) {
        return and(termQuery(field, value.toLowerCase()), typeQuery);
    }

    private Query and(Query q1, Query q2) {
        if (q1 == null) {
            return q2;
        }
        if (q2 == null) {
            return q1;
        }
        BooleanQuery result = new BooleanQuery(true);
        result.add(q1, BooleanClause.Occur.MUST);
        result.add(q2, BooleanClause.Occur.MUST);
        return result;
    }

    private Query termQuery(String field, String value) {
        return new TermQuery(new Term(field, value));
    }


    private List<SuggestedInstrument> getItems(TopDocs docs, int num, int orderIndex,
            KeysystemEnum nameKeysystem) throws IOException {
        final List<SuggestedInstrument> result = new ArrayList<>(num);
        final ByteBuffer bb = this.buffer.duplicate();
        for (int n = 0, m = Math.min(num, docs.totalHits); n < m; n++) {
            final int offset = this.offsets[docs.scoreDocs[n].doc];
            result.add(getSuggestedInstrument(bb, offset, orderIndex, nameKeysystem));
        }
        return result;
    }

    private SuggestedInstrumentImpl getSuggestedInstrument(ByteBuffer bb, int offset,
            int orderIndex, KeysystemEnum nameKeysystem) throws IOException {
        return (this.version < 2)
                ? getSuggestedInstrumentV1(bb, offset, orderIndex, nameKeysystem)
                : getSuggestedInstrumentV2(bb, offset, orderIndex, nameKeysystem);
    }

    private SuggestedInstrumentImpl getSuggestedInstrumentV1(ByteBuffer bb, int offset,
            int orderIndex, KeysystemEnum nameKeysystem) {
        bb.position(offset + orderIndex * 2);
        final int order = bb.getShort() + Short.MAX_VALUE + 1;
        bb.position(offset + this.orderStrategies.length * 2);

        final long iid = bb.getLong();
        final InstrumentTypeEnum type = InstrumentTypeEnum.values()[bb.get()];
        final String name = readString(bb);
        final String wmWpNameKurz = this.withWmWpNameKurz ? readString(bb) : null;

        return new SuggestedInstrumentImpl(order, iid, type,
                (wmWpNameKurz != null && nameKeysystem ==  WM_WP_NAME_KURZ) ? wmWpNameKurz : name,
                readString(bb), readString(bb));
    }

    private SuggestedInstrumentImpl getSuggestedInstrumentV2(ByteBuffer bb, int offset,
            int orderIndex, KeysystemEnum nameKeysystem) throws IOException {
        bb.position(offset);

        InstrumentProtos.Suggestion s
                = InstrumentProtos.Suggestion.parseFrom(getAndUncompress(bb));

        final long iid = s.getId();
        final int order = s.getRanks(orderIndex);
        final InstrumentTypeEnum type = InstrumentTypeEnum.values()[s.getTypeOrd()];

        String name = getName(s, nameKeysystem);
        String isin = s.hasIsin() ? s.getIsin() : null;
        String wkn = s.hasWkn() ? s.getWkn() : null;

        return new SuggestedInstrumentImpl(order, iid, type, name, isin, wkn);
    }

    private String getName(InstrumentProtos.Suggestion s, KeysystemEnum ks) {
        switch (ks) {
            case WM_WP_NAME_KURZ:
                if (s.hasWmWpNameKurz()) {
                    return s.getWmWpNameKurz();
                }
                break;
            case PM_INSTRUMENT_NAME:
                if (s.hasPmNameCost()) {
                    return s.getPmNameCost();
                }
                break;
            case PM_INSTRUMENT_NAME_FREE:
                if (s.hasPmNameFree()) {
                    return s.getPmNameFree();
                }
                break;
        }
        return s.getName();
    }

    private byte[] getAndUncompress(ByteBuffer bb) throws IOException {
        byte[] data = new byte[bb.getShort() & 0xFFFF];
        bb.get(data);
        return Snappy.uncompress(data);
    }

    private String readString(ByteBuffer bb) {
        final int len = bb.get() & 0xFF;
        if (len == 0) {
            return null;
        }
        final byte[] b = new byte[len];
        bb.get(b);
        return new String(b, CS);
    }

    private ByteBuffer mapData(File dataDir) throws IOException {
        return ByteBuffer.wrap(FileCopyUtils.copyToByteArray(new File(dataDir, "suggest.dat")));
    }

    public void close() throws IOException {
        this.indexReader.close();
        this.indexSearcher.close();
    }
}
