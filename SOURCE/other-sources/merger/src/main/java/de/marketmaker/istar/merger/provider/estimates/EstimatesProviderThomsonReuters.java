/*
 * CompanyFundamentalsProviderConvensys.java
 *
 * Created on 09.08.2006 07:57:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.estimates;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
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
public class EstimatesProviderThomsonReuters implements InitializingBean {
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

    private static final int MAXIMUM_YEAR_FORECAST = 2;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private File estimatesFile;

    private final AtomicReference<Map<Long, StockRevenues>> estimates = new AtomicReference<>();

    private final AtomicReference<List<Long>> instrumentIds = new AtomicReference<>();

    public void setEstimatesFile(File estimatesFile) {
        this.estimatesFile = estimatesFile;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void afterPropertiesSet() throws Exception {
        final FileResource estimates = new FileResource(this.estimatesFile);
        estimates.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                readEstimates();
            }
        });
        this.activeMonitor.addResource(estimates);

        readEstimates();
    }

    private void readEstimates() {
        try {
            final EstimatesReader br = new EstimatesReader();
            br.read(this.estimatesFile);

            this.estimates.set(br.getValues());
            this.instrumentIds.set(new ArrayList<>(br.getValues().keySet()));
        } catch (Exception e) {
            this.logger.error("<readEstimatesData> failed", e);
        }
    }

    public List<StockRevenueSummary> getEstimates(long instrumentid) {
        final Map<Long, StockRevenues> map = this.estimates.get();
        final StockRevenues sr = map.get(instrumentid);
        if (sr == null || sr.getElements() == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(sr.getElements());
    }

    public List<Long> getEstimatesDirectory(DateTime refDate) {
        if (null == refDate) {
            return this.instrumentIds.get();
        }
        final Map<Long, StockRevenues> map = this.estimates.get();
        final List<Long> result = new ArrayList<>();
        for (Map.Entry<Long, StockRevenues> entry : map.entrySet()) {
            final DateTime dmlDate = entry.getValue().getDmlDate();
            if (null != dmlDate && !dmlDate.isBefore(refDate)) {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    public static class EstimatesReader extends AbstractSaxReader {
        private final Matcher matcher = Pattern.compile("^(.*)_FY(.)$").matcher("");

        final Map<Long, StockRevenues> values = new HashMap<>();

        private EstimatesFields[] fields;

        private long instrumentid = -1;

        private String isin;

        private String currency;

        private String brokerNames;

        private LocalDate referenceDate;

        private BigDecimal price;

        private BigDecimal priceTarget;

        private BigDecimal recommendation;

        private Integer numBuy;

        private Integer numOverweight;

        private Integer numHold;

        private Integer numUnderweight;

        private Integer numSell;

        private Integer numTotal;

        private DateTime dmlDate;

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if (tagName.equals("ROW")) {
                    // i have finished a new quote => process
                    storeFields();
                    return;
                }

                if (!hasText()) {
                    return;
                }

                if (this.matcher.reset(tagName).matches()) {
                    readField();
                    return;
                }

                if (tagName.equals("IID")) {
                    this.instrumentid = getCurrentLong();
                    this.fields = new EstimatesFields[MAXIMUM_YEAR_FORECAST + 1];
                    for (int i = 0; i < this.fields.length; i++) {
                        this.fields[i] = new EstimatesFields(this.instrumentid, i);
                    }
                }
                else if (tagName.equals("CURRENCY")) {
                    this.currency = getCurrentString();
                }
                else if (tagName.equals("ISIN")) {
                    this.isin = getCurrentString();
                }
                else if (tagName.equals("BROKERNAMES")) {
                    this.brokerNames = getCurrentString();
                }
                else if (tagName.equals("REFERENCEDATE")) {
                    this.referenceDate = getDate().toLocalDate();
                }
                else if (tagName.equals("PRICE")) {
                    this.price = getCurrentBigDecimal();
                }
                else if (tagName.equals("PRICETARGET")) {
                    this.priceTarget = getCurrentBigDecimal();
                }
                else if (tagName.equals("RECOMMENDATION")) {
                    this.recommendation = getCurrentBigDecimal();
                }
                else if (tagName.equals("NUM_BUY")) {
                    this.numBuy = getCurrentInt();
                }
                else if (tagName.equals("NUM_OVERWEIGHT")) {
                    this.numOverweight = getCurrentInt();
                }
                else if (tagName.equals("NUM_HOLD")) {
                    this.numHold = getCurrentInt();
                }
                else if (tagName.equals("NUM_UNDERWEIGHT")) {
                    this.numUnderweight = getCurrentInt();
                }
                else if (tagName.equals("NUM_SELL")) {
                    this.numSell = getCurrentInt();
                }
                else if (tagName.equals("NUM_TOTAL")) {
                    this.numTotal = getCurrentInt();
                }
                else if (tagName.equals("DMLDATE")) {
                    this.dmlDate = getDate();
                }
                else if (tagName.equals("ROWS")) {
                    // ignored
                }
                else {
                    notParsed(tagName);
                }
            } catch (Exception e) {
                this.logger.error("<endElement> error in " + tagName + " for " + getCurrentString(false), e);
                this.errorOccured = true;
            }
        }

        private DateTime getDate() {
            return DTF.parseDateTime(getCurrentString(false));
        }

        private void readField() {
            final String totalFieldname = this.matcher.group(1);

            final int index = Integer.parseInt(this.matcher.group(2));
            if (index > MAXIMUM_YEAR_FORECAST) {
                return;
            }

            final EstimatesFields fields = this.fields[index];

            EstimatesFields.Fieldname fieldname = checkFieldname("_NUMESTIMATES", totalFieldname);
            if (fieldname != null) {
                fields.setNumberOfAnalysts(fieldname, getCurrentInt());
                return;
            }

            fieldname = checkFieldname("_NUMUP", totalFieldname);
            if (fieldname != null) {
                fields.setUpRevisions(fieldname, getCurrentInt());
                return;
            }
            fieldname = checkFieldname("_NUMDOWN", totalFieldname);
            if (fieldname != null) {
                fields.setDownRevisions(fieldname, getCurrentInt());
                return;
            }
            fieldname = checkFieldname("_NUMUNCHANGED", totalFieldname);
            if (fieldname != null) {
                fields.setUnchangedRevisions(fieldname, getCurrentInt());
                return;
            }

            final EstimatesFields.Fieldname f = EstimatesFields.Fieldname.valueOf(totalFieldname);
            if (f == EstimatesFields.Fieldname.FISCALYEAR) {
                if (fields.getFiscalYearEnd() == null) {
                    fields.setFiscalYearEnd(new LocalDate(getCurrentInt(), 12, 31).toDateTimeAtStartOfDay());
                }
            }
            else if (f == EstimatesFields.Fieldname.FISCALYEAR_END) {
                fields.setFiscalYearEnd(getDate());
            }
            else {
                fields.setValue(f, getCurrentBigDecimal());
            }
        }

        private EstimatesFields.Fieldname checkFieldname(String suffix, String tagname) {
            final int index = tagname.indexOf(suffix);
            if (index < 0) {
                return null;
            }
            return EstimatesFields.Fieldname.valueOf(tagname.substring(0, index));
        }

        private void storeFields() {
            this.limiter.ackAction();

            if (this.errorOccured) {
                reset();
                return;
            }

            if (this.instrumentid < 0) {
                reset();
                return;
            }

            final List<StockRevenueSummary> elements = new ArrayList<>();

            for (EstimatesFields field : this.fields) {
                final DateTime fiscalYearEnd = field.getFiscalYearEnd();

                final Interval interval;
                if (fiscalYearEnd == null) {
                    continue;
                }
                DateTime endMidnight = fiscalYearEnd.withTimeAtStartOfDay();
                interval = new Interval(endMidnight.minusYears(1).plusDays(1), endMidnight);

                final List<String> names = new ArrayList<>();
                if (this.brokerNames != null) {
                    final String[] tokens = this.brokerNames.split(",");
                    for (final String token : tokens) {
                        names.add(token.trim());
                    }
                }

                elements.add(field.toStockRevenueSummary(this.currency, interval, fiscalYearEnd.getYear(), this.priceTarget, null, null, null,
                        this.price, this.recommendation, this.numBuy, this.numOverweight, this.numHold,
                        this.numUnderweight, this.numSell, this.numTotal, names, this.referenceDate,
                        this.dmlDate));
            }

            this.values.put(this.instrumentid, new StockRevenues(this.isin, this.dmlDate, elements));

            reset();
        }

        protected void reset() {
            this.instrumentid = -1;
            this.currency = null;
            this.isin = null;
            this.brokerNames = null;
            this.referenceDate = null;
            this.price = null;
            this.priceTarget = null;
            this.recommendation = null;
            this.numBuy = null;
            this.numOverweight = null;
            this.numHold = null;
            this.numUnderweight = null;
            this.numSell = null;
            this.numTotal = null;
            this.fields = null;
            this.errorOccured = false;
        }

        public Map<Long, StockRevenues> getValues() {
            return values;
        }
    }

    public static void main(String[] args) throws Exception {
        final EstimatesProviderThomsonReuters ep = new EstimatesProviderThomsonReuters();
        //ep.setEstimatesFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-factset-estimates-dzbank.xml.gz"));
        ep.setEstimatesFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-tr-estimates-dzbank.xml.gz"));
        ep.setActiveMonitor(new ActiveMonitor());
        ep.afterPropertiesSet();

        for (Long iid : new Long[]{20665L, 98901861L, 34742L}) {
            print(ep.getEstimates(iid));
        }

        final DateTime[] dts = {null, DTF.parseDateTime("27.09.2011"),
                DTF.parseDateTime("25.09.2011"), null,
                DTF.parseDateTime("26.09.2011")};

        for (DateTime dt : dts) {
            final TimeTaker tt = new TimeTaker();
            final List<Long> symbols = ep.getEstimatesDirectory(dt);
            System.out.printf("%d took: %s%n", symbols.size(), tt.toString());
        }
    }

    private static void print(List<StockRevenueSummary> list) {
        System.out.println(list);
        for (StockRevenueSummary summary : list) {
            System.out.println(summary.getReference() + ": " + summary.getEarningPerShare() +
                    ", " + DTF.print(summary.getDmlDate()));
        }
    }

    public static class StockRevenues {
        private final List<StockRevenueSummary> elements;

        private final String isin;

        private final DateTime dmlDate;

        public StockRevenues(String isin, DateTime dmlDate, List<StockRevenueSummary> elements) {
            this.isin = isin;
            this.elements = elements;
            this.dmlDate = dmlDate;
        }

        public String getIsin() {
            return isin;
        }

        public DateTime getDmlDate() {
            return dmlDate;
        }

        public List<StockRevenueSummary> getElements() {
            return elements;
        }
    }
}
