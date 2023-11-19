package de.marketmaker.istar.merger.provider.pages;

import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.LengthFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

import static org.apache.lucene.analysis.StopFilter.getEnablePositionIncrementsVersionDefault;

/**
 *
 */
class DefaultAnalyzer extends Analyzer {

    private static final int DEFAULT_MAX_TOKEN_LENGTH = 50;
    private static final int DEFAULT_MIN_TOKEN_LENGTH = 3;

    private static final Version VERSION = Version.LUCENE_29;

    private static final Set<String> stopWords = new HashSet<>(Arrays.asList(new String[] {
            // "vwd",
            // "symbol",
            // "bid",
            // "ask",
            // "index",
            // "time",
            "...",
    }));

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        Tokenizer tokenStream = new CharsetTokenizer(reader);
        TokenStream result = new StandardFilter(tokenStream);
        result = new LengthFilter(result, DEFAULT_MIN_TOKEN_LENGTH, DEFAULT_MAX_TOKEN_LENGTH);
        result = new LowerCaseFilter(result);
        result = new StopFilter(getEnablePositionIncrementsVersionDefault(VERSION), result, stopWords);
        return result;
    }

    private static class CharsetTokenizer extends CharTokenizer {
        CharsetTokenizer(Reader in) {
            super(in);
        }

        @Override
        protected boolean isTokenChar(char c) {
            return !Character.isWhitespace(c) &&  '_' != c;
        }

    }

}
