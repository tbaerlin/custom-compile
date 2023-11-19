package de.marketmaker.istar.analyses.analyzer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;

/**
 * parse strings like
 * "collect A, B, C for index sort buy desc"
 *
 * not threadsafe
 */
public class QueryParser {

    // TODO:
    // we need a data value in the query to check the timeframe of the analysis against...

    private static final String QUERY_PATTERN = ""
            // specify the fields that will be part of the output
            + "\\s*collect\\s+(?<fields>\\w+(?:\\s*,\\s*\\w+)*)"
            // the datatype we want to select
            + "\\s+for\\s+(?<stream>\\w+)"
            // the order
            + "\\s+sort\\s+(?<sort>\\w+)"
            // inverted order
            + "\\s+(?<order>asc|desc)"
            // optional paging start and pagesize
            + "\\s*(?:"
            + "\\s+skip\\s+(?<skip>\\d+)"
            + "\\s+limit\\s+(?<limit>\\d+)"
            + ")?"
            + "\\s*(?:"
            + "\\s+date\\s+(?<date>[0-9]{4}-[0-9]{2}-[0-9]{2})"
            + ")?"
            + "\\s*"
            ;

    // valid source strings
    public static final String INDUSTRY = "industry";
    public static final String SECURITY = "security";
    public static final String INDEX = "index";
    public static final String AGENCY = "agency";
    public static final String ANALYSIS = "analysis";


    private final Matcher matcher = Pattern.compile(QUERY_PATTERN).matcher("");

    private List<String> fields;
    private String stream;
    private String sort;
    private String order;
    private int skip;
    private int limit;
    private int date;

    public boolean parse(String query) {
        fields = null;
        stream = null;
        sort = null;
        order = null;
        skip = 0;
        limit = 10;
        date = DateUtil.toYyyyMmDd(DateTime.now());

        matcher.reset(query);
        if (matcher.matches()) {
            fields = Arrays.stream(matcher.group("fields")
                    .split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            stream = matcher.group("stream");
            sort = matcher.group("sort");
            order = matcher.group("order");
            skip = readSkip();
            limit = readlimit();
            date = readDate();
            return true;
        } else {
            return false;
        }
    }

    private int readDate() {
        String dateString = matcher.group("date");
        if (StringUtils.isEmpty(dateString)) {
            return DateUtil.toYyyyMmDd(DateTime.now());
        }
        return DateUtil.toYyyyMmDd(DateTime.parse(dateString));
    }

    private int readlimit() {
        return Integer.parseInt(Optional.ofNullable(matcher.group("limit")).orElse("10"));
    }

    private int readSkip() {
        return Integer.parseInt(Optional.ofNullable(matcher.group("skip")).orElse("0"));
    }


    List<String> getFields() {
        return fields;
    }

    String getStream() {
        return stream;
    }

    String getSort() {
        return sort;
    }

    String getOrder() {
        return order;
    }

    long getLimit() {
        return limit;
    }

    long getSkip() {
        return skip;
    }

    int getDate() {
        return date;
    }
}
