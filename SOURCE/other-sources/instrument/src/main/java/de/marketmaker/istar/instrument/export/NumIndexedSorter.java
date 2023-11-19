/*
 * NumIndexedSorter.java
 *
 * Created on 20.08.2009 11:49:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.SortField;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.instrument.IndexConstants;

import static de.marketmaker.istar.instrument.IndexConstants.FIELDNAME_NUM_INDEXED;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NumIndexedSorter implements InstrumentSorter, InitializingBean {

    static final String PROP_NAME_INSTRUMENT_NUM = "index.instrument.sorter.num";

    public static final SortField SORT_FIELD
            = new SortField(IndexConstants.FIELDNAME_NUM_INDEXED, SortField.INT);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private int numIndexed = 0;

    private Properties properties;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    void setNumIndexed(int numIndexed) {
        this.numIndexed = numIndexed;
    }

    public void afterPropertiesSet() throws Exception {
        if (null == this.properties) {
            throw new IllegalStateException("An instance of Properties is required");
        }
    }

    public void prepare(File indexBaseDir, boolean update) throws IOException {
        if (!update) {
            return;
        }

        final TimeTaker tt = new TimeTaker();
        int n = 0;


        if (this.properties.containsKey(PROP_NAME_INSTRUMENT_NUM)) {
            n = Integer.parseInt(this.properties.getProperty(PROP_NAME_INSTRUMENT_NUM));
        }
        else {
            final IndexReader reader = IndexReader.open(FSDirectory.open(indexBaseDir), true);
            final Term t = new Term(FIELDNAME_NUM_INDEXED, "1");
            final TermEnum termEnum = reader.terms(t);
            while (termEnum.next()) {
                final Term term = termEnum.term();
                if (!t.field().equals(term.field())) {
                    break;
                }
                n = Math.max(n, Integer.parseInt(term.text()));
            }
            IoUtils.close(reader);
        }

        setNumIndexed(n);
        this.logger.info("<prepare> " + n + " in " + tt);
    }

    public String getOrder(Instrument instrument) {
        return Integer.toString(++this.numIndexed);
    }

    public SortField getSortField() {
        return SORT_FIELD;
    }

    public void afterInstrumentIndexed() {
        this.properties.put(PROP_NAME_INSTRUMENT_NUM, Integer.toString(this.numIndexed));
    }
}
