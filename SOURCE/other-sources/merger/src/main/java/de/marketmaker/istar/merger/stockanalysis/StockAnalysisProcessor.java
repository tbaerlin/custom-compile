/*
 * StockAnalysisProcessor.java
 *
 * Created on 09.08.2006 21:54:23
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.stockanalysis;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.DirectoryResource;
import de.marketmaker.istar.common.monitor.Resource;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.util.IsinUtil;
import de.marketmaker.istar.instrument.IstarInstrumentConnector;
import de.marketmaker.istar.instrument.search.SearchRequestStringBased;
import de.marketmaker.istar.instrument.search.SearchResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StockAnalysisProcessor implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd HH.mm.ss");

    private final static Set<String> ALLOWED_SITES = new HashSet<>(Arrays.asList("ac"));

    static final String ANALYSIS_SUBJECT = "Analysen";

    private final static Set<String> ALLOWED_SUBJECTS
            = new HashSet<>(Arrays.asList(ANALYSIS_SUBJECT, "News"));

    private static enum Action {
        UPDATE, INSERT, DELETE
    }

    private ActiveMonitor activeMonitor;

    private IstarInstrumentConnector instrumentConnector;

    private StockAnalysisDao stockAnalysisDao;

    private File incomingDir;

    private File problemDir;

    private File archiveDir;

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setInstrumentConnector(IstarInstrumentConnector instrumentConnector) {
        this.instrumentConnector = instrumentConnector;
    }

    public void setStockAnalysisDao(StockAnalysisDao stockAnalysisDao) {
        this.stockAnalysisDao = stockAnalysisDao;
    }

    public void setIncomingDir(File incomingDir) {
        this.incomingDir = incomingDir;
        if (!this.incomingDir.isDirectory()) {
            throw new IllegalArgumentException("not a directory: " + this.incomingDir.getAbsolutePath());
        }
    }

    public void setProblemDir(File problemDir) {
        this.problemDir = problemDir;
        if (!this.problemDir.isDirectory()) {
            throw new IllegalArgumentException("not a directory: " + this.problemDir.getAbsolutePath());
        }
    }

    public void setArchiveDir(File archiveDir) {
        this.archiveDir = archiveDir;
        if (!this.archiveDir.isDirectory()) {
            throw new IllegalArgumentException("not a directory: " + this.archiveDir.getAbsolutePath());
        }
    }

    public void afterPropertiesSet() throws Exception {
        this.activeMonitor.setFrequency(30 * 1000);

        final Resource resource = new DirectoryResource(this.incomingDir.getAbsolutePath());
        resource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                processIncoming();
            }
        });

        this.activeMonitor.setResources(new Resource[]{resource});
    }

    private void processIncoming() {
        try {
            doProcessIncoming();
        } catch (Throwable t) {
            this.logger.error("<processIncoming> failed", t);
        }
    }

    private void doProcessIncoming() {
        final File[] files = getIncomingFiles();

        for (final File file : files) {
            try {
                processIncoming(file);
                moveToArchiveDir(file);
            } catch (IllegalArgumentException iae) {
                if (iae.getMessage().startsWith("unknown symbol")) {
                    this.logger.warn("<processIncoming> failed for " + file.getAbsolutePath()
                            + " because of unknown symbol (" + iae.getMessage() + "), moving to " + this.problemDir.getAbsolutePath());
                }
                else {
                    this.logger.warn("<processIncoming> failed for " + file.getAbsolutePath()
                            + ", moving to " + this.problemDir.getAbsolutePath(), iae);
                }
                moveToProblemDir(file);
            } catch (JDOMParseException jpe) {
                this.logger.warn("<processIncoming> failed for " + file.getAbsolutePath()
                        + ", moving to " + this.problemDir.getAbsolutePath());
                moveToProblemDir(file);
            } catch (Exception e) {
                this.logger.warn("<processIncoming> XML error, failed for " + file.getAbsolutePath()
                        + ", moving to " + this.problemDir.getAbsolutePath(), e);
                moveToProblemDir(file);
            }
        }
    }

    private File[] getIncomingFiles() {
        return this.incomingDir.listFiles((dir, name) -> {
            return name.endsWith(".xml");
        });
    }

    private void moveToProblemDir(File file) {
        final boolean success = file.renameTo(new File(this.problemDir, file.getName()));
        if (!success) {
            this.logger.error("<processIncoming> failed moving to problem dir: " + file.getAbsolutePath());
        }
    }

    private void moveToArchiveDir(File file) {
        final File targetDir = new File(this.archiveDir, Integer.toString(DateUtil.dateToYyyyMmDd()));
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        final boolean success = file.renameTo(new File(targetDir, file.getName()));
        if (!success) {
            this.logger.error("<processIncoming> failed moving to archive dir: " + file.getAbsolutePath());
        }
        else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<moveToArchiveDir> moved to archive: " + file.getAbsolutePath());
            }
        }
    }

    private void processIncoming(File file) throws Exception {
        final SAXBuilder saxBuilder = new SAXBuilder();
        final Document document = saxBuilder.build(file);
        final Element root = document.getRootElement();

        //noinspection unchecked
        final List<Element> articles = root.getChildren("article");
        for (final Element article : articles) {
            process(article);
        }
    }

    private void process(Element article) {
        final Element head = article.getChild("head");
        final Element body = article.getChild("body");

        final String site = head.getChildTextTrim("site");
        if (!ALLOWED_SITES.contains(site)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<process> site " + site + " not allowed => skipping");
            }
            return;
        }

        final long id = Long.parseLong(head.getChildTextTrim("id"));
        final Action action = Action.valueOf(head.getChildTextTrim("action"));

        if (action == Action.DELETE) {
            final int numDeleted = this.stockAnalysisDao.deleteAnalysis(id);
            if (numDeleted == 0) {
                this.logger.info("<process> analysis with id " + id + " not in dao, no deletion performed");
            }
            else {
                this.logger.info("<process> deleted analysis with id " + id);
            }
            return;
        }

        //noinspection unchecked
        final List<Element> subjects = head.getChildren("subject");
        final String category = subjects.get(0).getTextTrim();
        final String subcategory = subjects.get(1).getTextTrim();

        if (!ALLOWED_SUBJECTS.contains(category)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<process> subject " + category + " not allowed => skipping");
            }
            return;
        }

        final String currency = head.getChildTextTrim("cur");
        Double aimMin = getValue(head.getChildTextTrim("aim_new"));
        Double aimMax = getValue(head.getChildTextTrim("aim_new_max"));
        if (aimMin == null) {
            aimMin = aimMax;
        }
        if (aimMax == null) {
            aimMax = aimMin;
        }

        final String analyst = head.getChildTextTrim("analyst");
        if (analyst == null) {
            throw new IllegalStateException("no analyst");
        }

        final String ratingText = head.getChildTextTrim("rating");
        final Rating rating = Rating.parse(ratingText);
        if (rating == null) {
            throw new IllegalArgumentException("unknown rating: " + ratingText);
        }

        final String timedateStr = head.getChildTextTrim("timedate");
        if ("0000-00-00 00.00.00".equals(timedateStr)) {
            this.logger.info("<process> incorrect time/data field (0000-00-00 00.00.00) => skipping");
            return;
        }
        final DateTime date = DTF.parseDateTime(timedateStr);
        if (date.isBefore(new DateTime().minusDays(91))) {
            this.logger.info("<process> " + id + " is too old: " + date + " => skipping");
            return;
        }
        final String wkn = head.getChildTextTrim("wkn");
        final String isin = head.getChildTextTrim("isin");

        final String headline = getHeadline(body);
        final String text = getText(body);
        final Instrument instrument = getInstrument(wkn, isin);

//        System.out.println("date = " + date);
//        System.out.println("id = " + id);
//        System.out.println("action = " + action);
//        System.out.println("category = " + category);
//        System.out.println("subcategory = " + subcategory);
//        System.out.println("analyst = " + analyst + " (" + analystId + ")");
//        System.out.println("wkn = " + wkn);
//        System.out.println("isin = " + isin);
//        System.out.println(" ==> instrument = " + instrument);
//        System.out.println("rating = " + rating);
//        System.out.println("headline = " + headline.toString());
//        System.out.println("text = " + text.toString());

        final int numInserts = this.stockAnalysisDao.storeAnalysis(id, analyst, instrument, rating,
                date, category, subcategory, headline, text, aimMin, aimMax, currency);
        if (numInserts == 0) {
            this.logger.info("<process> analysis with id " + id + " not inserted");
            throw new IllegalStateException("analysis with id " + id + " not inserted");
        }
        else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<process> inserted analysis with id " + id);
            }
        }
    }

    private String getHeadline(Element body) {
        final StringBuilder result = new StringBuilder();
        //noinspection unchecked
        final List<Element> headlineElements = body.getChild("headline").getChildren("t");
        for (final Element element : headlineElements) {
            result.append(element.getTextTrim());
        }
        return result.toString();
    }

    private String getText(Element body) {
        final StringBuilder result = new StringBuilder();
        //noinspection unchecked
        final List<Element> sections = body.getChildren("section");
        for (final Element section : sections) {
            //noinspection unchecked
            final List<Element> paras = section.getChildren("para");
            for (final Element para : paras) {
                //noinspection unchecked
                final List<Element> elements = para.getChildren("t");
                for (final Element element : elements) {
                    result.append(element.getTextTrim());
                }
                result.append("\n");
            }
            result.append("\n");
        }
        return result.toString();
    }

    private Instrument getInstrument(String wkn, String isin) {
        if (IsinUtil.isIsin(isin)) {
            return getInstrument(isin, KeysystemEnum.ISIN);
        }
        else if (StringUtils.hasText(wkn) && !"0".equals(wkn)) {
            return getInstrument(wkn, KeysystemEnum.WKN);
        }
        return null;
    }

    private Double getValue(String str) {
        if (!StringUtils.hasText(str)) {
            return null;
        }
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            this.logger.warn("<getValue> parse error for " + str);
        }
        return null;
    }

    private Instrument getInstrument(String symbol, KeysystemEnum keysystem) {
        final SearchRequestStringBased sr = new SearchRequestStringBased();
        sr.setMaxNumResults(1);
        final String expression = keysystem + ":" + symbol;
        sr.setSearchExpression(expression.toLowerCase());
        final SearchResponse response = this.instrumentConnector.search(sr);
        if (response.getInstruments().size() == 1) {
            return response.getInstruments().get(0);
        }
        else {
            throw new IllegalArgumentException("unknown symbol: " + expression);
        }
    }
}
