/*
 * NwsNachricht.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.apache.lucene.queryParser.ParseException;

import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.provider.news.NewsProvider;
import de.marketmaker.istar.merger.web.easytrade.NewsQueryException;
import de.marketmaker.istar.news.backend.News2Document;
import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;
import de.marketmaker.istar.news.frontend.NewsRecord;
import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class INSNews extends EasytradeCommandController {
    private NewsProvider newsProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public static class Command {
        private String[] newsid;

        private boolean blockAds = false;

        private boolean withRawText = false;

        public boolean isWithRawText() {
            return withRawText;
        }

        public void setWithRawText(boolean withRawText) {
            this.withRawText = withRawText;
        }

        @NotNull
        public String[] getNewsid() {
            return ArraysUtil.copyOf(newsid);
        }

        public void setNewsid(String[] newsid) {
            this.newsid = ArraysUtil.copyOf(newsid);
        }

        public boolean isBlockAds() {
            return blockAds;
        }

        public void setBlockAds(boolean blockAds) {
            this.blockAds = blockAds;
        }
    }

    public INSNews() {
        super(Command.class);
    }

    public void setNewsProvider(NewsProvider newsProvider) {
        this.newsProvider = newsProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;
        final List<NewsRecord> records = getRecords(cmd);

        final Map<String, Object> model = new HashMap<>();
        model.put("blockAds", cmd.isBlockAds());
        model.put("withRawText", cmd.isWithRawText());
        model.put("records", records);
        return new ModelAndView("insnews", model);
    }

    private List<NewsRecord> getRecords(final Command cmd) {
        final String query = toQuery(cmd.getNewsid());
        if (query == null) {
            return Collections.emptyList();
        }

        final NewsRequest newsRequest = new NewsRequest();
        newsRequest.setCount(cmd.getNewsid().length);
        newsRequest.setWithRawText(cmd.isWithRawText());
        try {
            newsRequest.setQuery(query);
        } catch (ParseException e) {
            this.logger.warn("<getRecords> setQuery failed: " + e.getMessage());
            throw new NewsQueryException(e);
        }

        final NewsResponse newsResponse = this.newsProvider.getNews(newsRequest, true);
        if (!newsResponse.isValid()) {
            return Collections.emptyList();
        }

        final List<NewsRecord> result = newsResponse.getRecords();
        return (result.size() > 1) ? sortRecords(result, cmd.getNewsid()) : result;
    }

    private List<NewsRecord> sortRecords(List<NewsRecord> records, String[] newsid) {
        final Map<String, NewsRecord> byShortId = new HashMap<>();
        for (NewsRecord record : records) {
            final int id = ((NewsRecordImpl) record).getShortId();
            byShortId.put(Integer.toString(id), record);
        }

        final List<NewsRecord> result = new ArrayList<>(newsid.length);
        for (int i = 0; i < newsid.length; i++) {
            final NewsRecord record = byShortId.get(newsid[i]);
            if (record != null) {
                result.add(record);
            }
        }
        return result;
    }

    private String toQuery(String[] newsid) {
        final StringBuilder sb = new StringBuilder(newsid.length * 10);
        sb.append(NewsIndexConstants.FIELD_SHORTID).append(":(");
        boolean validQuery = false;
        for (int i = 0; i < newsid.length; i++) {
            final int id;
            try {
                id = Integer.parseInt(newsid[i]);
            } catch (NumberFormatException e) {
                this.logger.warn("<toQuery> ignoring invalid id " + newsid[i]);
                continue;
            }
            sb.append(" ").append(News2Document.encodeShortid(id));
            validQuery = true;
        }
        return validQuery ? sb.append(")").toString() : null;
    }
}