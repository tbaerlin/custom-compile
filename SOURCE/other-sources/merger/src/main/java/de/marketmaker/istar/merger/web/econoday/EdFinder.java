/*
 * EerFinder.java
 *
 * Created on 21.03.12 10:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.econoday;

import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.DateFormat;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.econoday.EconodayProvider;
import de.marketmaker.istar.merger.provider.econoday.EconodaySearchRequest;
import de.marketmaker.istar.merger.provider.econoday.EconodaySearchResponse;
import de.marketmaker.istar.merger.web.PermissionDeniedException;
import de.marketmaker.istar.merger.web.QueryCommand;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;
import de.marketmaker.istar.merger.web.finder.Query2Term;
import de.marketmaker.istar.merger.web.finder.Term;

/**
 * Queries event releases of economic calendar using the given parameters.
 * <p>
 * Event releases can be searched using their meta data: <code>country</code> and <code>eventCode</code>.
 * To limit the query result one can use date constraints: <code>from</code> and <code>to</code>.
 * </p>
 * <p>
 * For advanced usage this service supports query directly.
 * </p>
 * @author zzhao
 */
public class EdFinder extends EasytradeCommandController {

    public static class Command extends ListCommand implements QueryCommand {

        private static final boolean DEFAULT_ORDER = false;

        private static final String DATE_PATTERN = "yyyy-MM-dd";

        private static final String DEFAULT_SORT_FIELD = "releasedOn";

        private String country;

        private String eventCode;

        private String from;

        private String to;

        private String query;

        public Command() {
            setAscending(DEFAULT_ORDER);
        }

        /**
         * Sort field, currently always {@value #DEFAULT_SORT_FIELD}.
         * @return the sort field
         */
        @Override
        public String getSortBy() {
            return DEFAULT_SORT_FIELD;
        }

        /**
         * Ascending order on the sort field. Default is {@value #DEFAULT_ORDER}.
         * @return ascending order on the sort field.
         */
        @Override
        public boolean isAscending() {
            return super.isAscending();
        }

        /**
         * {@inheritDoc}
         * <p>
         * Query on event releases supports the following
         * fields:
         * <ul>
         * <li>country</li>
         * <li>eventCode</li>
         * <li>releasedOn</li>
         * </ul>
         * Some query examples:
         * <table border="1">
         * <tr><th>Example</th><th>Explanation</th></tr>
         * <tr><td><code>country=='DE@US'</code> or <code>country IN ('DE', 'US')</code></td><td>Event releases from Germany or United States</td></tr>
         * <tr><td><code>country=='DE' &amp;&amp; releasedOn &gt;= '2012-02-29'</code></td><td>Event releases from Germany after Feb. 29</td></tr>
         * <tr><td><code>eventCode=='FARM@PMCXBAA' &amp;&amp; releasedOn &gt;= '2012-01-01' && releasedOn &lt; '2012-03-01'</code></td>
         * <td>Event releases with code <code>FARM</code> or <code>PMCXBAA</code> and released between Jan. 1(inclusive) and
         * Mar. 1(exclusive)</td></tr>
         * </table>
         * </p><p>
         * Using query string, <code>releasedOn</code> also supports date-time format: <code>yyyy-MM-dd HH:mm:ss</code>.
         * </p><p>
         * <b>If the query string is set, the query string would be used as conditions, not the other
         * command fields.</b>
         * </p>
         * @return a query
         */
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        /**
         * The country to restrict the events returned.
         * @return The country to restrict the events returned.
         */
        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getEventCode() {
            return eventCode;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setEventCode(String eventCode) {
            this.eventCode = eventCode;
        }

        /**
         * The start date of an interval. The query is evaluated with this date as start, inclusive.
         * @return the start date of an interval.
         */
        @DateFormat(format = DATE_PATTERN)
        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        /**
         * The end date of an interval. The query is evaluated with this date as end, exclusive.
         * @return the end date of an interval.
         */
        @DateFormat(format = DATE_PATTERN)
        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }

    private EconodayProvider econodayProvider;

    public EdFinder() {
        super(Command.class);
    }

    public void setEconodayProvider(EconodayProvider econodayProvider) {
        this.econodayProvider = econodayProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (!profile.isAllowed(Selector.ECONODAY)) {
            throw new PermissionDeniedException(Selector.ECONODAY.getId());
        }

        final Command cmd = (Command) o;
        final EconodaySearchRequest req;
        if (!StringUtils.hasText(cmd.getQuery())) {
            req = EconodaySearchRequest.byParameter(cmd.getOffset(), cmd.getCount(), getSortField(),
                    cmd.isAscending(), getCountries(cmd), getEventCodes(cmd), cmd.getFrom(), cmd.getTo());
        }
        else {
            req = EconodaySearchRequest.byQuery(cmd.getOffset(), cmd.getCount(), getSortField(),
                    cmd.isAscending(), fromQuery(cmd.getQuery()));
        }
        final EconodaySearchResponse resp = this.econodayProvider.getReleases(req);
        final ListResult listResult = ListResult.create(cmd,
                Arrays.asList(Command.DEFAULT_SORT_FIELD), Command.DEFAULT_SORT_FIELD,
                resp.getTotalCount());
        listResult.setCount(resp.getReleases().size());

        final HashMap<String, Object> model = new HashMap<>(5);
        model.put("listinfo", listResult);
        model.put("releases", resp.getReleases());
        model.put("metadata", resp.getMetaData());
        return new ModelAndView("edfinder", model);
    }

    private String getSortField() {
        return "r.released_on"; // does not make much sense for sorting on other field
    }

    private String fromQuery(String query) {
        try {
            final Term term = Query2Term.toTerm(query);
            final EdFinderTermVisitor visitor = new EdFinderTermVisitor();
            term.accept(visitor);
            return visitor.getMySqlQuery();
        } catch (Exception e) {
            throw new IllegalStateException("cannot parse query", e);
        }
    }

    private String[] getEventCodes(Command cmd) {
        final String eventCodes = cmd.getEventCode();
        return StringUtils.hasText(eventCodes) ? eventCodes.trim().split(",") : null;
    }

    private String[] getCountries(Command cmd) {
        final String countries = cmd.getCountry();
        return StringUtils.hasText(countries) ? countries.split(",") : null;
    }

    public static void main(String[] args) throws Exception {
        final String query = "(!(country IN ('DE','US')) && eventCode IN ('boo','far')) " +
                "|| releasedOn <='2012-02-01@2012-06-19@2012-06-05 20:30:00'";
        final Term term = Query2Term.toTerm(query);

        final EdFinderTermVisitor visitor = new EdFinderTermVisitor();
        term.accept(visitor);
        System.out.println(visitor.getMySqlQuery());
    }
}
