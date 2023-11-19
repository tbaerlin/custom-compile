/*
 * AnalyzerFactory.java
 *
 * Created on 09.01.2005 11:31:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;

import de.marketmaker.istar.common.lucene.IstarSimpleAnalyzer;
import de.marketmaker.istar.instrument.IndexConstants;
import de.marketmaker.istar.instrument.search.analysis.InstrumentAnalyzer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AnalyzerFactory {

    private AnalyzerFactory() {
    }

    public static Analyzer getQueryAnalyzer() {
        return createAnalyzer(InstrumentAnalyzer.forQuery());
    }

    public static Analyzer getIndexAnalyzer() {
        return createAnalyzer(InstrumentAnalyzer.forIndexing());
    }

    private static Analyzer createAnalyzer(InstrumentAnalyzer namesAnalyzer) {
        final PerFieldAnalyzerWrapper result = new PerFieldAnalyzerWrapper(new IstarSimpleAnalyzer());
        for (String field : IndexConstants.NAME_FIELDS) {
            result.addAnalyzer(field, namesAnalyzer);
        }
        return result;
    }
}
