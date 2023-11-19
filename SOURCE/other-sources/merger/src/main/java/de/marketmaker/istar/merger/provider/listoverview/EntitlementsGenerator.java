/*
 * ListOverviewProviderImpl.java
 *
 * Created on 14.07.2008 15:29:13
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.listoverview;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.feed.vwd.EntitlementProviderVwd;
import de.marketmaker.istar.instrument.IndexCompositionProvider;
import de.marketmaker.istar.instrument.IndexCompositionRequest;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IndexCompositionProviderImpl;
import de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy;
import de.marketmaker.iview.dmxml.ListOverviewColumn;
import de.marketmaker.iview.dmxml.ListOverviewItem;
import de.marketmaker.iview.dmxml.ListOverviewItemList;
import de.marketmaker.iview.dmxml.ListOverviewLinkItem;
import de.marketmaker.iview.dmxml.ListOverviewListItem;
import de.marketmaker.iview.dmxml.ListOverviewMultiListItem;
import de.marketmaker.iview.dmxml.ListOverviewPageItem;
import de.marketmaker.iview.dmxml.ListOverviewSection;
import de.marketmaker.iview.dmxml.ListOverviewType;

/**
 * Creates the property file <tt>dz-structure-entitlements.txt</tt> which maps list names
 * to selectors required to view the list constituents.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EntitlementsGenerator implements InitializingBean {
    private static final Set<String> SELECTORS_TO_DUMP = new HashSet<>(Arrays.asList(
            "245", "251", "520"
    ));

    private static final Set<String> SELECTORS_TO_IGNORE = new HashSet<>(Arrays.asList(
            "49", // 2W, Italian Stock Exchange - AFF Level 2
            "232", // 9X, ISIN for N
            "233", // 9Y, ISIN for Q
            "245", // 10K, Irish Stock Exchange -----> TODO: Wert 1.IE0001827041.ISE in Liste Ausland.EuroStoxx50.Index hat diesen Selector
            "251", // 10Q, Malaysia Stock Exchange ----->  TODO: Wert 6.FBMKLCI.MY in Liste EmergingMarketsÜbersicht hat diesen Selector
            "256", // 10V, NASDAQ OMX Copenhagen, Helsinki, Stockholm, Iceland - Equities Level 2
            "266", // 11F, SIX SWX und SWX Europe Level 2
            "409", // 16S, ISIN for TO
            "417", // 17A, FX to force FXVWD
            "422", // 17F, NYSE Euronext - Cash BBO1
            "520"  // 20Z, internal Selector (e.g. Tests) ------> TODO: Wert EURISK.FXM in Liste Devisen.CrossRates(EUR) hat diesen Selektor
    ));

    private static final Set<String> LISTIDS_TO_IGNORE = new HashSet<>(
//            Arrays.asList("Emerging Markets.Emerging Markets Übersicht")
    );

    private static final Map<String, String> MULTILIST_SELECTORS = new HashMap<>();

    static {
        MULTILIST_SELECTORS.put("Rohstoffe.Rohstoffindizes", "2084");
    }

    private static final Map<String, String> ADDITIONAL_SELECTORS = new HashMap<>();

    static {
        ADDITIONAL_SELECTORS.put("Ausland.Stoxx Europe 50 in Frankfurt.Index", "2004");
        ADDITIONAL_SELECTORS.put("Ausland.Euro Stoxx 50 in Frankfurt.Index", "2004");
        ADDITIONAL_SELECTORS.put("italy-stocks", "11K");
    }

    private static final List<String> PREFIXES_TO_OMIT = Arrays.asList(
            "Kennzahlen", "EDG-Rating", "S&P Fund Management", "Euronext Real-Time Reference"
    );

    private static final File SD_BASE_DIR = new File(LocalConfigProvider.getIstarSrcDir(), "merger/src/conf");
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ListOverviewProvider listOverviewProvider;

    private final EntitlementProviderVwd entitlementProvider;

    private final IndexCompositionProvider indexCompositionProvider;

    private File entitlementsFile;

    private Properties constituentsGroupPermissions;

    private final Set<Integer> selectors = new HashSet<>();

    final Map<String, String> mapEntitlements = new HashMap<>();

    final SAXBuilder builder = new SAXBuilder();

    public EntitlementsGenerator(
            ListOverviewProvider listOverviewProvider,
            EntitlementProviderVwd entitlementProvider,
            IndexCompositionProvider indexCompositionProvider) {
        this.listOverviewProvider = listOverviewProvider;
        this.entitlementProvider = entitlementProvider;
        this.indexCompositionProvider = indexCompositionProvider;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final TimeTaker tt = new TimeTaker();
        final SAXBuilder builder = new SAXBuilder();
        final Document document = builder.build(new URL("http://vwd-ent.df1.vwd-df.net:1968/vwdPermissions.asmx/SelectorDefinitions"));
        @SuppressWarnings({"unchecked"})
        final List<Element> children = document.getRootElement().getChildren("Sel");
        entitlements:
        for (final Element child : children) {
            final String type = child.getAttributeValue("subtype");
            if (!"Kurs".equals(type)) {
                continue;
            }
            final String description = child.getAttributeValue("description");
            for (final String prefix : PREFIXES_TO_OMIT) {
                if (description.startsWith(prefix)) {
                    continue entitlements;
                }
                if (description.contains("Level 2") && !description.contains("Level 1")) {
                    continue entitlements;
                }
            }

            this.selectors.add(EntitlementsVwd.toValue(child.getAttributeValue("selector")));
        }

        this.logger.info("<afterPropertiesSet> read " + this.selectors.size() + " selectors in " + tt);
    }

    public void process(LanguageAndVariant[] lavs) throws Exception {
        for (LanguageAndVariant lav : lavs) {
            process(lav);
        }
        writeEntitlementsFile();
    }

    private void writeEntitlementsFile() throws Exception {
        try (PrintWriter pw = new PrintWriter(this.entitlementsFile, "utf-8")) {
            for (Map.Entry<String, String> entry : new TreeMap<>(mapEntitlements).entrySet()) {
                String line = entry.getKey() + "=" + entry.getValue();
                this.logger.info("<writeEntitlementsFile> {}: {}", this.entitlementsFile, line);
                pw.println(line);
            }
        }
    }

    private void process(LanguageAndVariant lav) throws Exception {
        Profile profile = ProfileFactory.valueOf(true);
        RequestContextHolder.setRequestContext(new RequestContext(profile, LbbwMarketStrategy.INSTANCE));
        final ListOverviewType structure;
        try {
            structure = this.listOverviewProvider.getStructure(lav.isPlain() ? null : Arrays.asList(new Locale(lav.getLanguage())), lav.getVariant());
        } finally {
            RequestContextHolder.setRequestContext(null);
        }

        for (final ListOverviewColumn column : structure.getColumn()) {
            for (final ListOverviewSection section : column.getSection()) {
                final String name = section.getName();
                handle(name, section.getItem());
            }
        }
    }

    private void handle(String name, List<ListOverviewItem> items) throws Exception {
        for (final ListOverviewItem item : items) {
            final String namePath = name + "." + item.getName();

            if (item instanceof ListOverviewListItem) {
                if (!(mapEntitlements.containsKey(namePath.replaceAll(" ", "")) || mapEntitlements.containsKey(item.getId()))) {
                    handle(namePath, (ListOverviewListItem) item);
                }
            }
            else if (item instanceof ListOverviewMultiListItem) {
                final String selectors = MULTILIST_SELECTORS.get(namePath);
                if (StringUtils.hasText(selectors)) {
                    log(namePath, item.getId(), selectors);
                }
            }
            else if (item instanceof ListOverviewPageItem) {
                ListOverviewPageItem pageItem = (ListOverviewPageItem) item;
                if (pageItem.getDzPage() != null) {
                    log(namePath, item.getId(), "2075");
                }
                else {
                    log(namePath, item.getId(), ADDITIONAL_SELECTORS.get(item.getId()));
                }
            }
            else if (item instanceof ListOverviewLinkItem) {
                // PAGES only for DZ user
                log(namePath, item.getId(), "2075");
            }
            else if (item instanceof ListOverviewItemList) {
                handle(namePath, ((ListOverviewItemList) item).getItem());
            }
        }
    }

    private void handle(String namePath, ListOverviewListItem item) throws Exception {
        if (LISTIDS_TO_IGNORE.contains(namePath)) {
            return;
        }

        final Set<String> selectors = new HashSet<>();

        final List<String> elements = item.getElement();
        if (elements != null && !elements.isEmpty()) {
            selectors.addAll(getSelectors("listoverview|DZ|elements|" + namePath, null));
        }

        final ListOverviewListItem.List list = item.getList();
        if (list != null && StringUtils.hasText(list.getValue())) {
            selectors.addAll(getSelectors(list.getValue(), list.getMarket()));
        }

        final String selectorString = format(selectors);
        log(namePath, item.getId(), selectorString);
    }

    private String format(Set<String> selectors) {
        List<String> list = new ArrayList<>(selectors);
        list.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return EntitlementsVwd.toValue(o1) - EntitlementsVwd.toValue(o2);
            }
        });

        return StringUtils.collectionToDelimitedString(list, ",");
    }

    private void log(String namePath, String id, String selectorString) {
        final String previous1 = mapEntitlements.get(namePath.replaceAll(" ", ""));
        if (previous1 == null) {
            this.logger.info(namePath.replaceAll(" ", "") + "=" + selectorString);
        }
        if (id != null) {
            final String previous2 = mapEntitlements.put(id, selectorString);
            if (previous2 == null) {
                this.logger.info(id + "=" + selectorString);
            }
        }
    }

    private Set<String> getSelectors(String listid, String market) throws Exception {
        final String marketParameter = market == null ? "" : ("&marketStrategy=market:" + market);
        final String url = "http://mmf-xml.vwd.com/dmxml-1/iview/retrieve.xml" +
                // TODO: hack! define&use dedicated resource profile
                "?authentication=r&authenticationType=r(oo)t&atoms=MSC_List_Details"
                + "&listid=" + URLEncoder.encode(listid, "UTF-8")
                + marketParameter
                + "&disablePaging=true";
        final Element rootElement = getRootElement(url);

        final Element block = rootElement.getChild("data").getChild("block");
        if (block == null) {
            throw new Exception("NO DATA for " + url);
        }

        final Set<String> selectors = new HashSet<>();

        @SuppressWarnings({"unchecked"})
        final List<Element> elements = block.getChildren("element");
        for (final Element element : elements) {
            final String qid = element.getChild("quotedata").getChildTextTrim("qid");
            if (qid == null) {
                this.logger.warn("<getSelectors> no qid");
                continue;
            }

            final String symbolQuery = "http://mmf-xml.vwd.com/dmxml-1/iview/retrieve.xml"
                    + "?authentication=mm-xml&authenticationType=resource&atoms=MSC_StaticData"
                    + "&symbol=" + qid;
            this.logger.info("<getSelectors> symbolQuery: {}", symbolQuery);
            Element missingOnError = getRootElement(symbolQuery).getChild("data").getChild("block");
            if (Objects.isNull(missingOnError)) {
                continue;
            }
            final String vwdsymbol = missingOnError.getChildTextTrim("vwdsymbol");
            final int[] entitlements = this.entitlementProvider.getEntitlements(vwdsymbol);

            for (final int entitlement : entitlements) {
                if (!this.selectors.contains(entitlement)) {
                    continue;
                }

                final String e = Integer.toString(entitlement);
                if (SELECTORS_TO_DUMP.contains(e)) {
                    String message = String.format("%s %s %s %s %s %s", e, EntitlementsVwd.toEntitlement(entitlement), listid, qid, vwdsymbol, element.getChild("instrumentdata").getChildTextTrim("name"));
                    this.logger.info("<getSelectors> dump {}", message);
                }
                if (SELECTORS_TO_IGNORE.contains(e)) {
                    continue;
                }

                selectors.add(EntitlementsVwd.toEntitlement(entitlement));
            }
        }
        final String id = listid.substring(listid.lastIndexOf("|") + 1);
        if (ADDITIONAL_SELECTORS.containsKey(id)) {
            selectors.add(ADDITIONAL_SELECTORS.get(id));
        }

        if (!listid.contains("|elements|")) {
            addConstituentsGroupSelector(listid, selectors);
        }
        return selectors;
    }

    private void addConstituentsGroupSelector(String listid, Set<String> selectors) {
        String key = (listid.endsWith(".qid")) ? listid.substring(0, listid.length() - 4) : listid;
        IndexCompositionResponse icResponse =
                this.indexCompositionProvider.getIndexComposition(new IndexCompositionRequest(key));
        if (!icResponse.isValid()) {
            this.logger.error("no index composition for " + listid);
            return;
        }
        String group = icResponse.getIndexComposition().getConstituentsGroup();
        if (group != null) {
            String groupSelector = this.constituentsGroupPermissions.getProperty(group);
            if (groupSelector == null) {
                this.logger.error("no group selector for constituentsGroup " + group);
                return;
            }
            selectors.add(groupSelector);
        }
    }

    private Element getRootElement(String query) throws IOException, JDOMException {
        final URL url = new URL(query);
        final InputStream is = url.openStream();
        final Document document = builder.build(is);
        is.close();
        return document.getRootElement();
    }

    static class LanguageAndVariant {
        private final boolean plain;

        final String language;

        final String variant;

        LanguageAndVariant(boolean plain, String language, String variant) {
            this.plain = plain;
            this.language = language;
            this.variant = variant;
        }

        public boolean isPlain() {
            return plain;
        }

        public String getLanguage() {
            return language;
        }

        public String getVariant() {
            return variant;
        }

        public File getFile() {
            final String filename = this.plain ? "dz-structure-definition.xml" : ("dz-structure-definition." + toString() + ".xml");
            return new File(SD_BASE_DIR, filename);
        }

        @Override
        public String toString() {
            return this.language + "." + this.variant;
        }
    }

    public static void main(String[] args) throws Exception {
        final File fileSdTemplate = new File(SD_BASE_DIR, "dz-structure-definition-template.xml");
        final File entitlementsFile = new File(SD_BASE_DIR, "dz-structure-entitlements.txt");
        final LanguageAndVariant lavPlain = new LanguageAndVariant(true, "de", "DE");
        final LanguageAndVariant[] lavs = new LanguageAndVariant[]{
                lavPlain,
                new LanguageAndVariant(false, "de", "BE"),
                new LanguageAndVariant(false, "en", "BE"),
                new LanguageAndVariant(false, "it", "BE"),
                new LanguageAndVariant(false, "de", "CH"),
                new LanguageAndVariant(false, "en", "CH"),
                new LanguageAndVariant(false, "it", "CH"),
                new LanguageAndVariant(false, "de", "DE"),
                new LanguageAndVariant(false, "en", "DE"),
                new LanguageAndVariant(false, "it", "DE"),
                new LanguageAndVariant(false, "de", "FR"),
                new LanguageAndVariant(false, "en", "FR"),
                new LanguageAndVariant(false, "it", "FR"),
                new LanguageAndVariant(false, "it", "IT"),
                new LanguageAndVariant(false, "en", "IT"),
                new LanguageAndVariant(false, "de", "IT"),
                new LanguageAndVariant(false, "de", "NL"),
                new LanguageAndVariant(false, "en", "NL"),
                new LanguageAndVariant(false, "it", "NL")
        };

        final I18nTranslator i18nTranslator = new I18nTranslator();
        for (LanguageAndVariant lav : lavs) {
            i18nTranslator.translate(fileSdTemplate, lav.getFile(), lav.getLanguage(), lav.getVariant());
        }

        final ListOverviewProviderImpl p = new ListOverviewProviderImpl();
        p.setFile(lavPlain.getFile());
        p.setEntitlementsFile(entitlementsFile);
        p.setActiveMonitor(new ActiveMonitor());
        p.afterPropertiesSet();

        IndexCompositionProviderImpl ic = new IndexCompositionProviderImpl();
        ic.setListOverviewProvider(p);
        File icFile = new File(LocalConfigProvider.getProductionDir("var/data/provider/"), "istar-indexcomposition-new.xml.gz");
        ic.setFile(icFile);
        ic.afterPropertiesSet();

        final EntitlementProviderVwd ep = new EntitlementProviderVwd();
        File entDir = LocalConfigProvider.getProductionDir("var/data/vwd/feed");
        final File efgFile = new File(entDir, "EntitlementFieldGroups.txt");
        final File erFile = new File(entDir, "EntitlementRules.XFeed.txt");

        checkFileAge(efgFile);
        checkFileAge(erFile);
        checkFileAge(icFile);

        ep.setEntitlementFieldGroups(efgFile);
        ep.setEntitlementRules(erFile);
        ep.afterPropertiesSet();

        File cgFile = new File(LocalConfigProvider.getProductionDir("var/data/web/"), "constituentsGroupPermissions.txt");
        checkFileAge(cgFile);

        final EntitlementsGenerator g = new EntitlementsGenerator(p, ep, ic);
        g.entitlementsFile = entitlementsFile;
        g.constituentsGroupPermissions = PropertiesLoader.load(cgFile);
        g.afterPropertiesSet();
        g.process(lavs);
    }

    private static void checkFileAge(File file) {
        if ((System.currentTimeMillis() - file.lastModified()) / 1000 > 86400) {
            throw new IllegalStateException(file.getAbsolutePath() + " too old");
        }
    }
}
