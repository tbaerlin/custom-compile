/*
 * InstrumentIndexer.java
 *
 * Created on 19.11.2010 14:34:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.ItemWithSymbols;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.ContentFlags;
import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.Future;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import de.marketmaker.istar.instrument.IndexConstants;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.instrument.search.AnalyzerFactory;

import static de.marketmaker.istar.domain.KeysystemEnum.*;
import static de.marketmaker.istar.domain.instrument.ContentFlags.Flag.*;
import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.*;
import static de.marketmaker.istar.instrument.IndexConstants.*;
import static de.marketmaker.istar.instrument.export.ScoachInstrumentAdaptor.BIS_KEY_PREFIX_FFM;
import static de.marketmaker.istar.instrument.export.ScoachInstrumentAdaptor.FFMST;
import static de.marketmaker.istar.instrument.export.ScoachInstrumentAdaptor.replaceBisKeyMarketPrefix;
import static org.apache.lucene.index.IndexWriter.MaxFieldLength.LIMITED;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author zzhao
 * @since 1.2
 */
public class InstrumentIndexer {

    private static final Field CALL = IndexerUtil.noNorms(FIELDNAME_WM_WP_NAME_ZUSATZ, "call");

    private static final Field PUT = IndexerUtil.noNorms(FIELDNAME_WM_WP_NAME_ZUSATZ, "put");

    private static final Field LONG = IndexerUtil.noNorms(FIELDNAME_WM_WP_NAME_ZUSATZ, "long");

    private static final Field SHORT = IndexerUtil.noNorms(FIELDNAME_WM_WP_NAME_ZUSATZ, "short");

    private static final Field NOT_MM_XML_BLACKLISTED = IndexerUtil.noNorms(FIELDNAME_IS_MMXML_BLACKLIST, FIELD_VALUE_BOOLEAN_FALSE);

    private static final Field WITH_MMWKN = IndexerUtil.noNorms(FIELDNAME_QUOTESYMBOLS, KeysystemEnum.MMWKN.name().toLowerCase());

    private static final Field WITH_MMTYPE = IndexerUtil.noNorms(FIELDNAME_WITH_MMTYPE, FIELD_VALUE_BOOLEAN_TRUE);

    private static final Field WITH_VWDFEED = IndexerUtil.noNorms(FIELDNAME_QUOTESYMBOLS, VWDFEED.name().toLowerCase());

    private static final EnumMap<ContentFlags.Flag, String> FLAGS
            = new EnumMap<>(ContentFlags.Flag.class);

