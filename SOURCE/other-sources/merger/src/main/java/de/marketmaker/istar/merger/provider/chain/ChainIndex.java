package de.marketmaker.istar.merger.provider.chain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.AbstractField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.store.FSDirectory;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.lucene.IstarSimpleAnalyzer;
import de.marketmaker.istar.common.util.TimeTaker;


/**
 * search backend for chain definitions
 */
public class ChainIndex {

    // chains fields
    public static final String CHAIN_INSTRUMENT = "chainInstrument";
    public static final String CHAIN_NAME = "chainName";
    public static final String CHAIN_FLAG = "chainFlag";
    public static final String CHAIN_CHANGE_DATE = "chainChangeDate";

    // fields collected per chain
    public static final String NAMES = "names";
    public static final String TICKERS = "tickers";

    // chains element fields
    public static final String QID = "qid";
    public static final String VWD_CODE = "vwdCode";
    public static final String VWD_SYMBOL = "vwdSymbol";
    public static final String SORT_FIELD = "sortField";
    public static final String NAME = "name";
    public static final String VALOR = "valor";
    public static final String ISIN = "isin";
    public static final String WKN = "wkn";
    public static final String LEI = "lei";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File baseDir = new File(System.getProperty("user.home"), "produktion/var/data/chains");

    private File[] indexDirs = new File[2];

    private volatile int index = -1;

    private Object lock = new Object(); // don't switch while still processing the old index

    public static Analyzer createChainsAnalyzer() {
        return new IstarSimpleAnalyzer();
    }

    public ChainIndex() {
        File out = new File(this.baseDir, "out");
        indexDirs[0] = new File(out, "index0");
        indexDirs[1] = new File(out, "index1");
    }

    public void reset(Collection<ChainData> currentData) throws IOException {
        final TimeTaker tt = new TimeTaker();
        synchronized(lock) {
            switchOver(currentData);
        }
        this.logger.info("<reset> ...done, took " + tt);
    }

    private void switchOver(Collection<ChainData> currentData) throws IOException {

        int nextIndex = (index + 1) % 2;

        this.logger.info("<reset> processing ChainData index at " + indexDirs[nextIndex]);
        if (indexDirs[nextIndex].delete()) {
            this.logger.info("<reset> deleted old index dir at " + indexDirs[nextIndex]);
        }

        try (IndexWriter writer = new IndexWriter(
                FSDirectory.open(indexDirs[nextIndex]),
                createChainsAnalyzer(),
                true,
                IndexWriter.MaxFieldLength.LIMITED)) {
            int chain = 0;
            int elemts = 0;

            for (ChainData chainData : currentData) {
                final Document document = new Document();
                createStoredField(CHAIN_INSTRUMENT, chainData.getChainInstrument()).forEach(document::add);
                createUnStoredField(CHAIN_NAME, chainData.getChainName()).forEach(document::add);
                createUnStoredField(NAMES, chainData.getChainName()).forEach(document::add);
                //document.add(createUnStoredField(CHAIN_FLAG, chainData.getChainFlag()));
                createUnStoredField(CHAIN_CHANGE_DATE, chainData.getChainChangeDate()).forEach(document::add);
                for (ChainData.Element element : chainData.elements) {
                    elemts++;
                    createUnStoredField(NAME, element.getName()).forEach(document::add);
                    createUnStoredField(QID, Long.toString(element.getQid())).forEach(document::add);
                    createUnStoredField(VWD_SYMBOL, element.getVwdSymbol()).forEach(document::add);
                    createUnStoredField(VWD_CODE, element.getVwdCode()).forEach(document::add);

                    createUnStoredField(NAMES, element.getName()).forEach(document::add);
                    createUnStoredField(NAMES, element.getWpNameKurz()).forEach(document::add);
                    createUnStoredField(NAMES, element.getWpNameLang()).forEach(document::add);
                    createUnStoredField(NAMES, element.getWpNameZusatz()).forEach(document::add);
                    createUnStoredField(NAMES, element.getLongName()).forEach(document::add);

                    createUnStoredField(TICKERS, element.getWmTicker()).forEach(document::add);
                    createUnStoredField(TICKERS, element.getEurexTicker()).forEach(document::add);
                    createUnStoredField(TICKERS, element.getTicker()).forEach(document::add);

                    createUnStoredField(VALOR, element.getValor()).forEach(document::add);
                    createUnStoredField(ISIN, element.getIsin()).forEach(document::add);
                    createUnStoredField(WKN, element.getWkn()).forEach(document::add);
                    createUnStoredField(LEI, element.getLei()).forEach(document::add);
                }
                writer.addDocument(document);
                if (++chain % 10_000 == 0) {
                    this.logger.info("<reset> commited " + chain + "/" + currentData.size() + " total element count: " + elemts);
                    writer.commit();
                }
            }

            this.logger.info("<reset> commited " + chain + "/" + currentData.size() + " total element count: " + elemts);
            writer.commit();
            this.logger.info("<reset> optimize...");
            writer.optimize();
        }

        // new index is ready and can be used
        index = nextIndex;
        this.logger.info("<reset> switched to new index at " + indexDirs[index]);
    }

