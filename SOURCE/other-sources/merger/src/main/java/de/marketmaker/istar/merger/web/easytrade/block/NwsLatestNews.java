/*
 * NwsLatestNews.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Size;
import de.marketmaker.istar.merger.provider.news.NewsProvider;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.news.frontend.*;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Returns the latest available news (if any) for each of the given instrument ids.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NwsLatestNews extends EasytradeCommandController {
    public static class Command {
        private String realm = NwsFindersuchkriterien.DEFAULT_REALM;

        private boolean blockAds = false;

        private String[] iid;

        private boolean withText = false;

        /**
         * iids of the instruments queried for news
         */
        @NotNull
        @Size(min = 1, max = 100)
        public String[] getIid() {
            return iid;
        }

        public void setIid(String[] iid) {
            this.iid = iid;
        }

        @MmInternal
        public boolean isBlockAds() {
            return blockAds;
        }

        public void setBlockAds(boolean blockAds) {
            this.blockAds = blockAds;
        }

        /**
         * Whether the news text should be included in the response, default is false.
         */
        public boolean isWithText() {
            return withText;
        }

        public void setWithText(boolean withText) {
            this.withText = withText;
        }

        @MmInternal
        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }
    }

    private NewsProvider newsProvider;

    private NwsFindersuchergebnis nwsFindersuchergebnis;
    
    public NwsLatestNews() {
        super(Command.class);
    }

    public void setNewsProvider(NewsProvider newsProvider) {
        this.newsProvider = newsProvider;
    }

    public void setNwsFindersuchergebnis(NwsFindersuchergebnis nwsFindersuchergebnis) {
        this.nwsFindersuchergebnis = nwsFindersuchergebnis;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final LatestNewsRequest nr = createRequest(cmd);

        final NewsResponse news = (nr != null)
                ? this.newsProvider.getLatestNews(nr, false)
                : new NewsResponseImpl();

        final Map<String, Object> model = new HashMap<>();
        model.put("withText", cmd.isWithText());
        model.put("blockAds", cmd.isBlockAds());
        final Map<String, NewsRecord> newsMap = createNewsMap(cmd, news);
        model.put("news", newsMap);
        this.nwsFindersuchergebnis.addMetadata(model, new ArrayList<>(newsMap.values()), cmd.getRealm());
        return new ModelAndView("nwslatestnews", model);
    }

    private Map<String, NewsRecord> createNewsMap(Command cmd, NewsResponse news) {
        final Map<String, NewsRecord> result = new LinkedHashMap<>();
        NEXT_IID: for (String iid : cmd.getIid()) {
            final String niid = normalizeIid(iid);
            for (NewsRecord record : news.getRecords()) {
                if (record.getAttributes().get(NewsAttributeEnum.IID).contains(niid)) {
                    result.put(iid, record);
                    continue NEXT_IID;
                }
            }
        }
        return result;
    }

    private LatestNewsRequest createRequest(Command cmd) {
        final LatestNewsRequest result = new LatestNewsRequest();
        result.setWithText(cmd.isWithText());
        result.setIids(normalizeIids(cmd));
        return result;
    }

    private List<String> normalizeIids(Command cmd) {
        final List<String> result = new ArrayList<>(cmd.getIid().length);
        for (final String iid : cmd.getIid()) {
            result.add(normalizeIid(iid));
        }
        return result;
    }

    private String normalizeIid(String iid) {
        return Long.toString(EasytradeInstrumentProvider.id(iid));
    }
}