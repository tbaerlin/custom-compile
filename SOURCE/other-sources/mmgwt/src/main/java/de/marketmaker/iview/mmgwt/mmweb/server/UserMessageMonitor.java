/*
 * UserMessageMonitor.java
 *
 * Created on 07.08.2008 13:00:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.ClassUtils;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.news.NewsProvider;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.istar.news.frontend.NewsRecord;
import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserMessageMonitor implements MmwebResponseListener, InitializingBean, DisposableBean {
    protected final Log logger = LogFactory.getLog(getClass());

    private String category = "9O";

    private volatile String lastMessageId = "";

    private NewsProvider newsProvider;

    private Timer timer;

    private long period = 5 * 60 * 1000; // every 5min

    private static final RequestContext CONTEXT
            = new RequestContext(ProfileFactory.valueOf(true), (MarketStrategy) null);

    public void afterPropertiesSet() throws Exception {
        updateMessageId();
        this.timer = new Timer(ClassUtils.getShortName(getClass()), true);
        this.timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                updateMessageId();
            }
        }, this.period, this.period);
    }


    public void setCategory(String category) {
        this.category = category;
        this.logger.info("<setCategory> " + this.category);
    }


    public void destroy() throws Exception {
        this.timer.cancel();
    }

    public void onBeforeSend(HttpSession session, MmwebResponse response) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (!profile.isAllowed(Selector.DZBANK_USER_MESSAGES)) {
            return;
        }
        final String id = this.lastMessageId;
        if (id != null) {
            response.addProperty(AppConfig.PROP_KEY_MSGID, id);
        }
    }

    public void setPeriod(long period) {
        this.period = period;
        this.logger.info("<setPeriod> " + this.period);
    }

    @Required
    public void setNewsProvider(NewsProvider newsProvider) {
        this.newsProvider = newsProvider;
    }

    private void updateMessageId() {
        try {
            String messageId = getMessageId();
            if (!Objects.equals(messageId, this.lastMessageId)) {
                this.lastMessageId = messageId;
                this.logger.info("<updateMessageId> to '" + this.lastMessageId + "'");
            }
        } catch (Exception e) {
            this.logger.warn("<updateMessageId> failed: " + e.getMessage());
        }
    }

    private String getMessageId() throws Exception {
        try {
            final NewsRequest request = new NewsRequest();
            request.setCount(1);
            request.setWithHitCount(false);
            request.setWithText(false);
            request.setQuery("+topic:" + category);

            RequestContextHolder.setRequestContext(CONTEXT);

            final NewsResponse response = this.newsProvider.getNews(request, false);
            if (!response.isValid()) {
                throw new IllegalStateException("invalid response");
            }
            final List<NewsRecord> records = response.getRecords();
            return records.isEmpty() ? null : records.get(0).getId();
        } finally {
            RequestContextHolder.setRequestContext(null);
        }
    }
}