    public List<String> find(Query query) throws IOException {
        final List<String> result = new ArrayList<>(100);
        if (index < 0) {
            return result;
        }
        // TODO: reuse the searcher for multiple find() calls
        final IOException[] ioe = new IOException[1];
        final FSDirectory dir = FSDirectory.open(this.indexDirs[index]);
        final IndexReader reader = IndexReader.open(dir);
        try (IndexSearcher searcher = new IndexSearcher(reader)) {
            searcher.search(
                    query,
                    //new BooleanFilter(),
                    createCollector(result, ioe));
            if (ioe[0] != null) {
                throw ioe[0];
            }
        } finally {
            reader.close();
            dir.close();
        }
        return result;
    }

    @NonNull
    private Collector createCollector(final List<String> result, final IOException[] ioe) {
        return new Collector() {
            private IndexReader reader;

            @Override
            public void setScorer(Scorer scorer) {
                // ignore
            }

            @Override
            public void collect(int i) {
                try {
                    final String id = this.reader.document(i).getField(CHAIN_INSTRUMENT).stringValue();
                    result.add(id);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    ioe[0] = ex;
                }
            }

            @Override
            public void setNextReader(IndexReader indexReader, int i) {
                this.reader = indexReader;
            }

            @Override
            public boolean acceptsDocsOutOfOrder() {
                return true;
            }
        };
    }

    // see: http://oak.cs.ucla.edu/cs144/projects/lucene/
    private Stream<AbstractField> createStoredField(String name, String value) {
        if (StringUtils.isEmpty(value)) {
            return Stream.empty();
        } else {
            Field result = new Field(name, true, value,
                    Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.NO);
            result.setOmitTermFreqAndPositions(true);
            return Stream.of(result);
        }
    }

    private Stream<AbstractField> createUnStoredField(final String name, String value) {
        if (StringUtils.isEmpty(value)) {
            return Stream.empty();
        } else {
            if (!value.contains(" ")) {
                Field result = new Field(name, true, value,
                        Field.Store.NO,
                        Field.Index.ANALYZED,
                        Field.TermVector.NO);
                result.setOmitTermFreqAndPositions(true);
                return Stream.of(result);
            } else {
                return Stream.of(value.split(" "))
                        .map(s -> {
                            Field result = new Field(name, true, s,
                                    Field.Store.NO,
                                    Field.Index.ANALYZED,
                                    Field.TermVector.NO);
                            result.setOmitTermFreqAndPositions(true);
                            return result;
                        });
            }
        }
    }

    private Stream<AbstractField> createUnStoredField(String name, DateTime value) {
        if (value == null) {
            return Stream.empty();
        } else {
            final AbstractField result = new NumericField(name).setLongValue(value.getMillis());
            result.setOmitTermFreqAndPositions(true);
            return Stream.of(result);
        }
    }

    // for debugging only
    void dumpIndexContent() throws IOException {
        if (index < 0) {
            System.out.println("nothing indexed yet");
        }
        try (IndexReader indexReader = IndexReader.open(FSDirectory.open(this.indexDirs[index]));
             TermEnum termEnum = indexReader.terms()) {
            while (termEnum.next()) {
                Term term = termEnum.term();
                System.out.println(term.field() + ": '" + term.text() + "'");
            }
        }
    }

}
