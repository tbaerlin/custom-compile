/*
 * NwsFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.joda.time.DateTime;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.merger.provider.news.NewsProvider;
import de.marketmaker.istar.merger.provider.news.QueryConverter;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.NewsQueryException;
import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class INSNewsSearch extends EasytradeCommandController {
    private static final List<String> SORT_FIELDS = Collections.emptyList();

    public static class Command extends ListCommand {
        private DateTime start = new DateTime().minusDays(1).withTimeAtStartOfDay();
        private DateTime end;
        private String query;
        private boolean blockAds = false;

        public Command() {
            super(100);
        }

        @Override
        public boolean isAscending() {
            return false;
        }

        public DateTime getStart() {
            return start;
        }

        public void setStart(DateTime start) {
            this.start = start;
        }

        public DateTime getEnd() {
            return end;
        }

        public void setEnd(DateTime end) {
            this.end = end;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        @Range(min = 0, max = 1000)
        public int getAnzahl() {
            return super.getAnzahl();
        }

        public boolean isBlockAds() {
            return blockAds;
        }

        public void setBlockAds(boolean blockAds) {
            this.blockAds = blockAds;
        }
    }

    private NewsProvider newsProvider;

    private QueryConverter converter;

    public INSNewsSearch() {
        super(Command.class);
    }

    public void setConverter(QueryConverter converter) {
        this.converter = converter;
    }

    public void setNewsProvider(NewsProvider newsProvider) {
        this.newsProvider = newsProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;
        final NewsRequest nr = createRequest(cmd);
        final NewsResponse news = findNews(nr);

        return createResult(cmd, news);
    }

    NewsResponse findNews(NewsRequest nr) {
        return this.newsProvider.getNews(nr, true);
    }

    private ModelAndView createResult(Command cmd, NewsResponse news) {
        final ListResult listResult = ListResult.create(cmd, SORT_FIELDS, "datum", news.getHitCount());
        listResult.setCount(news.getRecords().size());

        final Map<String, Object> model = new HashMap<>();
        model.put("listinfo", listResult);
        model.put("records", news.getRecords());
        model.put("blockAds", cmd.isBlockAds());
        return new ModelAndView("insnewssearch", model);
    }

    private NewsRequest createRequest(Command cmd) throws Exception {
        final NewsRequest nr = new NewsRequest();
        nr.setCount(cmd.getAnzahl());
        nr.setOffset(cmd.getOffset());
        nr.setWithText(false);
        nr.setWithHitCount(false);
        addQuery(nr, cmd);

        if (cmd.getStart() != null) {
            nr.setFrom(cmd.getStart());
        }
        if (cmd.getEnd() != null) {
            nr.setTo(cmd.getEnd());
        }

        return nr;
    }

    private void addQuery(NewsRequest nr, Command cmd) throws Exception {
        final String query = this.converter.toLuceneQueryString(cmd.getQuery());
        if (query == null) {
            nr.setLuceneQuery(new MatchAllDocsQuery());
        }
        else {
            try {
                nr.setQuery(query);
            } catch (ParseException e) {
                throw new NewsQueryException(e);
            }
        }
    }
}