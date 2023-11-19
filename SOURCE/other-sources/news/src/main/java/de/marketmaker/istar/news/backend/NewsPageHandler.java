/*
 * NewsPageHandler.java
 *
 * Created on 03.05.12 11:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.util.Set;

import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.feed.pages.PageDao;
import de.marketmaker.istar.feed.pages.PageData;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.news.data.NewsRecordImpl;

/**
 * A NewsRecordHandler that recognizes vwd Pages and forwards them to a PageDao to store them.
 * Not implemented as a FeedBuilder to be able to reuse {@link NewsRecordBuilder}s processing
 * of selectors.
 *
 * @author oflege
 */
public class NewsPageHandler implements NewsRecordHandler {

    private PageDao dao;

    public void setDao(PageDao dao) {
        this.dao = dao;
    }

    @Override
    public void handle(NewsRecordImpl newsRecord) {
        final SnapField pageNumber = newsRecord.getField(VwdFieldDescription.NDB_Page_Number.id());
        if (!pageNumber.isDefined()) {
            return;
        }

        final int pid = SnapRecordUtils.getInt(pageNumber);
        final long timestamp = newsRecord.getTimestamp().getMillis();
        final Set<String> selectors = newsRecord.getNumericSelectors();

        final String headline = newsRecord.getHeadline();
        final String text = newsRecord.getText();

        final String sep = text.contains("\r\n") ? "\r\n" : "\n";

        final StringBuilder sb = new StringBuilder(headline.length() + sep.length() * 2 + text.length());
        sb.append(headline).append(sep).append(sep).append(text);

        this.dao.store(new PageData(pid, sb.toString(), null, false, timestamp, selectors));
    }
}
