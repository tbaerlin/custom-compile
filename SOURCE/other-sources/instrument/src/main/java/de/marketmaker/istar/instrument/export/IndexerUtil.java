/*
 * IndexerUtil.java
 *
 * Created on 19.11.2010 15:01:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;

/**
 * @author zzhao
 */
public final class IndexerUtil {

    private static final Logger logger = LoggerFactory.getLogger(IndexerUtil.class);

    private IndexerUtil() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    public static void close(final IndexWriter w, boolean optimize) throws IOException {
        final TimeTaker tt = new TimeTaker();
        if (optimize) {
            logger.info("<close> with optimization");
            w.optimize();
        }
        IoUtils.close(w);
        logger.info("<close> took " + tt);
    }

    public static Field unstored(String field, String term) {
        return createField(field, term, Field.Store.NO, Field.Index.ANALYZED);
    }

    public static Field noNorms(String field, String term) {
        return createField(field, term, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    public static Field keyword(String field, String term) {
        return createField(field, term, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    public static Field unindexed(String field, String term) {
        return createField(field, term, Field.Store.YES, Field.Index.NO);
    }

    private static Field createField(String field, String term, final Field.Store store,
            final Field.Index index) {
        final String s = isAnalyzed(index) ? term : term.toLowerCase();
        final Field result = new Field(field, s, store, index);
        // we never need term frequencies; reduces instrument index size by 40%!
        result.setOmitTermFreqAndPositions(true);
        return result;
    }

    private static boolean isAnalyzed(Field.Index index) {
        return index == Field.Index.ANALYZED || index == Field.Index.ANALYZED_NO_NORMS;
    }
}
