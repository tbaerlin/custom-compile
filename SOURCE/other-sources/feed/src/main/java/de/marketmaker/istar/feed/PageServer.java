/*
 * IntradayServer.java
 *
 * Created on 02.03.2005 16:28:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.util.concurrent.atomic.AtomicInteger;

import com.netflix.servo.annotations.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.feed.api.PageFeedConnector;
import de.marketmaker.istar.feed.api.PageRequest;
import de.marketmaker.istar.feed.api.PageResponse;
import de.marketmaker.istar.feed.pages.PageDao;
import de.marketmaker.istar.feed.pages.PageData;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PageServer implements PageFeedConnector {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Logger accessLogger = LoggerFactory.getLogger("[access].[PageServer]");

    private PageDao pageDao;

    @Monitor(type = COUNTER)
    private AtomicInteger numRequestsProcessed = new AtomicInteger();

    public void setPageDao(PageDao pageDao) {
        this.pageDao = pageDao;
    }

    private StringBuilder getBuilderForAccessLog(String name, int size, String clientInfo) {
        if (!this.accessLogger.isInfoEnabled()) {
            return null;
        }
        // clientInfo = pid@host.domain,timestamp,requestId -- we only need pid@host
        final int p = clientInfo.indexOf('.');
        return new StringBuilder(size).append(name)
                .append(";").append(clientInfo, 0, p > 0 ? p : clientInfo.length());
    }

    private void logAccess(final StringBuilder sb) {
        if (sb != null) {
            this.accessLogger.info(sb.toString());
        }
    }

    public PageResponse getPage(PageRequest request) {
        if (this.pageDao == null) {
            throw new RuntimeException("<getPage> no pageDao set");
        }

        this.numRequestsProcessed.incrementAndGet();
        final StringBuilder sb = getBuilderForAccessLog("Page", 60, request.getClientInfo());

        try {
            final PageResponse pageResponse = doGetPage(request, sb);

            logAccess(sb);

            return pageResponse;
        } catch (Throwable t) {
            this.logger.error("<getPage> failed", t);
            throw new RuntimeException("<getPage> failed", t);
        }
    }

    private PageResponse doGetPage(PageRequest request, StringBuilder sb) {
        if (sb != null) {
            sb.append(";").append(request.getPagenumber());
            if (request.isPreferGermanText()) {
                sb.append("g");
            }
        }

        final PageData pageData = this.pageDao.getPageData(request.getPagenumber());
        if (pageData == null) {
            return null;
        }

        String text = pageData.getText();
        if (request.isPreferGermanText() && pageData.getTextg() != null) {
            text = pageData.getTextg();
        }

        if (sb != null) {
            sb.append(";").append(text == null ? 0 : text.length());
            sb.append(";").append(pageData.getKeys().size());
        }

        pageData.setKeys(pageData.getKeys());
        PageDao.Neighborhood neighbors = this.pageDao.getNeighborhood(request.getPagenumber());
        return new PageResponse(text, pageData.getKeys(), pageData.getSelectors(),
                pageData.isDynamic(), pageData.getTimestamp(),
                neighbors.getNextPagenumber() == null ? null :
                        Integer.toString(neighbors.getNextPagenumber()),
                neighbors.getPreviousPagenumber() == null ? null :
                        Integer.toString(neighbors.getPreviousPagenumber()));
    }
}
