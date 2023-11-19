/*
 * SuggestRuleFactory.java
 *
 * Created on 18.06.2009 07:45:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.DisposableBean;

import de.marketmaker.istar.common.lifecycle.Initializable;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.IndexCompositionProvider;
import de.marketmaker.istar.instrument.IndexCompositionRequest;
import de.marketmaker.istar.instrument.IndexCompositionResponse;

import static de.marketmaker.istar.instrument.IndexConstants.FIELDNAME_IID;

/**
 * Provides a meaningful order for elements in the suggestion index.
 * <p><p>
 * Supports a number of distinct <em>strategies</em>, from which exactly one is selected for each search.
 * A german strategy, for example, will prefer the DAX and its constituents, whereas strategies for
 * other countries would most likely rank constituents of indices from that country higher.
 * <p/>
 * For each strategy, each instrument may have a particular order value (int) assigned to it.
 * Instruments will be suggested by ascending order value, i.e., from the matching instruments
 * the one with the lowest order will appear at the top.
 * <p/>
 * The overall goal is to show the most "important" instruments first, and importance is measured
 * by reading data from a "ranking" file, that contains trade volume information for the
 * past week(?) based on the assumption that volume correlates with importance.
 * <p><p>
 * In order to understand what this class does, see the comments in the file <tt>suggestion.properties</tt>.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SuggestionRankingsImpl implements Initializable, DisposableBean, SuggestionRankings {
    private static class Item implements Comparable<Item> {
        private final Long iid;

        private int[] orders; // one order for each strategy

        private final long rank; // from the instruments ranking file

        private Item(Long iid, long rank, int[] n) {
            this.iid = iid;
            this.rank = rank;
            this.orders = n;
        }

        public int compareTo(Item o) {
            return Long.compare(o.rank, this.rank); // reversed order, highest rank at the front
        }

        /**
         * Sets the n-th order to value if force is true or the current value is the default value.
         *
         * @param n     n-th order value is to be set
         * @param value to be used
         * @param force whether to override an already set non-default value
         * @return true iff value has been used
         */
        public boolean setOrder(int n, int value, boolean force) {
            if (force || value < this.orders[n]) {
                this.orders[n] = value;
                return true;
            }
            return false;
        }

        public String toString() {
            return "Item[" + this.iid + ", " + this.rank + ", " + Arrays.toString(this.orders) + "]";
        }
    }

    private static final int DEFAULT_ORDER = 65535;

    private static final long ONE = 1L;

    private static final long ZERO = 0L;

    private IndexCompositionProvider indexCompositionProvider;

    private int defaultOrder = DEFAULT_ORDER;

    private File indexDir;

    private IndexReader indexReader;

    private IndexSearcher indexSearcher;

    private InstrumentDao instrumentDao;

    private Map<InstrumentTypeEnum, Map<Long, Item>> items
            = new EnumMap<>(InstrumentTypeEnum.class);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File ranksFile;

    private Properties stragegies;

    private File strategyFile;

    private String[] strategyNames;
    
    private int[] orders;

    public SuggestionRankingsImpl() {
        this.items.put(InstrumentTypeEnum.FND, new HashMap<Long, Item>());
        this.items.put(InstrumentTypeEnum.IND, new HashMap<Long, Item>());
        this.items.put(InstrumentTypeEnum.STK, new HashMap<Long, Item>());
    }

    public void setDefaultOrder(int defaultOrder) {
        this.defaultOrder = defaultOrder;
    }

    public void destroy() throws Exception {
        IoUtils.close(this.indexReader);
        IoUtils.close(this.indexSearcher);
    }

    /**
     * @param instrument for which order is requested
     * @param i          strategy index
     * @return returns the instrument's order for suggestions. Lower values indicate a higher rank.
     */
    public short getOrder(Instrument instrument, int i) {
        final Map<Long, Item> map = getTypeMap(instrument.getInstrumentType());
        if (map == null) {
            return Short.MAX_VALUE;
        }

        Item item = map.get(instrument.getId());
        if (item == null) {
            item = map.get(ZERO);
        }

        return (short) (item.orders[i] - Short.MAX_VALUE - 1);
    }

    @Override
    public int getIntOrder(Instrument instrument, int i) {
        final Map<Long, Item> map = getTypeMap(instrument.getInstrumentType());
        if (map == null) {
            return this.defaultOrder;
        }

        Item item = map.get(instrument.getId());
        if (item == null) {
            item = map.get(ZERO);
        }

        return item.orders[i];
    }

    private Map<Long, Item> getTypeMap(final InstrumentTypeEnum type) {
        if (type == InstrumentTypeEnum.GNS) {
            // takes care of Roche GNS, member of SMI
            return this.items.get(InstrumentTypeEnum.STK); 
        }
        return this.items.get(type);
    }

    public String[] getStrategyNames() {
        return strategyNames;
    }

    public void initialize() throws Exception {
        this.indexReader = IndexReader.open(FSDirectory.open(this.indexDir), true);
        this.logger.info("<initialize> opened index in " + this.indexDir.getAbsolutePath()
                + ", numDocs=" + this.indexReader.numDocs());
        this.indexSearcher = new IndexSearcher(this.indexReader);
        loadStrategies();
        this.orders = new int[this.strategyNames.length];
        readRanks();
        processStrategies();
    }

    public void setIndexCompositionProvider(IndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setIndexDir(File indexDir) {
        this.indexDir = indexDir;
    }

    public void setInstrumentDao(InstrumentDao idf) {
        this.instrumentDao = idf;
    }

    public void setRanksFile(File ranksFile) {
        this.ranksFile = ranksFile;
    }

    public void setStrategyFile(File strategyFile) {
        this.strategyFile = strategyFile;
    }

    public boolean hasFundRank(Long id) {
        return this.items.get(InstrumentTypeEnum.FND).get(id) != null;
    }

    /**
     * Adds an order value for a given vwdcode
     *
     * @param vwdcode code for which order is to be added
     * @param n       index of the strategy in the orders array
     * @param items if vwdcode specifies an index, its constituents should be added to items.
     * @throws IOException on error
     */
    private void add(String vwdcode, int n, List<Item> items) throws IOException {
        final TopDocs docs =
                this.indexSearcher.search(new TermQuery(new Term("vwdcode", vwdcode.toLowerCase())), 1);
        if (docs.totalHits == 0) {
            this.logger.warn("<add> no such vwdcode: " + vwdcode);
            return;
        }
        final Document doc = this.indexReader.document(docs.scoreDocs[0].doc);
        final long iid = Long.parseLong(doc.get(FIELDNAME_IID));
        final Instrument instrument = this.instrumentDao.getInstrument(iid);

        final Map<Long, Item> map = getTypeMap(instrument.getInstrumentType());
        if (map == null) {
            this.logger.warn("<add> invalid type for '" + vwdcode + "': " + instrument.getInstrumentType());
            return;
        }

        final Item item = getOrCreateItem(instrument.getId(), map);
        item.setOrder(n, this.orders[n]++, true);

        if (instrument.getInstrumentType() == InstrumentTypeEnum.IND) {
            items.addAll(getItemsForConstituents(instrument, vwdcode));
        }
    }

    private List<Item> getItemsForConstituents(Instrument instrument, String vwdcode) {
        final long qid = getQuoteId(instrument, vwdcode);
        IndexCompositionResponse response
                = this.indexCompositionProvider.getIndexComposition(new IndexCompositionRequest(qid));
        if (!response.isValid()) {
            return Collections.emptyList();
        }
        return getPositionItems(response.getIndexComposition().getIids());
    }

    private void setPositionForItems(int n, List<Item> items) {
        items.sort(null);
        for (final Item position : items) {
            if (position.setOrder(n, this.orders[n], false)) {
                this.orders[n]++;
            }
        }
    }

    private List<Item> getPositionItems(List<Long> positions) {
        final List<Item> result = new ArrayList<>(positions.size());
        for (Long positionIid : positions) {
            result.add(getItemForStk(positionIid));
        }
        return result;
    }

    private void addDefaultOrder(int n, Map<Long, Item> map, int defaultOrder) {
        final Item defaultItem = getOrCreateItem(ZERO, map);
        defaultItem.setOrder(n, defaultOrder, true);
        map.put(ZERO, defaultItem);
    }

    private Item getOrCreateItem(Long iid, Map<Long, Item> map) {
        final Item existing = map.get(iid);
        if (existing != null) {
            return existing;
        }
        // item was not in ranking file, so create it with zero rank
        final Item result = new Item(iid, ZERO, newOrders());
        map.put(result.iid, result);
        return result;
    }

    private Item getItemForStk(Long iid) {
        final Map<Long, Item> map = this.items.get(InstrumentTypeEnum.STK);
        return getOrCreateItem(iid, map);
    }

    private long getQuoteId(Instrument instrument, String code) {
        for (Quote quote : instrument.getQuotes()) {
            if (code.equals(quote.getSymbolVwdcode())) {
                return quote.getId();
            }
        }
        return 0;
    }

    private void loadStrategies() throws IOException {
        if (this.strategyFile != null) {
            this.stragegies = PropertiesLoader.load(this.strategyFile);
        } else {
            this.stragegies = PropertiesLoader.load(getClass().getResourceAsStream("suggestion.properties"));
        }
        this.strategyNames = stragegies.getProperty("strategies").split(",");
        this.logger.info("<loadStrategies> names = " + Arrays.toString(this.strategyNames));
    }

    private void processStrategies() throws IOException {
        for (int i = 0; i < strategyNames.length; i++) {
            processStrategy(i);
        }
    }

    private void processStrategy(int n) throws IOException {
        processExplicitOrders(n);
        processTypeOrders(n);
    }

    private void processTypeOrders(int n) {
        for (InstrumentTypeEnum typeEnum : items.keySet()) {
            final Map<Long, Item> map = this.items.get(typeEnum);

            final String s = this.stragegies.getProperty(strategyNames[n] + "." + typeEnum.name());
            if (s == null) {
                addDefaultOrder(n, map, this.defaultOrder);
                continue;
            }
            final String[] orders = s.split(",");
            final int from = Integer.parseInt(orders[0]);
            final int to = Integer.parseInt(orders[1]);

            List<Item> items = new ArrayList<>(map.values());
            items.sort(null);

            double step = (to - from) / (double) items.size();
            double order = from;
            for (Item item : items) {
                item.setOrder(n, (int) order, false);
                order += step;
            }
            addDefaultOrder(n, map, Integer.parseInt(orders[2]));
        }
    }

    private void processExplicitOrders(int n) throws IOException {
        for (int i = 1; ; i++) {
            final String s = this.stragegies.getProperty(strategyNames[n] + "." + i);
            if (s == null) {
                this.logger.info("<processExplicitOrders> max order for " + strategyNames[n]
                        + " is " + this.orders[n]);
                return;
            }
            ArrayList<Item> items = new ArrayList<>(200);
            for (String code : s.split(",")) {
                add(code, n, items);
            }
            if (!items.isEmpty()) {
                setPositionForItems(n, items);
            }
        }
    }

    private void readRanks() throws Exception {
        new IstarMdpExportReader<Void>(false) {
            protected Void getResult() {
                return null;
            }

            protected void handleRow() {
                final Long iid = getLong("IID");
                final InstrumentTypeEnum type = InstrumentTypeEnum.valueOf(getInt("TYPE"));
                final long rank = Math.round(Double.parseDouble(get("RANK")));
                if (rank > ONE) {
                    final Item item = new Item(iid, rank, newOrders());
                    items.get(type).put(item.iid, item);
                }
            }
        }.read(this.ranksFile);

        this.logger.info("<readRanks> read " + this.items.size());
    }

    private int[] newOrders() {
        int[] result = new int[this.strategyNames.length];
        Arrays.fill(result, this.defaultOrder);
        return result;
    }
}
