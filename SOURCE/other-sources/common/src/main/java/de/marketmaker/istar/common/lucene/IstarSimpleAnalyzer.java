/*
 * IstarSimpleAnalyzer.java
 *
 * Created on 24.08.2005 16:06:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IstarSimpleAnalyzer extends Analyzer {

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new LowerCaseFilter(new KeywordTokenizer(reader));
    }

}
