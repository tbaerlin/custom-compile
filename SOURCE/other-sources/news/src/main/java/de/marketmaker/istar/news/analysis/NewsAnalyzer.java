package de.marketmaker.istar.news.analysis;

import java.io.Reader;
import java.util.Collections;

import org.apache.lucene.analysis.LengthFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

import de.marketmaker.istar.common.lucene.ASCIIFoldingFilterMM;

/**
 * Extends {@link org.apache.lucene.analysis.standard.StandardAnalyzer} by wrapping its TokenStream
 * with a {@link de.marketmaker.istar.common.lucene.ASCIIFoldingFilterMM} and a
 * {@link org.apache.lucene.analysis.LengthFilter} that only accepts tokens with a minimum of
 * 2 characters.
 */
public class NewsAnalyzer extends StandardAnalyzer {

    public NewsAnalyzer() {
        super(Version.LUCENE_30, Collections.emptySet());
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new LengthFilter(new ASCIIFoldingFilterMM(super.tokenStream(fieldName, reader)), 2, 512);
    }
}