    static {
        for (ContentFlags.Flag flag : new ContentFlags.Flag[]
                {CerUnderlying, FutUnderlying, OptUnderlying, WntUnderlying}) {
            FLAGS.put(flag, flag.name().toLowerCase());
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File synonymNames;

    private final Map<Long, String[]> synonyms = new HashMap<>();

    private final StringBuilder namesStb = new StringBuilder();

    private boolean updateInstrumentMode;

    private boolean optimize = true;

    private boolean useCompoundFile = true;

    private int rAMBufferSizeMB = 48;

    private int mergeFactor = 20;

    private int numExcluded = 0;

    private List<InstrumentSorter> instrumentSorters = Collections.emptyList();

    private List<QuoteSorter> quoteSorters = Collections.emptyList();

    private Predicate<Instrument> excludeFilter;

    public void setExcludeFilter(Predicate<Instrument> excludeFilter) {
        this.excludeFilter = excludeFilter;
    }

    public void setRAMBufferSizeMB(int rAMBufferSizeMB) {
        this.rAMBufferSizeMB = rAMBufferSizeMB;
    }

    public void setMergeFactor(int mergeFactor) {
        this.mergeFactor = mergeFactor;
    }

    public void setSynonymNames(File synonymNames) {
        this.synonymNames = synonymNames;
    }

    public void setUseCompoundFile(boolean useCompoundFile) {
        this.useCompoundFile = useCompoundFile;
    }

    public void setInstrumentSorters(List<InstrumentSorter> instrumentSorters) {
        this.instrumentSorters = instrumentSorters;
    }

    public void setQuoteSorters(List<QuoteSorter> quoteSorters) {
        this.quoteSorters = quoteSorters;
    }

    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    public void setUpdateInstrumentMode(boolean updateInstrumentMode) {
        this.updateInstrumentMode = updateInstrumentMode;
    }

    /**
     * Creates index for the given instruments(dao) into the given index directory.
     * @param instrumentIndexDir a directory into which the index files are written.
     * @param dao an instrument DAO
     * @throws Exception if any occurred during indexing
     */
    public void index(File instrumentIndexDir, InstrumentDao dao) throws Exception {
        Assert.notNull(dao, "instrument DAO required");

        if (dao.size() > 0) {
            Assert.isTrue(null != instrumentIndexDir && instrumentIndexDir.exists(),
                    "instrument index directory required");
            final TimeTaker tt = new TimeTaker();

            prepareIndexer(instrumentIndexDir); // index file read mode

            final IndexWriter idxWriter = createIndexWriter(instrumentIndexDir,
                    AnalyzerFactory.getIndexAnalyzer(), true); // index file write mode

            try {
                final int n = indexInternal(idxWriter, dao, tt);
                this.logger.info("<index> " + n + " instruments indexed in: " + tt
                    + (this.numExcluded > 0 ? ", #excluded=" + this.numExcluded : ""));
            } finally {
                IndexerUtil.close(idxWriter, this.optimize);
                afterInstrumentsIndexed();
            }
        }
        else {
            this.logger.warn("<index> no instruments to index - instrument index directory " +
                    "untouched, make sure not to use old index");
        }
    }

    private void afterInstrumentsIndexed() {
        for (final InstrumentSorter is : this.instrumentSorters) {
            is.afterInstrumentIndexed();
        }
    }

    private int indexInternal(IndexWriter idxWriter, InstrumentDao dao, TimeTaker tt) throws Exception {
        int n = 0;
        for (final Instrument ins : dao) {
            if (isToBeIndexed(ins)) {
                indexInstrument(ins, idxWriter);
            }
            if (0 == (++n % 10000)) {
                this.logger.info("<indexInternal> indexed " + n + " in " + tt);
            }
        }
        return n;
    }

    private boolean isToBeIndexed(Instrument ins) {
        if (this.excludeFilter != null && this.excludeFilter.test(ins)) {
            this.numExcluded++;
            return false;
        }
        return !ins.getQuotes().isEmpty();
    }

    private void indexInstrument(final Instrument instrument, IndexWriter idxWriter) throws Exception {
        final String names = getNames(instrument);
        final List<Fieldable> insFields = getInstrumentFields(instrument);

        for (QuoteSorter quoteSorter : quoteSorters) {
            quoteSorter.prepare(instrument);
        }

        for (Quote quote : instrument.getQuotes()) {
            final Document doc = new Document();

            insFields.forEach(doc::add);
            for (QuoteSorter quoteSorter : this.quoteSorters) {
                doc.add(IndexerUtil.noNorms(quoteSorter.getSortField().getField(),
                        Integer.toString(quoteSorter.getOrder(quote))));
            }

            addQuote(doc, quote);
            doc.add(IndexerUtil.unstored(FIELDNAME_NAMES, names));
            idxWriter.addDocument(doc);
        }
    }

    private void addQuote(Document doc, Quote quote) throws Exception {
        doc.add(IndexerUtil.keyword(FIELDNAME_QID, Long.toString(quote.getId())));
        addSymbols(doc, quote, IndexConstants.QUOTE_SYMBOLS);
        addSecondarySymbols(doc, quote.getSymbolVwdfeedSecondary());

        // we also add the vwd market under the same fieldname;
        doc.add(IndexerUtil.noNorms(FIELDNAME_MARKET, Long.toString(quote.getMarket().getId())));
        add(doc, quote.getCurrency());

        final String symbolVwdfeed = quote.getSymbolVwdfeed();
        if (StringUtils.hasText(symbolVwdfeed)) {
            doc.add(WITH_VWDFEED);

            // ISTAR-395: for FFMST, always add the FFM bis_key as alias
            if (symbolVwdfeed.endsWith(FFMST)) {
                addSymbol(doc, QUOTE_SYMBOLS.get(KeysystemEnum.BIS_KEY),
                        replaceBisKeyMarketPrefix(quote.getSymbolBisKey(), BIS_KEY_PREFIX_FFM));
            }

            final String market = quote.getSymbolVwdfeedMarket();
            doc.add(IndexerUtil.noNorms(FIELDNAME_MARKET, market));
            if (quote.getMarket().getId() == quote.getInstrument().getHomeExchange().getId()) {
                doc.add(IndexerUtil.noNorms(FIELDNAME_HOME_EXCHANGE, market));
            }
            if (!symbolVwdfeed.startsWith("1.") || !MARKET_BLACKLIST_MM_XML.contains(market)) {
                doc.add(NOT_MM_XML_BLACKLISTED);
            }
        }
        if (StringUtils.hasText(quote.getSymbol(KeysystemEnum.MMWKN))) {
            doc.add(WITH_MMWKN);
        }

        final InstrumentTypeEnum type = quote.getInstrument().getInstrumentType();
        if (type == WNT || type == CER || type == OPT) {
            add(doc, getCallOrPut(quote));
        }

        addEntitlements(doc, quote, VWDFEED, FIELDNAME_ENTITLEMENT_VWD);
        addEntitlements(doc, quote, KeysystemEnum.MM, FIELDNAME_ENTITLEMENT_ABO);

        final ContentFlags cfs = quote.getContentFlags();
        for (Map.Entry<ContentFlags.Flag, String> e : FLAGS.entrySet()) {
            if (cfs.hasFlag(e.getKey())) {
                doc.add(IndexerUtil.noNorms(FIELDNAME_FLAG, e.getValue()));
            }
        }
    }

    private Field getCallOrPut(Quote quote) {
        if (InstrumentUtil.isOpraInstrument(quote.getInstrument())) {
            final String s = quote.getSymbolVwdfeed();
            // symbolVwdfeed for OPRA ends with maturity which is [0-9][A-Y]
            return (s != null) ? (s.charAt(s.length() - 1) <= 'L' ? CALL : PUT) : null;
        }
        return getCallOrPutFromNameZusatz(quote);
    }

    private Field getCallOrPutFromNameZusatz(Quote quote) {
        final String symbol = quote.getSymbol(KeysystemEnum.WM_WP_NAME_ZUSATZ);
        if (!StringUtils.hasText(symbol)) {
            return null;
        }
        final String s = symbol.toLowerCase();
        return s.contains("kos") ? CALL : (s.contains("vos") ? PUT : null);
    }

    private void addEntitlements(Document doc, Quote quote, final KeysystemEnum ks,
            final String field) {
        final String[] ents = quote.getEntitlement().getEntitlements(ks);
        for (String ent : ents) {
            doc.add(IndexerUtil.noNorms(field, ent));
        }
    }

    private List<Fieldable> getInstrumentFields(Instrument instrument) {
        final List<Fieldable> result = getInstrumentWeights(instrument);
        final Document doc = new Document();
        addInstrument(doc, instrument);
        result.addAll(doc.getFields());
        return result;
    }

    private void addInstrument(Document doc, final Instrument instrument) {
        doc.add(IndexerUtil.keyword(IndexConstants.FIELDNAME_IID, Long.toString(instrument.getId())));
        addSymbols(doc, instrument, IndexConstants.INSTRUMENT_SYMBOLS);

        doc.add(IndexerUtil.noNorms(FIELDNAME_HOME_EXCHANGE, Long.toString(instrument.getHomeExchange().getId())));
        add(doc, instrument.getCountry());

        if (StringUtils.hasText(instrument.getName())) {
            doc.add(IndexerUtil.unstored(FIELDNAME_NAME, instrument.getName()));
        }

        if (FeatureFlags.Flag.NEW_WP_NAMES.isEnabled()) {
            String nameCost = instrument.getSymbol(KeysystemEnum.PM_INSTRUMENT_NAME);
            if (StringUtils.hasText(nameCost)) {
                doc.add(IndexerUtil.unstored(FIELDNAME_NAME_COST, nameCost));
            }
            String nameFree = instrument.getSymbol(KeysystemEnum.PM_INSTRUMENT_NAME);
            if (StringUtils.hasText(nameFree)) {
                doc.add(IndexerUtil.unstored(FIELDNAME_NAME_FREE, nameFree));
            }
        }

        addAliases(doc, instrument);
        addLei(doc, instrument);
        addSynonyms(doc, instrument);

        doc.add(IndexerUtil.noNorms(FIELDNAME_TYPE, instrument.getInstrumentType().name()));
        final String wmtype = instrument.getDetailedInstrumentType().getSymbol(KeysystemEnum.WM_GD195_ID);
        if (StringUtils.hasText(wmtype)) {
            doc.add(IndexerUtil.noNorms(FIELDNAME_WMTYPE, wmtype));
        }

        if (instrument instanceof Derivative) {
            final long underlyingId = ((Derivative) instrument).getUnderlyingId();
            if (underlyingId > 0) {
                doc.add(IndexerUtil.noNorms(FIELDNAME_UNDERLYINGID, Long.toString(underlyingId)));
            }
        }

        if (instrument instanceof Future) {
            final long underlyingId = ((Future) instrument).getUnderlyingProductId();
            if (underlyingId > 0) {
                doc.add(IndexerUtil.noNorms(FIELDNAME_UNDERLYINGID, Long.toString(underlyingId)));
            }
        }

        // HACK. It would be better to be able to derive long/short based on some field
        if (instrument.getInstrumentType() == CER && instrument.getName() != null) {
            if (instrument.getName().indexOf(" L ") > 0) {
                doc.add(LONG);
            }
            else if (instrument.getName().indexOf(" S ") > 0) {
                doc.add(SHORT);
            }
        }

        if (instrument.getMmInstrumentclass() != null) {
            doc.add(WITH_MMTYPE);
        }
    }

    private void addAliases(Document doc, Instrument instrument) {
        // istar-136
        final String aliases = ((InstrumentDp2) instrument).getAliases();
        if (!StringUtils.hasText(aliases)) {
            return;
        }
        for (String alias : aliases.split(";")) {
            doc.add(IndexerUtil.unstored(FIELDNAME_ALIAS, alias));
        }
    }

    private void addLei(Document doc, Instrument instrument) {
        final String lei = ((InstrumentDp2) instrument).getLei();
        if (!StringUtils.hasText(lei)) {
            return;
        }
        doc.add(IndexerUtil.unstored(FIELDNAME_LEI, lei));
    }

    private void addSynonyms(Document doc, Instrument instrument) {
        final String[] synonym = synonyms.get(instrument.getId());
        if (synonym != null) {
            for (String s : synonym) {
                addSynonym(doc, s.trim());
            }
        }
        addSynonym(doc, instrument.getSymbol(KeysystemEnum.MDP_SYNONYM_NAME));

        // ISTAR-456
        if (instrument.getInstrumentType() == FND && instrument.getName().startsWith("D&R ")) {
            addSynonym(doc, "donner");
            addSynonym(doc, "reuschel");
        }
    }

    private void addSecondarySymbols(Document doc, final String secondary) {
        if (secondary == null) {
            return;
        }
        // istar-534 secondary symbol is indexed using the same fields as the primary symbol
        addSymbol(doc, QUOTE_SYMBOLS.get(VWDFEED), secondary);
        final String code = secondary.substring(secondary.indexOf('.') + 1);
        addSymbol(doc, QUOTE_SYMBOLS.get(KeysystemEnum.VWDCODE), code);
        final int p = code.indexOf('.');
        final String symbol = p > 0 ? code.substring(0, p) : code;
        addSymbol(doc, QUOTE_SYMBOLS.get(VWDSYMBOL), symbol);
    }

    private void addSymbols(Document doc, ItemWithSymbols item, Map<KeysystemEnum, String> enums) {
        for (final Map.Entry<KeysystemEnum, String> entry : enums.entrySet()) {
            addSymbol(doc, entry.getValue(), item.getSymbol(entry.getKey()));
        }
    }

    private void addSymbol(Document doc, final String key, String symbol) {
        if (StringUtils.hasText(symbol)) {
            doc.add(IndexerUtil.unstored(key, symbol));
        }
    }

    private void addSynonym(Document doc, String synonym) {
        if (StringUtils.hasText(synonym)) {
            doc.add(IndexerUtil.unstored(FIELDNAME_NAME, synonym));
        }
    }

    private void add(Document doc, Fieldable f) {
        if (f != null) {
            doc.add(f);
        }
    }

    private void add(Document doc, final Country country) {
        add(doc, country, FIELDNAME_COUNTRY);
    }

    private void add(Document doc, final Currency currency) {
        add(doc, currency, FIELDNAME_CURRENCY);
    }

    private void add(Document doc, final ItemWithSymbols item, String fieldname) {
        doc.add(IndexerUtil.noNorms(fieldname, Long.toString(item.getId())));
        final String isoSymbol = item.getSymbol(KeysystemEnum.ISO);
        if (StringUtils.hasText(isoSymbol)) {
            doc.add(IndexerUtil.noNorms(fieldname, isoSymbol));
        }
    }

    private List<Fieldable> getInstrumentWeights(Instrument instrument) {
        final List<Fieldable> result = new ArrayList<>();
        for (InstrumentSorter sorter : this.instrumentSorters) {
            result.add(IndexerUtil.noNorms(sorter.getSortField().getField(), sorter.getOrder(instrument)));
        }
        return result;
    }

    private String getNames(final Instrument instrument) {
        final Set<String> namesSet = new HashSet<>();
        addNames(namesSet, instrument.getName());

        if (FeatureFlags.Flag.NEW_WP_NAMES.isEnabled()) {
            final String freeName = instrument.getSymbol(KeysystemEnum.PM_INSTRUMENT_NAME_FREE);
            if (freeName != null && !freeName.equals(instrument.getName())) {
                addNames(namesSet, freeName);
            }
        }

        for (final Quote quote : instrument.getQuotes()) {
            addNames(namesSet, quote.getSymbol(KeysystemEnum.WM_WP_NAME_KURZ));
            addNames(namesSet, quote.getSymbol(KeysystemEnum.WM_WP_NAME_LANG));
            addNames(namesSet, quote.getSymbol(KeysystemEnum.WM_WP_NAME_ZUSATZ));
        }

        this.namesStb.setLength(0);
        for (final String s : namesSet) {
            this.namesStb.append(s);
            this.namesStb.append(" ");
        }
        return this.namesStb.toString();
    }

    private void addNames(final Set<String> names, final String symbol) {
        if (StringUtils.hasText(symbol)) {
            names.add(symbol);
        }
    }

    private IndexWriter createIndexWriter(final File indexBaseDir, final Analyzer analyzer,
            final boolean create) throws IOException {
        final IndexWriter result = new IndexWriter(FSDirectory.open(indexBaseDir), analyzer, create,
                LIMITED);
        result.setMergeFactor(this.mergeFactor);
        result.setRAMBufferSizeMB(this.rAMBufferSizeMB);
        // we can roughly fit 1300 docs in a meg, use 5.5 times that according to
        // http://www.gossamer-threads.com/lists/lucene/java-dev/51041
        result.setMaxBufferedDocs((int) (5.5d * this.rAMBufferSizeMB * 1300));
//        this.writer.setInfoStream(System.out);
        result.setUseCompoundFile(this.useCompoundFile);
        return result;
    }

    private void prepareIndexer(File instrumentIndexDir) throws Exception {
        readSynonymNames();
        for (InstrumentSorter sorter : this.instrumentSorters) {
            sorter.prepare(instrumentIndexDir, this.updateInstrumentMode);
        }
    }

    private void readSynonymNames() throws IOException {
        if (this.synonymNames == null) {
            this.logger.info("<readSynonymNames> synonymNames not set");
            return;
        }

        this.synonyms.clear();
        final Properties ps = PropertiesLoader.load(this.synonymNames);
        for (String iid : ps.stringPropertyNames()) {
            final String[] names = ps.getProperty(iid).split(",");
            this.synonyms.put(Long.valueOf(iid), names);
            this.logger.info("<readSynonymNames> " + iid + " => " + Arrays.toString(names));
        }
    }
}
