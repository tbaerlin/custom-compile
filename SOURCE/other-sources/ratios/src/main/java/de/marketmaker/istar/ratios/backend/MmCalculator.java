/*
 * EmbeddedCalculator.java
 *
 * Created on 19.10.2005 08:02:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import static de.marketmaker.istar.ratios.RatioFieldDescription.referenceTimestamp;

import de.marketmaker.istar.common.mm.MMKeyType;
import de.marketmaker.istar.common.mm.MMService;
import de.marketmaker.istar.common.mm.MMServiceResponse;
import de.marketmaker.istar.common.mm.MMTalkException;
import de.marketmaker.istar.common.mm.MMTalkTableRequest;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.common.util.PriceFormatter;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class MmCalculator implements InitializingBean, DisposableBean, BeanNameAware, Calculator {
    private final static Double NOT_AVAILABLE = Double.valueOf("1.0E+300");

    private static final int[] DATE_FIELD_IDS = new int[]{
            VwdFieldDescription.MMF_Bezahlt_Datum.id(),
            VwdFieldDescription.ADF_Handelsdatum.id(),
            VwdFieldDescription.ADF_Schluss_Vortagesdatum.id(),
            VwdFieldDescription.ADF_DATEOFARR.id()
    };

    private static final int NULL_HANDLE = -1;

    private static final int UNKNOWN_HANDLE = Integer.MIN_VALUE;

    private static final double MAX_DATE = DateUtil.javaDateToComDate(new DateTime(2199, 12, 31, 23, 59).toDate());

    private static final long EURONEXT_DELETED_BIDASK = 999999000L;

    private static final long XETF_DELETED_BIDASK = 99999999000L;

    private static final Set<String> IGNORE_KO_MARKETS = new HashSet<>(Arrays.asList("WIEN", "AU"));

    // formula that returns a security's handle as a Double
    private static final String ID = "id";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RatiosEncoder encoder = new RatiosEncoder();

    private File formulaFile;

    private Map<InstrumentTypeEnum, FormulasPerType> formulasPerType = new HashMap<>();

    private MMService mmService;

    private int mmPort = 8570;

    private final Int2IntMap handleCache = new Int2IntOpenHashMap();

    private StaticDataProvider staticDataProvider;

    private CalcDataPartitioner partitioner;

    private volatile long lastCalcAt;

    private String name;

    public MmCalculator() {
        handleCache.defaultReturnValue(NULL_HANDLE);
    }

    public void setPartitioner(CalcDataPartitioner partitioner) {
        this.partitioner = partitioner;
    }

    public void setMmNative(MMService mmService) {
        this.mmService = mmService;
    }

    public void setStaticDataProvider(StaticDataProvider staticDataProvider) {
        this.staticDataProvider = staticDataProvider;
    }

    public void setFormulaFile(File formulaFile) {
        this.formulaFile = formulaFile;
    }

    public void setMmPort(int mmPort) {
        this.mmPort = mmPort;
    }

    @Override
    public void setBeanName(String s) {
        this.name = s;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public void destroy() throws Exception {
        this.logger.info("<destroy> " + this + ", #handles=" + numHandles());
    }

    private int numHandles() {
        synchronized (this.handleCache) {
            return this.handleCache.size();
        }
    }

    @ManagedOperation
    @Scheduled(cron = "0 0 7 * * *")
    public void clearHandles() {
        synchronized (this.handleCache) {
            this.logger.info("<clearHandles> " + this + " " + this.handleCache.size());
            this.handleCache.clear();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.name == null) {
            this.name = getClass().getSimpleName() + ":" + this.mmPort;
        }

        final SAXBuilder saxBuilder = new SAXBuilder();
        final Document document = saxBuilder.build(this.formulaFile);
        final Element element = document.getRootElement();

        final Map<Integer, Formula> formulaById = new HashMap<>();

        //noinspection unchecked
        final List<Element> formulas = element.getChild("formulas").getChildren();
        for (final Element formula : formulas) {
            final String fieldname = formula.getAttributeValue("name");
            final RatioFieldDescription.Field field = RatioFieldDescription.getFieldByName(fieldname);

            if (field == null) {
                throw new IllegalArgumentException("unknown field: " + fieldname);
            }
            final boolean scaleResult = "true".equals(formula.getAttributeValue("scaleResult"));

            String mindaysAttribute = formula.getAttributeValue("mindays");
            final int mindays = StringUtils.hasText(mindaysAttribute)
                    ? Integer.parseInt(mindaysAttribute)
                    : 0;
            final String formulaStr = formula.getTextTrim();

            String minPriceAttribute = formula.getAttributeValue("minPrice");
            final BigDecimal minPrice = StringUtils.hasText(minPriceAttribute)
                    ? new BigDecimal(minPriceAttribute)
                    : BigDecimal.ZERO;

            formulaById.put(field.id(), new Formula(field.id(), mindays, scaleResult, formulaStr, minPrice));
        }

        boolean nonValidFormula = false;

        //noinspection unchecked
        final List<Element> types = element.getChild("formulasPerType").getChildren();
        for (final Element type : types) {
            final InstrumentTypeEnum instrumenttype = InstrumentTypeEnum.valueOf(type.getAttributeValue("id"));
            final List<Formula> formulaPerType = new ArrayList<>();

            //noinspection unchecked
            final List<Element> formulaList = type.getChildren();
            for (final Element e : formulaList) {
                final RatioFieldDescription.Field field = RatioFieldDescription.getFieldByName(e.getTextTrim());
                if (field == null) {
                    this.logger.error("<initialize> unknown field '" + e.getTextTrim() + "'");
                    continue;
                }
                formulaPerType.add(formulaById.get(field.id()));

                if (!field.isApplicableFor(instrumenttype)) {
                    nonValidFormula = true;
                    this.logger.error("<initialize> " + field.name() + " is not applicable for " + instrumenttype);
                }
            }

            this.formulasPerType.put(instrumenttype, new FormulasPerType(instrumenttype, formulaPerType));
        }

        if (nonValidFormula) {
            throw new IllegalArgumentException("non-applicable field, see log");
        }

        this.logger.info("<initialize> this.formulasPerType = " + this.formulasPerType);
    }

    public void calc(List<CalcData> toCalc, ComputedRatiosHandler handler) {
        this.lastCalcAt = System.currentTimeMillis();
        try {
            if (this.mmPort != 0) {
                pushDataToMm(toCalc);
            }

            toCalc.sort(Comparator.comparing(CalcData::getInstrumentType));

            int i = 0;
            for (int j = 1; j < toCalc.size(); j++) {
                if (toCalc.get(i).getInstrumentType() != toCalc.get(j).getInstrumentType()) {
                    calcType(toCalc.subList(i, j), handler);
                    i = j;
                }
            }
            calcType(toCalc.subList(i, toCalc.size()), handler);
        } catch (Exception e) {
            this.logger.warn("<calc> failed to get calc from pm", e);
        }
    }

    public long unusedSinceMillis() {
        return System.currentTimeMillis() - this.lastCalcAt;
    }

    private void calcType(List<CalcData> cds, ComputedRatiosHandler handler) throws Exception {
        if (cds.isEmpty()) {
            return;
        }

        final InstrumentTypeEnum type = cds.get(0).getInstrumentType();
        if (CalcController.WITHBENCHMARK_NO_FUNDS.contains(type) || CalcController.WITHBENCHMARK_FUNDS.contains(type)) {
            final Map<Quote, List<CalcData>> cdsByBenchmark = new HashMap<>();
            final List<CalcData> noBenchmark = new ArrayList<>();
            for (final CalcData cd : cds) {
                final Quote benchmark = cd.getReferenceQuote();
                if (benchmark == null || benchmark.getSymbolMmwkn() == null) {
                    noBenchmark.add(cd);
                    continue;
                }

                List<CalcData> tmp = cdsByBenchmark.get(benchmark);
                if (tmp == null) {
                    tmp = new ArrayList<>();
                    cdsByBenchmark.put(benchmark, tmp);
                }
                tmp.add(cd);
            }

            this.logger.debug("<calcType> " + type.name() + ", no benchmark: #" + noBenchmark.size());
            calcType(noBenchmark, null, handler);
            for (final Map.Entry<Quote, List<CalcData>> entry : cdsByBenchmark.entrySet()) {
                this.logger.debug("<calcType> " + type.name() + ", benchmark '" + entry.getKey().getInstrument().getName() + ": #" + entry.getValue().size());
                calcType(entry.getValue(), entry.getKey().getSymbolMmwkn(), handler);
            }
        }
//        else if (type == InstrumentTypeEnum.BND) {
//            this.logger.info("<calcType> " + type.name() + ": #" + cds.size());
//            calcType(cds, "W00KL14", handler);  // continuous Bund Future
//        }
        else {
            this.logger.debug("<calcType> " + type.name() + ": #" + cds.size());
            calcType(cds, null, handler);
        }
    }

    private void calcType(List<CalcData> cds, String contextMmwkn,
            ComputedRatiosHandler handler) throws Exception {
        if (cds.isEmpty()) {
            return;
        }

        InstrumentTypeEnum type = cds.get(0).getInstrumentType();
        final FormulasPerType formulas = this.formulasPerType.get(type);

        if (formulas == null) {
            return;
        }

        final int numFormulas = formulas.getFormulas().length;

        // same instrument type, w/o benchmark, separate processing for different market
        // for MSCI, high/low replaced by close
        // partition strategy with partition enum, adapt formulas using partition, make mm talks
        // with each partition and its formulas
        Map<QuoteCategory, List<CalcData>> partitions = this.partitioner.partition(cds);

        for (Map.Entry<QuoteCategory, List<CalcData>> partition : partitions.entrySet()) {
            final List<CalcData> validCalcDatas = getHandles(partition.getValue());
            final int[] validHandles = new int[validCalcDatas.size()];

            for (int i = 0; i < validHandles.length; i++) {
                final CalcData calcData = validCalcDatas.get(i);
                validHandles[i] = getHandleFromCache((int) calcData.getQid());
            }

            final String[] configuredFormulas = formulas.getMmFormulas(contextMmwkn);
            final String[] effectiveFormulas = FormulaAdaptor.adapt(partition.getKey(), configuredFormulas);
            assert ID.equals(effectiveFormulas[numFormulas]) : Arrays.toString(effectiveFormulas);

            final long referenceTimestampValue = computeReferenceTimestamp();

            MMTalkTableRequest req = new MMTalkTableRequest(MMKeyType.SECURITY)
                    .withKeys(asListOfStrings(validHandles))
                    .withContextHandle(contextMmwkn)
                    .withFormulas(Arrays.asList(effectiveFormulas));

            MMServiceResponse response = letPmCalc(req);
            final Object[] values = response.getData();

            for (int i = 0; i < validHandles.length; i++) {
                final CalcData calcData = validCalcDatas.get(i);

                Object id = values[numFormulas * validHandles.length + i];
                if (id == null) {
                    this.logger.warn("<calcType> handle " + validHandles[i]
                            + " for " + calcData.getQid() + ".qid is no longer valid");
                    removeHandleFromCache((int) calcData.getQid());
                    continue;
                }

                this.encoder.reset(type, calcData.getIid(), calcData.getQid());
                this.encoder.add(referenceTimestamp, referenceTimestampValue);

                //this.encoder.add(RatioFieldDescription.referencePrice.id(), (long) (doublevalue * Constants.SCALE_FOR_DECIMAL));

                final int first = calcData.getQuote().getFirstHistoricPriceYyyymmdd();
                final int daysAvailable = first > 0 ? DateUtil.getDaysToToday(first) : 0;

                for (int j = 0; j < numFormulas; j++) {
                    final Formula formula = formulas.getFormulas()[j];

                    if (calcData.isTrace()) {
                        this.logger.info("TRACE <calcTypeMinDays> min " + formula.getMindays() + " avail " + daysAvailable);
                    }

                    if (formula.getMindays() > daysAvailable) {
                        addNullValue(formula);
                        continue;
                    }
                    if (type == InstrumentTypeEnum.FND) {

                        final long fundPrice = InstrumentUtil.isVwdFund(calcData.getQuote())
                                ? getFundPrice(calcData.getSnap())
                                : getLastPrice(calcData.getQuote(), calcData.getSnap());

                        BigDecimal price = PriceCoder.decode(fundPrice);
                        if ((BigDecimal.ZERO.compareTo(price) != 0) && (price.compareTo(formula.getMinPrice()) < 0)) {
                            addNullValue(formula);
                            continue;
                        }
                    }

                    final int valueIndex = j * validHandles.length + i;
                    final Object value = values[valueIndex];

                    if (value instanceof Double) {
                        double doublevalue = (Double) value;

                        switch (formula.getField().type()) {
                            case DATE:
                                final int date = doublevalue > 0 && doublevalue < MAX_DATE
                                        ? DateUtil.dateToYyyyMmDd(DateUtil.comDateToDate(doublevalue))
                                        : Integer.MIN_VALUE;
                                this.encoder.add(formula.getFieldid(), date);
                                break;
                            case DECIMAL:
                                if (formula.isScaleResult()) {
                                    doublevalue *= calcData.getCurrencyFactor();
                                }
                                if (isInvalid(doublevalue)) {
                                    this.encoder.add(formula.getFieldid(), Long.MIN_VALUE);
                                }
                                else {
                                    // using (long)(doublevalue * Constants.SCALE_FOR_DECIMAL)
                                    // would be faster but introduces rounding errors:
                                    // d = 9430.46d; l = (long)(d * 100000); => l = 943045999
                                    // using BigDecimal ensures l = 943046000
                                    try {
                                        this.encoder.add(formula.getFieldid(),
                                                BigDecimal.valueOf(doublevalue).movePointRight(5).longValue());
                                    } catch (NumberFormatException e) {
                                        this.logger.warn("<calcType> failed for decimal " + doublevalue
                                                + ", in field " + formula.getFieldid()
                                                + " for " + calcData.getQuote().getSymbolVwdfeed());
                                        this.encoder.add(formula.getFieldid(), Long.MIN_VALUE);
                                    }
                                }
                                break;
                            case NUMBER:
                                this.encoder.add(formula.getFieldid(), (long) doublevalue);
                                break;
                        }
                    }
                    else {
                        addNullValue(formula);
                    }
                }
                final byte[] encoded = this.encoder.getData();
                if (calcData.isTrace()) {
                    this.logger.info("TRACE <calcType> {}", RatiosDecoder.decode(encoded));
                }
                handler.handle(new ComputedRatios(calcData.getQid(), encoded));
            }
        }
    }

    private MMServiceResponse letPmCalc(MMTalkTableRequest req) throws MMTalkException {
        try {
            return this.mmService.getMMTalkTable(req);
        } catch (Throwable t) {
            this.logger.warn("<letPmCalc> failed: keys: {}, handle: {}, formulas: {}",
                req.getKeys(), req.getContextHandle(), req.getFormulas());
            throw t;
        }
    }

    private void addNullValue(Formula formula) {
        switch (formula.getField().type()) {
            case DATE:
                this.encoder.add(formula.getFieldid(), Integer.MIN_VALUE);
                break;
            case DECIMAL:
            case NUMBER:
                this.encoder.add(formula.getFieldid(), Long.MIN_VALUE);
                break;
        }
    }

    private long computeReferenceTimestamp() {
        DateTime now = new DateTime();
        return DateUtil.toYyyyMmDd(now) * 100000L + now.getSecondOfDay();
    }

    private List<String> asListOfStrings(int[] handles) {
        ArrayList<String> result = new ArrayList<>(handles.length);
        for (int h : handles) {
            result.add(Integer.toString(h));
        }
        return result;
    }

    private boolean isInvalid(double doublevalue) {
        return Double.isInfinite(doublevalue) || Double.isNaN(doublevalue) || doublevalue >= NOT_AVAILABLE;
    }

    private List<CalcData> getHandles(List<CalcData> cds) throws Exception {
        final List<CalcData> validCalcDatas = new ArrayList<>(cds.size());

        for (final CalcData calcData : cds) {
            final String mmwkn = calcData.getQuote().getSymbol(KeysystemEnum.MMWKN);
            if (!StringUtils.hasText(mmwkn)) {
                continue;
            }

            final int handle = getHandle(mmwkn, calcData.getQid());
            if (handle > 0) {
                validCalcDatas.add(calcData);
            }
        }
        return validCalcDatas;
    }

    private int getHandle(String mmwkn, final long qid) throws MMTalkException {
        final int key = (int) qid;
        int handle = getHandleFromCache(key);
        if (handle == NULL_HANDLE) {
            handle = getHandle(mmwkn);
            addHandleToCache(key, handle);
        }
        return handle;
    }

    private int addHandleToCache(int key, int handle) {
        synchronized (this.handleCache) {
            return this.handleCache.put(key, handle);
        }
    }

    private int getHandleFromCache(int key) {
        synchronized (this.handleCache) {
            return this.handleCache.get(key);
        }
    }

    private void removeHandleFromCache(int key) {
        synchronized (this.handleCache) {
            this.handleCache.remove(key);
        }
    }

    private int getHandle(String mmwkn) throws MMTalkException {
        MMServiceResponse r;
        try {
            r = this.mmService.getMMTalkTable(new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                    .withKey(mmwkn).withFormula(ID));
        } catch (MMTalkException e) {
            // has been logged by MmNative
            return UNKNOWN_HANDLE;
        }

        final Object[] data = r.getData();
        if (data[0] instanceof Double) {
            return ((Double) data[0]).intValue();
        }
        return UNKNOWN_HANDLE;
    }

    private void pushDataToMm(List<CalcData> toCalc) throws IOException {
        final Socket socket = new Socket("localhost", this.mmPort);

        final InputStream is = socket.getInputStream();
        final OutputStreamWriter os = new OutputStreamWriter(socket.getOutputStream());

        os.write("PushSnapData ");

        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        final PriceFormatter pf = new PriceFormatter(5, 5);
        pf.setDecimalSeparator(".");
        final StringBuilder stb = new StringBuilder();
        final Set<Long> pushedBenchmarks = new HashSet<>();

        for (final CalcData calcData : toCalc) {
            final boolean trace = calcData.isTrace();
            addPrices(stb, os, calcData.getInstrumentType(),
                    calcData.getQuote(), calcData.getSnap(), sdf, pf, trace);

            if (calcData.getReferenceQuote() != null
                    && !pushedBenchmarks.contains(calcData.getReferenceQuote().getId())) {
                pushedBenchmarks.add(calcData.getReferenceQuote().getId());
                addPrices(stb, os, calcData.getReferenceQuote().getInstrument().getInstrumentType(),
                        calcData.getReferenceQuote(), calcData.getReferenceSnap(), sdf, pf, trace);
            }
        }
        os.write(10);
        os.flush();

        int i;
        while ((i = is.read()) >= 0) {
//            System.out.print((char) i);
            if (i == 'k') {
                break;
            }
        }

        os.write("/");
        os.flush();

        os.close();
        is.close();
        socket.close();
    }

    private void addPrices(StringBuilder stb, OutputStreamWriter os, InstrumentTypeEnum type,
            Quote quote, SnapRecord sr, SimpleDateFormat sdf,
            PriceFormatter pf, boolean trace) throws IOException {
        stb.setLength(0);

        final String mmwkn = quote.getSymbol(KeysystemEnum.MMWKN);
        if (!StringUtils.hasText(mmwkn) || sr == null) {
            return;
        }
        stb.append(mmwkn).append(";");

        final int date = getDate(sr);
        if (date == -1) {
            return;
        }
        try {
            stb.append(sdf.format(DateUtil.yyyyMmDdToDate(date)));
        } catch (org.joda.time.IllegalFieldValueException e) {
            this.logger.warn("<addPrices> invalid date '" + date + " for " + quote.getSymbolVwdfeed()
                    + " found in " + sr);
            return;
        }

        final int currencyFactor = quote.getCurrency().isCent() ? 100 : 1;

        appendPrice(stb, pf, currencyFactor, getPrice(sr, VwdFieldDescription.ADF_Anfang));
        appendPrice(stb, pf, currencyFactor, getPrice(sr, VwdFieldDescription.ADF_Tageshoch));
        appendPrice(stb, pf, currencyFactor, getPrice(sr, VwdFieldDescription.ADF_Tagestief));
        appendPrice(stb, pf, currencyFactor, getLastPrice(quote, sr));

        if (type == InstrumentTypeEnum.FND && InstrumentUtil.isVwdFund(quote)) {
            appendPrice(stb, pf, currencyFactor, getFundPrice(sr));
        }
        if (trace) {
            this.logger.info("TRACE <addPrices> " + stb.toString());
        }

        os.write(stb.toString());
        os.write(13);
    }

    private void appendPrice(StringBuilder stb, PriceFormatter pf, int currencyFactor, long price) {
        stb.append(";");
        if (price != 0 && price != Long.MIN_VALUE) {
            stb.append(pf.formatPrice(price / currencyFactor));
        }
    }

    private int getDate(SnapRecord sr) {
        for (int fid : DATE_FIELD_IDS) {
            final int value = SnapRecordUtils.getInt(sr, fid);
            if (value > 0) {
                return value;
            }
        }
        return -1;
    }

    private long getPrice(SnapRecord sr, VwdFieldDescription.Field field) {
        return SnapRecordUtils.getLong(sr, field.id());
    }

    private long getFundPrice(SnapRecord sr) {
        final long price = SnapRecordUtils.getLong(sr, VwdFieldDescription.ADF_Ruecknahme.id());
        if (price != 0) {
            return price;
        }

        return SnapRecordUtils.getLong(sr, VwdFieldDescription.ADF_Ruecknahme_Vortag.id());
    }

    private long getLastPrice(Quote quote, SnapRecord sr) {
        if ((quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.CER) && isKnockedOut(sr)
                && !IGNORE_KO_MARKETS.contains(quote.getSymbolVwdfeedMarket())) {
            //todo: IGNORE_KO_MARKETS after insertation of "==CER" useful ?
            return 100;
        }

        final long trade = SnapRecordUtils.getLong(sr, VwdFieldDescription.ADF_Bezahlt.id());
        if (trade > 0) {
            return trade;
        }

        if (quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.STK) {
            return 0L;
        }

        final long ask = SnapRecordUtils.getLong(sr, VwdFieldDescription.ADF_Brief.id());
        final long bid = SnapRecordUtils.getLong(sr, VwdFieldDescription.ADF_Geld.id());

        if (isValidBidAsk(ask)) {
            if (isValidBidAsk(bid)) {
                return (ask + bid) / 2;
            }
            return ask;
        }
        if (isValidBidAsk(bid)) {
            return bid;
        }
        return 0L;
    }

    private boolean isValidBidAsk(long value) {
        return value > 0 && value != EURONEXT_DELETED_BIDASK && value != XETF_DELETED_BIDASK;
    }

    private boolean isKnockedOut(SnapRecord sr) {
        final SnapField bid = sr.getField(VwdFieldDescription.ADF_Geld.id());
        final SnapField ask = sr.getField(VwdFieldDescription.ADF_Brief.id());
        final long bidValue = SnapRecordUtils.getLong(bid);
        final long askValue = SnapRecordUtils.getLong(ask);

        // false, if bid or ask is undefined
        // true, if bid very much smaller than ask or ask smaller than bid
        return !(!bid.isDefined() || !ask.isDefined() || bidValue == 0L || askValue == 0L)
                && ((bidValue * 20) < askValue);
    }

    private static class FormulasPerType {
        private final InstrumentTypeEnum type;

        private final String[] mmFormulas;

        private final String[] mmFormulasWithoutBenchmark;

        private final Formula[] formulas;

        public FormulasPerType(InstrumentTypeEnum type, List<Formula> formulas) {
            this.type = type;
            this.formulas = new Formula[formulas.size()];
            this.mmFormulas = new String[formulas.size() + 1];
            this.mmFormulasWithoutBenchmark = new String[formulas.size() + 1];

            int i = 0;
            for (Formula formula : formulas) {
                try {
                    this.formulas[i] = formula;
                    this.mmFormulas[i] = formula.getMmFormula();
                    this.mmFormulasWithoutBenchmark[i]
                            = formula.getMmFormula().contains("$vgl1") ? "na" : formula.getMmFormula();
                    i++;
                } catch (Exception e) {
                    throw new RuntimeException("failed for " + type + ", index " + i);
                }
            }
            // id returns the handle as a Double or null if the input handle was invalid
            this.mmFormulas[i] = ID;
            this.mmFormulasWithoutBenchmark[i] = ID;
        }

        public InstrumentTypeEnum getType() {
            return type;
        }

        public String[] getMmFormulas(String contextWkn) {
            return (contextWkn == null) ? this.mmFormulasWithoutBenchmark : this.mmFormulas;
        }

        public Formula[] getFormulas() {
            return formulas;
        }
    }

    private static class Formula implements Comparable<Formula> {
        private final int fieldid;

        private final int mindays;

        private final boolean scaleResult;

        private final String mmFormula;

        private final RatioFieldDescription.Field field;

        private final BigDecimal minPrice;

        public Formula(int fieldid, int mindays, boolean scaleResult, String formula,
                BigDecimal minPrice) {
            this.fieldid = fieldid;
            this.field = RatioFieldDescription.getFieldById(this.fieldid);
            this.mindays = mindays;
            this.scaleResult = scaleResult;
            this.mmFormula = formula;
            this.minPrice = minPrice;
        }

        public int getFieldid() {
            return fieldid;
        }

        public RatioFieldDescription.Field getField() {
            return field;
        }

        public int getMindays() {
            return mindays;
        }

        public BigDecimal getMinPrice() {
            return minPrice;
        }

        public boolean isScaleResult() {
            return scaleResult;
        }

        public String getMmFormula() {
            return mmFormula;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Formula formula = (Formula) o;

            if (fieldid != formula.fieldid) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return fieldid;
        }

        public int compareTo(Formula o) {
            return field.name().compareTo(o.field.name());
        }
    }

    public static void main(String[] args) throws Exception {
        final MmCalculator calculator = new MmCalculator();
        calculator.setFormulaFile(new File(LocalConfigProvider.getIstarSrcDir(),
                "config/src/main/resources/istar-ratios-backend/conf/mm-formulas.xconf"));
        calculator.afterPropertiesSet();

        final RmiProxyFactoryBean proxy = new RmiProxyFactoryBean();
        proxy.setServiceUrl("rmi://tepm1.market-maker.de:9880/pmserver");
        proxy.setServiceInterface(MMService.class);
        proxy.afterPropertiesSet();

        final MMService service = (MMService) proxy.getObject();

        final Set<Formula> formulaSet = new TreeSet<>();
        for (final Map.Entry<InstrumentTypeEnum, FormulasPerType> entry : calculator.formulasPerType.entrySet()) {
            final FormulasPerType fpt = entry.getValue();
            formulaSet.addAll(Arrays.asList(fpt.getFormulas()));
        }

        final List<Formula> formulas = new ArrayList<>();
        final List<String> formulasDefinition = new ArrayList<>();
        for (final Formula formula : formulaSet) {
            formulas.add(formula);
            formulasDefinition.add(formula.getMmFormula());
        }

        List<String> ef = FormulaAdaptor.adapt(QuoteCategory.FUND_MARKET, formulasDefinition);
        final MMTalkTableRequest request = new MMTalkTableRequest(MMKeyType.SECURITY_WKN);
//        request.withKey("999096").withContextHandle("I846900").withFormulas(ef);
        request.withKey("965264").withContextHandle("I846900").withFormulas(ef);

        final MMServiceResponse response = service.getMMTalkTable(request);
        final Object[] values = response.getData();

        final PriceFormatter pf = new PriceFormatter();
        pf.setDecimalSeparator(".");

        for (int i = 0; i < values.length; i++) {
            final Formula formula = formulas.get(i);

            if (values[i] instanceof Double) {
                final double doublevalue = (Double) values[i];

                switch (formula.getField().type()) {
                    case DATE:
                        System.out.println(formula.getField().name() + ": " + DateUtil.dateToYyyyMmDd(DateUtil.comDateToDate(doublevalue)));
                        break;
                    case DECIMAL:
                    case NUMBER:
                        System.out.println(formula.getField().name() + ": " + doublevalue);
                        break;
                }
            }
            else {
                System.out.println(formula.getField().name() + ": " + values[i] + "/ERROR");
            }
        }
    }
}
