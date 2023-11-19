/*
 * SynonymFilter.java
 *
 * Created on 09.01.2005 11:24:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import org.apache.lucene.analysis.TokenStream;

import de.marketmaker.istar.common.lucene.TokenFilterWithStack;
import de.marketmaker.istar.instrument.search.analysis.InstrumentTokenizer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SplitNamesFilter extends TokenFilterWithStack {
    private static final String ALPHANUM_TYPE
            = InstrumentTokenizer.TOKEN_TYPES[InstrumentTokenizer.ALPHANUM];

    public SplitNamesFilter(TokenStream in) {
        super(in);
    }

    @Override
    protected void onIncrementToken() {
        applyFilter(this.termAtt.term(), this.typeAtt.type());
    }


    protected void applyFilter(String term, String type) {
        if (ALPHANUM_TYPE.equals(type)) {
            addSubstringsToStack(term);
        }
    }

    private void addSubstringsToStack(String text) {
        final int len = text.length();

        for (int i = 1; i < len - 1; i++) {
            push(text.substring(i));
        }
    }
}
