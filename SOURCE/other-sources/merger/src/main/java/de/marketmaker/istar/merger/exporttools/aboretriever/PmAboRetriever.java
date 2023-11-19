package de.marketmaker.istar.merger.exporttools.aboretriever;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.sql.DataSource;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.feed.vwd.EntitlementProviderVwd;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PmAboRetriever implements InitializingBean {
    private final static String ABO_2_FILE =
            "/File=DEUT/AboFiles=D;I;W;F;M;O;V;G;Z;4/CombineMode=Union/SeriesTypes=/SplittFileAtSize=2040109465\n" +
                    "/File=EUREX/AboFiles=T;S;R;X/CombineMode=Union/SeriesTypes=\n" +
                    "/File=EUROPA/AboFiles=E;I;W/CombineMode=Union/SeriesTypes=\n" +
                    "/File=FONDS/AboFiles=O;I;W/CombineMode=Sequential/SeriesTypes=\n" +
                    "/File=FOSTART/AboFiles=0/CombineMode=Union/SeriesTypes=\n" +
                    "/File=LIGHT/AboFiles=L;M;I;W/CombineMode=Sequential/SeriesTypes=\n" +
                    "/File=MKSPEZI/AboFiles=9/CombineMode=Union/SeriesTypes=\n" +
                    "/File=PROFI /AboFiles=PROFI /CombineMode=Union /SeriesTypes=\n" +
                    "/File=SCHWEIZ/AboFiles=C;X;Y;I;W/CombineMode=Union/SeriesTypes=\n" +
                    "/File=STARTER/AboFiles=2;P/CombineMode=Sequential/SeriesTypes=/SplittFileAtSize=2040109465\n" +
                    "/File=TFI/AboFiles=TFI/CombineMode=Union/SeriesTypes=\n" +
                    "/File=UEBERSEE/AboFiles=U;A;J;I;W;3;5/CombineMode=Union/SeriesTypes=\n" +
                    "/File=USISIN /AboFiles=7 /CombineMode=Union /SeriesTypes=\n" +
                    "/File=W/AboFiles=W/CombineMode=Union/SeriesTypes=\n" +
                    "/File=WA /AboFiles=WA /CombineMode=Union /SeriesTypes=\n" +
                    "/File=WB /AboFiles=WB /CombineMode=Union /SeriesTypes=\n" +
                    "/File=WC /AboFiles=WC /CombineMode=Union /SeriesTypes=\n" +
                    "/File=WISO/AboFiles=P/CombineMode=Union/SeriesTypes=\n" +
                    "/File=ZUSISIN /AboFiles=ZI /CombineMode=Union /SeriesTypes= ";

    private static final Matcher ABO_MATCHER = Pattern.compile("^/File=([^/]*)/AboFiles=([^/]*)").matcher("");

    private DataSource dataSource;

    private EntitlementProviderVwd entitlementProvider;

    private Map<String, Set<String>> abo2files;

    private Map<Integer, QuoteData> quoteData;

    private List<Subscription> subscriptions;

    private List<SubscriptionQuote> quotes;

    private File baseDir;

    private Map<String, String> selectorDefinition;

    private final Map<String, Set<Integer>> selector2iids = new HashMap<>();

    private VelocityEngine velocityEngine;

    private String velocityDir;

    private SubscriptionQuoteTool subscriptionQuoteTool;

    private String templatePath = getClass().getPackage().getName().replace('.', '/') + "/";

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setEntitlementProvider(EntitlementProviderVwd entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setVelocityDir(String velocityDir) {
        this.velocityDir = velocityDir;
    }

    public void afterPropertiesSet() throws Exception {
        this.velocityEngine = new VelocityEngine();
        final Properties p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        p.setProperty("input.encoding", "UTF-8");
        p.setProperty("output.encoding", "UTF-8");
        this.velocityEngine.init(p);
    }

    public void process() throws Exception {
        readData();

        this.subscriptionQuoteTool = new SubscriptionQuoteTool(this.subscriptions, this.quotes);

        final VelocityContext context = new VelocityContext();
        context.put("abos", this.abo2files.keySet());
        context.put("date", new LocalDate());
        final PrintWriter writer = new PrintWriter(new FileWriter(new File(this.baseDir, "index.html")));
        getTemplate("abos.vm").merge(context, writer);
        writer.close();

        for (final String abo : this.abo2files.keySet()) {
            process(abo, false);
            process(abo, true);
        }
//        process("EUREX", false);
//        process("EUREX", true);
    }

    private void process(String abo, boolean perFile) throws Exception {
        if (perFile) {
            for (final String file : this.abo2files.get(abo)) {
                final Subscription subscription = getSubscription(file);

                if (subscription == null) {
                    System.out.println("ERROR for " + abo + ", " + file);
                    continue;
                }

                renderFile(abo, file, subscription.getName() + " (" + subscription.getDescription() + ")", Collections.singletonList(subscription));
            }
        }
        else {
            final List<Subscription> subscriptions = new ArrayList<>();
            for (final String file : this.abo2files.get(abo)) {
                final Subscription subscription = getSubscription(file);
                if (subscription != null) {
                    subscriptions.add(subscription);
                }
            }
            renderFile(abo, "all", "ABO: " + abo, subscriptions);
        }

        final VelocityContext context = new VelocityContext();
        context.put("date", new LocalDate());
        context.put("abo", abo);
        context.put("files", this.abo2files.get(abo));
        final PrintWriter writer = new PrintWriter(new FileWriter(new File(this.baseDir, abo + ".html")));
        getTemplate("abo.vm").merge(context, writer);
        writer.close();
        System.out.println();
    }

    private void renderFile(String abo, String file,
            String headline, List<Subscription> subscriptions) throws Exception {
        final VelocityContext context = new VelocityContext();
        context.put("date", new LocalDate());
        context.put("selectorDefinitions", this.selectorDefinition);
        context.put("headline", headline);
        context.put("file", file);
        context.put("abo", abo);
        context.put("subscriptionQuoteTool", this.subscriptionQuoteTool);
        process(subscriptions, context);
        final PrintWriter writer = new PrintWriter(new FileWriter(new File(this.baseDir, abo + "-" + file + ".html")));
        getTemplate("file.vm").merge(context, writer);
        writer.close();
    }

    private Template getTemplate(final String name) {
        return this.velocityEngine.getTemplate(templatePath + name);
    }


    private void process(List<Subscription> subscription, VelocityContext context) {
        final List<SubscriptionQuote> quotes = getQuotes(subscription);
        final List<SubscriptionQuote> quotesWithoutSelector = new ArrayList<>();
        final TreeSet<Integer> allEntitlements = new TreeSet<>();
        final TreeSet<String> allSelectors = new TreeSet<>();

        for (final SubscriptionQuote sq : quotes) {
            final int[] entitlements = sq.getQuote().getEntitlements();
            if (entitlements == null) {
                quotesWithoutSelector.add(sq);
            }
            else {
                for (int entitlement : entitlements) {
                    allEntitlements.add(entitlement);
                }
            }
        }

        for (final Integer entitlement : allEntitlements) {
            allSelectors.add(EntitlementsVwd.toEntitlement(entitlement));
        }

        context.put("quotes", quotes);
        context.put("allSelectors", allSelectors);
        context.put("quotesWithoutSelector", quotesWithoutSelector);

        final Map<String, Object> quotesBySelector = new HashMap<>();
        final Map<String, Object> missingIidsBySelector = new HashMap<>();
        for (final String selector : allSelectors) {
            final int entitlement = EntitlementsVwd.toValue(selector);

            final List<SubscriptionQuote> quotesForSelector = new ArrayList<>();
            final Set<Integer> iidsForSelectorInSubscription = new HashSet<>();

            for (final SubscriptionQuote sq : quotes) {
                final int[] entitlements = sq.getQuote().getEntitlements();
                if (entitlements == null || Arrays.binarySearch(entitlements, entitlement) < 0) {
                    continue;
                }
                quotesForSelector.add(sq);
                iidsForSelectorInSubscription.add(sq.getQuote().getIid());
            }

            if (isToBeIgnored(selector)) {
                continue;
            }

            if (!this.selector2iids.containsKey(selector)) {
                throw new IllegalStateException("what?");
            }

            final Set<Integer> missingIids = new HashSet<>(this.selector2iids.get(selector));
            missingIids.removeAll(iidsForSelectorInSubscription);
            final Map<Integer, QuoteData> sampleQuotes = new HashMap<>();
            for (final QuoteData data : this.quoteData.values()) {
                if (!missingIids.contains(data.getIid())) {
                    continue;
                }
                if (sampleQuotes.containsKey(data.getIid())) {
                    continue;
                }
                sampleQuotes.put(data.getIid(), data);
            }
            missingIidsBySelector.put(selector, new ArrayList<>(sampleQuotes.values()));

            quotesBySelector.put(selector, quotesForSelector);
        }
        context.put("quotesBySelector", quotesBySelector);
        context.put("missingIidsBySelector", missingIidsBySelector);
    }

    private boolean isToBeIgnored(String selector) {
        try {
            final int numSel = Integer.parseInt(selector);
            if (numSel >= 3032 && numSel <= 3437) {
                return true;
            }
        }
        catch (NumberFormatException ignore) {
            // ignore
        }
        return false;
    }

    private int[] getEntitlements(String vwdsymbol) {
        if (vwdsymbol == null) {
            return null;
        }
        final int[] entitlements = this.entitlementProvider.getEntitlements(vwdsymbol);
        Arrays.sort(entitlements);
        return entitlements;
    }

    private List<SubscriptionQuote> getQuotes(List<Subscription> subscriptions) {
        final Set<SubscriptionQuote> result = new HashSet<>();

        for (final Subscription subscription : subscriptions) {
            for (final SubscriptionQuote quote : this.quotes) {
                if (subscription.getId() == quote.getId()) {
                    result.add(quote);
                }
            }
        }

        return new ArrayList<>(result);
    }

    private Subscription getSubscription(String file) {
        for (final Subscription subscription : this.subscriptions) {
            if (("MM " + file + "-Datei").equals(subscription.getName())) {
                return subscription;
            }
        }
        return null;
    }

    private void readData() throws Exception {
        this.abo2files = new HashMap<>();
        final Scanner scanner = new Scanner(ABO_2_FILE);
        while (scanner.hasNextLine()) {
            final Matcher matcher = ABO_MATCHER.reset(scanner.nextLine());
            if (matcher.find()) {
                final String abo = matcher.group(1).trim();
                @SuppressWarnings({"unchecked"})
                final Set<String> files = StringUtils.commaDelimitedListToSet(matcher.group(2).trim().replaceAll(";", ","));

                this.abo2files.put(abo, files);
            }
        }
        scanner.close();
        System.out.println(this.abo2files);

        this.subscriptions = readSubscriptions();
        System.out.println(this.subscriptions);

        this.quoteData = readQuoteDataFromFile();

        this.quotes = readSubscriptionQuotesFromFile();
        System.out.println(this.quotes.size());

        this.selectorDefinition = new LinkedHashMap<>();
        final URL url = new URL("file:///home/meadm/produktion/prog/tools-pmabo/conf/selectors.xml");
//        final URL url = new URL("http://vwd-ent:1968/vwdPermissions.asmx/SelectorDefinitions");
        final URLConnection conn = url.openConnection();
        final SAXBuilder builder = new SAXBuilder();
        final InputStreamReader sr = new InputStreamReader(conn.getInputStream());
        final Element root = builder.build(sr).getRootElement();
        sr.close();
        @SuppressWarnings({"unchecked"})
        final List<Element> elements = (List<Element>) root.getChildren("Sel");
        for (final Element element : elements) {
            final String selector = element.getAttributeValue("selector");

            if (isToBeIgnored(selector)) {
                continue;
            }

            final String description = element.getAttributeValue("description");
            this.selectorDefinition.put(selector.startsWith("0") ? selector.substring(1) : selector, description);
        }
        System.out.println(this.selectorDefinition);
        System.out.println(this.selectorDefinition.size());
    }

    private List<Subscription> readSubscriptions() {
        final JdbcTemplate jt = new JdbcTemplate(this.dataSource);
        jt.afterPropertiesSet();
        //noinspection unchecked
        return (List<Subscription>) jt.query("select * from subscription where subscriptiongroup=1",
                new ResultSetExtractor() {
                    public Object extractData(
                            ResultSet rs) throws SQLException, DataAccessException {
                        final List<Subscription> result = new ArrayList<>();
                        while (rs.next()) {
                            result.add(new Subscription(rs.getShort("subscriptionid"), rs.getString("id"), rs.getString("description")));
                        }
                        return result;
                    }
                });
    }

    private List<SubscriptionQuote> readSubscriptionQuotesFromFile() throws Exception {
        final List<SubscriptionQuote> result = new ArrayList<>();

        final Scanner scanner = new Scanner(new GZIPInputStream(new FileInputStream(new File(baseDir, "subscription-quotes.txt.gz"))));
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();

            final String[] tokens = line.split(",");
            final short id = Short.parseShort(tokens[0]);
            final int qid = Integer.parseInt(tokens[1]);

            final QuoteData data = this.quoteData.get(qid);
            if (data == null) {
                System.out.println(line);
                continue;
            }
            result.add(new SubscriptionQuote(id, data));
        }
        scanner.close();

        return result;
    }

    private Map<Integer, QuoteData> readQuoteDataFromFile() throws Exception {
        final Map<Integer, QuoteData> result = new HashMap<>();

        final Scanner scanner = new Scanner(new GZIPInputStream(new FileInputStream(new File(baseDir, "iid-qid-vwdsymbol.txt.gz"))));
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();

            final QuoteData qd = new QuoteData(line);
            result.put(qd.getQid(), qd);

            final int[] entitlements = getEntitlements(qd.getVwdsymbol());
            if (entitlements == null || entitlements.length == 0) {
//                System.out.println("Fehler: kein Entitlement für " + line);
                continue;
            }

            qd.setEntitlements(entitlements);

            for (final int entitlement : entitlements) {
                final String selector = EntitlementsVwd.toEntitlement(entitlement);
                Set<Integer> iids = this.selector2iids.get(selector);
                if (iids == null) {
                    iids = new HashSet<>();
                    this.selector2iids.put(selector, iids);
                }
                iids.add(qd.getIid());
            }
        }
        scanner.close();

        QuoteData.resetNames();

        return result;
    }

    public static void main(String[] args) throws Exception {
        final FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(args[0]);
        final PmAboRetriever generator = (PmAboRetriever) context.getBean("pmAboRetriever");
        generator.process();
        context.destroy();
    }
}

