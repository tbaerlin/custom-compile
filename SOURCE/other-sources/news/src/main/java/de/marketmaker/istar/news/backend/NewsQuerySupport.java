/*
 * NewsQuerySupport.java
 *
 * Created on 10.08.2010 13:44:28
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import de.marketmaker.istar.common.lucene.IstarSimpleAnalyzer;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.news.analysis.NewsAnalyzer;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;

/**
 * @author oflege
 */
public class NewsQuerySupport {

    private static final DateTimeFormatter DTF
            = ISODateTimeFormat.localDateOptionalTimeParser().withZone(DateUtil.DTZ_BERLIN);

    private NewsQuerySupport() {
    }

    public static Analyzer createAnalyzer() {
        final PerFieldAnalyzerWrapper result = new PerFieldAnalyzerWrapper(new IstarSimpleAnalyzer());
        final Analyzer newsAnalyzer = new NewsAnalyzer();
        result.addAnalyzer(NewsIndexConstants.FIELD_HEADLINE, newsAnalyzer);
        result.addAnalyzer(NewsIndexConstants.FIELD_TEASER, newsAnalyzer);
        result.addAnalyzer(NewsIndexConstants.FIELD_TEXT, newsAnalyzer);
        return result;
    }

    public static Query parse(String query) throws ParseException {
        final QueryParser parser = new QueryParser(Version.LUCENE_30, NewsIndexConstants.FIELD_ID,
                NewsQuerySupport.createAnalyzer()) {
            @Override
            protected Query getRangeQuery(String field, String part1, String part2,
                    boolean inclusive) throws ParseException {
                return NumericRangeQuery.newIntRange(field, parseDate(part1), parseDate(part2), true, true);
            }
        };

        return parser.parse(query);
    }

    private static int parseDate(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            final DateTime dt = DTF.parseDateTime(s);
            return News2Document.encodeTimestamp(dt);
        }
    }
}
