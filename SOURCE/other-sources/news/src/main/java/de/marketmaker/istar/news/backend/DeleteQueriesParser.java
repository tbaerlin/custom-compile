/*
 * DeleteQueriesParser.java
 *
 * Created on 30.03.2007 12:43:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Reads a file line by line and tries to create a lucene query for each line. If the input
 * contains any occurrence of "now - </em>period</em>", period will be interpreted as the
 * standard ISO period format and the occurrence will be replaced with the corresponding
 * encoded timestamp.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DeleteQueriesParser {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Resource resource;

    private static final Pattern PATTERN = Pattern.compile("now\\s*-\\s*(\\w+)");

    private final QueryParser queryParser
            = new QueryParser(Version.LUCENE_30, null, NewsQuerySupport.createAnalyzer()) {
        @Override
        protected Query getRangeQuery(String field, String part1, String part2,
                boolean inclusive) throws ParseException {
            return NumericRangeQuery.newIntRange(field, Integer.parseInt(part1), Integer.parseInt(part2), true, true);
        }
    };

    public static Query parse(String s) throws ParseException {
        DeleteQueriesParser parser = new DeleteQueriesParser(null);
        return parser.createQuery(new LocalDate().toDateTimeAtStartOfDay(), s);
    }

    public DeleteQueriesParser(Resource resource) {
        this.resource = resource;
    }

    public List<Query> getQueries() {
        try (Scanner s = new Scanner(this.resource.getInputStream())) {
            return parseDeleteQueries(s);
        } catch (IOException e) {
            this.logger.error("<getQueries> failed to parse", e);
            return Collections.emptyList();
        }
    }

    private List<Query> parseDeleteQueries(Scanner s) {

        final List<Query> result = new ArrayList<>();

        // "now" in delete queries will be translated into midnight
        final DateTime now = new LocalDate().toDateTimeAtStartOfDay();

        while (s.hasNextLine()) {
            final String line = s.nextLine();
            if (!StringUtils.hasText(line) || line.startsWith("#")) {
                continue;
            }

            try {
                final Query query = createQuery(now, line);
                this.logger.info("<parseDeleteQueries> parsed: " + query);
                result.add(query);
            } catch (Exception e) {
                this.logger.warn("<parseDeleteQueries> failed to parse '" + line + "'", e);
            }
        }

        return result;
    }

    private Query createQuery(DateTime now, String line) throws ParseException {
        final String queryStr = toQueryString(line, now);
        return this.queryParser.parse(queryStr);
    }

    private String toQueryString(String line, DateTime now) {
        final Matcher m = PATTERN.matcher(line);

        final StringBuffer sb = new StringBuffer();
        while (m.find()) {
            final Period p = ISOPeriodFormat.standard().parsePeriod(m.group(1));
            final int ts = News2Document.encodeTimestamp(now.minus(p));
            m.appendReplacement(sb, Integer.toString(ts));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
