/*
 * EstimatesProviderFactset.java
 *
 * Created on 09.08.2006 07:57:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.estimates;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.marketmaker.istar.domain.profile.Profile;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.data.StockRevenueSummary;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EstimatesProviderFactsetImpl implements InitializingBean, EstimatesProviderFactset {
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

    private static final int MAXIMUM_YEAR_FORECAST = 4;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private File estimatesFile;

    private volatile Map<Long, StockRevenues> estimates = Collections.emptyMap();

    public void setEstimatesFile(File estimatesFile) {
        this.estimatesFile = estimatesFile;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void afterPropertiesSet() throws Exception {
        final FileResource estimates = new FileResource(this.estimatesFile);
        estimates.addPropertyChangeListener(evt -> readEstimates(false));
        this.activeMonitor.addResource(estimates);

        readEstimates(true);
    }

    private void readEstimates(boolean throwException) {
        this.logger.info("<readEstimates> from '" + this.estimatesFile.getName() + "' ...");
        try {
            final EstimatesReader br = new EstimatesReader();
            br.read(this.estimatesFile);
            this.estimates = br.getValues();
            this.logger.info("<readEstimates> succeeded");
            if (!br.unknownFieldnames.isEmpty()) {
                this.logger.warn("<readEstimates> found unknown fieldnames: "
                        + new TreeMap<>(br.unknownFieldnames));
            }
        } catch (Exception e) {
            if (throwException) {
                throw new RuntimeException(e);
            }
            this.logger.error("<readEstimates> failed", e);
        }
    }


    @Override
    public List<StockRevenueSummary> getEstimates(Profile profile, long instrumentid) {
        final Map<Long, StockRevenues> map = this.estimates;
        final StockRevenues sr = map.get(instrumentid);
        if (sr == null || sr.getElements() == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(sr.getElements());
    }

    @Override
    public List<Long> getEstimatesDirectory(Profile profile, DateTime refDate) {
        final Map<Long, StockRevenues> map = this.estimates;
        final List<Long> result = new ArrayList<>();
        for (Map.Entry<Long, StockRevenues> entry : map.entrySet()) {
            final StockRevenues sr = entry.getValue();
            if (null == refDate) {
                result.add(entry.getKey());
            }
            else {
                final DateTime dmlDate = sr.getDmlDate();
                if (null != dmlDate && !dmlDate.isBefore(refDate)) {
                    result.add(entry.getKey());
                }
            }
        }

        return result;
    }

    public class EstimatesReader extends AbstractSaxReader {
        private final Matcher matcher = Pattern.compile("^A_(.*)_FY(.)$").matcher("");

        final Map<Long, StockRevenues> values = new HashMap<>();

        final Map<String, MutableInt> unknownFieldnames = new HashMap<>();

        private EstimatesFields[] fields;

        private long instrumentid = -1;

        private String currency;

        private String brokerNames;

        private BigDecimal priceTarget;

        private BigDecimal longTermGrowth;

        private Integer numPriceTarget;

        private Integer numLongTermGrowth;

        private Integer fiscalYear;

        private BigDecimal recommendation;

        private Integer numBuy;

        private Integer numOverweight;

        private Integer numHold;

        private Integer numUnderweight;

        private Integer numSell;

        private Integer numTotal;

        private DateTime dmlDate;

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            if (tagName.equals("ROW")) {
                // i have finished a new quote => process
                storeFields();
                reset();
                return;
            }

            if (!hasText()) {
                return;
            }

            if (tagName.equals("IID")) {
                this.instrumentid = getCurrentLong();
                this.fields = new EstimatesFields[MAXIMUM_YEAR_FORECAST + 1];
                for (int i = 0; i < this.fields.length; i++) {
                    this.fields[i] = new EstimatesFields(this.instrumentid, i);
                }
                return;
            }
            else if (tagName.equals("CURRENCY")) {
                this.currency = getCurrentString();
                return;
            }
            else if (tagName.equals("BROKER_NAMES")) {
                this.brokerNames = getCurrentString();
            }
            else if (tagName.equals("A_FISCALYEAR")) {
                this.fiscalYear = getCurrentInt();
                return;
            }
            else if (tagName.equals("A_TARGET_PRICE")) {
                this.priceTarget = getCurrentBigDecimal();
                return;
            }
            else if (tagName.equals("A_LONG_TERM_GROWTH")) {
                this.longTermGrowth = getCurrentBigDecimal();
                return;
            }
            else if (tagName.equals("A_NUM_EST_TARGET_PRICE")) {
                this.numPriceTarget = getCurrentInt();
                return;
            }
            else if (tagName.equals("A_NUM__EST__LONG_TERM_GROWTH")) {
                this.numLongTermGrowth = getCurrentInt();
                return;
            }
            else if (tagName.equals("A_RC_CONSENSUS")) {
                this.recommendation = getCurrentBigDecimal();
            }
            else if (tagName.equals("A_RC_BUY")) {
                this.numBuy = getCurrentInt();
            }
            else if (tagName.equals("A_RC_OVERWEIGHT")) {
                this.numOverweight = getCurrentInt();
            }
            else if (tagName.equals("A_RC_HOLD")) {
                this.numHold = getCurrentInt();
            }
            else if (tagName.equals("A_RC_UNDERWEIGHT")) {
                this.numUnderweight = getCurrentInt();
            }
            else if (tagName.equals("A_RC_SELL")) {
                this.numSell = getCurrentInt();
            }
            else if (tagName.equals("A_RC_TOTAL")) {
                this.numTotal = getCurrentInt();
            }
            else if (tagName.equals("DMLDATE")) {
                this.dmlDate = getDate();
                return;
            }

            if (this.fields == null) {
                return;
            }

            if (!this.matcher.reset(tagName).matches()) {
                return;
            }

            final int index = Integer.parseInt(this.matcher.group(2));
            if (index > MAXIMUM_YEAR_FORECAST) {
                return;
            }

            String totalFieldname = this.matcher.group(1);

            final EstimatesFields fields = this.fields[index];

            EstimatesFields.Fieldname fieldname = checkFieldname("NE_", totalFieldname);
            if (fieldname != null) {
                fields.setNumberOfAnalysts(fieldname, getCurrentInt());
                return;
            }

            fieldname = checkFieldname("NR_UPWARD_", totalFieldname);
            if (fieldname != null) {
                fields.setUpRevisions(fieldname, getCurrentInt());
                return;
            }
            fieldname = checkFieldname("NR_DOWNWARD_", totalFieldname);
            if (fieldname != null) {
                fields.setDownRevisions(fieldname, getCurrentInt());
                return;
            }
            fieldname = checkFieldname("NR_DOWN_", totalFieldname);
            if (fieldname != null) {
                fields.setDownRevisions(fieldname, getCurrentInt());
                return;
            }
            fieldname = checkFieldname("NR_UNCHANGED_", totalFieldname);
            if (fieldname != null) {
                fields.setUnchangedRevisions(fieldname, getCurrentInt());
                return;
            }
            fieldname = checkFieldname("NR_UNCH_", totalFieldname);
            if (fieldname != null) {
                fields.setUnchangedRevisions(fieldname, getCurrentInt());
                return;
            }
            fieldname = asFieldnameEnum(totalFieldname);
            if (fieldname != null) {
                fields.setValue(fieldname, getCurrentBigDecimal());
                return;
            }

            notParsed(tagName);
        }

        protected EstimatesFields.Fieldname asFieldnameEnum(String name) {
            try {
                return EstimatesFields.Fieldname.parse(name);
            } catch (IllegalArgumentException e) {
                this.unknownFieldnames.computeIfAbsent(name, s -> new MutableInt()).increment();
                return null;
            }
        }

        private DateTime getDate() {
            return DTF.parseDateTime(getCurrentString(false));
        }

        private EstimatesFields.Fieldname checkFieldname(String prefix, String tagname) {
            if (!tagname.startsWith(prefix)) {
                return null;
            }
            return asFieldnameEnum(tagname.substring(prefix.length()));
        }

        private void storeFields() {
            this.limiter.ackAction();

            if (this.instrumentid < 0 || this.fiscalYear == null) {
                return;
            }

            final StockRevenues existing = this.values.get(this.instrumentid);
            if (existing != null && existing.getFiscalYear() > this.fiscalYear) {
                return;
            }

            final List<StockRevenueSummary> elements = new ArrayList<>();

            for (EstimatesFields field : this.fields) {
                final DateTime start = new DateTime(this.fiscalYear + field.getIncrement() - 1, 1, 1, 0, 0, 0, 0);
                final DateTime end = start.plusYears(1).minusDays(1);
                final Interval interval = new Interval(start, end);

                final List<String> names = new ArrayList<>();
                if (this.brokerNames != null) {
                    final String[] tokens = this.brokerNames.split(",");
                    for (final String token : tokens) {
                        names.add(token.trim());
                    }
                }

                elements.add(field.toStockRevenueSummary(this.currency, interval, this.fiscalYear,
                        this.priceTarget, this.numPriceTarget, this.longTermGrowth, this.numLongTermGrowth,
                        null, this.recommendation, this.numBuy, this.numOverweight, this.numHold,
                        this.numUnderweight, this.numSell, this.numTotal, names,
                        this.dmlDate == null ? null : this.dmlDate.toLocalDate(), this.dmlDate));

            }

            this.values.put(this.instrumentid, new StockRevenues(this.fiscalYear, elements, this.dmlDate));
        }


        protected void reset() {
            this.instrumentid = -1;
            this.currency = null;
            this.brokerNames = null;
            this.fiscalYear = null;
            this.numLongTermGrowth = null;
            this.numPriceTarget = null;
            this.priceTarget = null;
            this.longTermGrowth = null;
            this.currency = null;
            this.recommendation = null;
            this.numBuy = null;
            this.numOverweight = null;
            this.numHold = null;
            this.numUnderweight = null;
            this.numSell = null;
            this.numTotal = null;
            this.fields = null;
        }

        public Map<Long, StockRevenues> getValues() {
            return values;
        }
    }

    public static void main(String[] args) throws Exception {
        final EstimatesProviderFactsetImpl ep = new EstimatesProviderFactsetImpl();
        ep.setEstimatesFile(new File(LocalConfigProvider.getProductionBaseDir(),
                "var/data/provider/istar-factset-estimates_extd.xml.gz"));
        ep.setActiveMonitor(new ActiveMonitor());
        ep.afterPropertiesSet();

        for (Long iid : new Long[]{20665L}) {
            System.out.println("iid: " + iid);
            final List<StockRevenueSummary> list = ep.getEstimates(null, iid);
            for (StockRevenueSummary summary : list) {
                System.out.printf("%s: EPS: %s DPS: %s DMLDATE: %s%n",
                        summary.getReference(),
                        (null == summary.getEarningPerShare()) ? "null" : summary.getEarningPerShare().getValue(),
                        (null == summary.getDividend()) ? "null" : summary.getDividend().getValue(),
                        DTF.print(summary.getDmlDate()));
            }
            System.out.println();
        }

        final Map<Long, StockRevenues> map = ep.estimates;
        System.out.println(map.size() + " entries");
        final SortedSet<DateTime> dtSet = map.values().stream().map(StockRevenues::getDmlDate).collect(Collectors.toCollection(TreeSet::new));

        System.out.println(dtSet.size() + " different dml dates with last: " + DTF.print(dtSet.last()));

        final DateTime[] dts = {null, DTF.parseDateTime("27.09.2011"),
                DTF.parseDateTime("25.09.2011"), null,
                DTF.parseDateTime("26.09.2011")};

        for (DateTime dt : dts) {
            final TimeTaker tt = new TimeTaker();
            final List<Long> symbols = ep.getEstimatesDirectory(null, dt);
            System.out.printf("%d took: %s%n", symbols.size(), tt.toString());
        }

        System.out.println("###################");
        final List<StockRevenueSummary> list = ep.getEstimates(null, 20665L);
        for (final StockRevenueSummary srs : list) {
            System.out.println(srs.getDmlDate());
            System.out.println(srs.getFiscalYear());
            System.out.println(srs.getReference());
            System.out.println(srs.getEarningPerShare());
            System.out.println(srs.getDividendYield());
            System.out.println(srs.getPriceEarningRatio());
            System.out.println(srs.getPostEventConsensus());
            System.out.println(srs.getDividend());
            System.out.println(srs.getFreeCashFlow().getValue());
            System.out.println(srs.getReturnOnEquity().getValue());
            System.out.println(srs.getReturnOnInvestedCapital().getValue());
            System.out.println();
        }
    }

    private static class StockRevenues {
        private final int fiscalYear;

        private final List<StockRevenueSummary> elements;

        private final DateTime dmlDate;

        public StockRevenues(int fiscalYear, List<StockRevenueSummary> elements, DateTime dmlDate) {
            this.fiscalYear = fiscalYear;
            this.elements = elements;
            this.dmlDate = dmlDate;
        }

        public List<StockRevenueSummary> getElements() {
            return elements;
        }

        public DateTime getDmlDate() {
            return dmlDate;
        }

        public int getFiscalYear() {
            return fiscalYear;
        }
    }
}
