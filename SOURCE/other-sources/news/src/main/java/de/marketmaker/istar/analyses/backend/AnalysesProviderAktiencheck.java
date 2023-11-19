/*
 * AnalysesProviderAktiencheck.java
 *
 * Created on 22.03.12 09:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.marketmaker.istar.analyses.backend.Protos.Analysis;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Builder;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Provider;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Rating;
import de.marketmaker.istar.analyses.frontend.AnalysesMetaRequest;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.StockAnalysis.Recommendation;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domain.util.IsinUtil;

/**
 * @author oflege
 */
@ManagedResource
public class AnalysesProviderAktiencheck extends PassiveAnalysesProvider {
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd HH.mm.ss");

    private static final Pattern FILENAME_PATTERN = Pattern.compile("\\d+_(\\d+)\\.xml");

    private final static Set<String> ALLOWED_SITES = new HashSet<>(Arrays.asList("ac"));

    static final String ANALYSIS_SUBJECT = "Analysen";

    private final static Set<String> ALLOWED_SUBJECTS
            = new HashSet<>(Arrays.asList(ANALYSIS_SUBJECT, "News"));

    private static final Map<String, Recommendation> RATINGS
            = new HashMap<>();

    private static final int MAX_AGE_IN_DAYS = 91;

    static {
        RATINGS.put("ohne", Recommendation.NONE);
        RATINGS.put("stark verkaufen", Recommendation.STRONG_SELL);
        RATINGS.put("verkaufen", Recommendation.SELL);
        RATINGS.put("halten", Recommendation.HOLD);
        RATINGS.put("kaufen", Recommendation.BUY);
        RATINGS.put("stark kaufen", Recommendation.STRONG_BUY);
        RATINGS.put("zeichnen", Recommendation.SIGN);
        RATINGS.put("nicht zeichnen", Recommendation.NOT_SIGN);
    }

