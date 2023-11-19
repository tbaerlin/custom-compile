/*
 * PageBuilder.java
 *
 * Created on 13.06.2005 11:27:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.feed.FeedBuilder;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.pages.PageDao;
import de.marketmaker.istar.feed.pages.PageData;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import static de.marketmaker.istar.domain.data.SnapRecord.DEFAULT_CHARSET;
import static de.marketmaker.istar.feed.vwd.VwdFeedConstants.MESSAGE_TYPE_NEWS;
import static de.marketmaker.istar.feed.vwd.VwdFeedConstants.MESSAGE_TYPE_PAGE;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class PageBuilder implements FeedBuilder, InitializingBean {
    private static final String GERMAN_PAGE_SUFFIX = ".G";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Used to store pages
     */
    private PageDao pageDao;

    private PageDao backupPageDao;

    /**
     * Whenever a page contains the field VwdFieldDescription.ADF_PageNextLink, the page's text
     * is incomplete and will be continued in the following record. This field stores the page
     * until it is complete.
     */
    private PageData pageToBeContinued;

    /**
     * Used to extract the page number from an mdps page message as there is no extra field for
     * the number. The actual page number may be followed by a suffix that indicates a variant
     * (e.g., .G for german, .P for default).
     */
    private final Pattern pnPattern = Pattern.compile("<PN>(\\d+)(\\.P|\\.G)?</PN>");

    public void setPageDao(PageDao pageDao) {
        this.pageDao = pageDao;
    }

    public void setBackupPageDao(PageDao backupPageDao) {
        this.backupPageDao = backupPageDao;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.pageDao == null) {
            throw new IllegalStateException("store and pageDao not set");
        }
        if (this.backupPageDao != null) {
            syncWithBackup();
        }
    }

    @ManagedOperation
    public void syncWithBackup() {
        final Map<Integer, PageData> backupPages = getStaticPages(this.backupPageDao);
        this.logger.info("<syncWithBackup> read " + backupPages.size() + " from backup dao");
        final Map<Integer, PageData> localPages = getStaticPages(this.pageDao);
        this.logger.info("<syncWithBackup> read " + localPages.size() + " from local dao");
        int numAdded = 0;
        for (Map.Entry<Integer, PageData> e : backupPages.entrySet()) {
            if (isMoreRecent(e.getValue(), localPages.get(e.getKey()))) {
                addPage(e.getValue());
                numAdded++;
            }
        }
        this.logger.info("<syncWithBackup> added " + numAdded + " pages to local db");
    }

    private boolean isMoreRecent(PageData remote, PageData local) {
        return local == null ||
                (remote.getTimestamp() > local.getTimestamp()
                        && !Objects.equals(remote.getText(), local.getText()));
    }

    private Map<Integer, PageData> getStaticPages(final PageDao dao) {
        final Map<Integer, PageData> result = new HashMap<>();
        dao.getAllPages(data -> result.put(data.getId(), data), Boolean.FALSE);
        return result;
    }

    public byte[] getApplicableMessageTypes() {
        return new byte[]{MESSAGE_TYPE_NEWS, MESSAGE_TYPE_PAGE};
    }

    public void process(FeedData data, ParsedRecord pr) {
        final PageData page = buildPage(data, pr);
        if (page != null) {
            addPage(page);
        }
    }

    private void addPage(PageData page) {
        this.pageDao.store(page);
    }

    private PageData buildMdpsPage(ParsedRecord pr) {
        String text = pr.getString(VwdFieldDescription.ADF_CUSTOM_FID_22.id(), DEFAULT_CHARSET);
        if (text == null) {
            return null;
        }
        final Matcher m = this.pnPattern.matcher(text);
        if (!m.find()) {
            this.logger.warn("<processMdpsPage> no PN tag found, ignoring page");
            return null;
        }
        String textg = null;
        final int pn = Integer.parseInt(m.group(1));
        if (m.groupCount() == 2) {
            if (GERMAN_PAGE_SUFFIX.equals(m.group(2))) {
                textg = text;
                text = null;
            }
        }
        return new PageData(pn, text, textg, true, getTimestamp(pr), getSelectors(pr));
    }

    private Set<String> getSelectors(ParsedRecord pr) {
        String selectorStr = pr.getString(VwdFieldDescription.NDB_Selectors.id());
        if (!StringUtils.hasText(selectorStr)) {
            return null;
        }
        final Set<String> result = new HashSet<>();
        for (String s : selectorStr.split(",")) {
            result.add(EntitlementsVwd.toNumericSelector(s.trim()));
        }
        return result;
    }

    private PageData buildNewsPage(ParsedRecord pr) {
        final long id = pr.getNumericValue(VwdFieldDescription.NDB_Page_Number.id());
        if (id == Long.MIN_VALUE) {
            return null;
        }
        final String headline = pr.getString(VwdFieldDescription.NDB_Headline.id(), DEFAULT_CHARSET);
        final String story = pr.getString(VwdFieldDescription.NDB_Story.id(), DEFAULT_CHARSET);
        final String text = getPageText(headline, story);

        return new PageData((int) id, text, null, false, getTimestamp(pr), getSelectors(pr));
    }

    private PageData buildPage(FeedData data, ParsedRecord pr) {
        if (pr.getMessageType() == MESSAGE_TYPE_NEWS) {
            return buildNewsPage(pr);
        }
        if (isPageFromMdpsFeed(pr)) {
            return buildMdpsPage(pr);
        }
        return buildSDMReaderPage(data, pr);
    }

    private boolean isPageFromMdpsFeed(ParsedRecord pr) {
        return pr.isFieldPresent(VwdFieldDescription.ADF_CUSTOM_FID_22.id());
    }

    private PageData buildSDMReaderPage(FeedData data, ParsedRecord pr) {
        if (this.pageToBeContinued != null) {
            return continuePage(data, pr);
        }

        final long id = pr.getNumericValue(VwdFieldDescription.ADF_PageNumber.id());
        if (id == Long.MIN_VALUE) {
            return null;
        }

        final PageData pd = new PageData((int) id, getPageText(pr), null, true, getTimestamp(pr), getSelectors(pr));

        if (pr.isFieldPresent(VwdFieldDescription.ADF_PageNextLink.id())) {
            pd.setNextLink(pr.getString(VwdFieldDescription.ADF_PageNextLink.id()));
            this.pageToBeContinued = pd;
            return null;
        }

        return pd;
    }

    private PageData continuePage(FeedData data, ParsedRecord pr) {
        if (!data.getVwdcode().toString().endsWith(this.pageToBeContinued.getNextLink())) {
            this.logger.warn("<continuePage> failed for page " + this.pageToBeContinued.getId()
                    + ", expected key " + this.pageToBeContinued.getNextLink()
                    + ", got " + data.getVwdcode());
            this.pageToBeContinued = null;
            return buildSDMReaderPage(data, pr);
        }

        this.pageToBeContinued.appendText(getPageText(pr));
        if (pr.isFieldPresent(VwdFieldDescription.ADF_PageNextLink.id())) {
            return null; // still a next page
        }

        final PageData result = this.pageToBeContinued;
        this.pageToBeContinued = null;
        return result;
    }

    private String getPageText(final String headline, final String story) {
        if (StringUtils.hasText(headline)) {
            return (StringUtils.hasText(story)) ? (headline + "\n" + story) : headline;
        }
        return StringUtils.hasText(story) ? story : "";
    }

    private String getPageText(ParsedRecord pr) {
        final String text = pr.getString(VwdFieldDescription.DB_DATA.id());
        return StringUtils.hasText(text) ? text : "";
    }

    private long getTimestamp(ParsedRecord pr) {
        if (pr.isFieldPresent(VwdFieldDescription.ADF_TIMEOFARR.id())
                && pr.isFieldPresent(VwdFieldDescription.ADF_DATEOFARR.id())) {
            return DateUtil.toDateTime(pr.getAdfDateOfArrival(),
                    pr.getAdfTimeOfArrival()).getMillis();
        }
        return System.currentTimeMillis();
    }
}
