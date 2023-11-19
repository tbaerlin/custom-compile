/*
 * NewsQueryParser.java
 *
 * Created on 24.09.2009 14:53:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.frontend;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import de.marketmaker.istar.news.backend.NewsQuerySupport;

/**
 * This class helps to parse news queries and makes sure that queries are not "too complex".
 * Lucene can be overwhelmed when too many terms are included in a query, especially if those
 * terms refer to complex fields such as text or headline. So this class computes a complexity
 * measure for a given query and may reject certain queries.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class NewsQueryParserUtil {
    private static final int MAX_COMPLEXITY = Integer.getInteger("news.query.max.compexity", 120);

    private NewsQueryParserUtil() {
    }

    static Query parse(String query) throws ParseException {
        final Query result = NewsQuerySupport.parse(query);
        final int compexity = getCompexity(result);
        if (compexity < MAX_COMPLEXITY) {
            return result;
        }
        throw new ParseException("Too complex: '" + query + "', complexity=" + compexity);
    }


    static int getCompexity(Query q) {
        if (q instanceof BooleanQuery) {
            final BooleanQuery bq = (BooleanQuery) q;
            final BooleanClause[] clauses = bq.getClauses();
            int n = 0;
            for (BooleanClause clause : clauses) {
                n += getCompexity(clause.getQuery());
            }
            return n;
        }
        if (q instanceof PhraseQuery) {
            final PhraseQuery pq = (PhraseQuery) q;
            final Term[] terms = pq.getTerms();
            return terms.length * getFieldWeight(terms[0].field());
        }
        if (q instanceof TermQuery) {
            return getFieldWeight(((TermQuery) q).getTerm().field());
        }
        return 1;
    }

    static int getFieldWeight(String field) {
        if (field.equals(NewsIndexConstants.FIELD_TEXT)) {
            return 8;
        }
        if (field.equals(NewsIndexConstants.FIELD_TEASER)) {
            return 4;
        }
        if (field.equals(NewsIndexConstants.FIELD_HEADLINE)) {
            return 2;
        }
        return 1;
    }

    public static void main(String[] args) throws ParseException {
        String q = "+(+(+(+*:* -(headline:ANALYSE-FLASH text:ANALYSE-FLASH)) -(headline:ROUNDUP text:ROUNDUP)) -(headline:AKTIEN-FLASH text:AKTIEN-FLASH)) -(headline:DPA-AFXBROKER text:DPA-AFXBROKER)";
        Query query = parse(q);
        System.out.println("Complexity = " + getCompexity(query));
    }
}
