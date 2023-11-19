/*
 * SynonymFilter.java
 *
 * Created on 09.01.2005 11:24:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;

import de.marketmaker.istar.common.lucene.TokenFilterWithStack;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StandardSynonymFilter extends TokenFilterWithStack {
    public static final String TOKEN_TYPE_SYNONYM = "SYNONYM";

    private final Map<String, String[]> synonyms = new HashMap<>();

    public StandardSynonymFilter(TokenStream in) {
        super(in);
        init();
    }

    private void init() {
        this.synonyms.put("dt",new String[]{"deutsche"});
        this.synonyms.put("deutsche",new String[]{"dt"});
        this.synonyms.put("disc",new String[]{"discount", "disco"});
        this.synonyms.put("cement",new String[]{"zement"});
    }

    @Override
    protected void onIncrementToken() {
        final String[] synonyms = this.synonyms.get(this.termAtt.term());
        if (synonyms != null) {
            for (String s : synonyms) {
                push(s);
            }
        }
    }
}
