/*
 * MpcIndexer.java
 *
 * Created on 19.11.2010 14:53:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.instrument.IndexConstants;

import static de.marketmaker.istar.instrument.IndexConstants.FIELDNAME_MPC_COUNT;
import static de.marketmaker.istar.instrument.IndexConstants.FIELDNAME_MPC_VALUE;
import static org.apache.lucene.index.IndexWriter.MaxFieldLength.LIMITED;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author zzhao
 * @since 1.2
 */
public class MpcIndexer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void index(File mpcIndexDir, File instrumentIndexDir) throws IOException {
        final Map<String, Integer> strings = computeMpcNames(instrumentIndexDir);
        this.logger.info("<index> #name strings: " + strings.size());

        final TimeTaker tt = new TimeTaker();
        final IndexWriter iw = new IndexWriter(FSDirectory.open(mpcIndexDir),
                new StandardAnalyzer(Version.LUCENE_24), true, LIMITED);

        final Document doc = new Document();
        final Field value = IndexerUtil.keyword(FIELDNAME_MPC_VALUE, "");
        final Field count = IndexerUtil.unindexed(FIELDNAME_MPC_COUNT, "");
        doc.add(value);
        doc.add(count);

        for (final Map.Entry<String, Integer> entry : strings.entrySet()) {
            value.setValue(entry.getKey());
            count.setValue(entry.getValue().toString());
            iw.addDocument(doc);
        }

        IndexerUtil.close(iw, true);
        this.logger.info("<index> indexing MPC (" + strings.size() + " elements), took " + tt);
    }

    private Map<String, Integer> computeMpcNames(File instrumentIndexDir) throws IOException {
        final Map<String, Integer> result = new HashMap<>();
        final IndexReader reader = IndexReader.open(FSDirectory.open(instrumentIndexDir), true);
        final Term term = new Term(IndexConstants.FIELDNAME_NAMES, "0");

        final Matcher m = Pattern.compile("[a-z]{4,}").matcher("");
        final TermEnum termEnum = reader.terms(term);
        do {
            final String text = termEnum.term().text();
            if (m.reset(text).matches()) {
                result.put(text, termEnum.docFreq());
            }
        } while (termEnum.next() && termEnum.term().field() == term.field());

        IoUtils.close(reader);
        return result;
    }
}