    private final XPath xPath = XPathFactory.newInstance().newXPath();

    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    private String getString(Node node, String xPathExpression) throws Exception {
        final String s = (String) xPath.evaluate(xPathExpression, node, XPathConstants.STRING);
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    private Node getNode(Node node, String xPathExpression) throws Exception {
        return (Node) xPath.evaluate(xPathExpression, node, XPathConstants.NODE);
    }

    private NodeList getNodeList(Node node, String xPathExpression) throws Exception {
        return (NodeList) xPath.evaluate(xPathExpression, node, XPathConstants.NODESET);
    }

    private String getTextContent(NodeList nodes, int i) {
        return (i < nodes.getLength()) ? nodes.item(i).getTextContent().trim() : null;
    }

    @Override
    protected String getId() {
        return "aktiencheck";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (size() == 0) {
            initFromArchive();
        }
    }

    @ManagedOperation
    public void dumpState(String filename) throws IOException {
        dumpState(new File(filename));
    }

    private void initFromArchive() {
        this.logger.info("<initFromArchive> ...");
        final LocalDate today = new LocalDate();
        for (LocalDate day = today.minusDays(91); !day.isAfter(today); day = day.plusDays(1)) {
            addFiles(new File(this.archiveDir, "" + DateUtil.toYyyyMmDd(day)));
        }
        this.logger.info("<initFromArchive> added " + size() + " analyses");
    }


    @SuppressWarnings("Duplicates")
    @Override
    Map<String, Map<String, String>> doGetMetaData(AnalysesMetaRequest request) {
        final Map<String, Map<String, String>> result = new HashMap<>();
        result.put("ratings", createRatingsMetaMap());
        result.put("analysts", getSources(request.isIgnoreAnalysesWithoutRating()));
        result.put("sectors", getSectors(request.isIgnoreAnalysesWithoutRating()));
        result.put("regions", getRegions(request.isIgnoreAnalysesWithoutRating()));
        return result;
    }

    private Map<String, String> createRatingsMetaMap() {
        final Map<String, String> result = new LinkedHashMap<>();
        result.put(Recommendation.STRONG_BUY.name(), "Kaufen (hoch)");
        result.put(Recommendation.BUY.name(), "Kaufen");
        result.put(Recommendation.HOLD.name(), "Halten");
        result.put(Recommendation.SELL.name(), "Verkaufen");
        result.put(Recommendation.STRONG_SELL.name(), "Verkaufen (hoch)");
        return result;
    }

    private LinkedHashMap<String, String> getRegions(boolean ignoreAnalysesWithoutRating) {
        final Set<String> tmpRegions = new TreeSet<>(Collator.getInstance(Locale.GERMAN));
        addRegions(tmpRegions, ignoreAnalysesWithoutRating);
        return toMap(tmpRegions);
    }

    private LinkedHashMap<String, String> getSectors(boolean ignoreAnalysesWithoutRating) {
        final Set<String> tmpSectors = new TreeSet<>(Collator.getInstance(Locale.GERMAN));
        addBranches(tmpSectors, ignoreAnalysesWithoutRating);
        return toMap(tmpSectors);
    }

    @Override
    public Selector getSelector() {
        return Selector.SMARTHOUSE_ANALYSES;
    }

    @Override
    public Provider getProvider() {
        return Provider.AKTIENCHECK;
    }

    @Override
    protected void addFile(File file) {
        try {
            processIncoming(file);
            moveToArchive(file);
        } catch (Exception e) {
            this.logger.warn("<addFile> failed for " + file.getAbsolutePath(), e);
            moveToProblems(file);
        }
    }

    /**
     * aktiencheck files are named id_mmdd.xml, in order to avoid storing thousands of files in
     * the archive directory, we create a directory for each day.
     * @param f to be archived
     * @return archived file location
     */
    @Override
    protected File getArchiveFile(File f) {
        final Matcher matcher = FILENAME_PATTERN.matcher(f.getName());
        if (!matcher.matches()) {
            return super.getArchiveFile(f);
        }
        final int mmdd = Integer.parseInt(matcher.group(1));
        final int mm = mmdd / 100;
        final int dd = mmdd % 100;
        final LocalDate today = new LocalDate();
        final int yyyy = (mm < today.getMonthOfYear()
                || (mm == today.getMonthOfYear() && dd <= today.getDayOfMonth()))
                ? today.getYear() : today.getYear() - 1;
        final File dir = ensureDir(new File(this.archiveDir, Integer.toString((yyyy * 10000) + mmdd)));
        return new File(dir, f.getName());
    }

    private void processIncoming(File file) throws Exception {
        org.w3c.dom.Document d = factory.newDocumentBuilder().parse(file);
        process(getNode(d, "ac_xml/article"), file.getName());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void process(Node article, String filename) throws Exception {
        final Node head = getNode(article, "head");

        final String site = getString(head, "site");
        if (!ALLOWED_SITES.contains(site)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<process> " + filename + " site " + site + " not allowed");
            }
            return;
        }

        final String agencyId = getString(head, "id");
        final String action = getString(head, "action");

        if ("DELETE".equals(action)) {
            final int numDeleted = deleteAnalysis(agencyId);
            this.logger.info("<process> deleted analysis with id " + agencyId + ": " + numDeleted);
            return;
        }

        final String timedateStr = getString(head, "timedate");
        final DateTime date;
        try {
            date = DTF.parseDateTime(timedateStr);
        } catch (IllegalArgumentException e) {
            this.logger.warn("<process> " + filename + " invalid timedate: " + timedateStr);
            return;
        }
        if (date.isBefore(new DateTime().minusDays(MAX_AGE_IN_DAYS))) {
            this.logger.info("<process> " + filename + " is too old: " + date);
            return;
        }

        //noinspection unchecked
        NodeList subjects = getNodeList(head, "subject");
        final String category = getTextContent(subjects, 0);

        if (!ALLOWED_SUBJECTS.contains(category)) {
            this.logger.debug("<process> " + filename + " subject " + category + " not allowed");
            return;
        }

        final String analyst = getString(head, "analyst");
        if (analyst == null) {
            this.logger.warn("<process> " + filename + " no analyst in " + agencyId);
            return;
        }

        final String ratingText = getString(head, "rating");
        final Recommendation rating = getRating(ratingText);
        if (rating == null) {
            this.logger.warn("<process> " + filename + " invalid rating " + ratingText);
            return;
        }

        final Builder builder = Analysis.newBuilder()
                .setProvider(getProvider())
                .setId(Long.parseLong(agencyId))
                .setDate(System.currentTimeMillis())
                .setAgencyDate(date.getMillis())
                .setAgencyId(agencyId)
                .setSource(analyst)
                .setRating(Rating.valueOf(rating.ordinal()))
                .addCategory(category);
        if (subjects.getLength() > 1) {
            builder.addSubcategory(getTextContent(subjects, 1));
        }

        addTargets(head, builder);

        if (!addInstrument(head, builder)) {
            this.logger.info("<process> could not add instrument for analysis " + agencyId);
            return;
        }

        final Node body = getNode(article, "body");

        addHeadline(body, builder);
        addText(body, builder);

        final String correction = getString(head, "correction");
        if (correction != null) {
            deleteAnalysis(correction);
        }

        // this call stores the analysis in the DB backend
        long id = addAnalysis(builder);
        if (id == INVALID_ANALYSIS_ID) {
            this.logger.info("<process> analysis with id " + agencyId + " not inserted");
        }
        else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<process> inserted analysis with id " + agencyId);
            }
        }
    }

    private boolean addInstrument(Node head, Builder builder) throws Exception {
        final String isin = getString(head, "isin");
        if (isin == null) {
            this.logger.info("<addInstrument> analysis has no isin");
            return false;
        }

        builder.addSymbol(isin);
        if (IsinUtil.isIsin(isin)) {
            return addInstrument(builder, isin);
        }
        this.logger.info("<addInstrument> no valid isin: " + isin);
        return false;
    }

    private void addTargets(Node head, Builder builder) throws Exception {
        final String currency = getString(head, "cur");
        if (currency != null) {
            builder.setCurrency(currency);
        }

        final String aimMin = getString(head, "aim_new");
        final String aimMax = getString(head, "aim_new_max");
        if (aimMax != null) {
            builder.setTarget(aimMax);
            if (aimMin != null) {
                builder.setMinTarget(aimMin);
            }
        }
        else if (aimMin != null) {
            builder.setTarget(aimMin);
        }

        final String aimOld = getString(head, "aim_old");
        if (aimOld != null) {
            builder.setPreviousTarget(aimOld);
        }
    }

    private Recommendation getRating(String ratingText) {
        if (!StringUtils.hasText(ratingText)) {
            return Recommendation.NONE;
        }
        return RATINGS.get(ratingText.toLowerCase());
    }

    private void addHeadline(Node body, Builder builder) throws Exception {
        NodeList ts = getNodeList(body, "headline/t");
        if (ts.getLength() == 1) {
            builder.setHeadline(getTextContent(ts, 0));
        }
        else {
            final StringBuilder sb;
            sb = new StringBuilder(255);
            for (int i = 0; i < ts.getLength(); i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                sb.append(getTextContent(ts, i));
            }
            builder.setHeadline(sb.toString());
        }
    }

    private void addText(Node body, Builder builder) throws Exception {
        NodeList paras = getNodeList(body, "section/para");
        for (int i = 0; i < paras.getLength(); i++) {
            NodeList ts = getNodeList(paras.item(i), "t");
            if (ts.getLength() == 1) {
                builder.addText(ts.item(0).getTextContent().trim());
            }
            if (ts.getLength() > 1) {
                this.logger.warn("<addText> strange text in " + builder.getId());
            }
        }
    }
}
