/*
 * Query2Term.java
 *
 * Created on 15.03.13 09:29
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.protobuf.Descriptors;
import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domainimpl.data.PriceImpl;
import de.marketmaker.istar.domainimpl.data.PriceRecordImpl;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.feed.vwd.PriceRecordVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.provider.history.eod.EodPriceHistoryResponse;
import de.marketmaker.istar.merger.provider.protobuf.EodHistoryProtos;

import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.qidSymbol;

/**
 * @author zzhao
 */
@ManagedResource
public class EodTermRepo implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(EodTermRepo.class);

    public interface Term {
        void collectFieldIds(IntArrayList list);

        HistoricTimeseries getPrice(EodPriceHistoryResponse resp);

        Price getPrice(PriceRecord priceRecord);

        void collectVwdFieldIds(IntArrayList list);
    }

    public static int getTagNum(int adfId) {
        final Descriptors.FieldDescriptor fd = EodHistoryProtos.EodPrice.getDescriptor()
                .findFieldByName("adf_" + adfId);
        if (fd == null) {
            LOG.warn("<getTagNum> no field descriptor found for: adf_{}", adfId);
            return 0;
        }
        return fd.getNumber() - 2;
    }

    private static abstract class TermBase implements Term, Comparable<Term> {
        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj != null && toString().equals(obj.toString());
        }

        @Override
        public int compareTo(Term o) {
            if (o == null) {
                throw new IllegalStateException("no null term allowed");
            }
            return toString().compareTo(o.toString());
        }
    }

    private static class ADF extends TermBase {

        private enum DateType {
            BIDASK, TRADE, OPENINTEREST, FUND, MSCI, LME
        }

        private final static ConcurrentHashMap<Integer, DateType> FIELDID_2_DATETYPE = new ConcurrentHashMap<>();

        static {
            initDateRelation(DateType.BIDASK, 28, 29, 30, 31, 37, 40, 137, 143, 331, 332, 395, 410, 699, 700);
            initDateRelation(DateType.TRADE, 41, 46, 53, 60, 63, 64, 67, 71, 72, 80, 81, 83, 84, 100, 103, 107, 110, 111, 112, 113, 114, 115, 116, 177, 229, 234, 252, 267, 268, 277, 281, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 333, 422, 423, 427, 430, 472, 473, 553, 578, 617, 618, 619, 621, 791, 1224, 1347, 1369, 1370, 1371, 1398, 1525, 1526, 1527, 1528, 1529, 1530, 2249);
            initDateRelation(DateType.OPENINTEREST, 66);
            initDateRelation(DateType.FUND, 68, 175, 225, 244, 246, 269, 383, 414, 494, 600, 602, 1190, 1356, 1500, 1501, 1502, 1503);
            initDateRelation(DateType.MSCI, 104, 165, 173);
            initDateRelation(DateType.LME, 235, 236, 1168, 1169, 1170, 1171);
        }

        private static void initDateRelation(DateType type, int... fieldids) {
            for (final int fieldid : fieldids) {
                final DateType duplicate = FIELDID_2_DATETYPE.put(fieldid, type);
                if (duplicate != null) {
                    throw new IllegalStateException("duplicate field id in DateType relation");
                }
            }
        }

        private final VwdFieldDescription.Field vwdField;

        private final int fieldId;

        private ADF(int adfId) {
            this.vwdField = VwdFieldDescription.getField(adfId);
            this.fieldId = getTagNum(adfId);
            HistoryUtil.ensureUnsignedByte(this.fieldId);
        }

        @Override
        public String toString() {
            return this.vwdField.toString();
        }

        @Override
        public void collectFieldIds(IntArrayList list) {
            list.add(this.fieldId);
        }

        @Override
        public HistoricTimeseries getPrice(EodPriceHistoryResponse resp) {
            return resp.getHistory(this.fieldId);
        }

        @Override
        public Price getPrice(PriceRecord pr) {
            if (pr == null || this.vwdField == null) {
                return null;
            }

            if (pr instanceof PriceRecordVwd) {
                final PriceRecordVwd priceRecordVwd = (PriceRecordVwd) pr;
                final SnapRecord sr = priceRecordVwd.getSnapRecord();
                if (sr != null) {
                    final SnapField sf = sr.getField(this.vwdField.id());
                    if (!sf.isDefined()) {
                        return null;
                    }
                    return getPriceFromSnapField(sf, priceRecordVwd);
                }
            }
            else if (pr instanceof PriceRecordImpl) { // presently hack for quote def 2022
                final PriceRecordImpl pri = (PriceRecordImpl) pr;
                if (VwdFieldDescription.ADF_Anfang.id() == this.vwdField.id()) {
                    return toPrice(pri.getOpen(), pri.getDate());
                }
                else if (VwdFieldDescription.ADF_Tageshoch.id() == this.vwdField.id()) {
                    return toPrice(pri.getHighDay(), pri.getDate());
                }
                else if (VwdFieldDescription.ADF_Tagestief.id() == this.vwdField.id()) {
                    return toPrice(pri.getLowDay(), pri.getDate());
                }
                else if (VwdFieldDescription.ADF_Schluss.id() == this.vwdField.id()) {
                    return toPrice(pri.getClose(), pri.getDate());
                }
            }

//            log.warn("<getPrice> no price to vwd field: {} in {}",
//                    this.vwdField.toString(), pr.getClass().getSimpleName());
            return null;
        }

        protected Price getPriceFromSnapField(SnapField sf, PriceRecordVwd pr) {
            final DateTime date = getDate(pr);
            // not totally correct as this ignores valid 0 values (but the feed does not allow for differentiation)
            switch (sf.getType()) {
                case PRICE:
                    if (sf.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                        return null;
                    }
                    return new PriceImpl(sf.getPrice(), null, null, date, null);
                case NUMBER:
                    final BigDecimal number = new BigDecimal(((Number) sf.getValue()).longValue());
                    if (number.compareTo(BigDecimal.ZERO) == 0) {
                        return null;
                    }
                    return new PriceImpl(number, null, null, date, null);
                case STRING:
                    String strVal = (String) sf.getValue();
                    if (!StringUtils.hasText(strVal)) {
                        return null;
                    }
                    try {
                        final BigDecimal strNumber = new BigDecimal(strVal);
                        if (strNumber.compareTo(BigDecimal.ZERO) == 0) {
                            return null;
                        }
                        return new PriceImpl(strNumber, null, null, date, null);
                    } catch (NumberFormatException e) {
                        LOG.warn("<getPrice> cannot make decimal from '" + sf
                                + "' for " + qidSymbol(pr.getQuoteid()));
                        return null;
                    }
                default:
                    LOG.warn("<getPrice> no support for '" + sf.getType()
                            + "' for " + qidSymbol(pr.getQuoteid()));
                    return null;
            }
        }

        private DateTime getDate(PriceRecord pr) {
            final DateType dateType = FIELDID_2_DATETYPE.get(this.vwdField.id());

            final DateTime date;
            if (dateType == null) {
                date = null;
            }
            else {
                switch (dateType) {
                    case BIDASK:
                        date = DateUtil.max(pr.getBid().getDate(), pr.getAsk().getDate());
                        break;
                    case OPENINTEREST:
                        // TODO: use ADF_OI_DATE
                        // break;
                    case FUND:
                    case MSCI:
                    case LME:
                    case TRADE:
                        date = pr.getPrice().getDate();
                        break;
                    default:
                        date = null;
                        break;
                }
            }

            final DateTime result = (date != null) ? date : pr.getPrice().getDate();
            return (result != null) ? result : pr.getDate();
        }

        private Price toPrice(Price price, DateTime date) {
            return (price == null) ? null : new PriceImpl(price.getValue(), null, null, date, null);
        }

        @Override
        public void collectVwdFieldIds(IntArrayList list) {
            list.add(this.vwdField.id());
        }
    }

    private static abstract class Combi extends TermBase {
        protected final List<Term> terms;

        protected Combi(List<Term> terms) {
            if (terms.isEmpty()) {
                throw new IllegalArgumentException("terms cannot be empty");
            }
            this.terms = terms;
        }

        @Override
        public void collectFieldIds(IntArrayList list) {
            for (Term term : terms) {
                term.collectFieldIds(list);
            }
        }

        @Override
        public void collectVwdFieldIds(IntArrayList list) {
            for (Term term : terms) {
                term.collectVwdFieldIds(list);
            }
        }

        protected List<HistoricTimeseries> getPrices(EodPriceHistoryResponse resp) {
            final List<HistoricTimeseries> results = new ArrayList<>(this.terms.size());
            for (Term term : this.terms) {
                final HistoricTimeseries ht = term.getPrice(resp);
                if (ht != null) {
                    results.add(ht);
                }
            }
            return results;
        }

        protected List<Price> getPrices(PriceRecord priceRecord) {
            final List<Price> results = new ArrayList<>(this.terms.size());
            for (Term term : this.terms) {
                final Price ht = term.getPrice(priceRecord);
                if (ht != null && ht.getValue() != null) {
                    results.add(ht);
                }
            }
            return results;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(getClass().getSimpleName()).append("(");
            for (Term term : terms) {
                sb.append(term).append(",");
            }
            sb.replace(sb.length() - 1, sb.length(), ")");
            return sb.toString();
        }

        public int size() {
            return this.terms.size();
        }

        public Term getChildAt(int idx) {
            if (idx < 0 || idx >= size()) {
                throw new IllegalArgumentException("index out of range: " + idx + ", size: " + size());
            }
            return this.terms.get(idx);
        }
    }

    private static class NVL extends Combi {

        protected NVL(List<Term> terms) {
            super(terms);
        }

        @Override
        public HistoricTimeseries getPrice(EodPriceHistoryResponse resp) {
            final List<HistoricTimeseries> prices = getPrices(resp);
            if (prices.isEmpty()) {
                return null;
            }
            if (prices.size() == 1) {
                return prices.get(0);
            }
            final LocalDate startDay = prices.get(0).getStartDay();
            final double[] values = prices.get(0).getValues();
            for (int i = 0; i < values.length; i++) {
                if (Double.isNaN(values[i])) {
                    values[i] = getNextNumber(prices, i);
                }
            }

            return new HistoricTimeseries(values, startDay);
        }

        private double getNextNumber(List<HistoricTimeseries> prices, int idx) {
            for (int i = 1; i < prices.size(); i++) {
                if (!Double.isNaN(prices.get(i).getValue(idx))) {
                    return prices.get(i).getValue(idx);
                }
            }
            return Double.NaN;
        }

        @Override
        public Price getPrice(PriceRecord priceRecord) {
            for (Term term : this.terms) {
                final Price price = term.getPrice(priceRecord);
                if (price != null && price.getValue() != null) {
                    return price;
                }
            }
            return null;
        }
    }

    private static class NZL extends NVL {

        protected NZL(List<Term> terms) {
            super(terms);
        }

        @Override
        public HistoricTimeseries getPrice(EodPriceHistoryResponse resp) {
            final List<HistoricTimeseries> prices = getPrices(resp);
            if (prices.isEmpty()) {
                return null;
            }
            if (prices.size() == 1) {
                return prices.get(0);
            }
            final LocalDate startDay = prices.get(0).getStartDay();
            final double[] values = prices.get(0).getValues();
            for (int i = 0; i < values.length; i++) {
                if (Double.isNaN(values[i]) || values[i] == 0.0D) {
                    values[i] = getNextNumber(prices, i, values[i]);
                }
            }

            return new HistoricTimeseries(values, startDay);
        }

        private double getNextNumber(List<HistoricTimeseries> prices, int idx, double value) {
            for (int i = 1; i < prices.size(); i++) {
                final double val = prices.get(i).getValue(idx);
                if (!Double.isNaN(val) && val != 0.0D) {
                    return val;
                }
            }
            return value;
        }

        @Override
        public Price getPrice(PriceRecord priceRecord) {
            Price firstPrice = null;
            for (int i = 0; i < this.terms.size(); i++) {
                final Price price = this.terms.get(i).getPrice(priceRecord);
                if (i == 0) {
                    firstPrice = price;
                }
                if (price != null && price.getValue() != null && !price.getValue().equals(BigDecimal.ZERO)) {
                    return price;
                }
            }
            return firstPrice;
        }
    }

    private static class AVG extends Combi {

        protected AVG(List<Term> terms) {
            super(terms);
        }

        @Override
        public HistoricTimeseries getPrice(EodPriceHistoryResponse resp) {
            final List<HistoricTimeseries> prices = getPrices(resp);
            if (prices.isEmpty()) {
                return null;
            }
            if (prices.size() == 1) {
                return prices.get(0);
            }

            final LocalDate startDay = prices.get(0).getStartDay();
            final double[] avgValues = new double[prices.get(0).size()];
            final double[][] values = new double[prices.size()][];
            for (int i = 0; i < values.length; i++) {
                values[i] = prices.get(i).getValues();
            }

            for (int i = 0; i < avgValues.length; i++) {
                avgValues[i] = avg(values, i);
            }

            return new HistoricTimeseries(avgValues, startDay);
        }

        private double avg(double[][] values, int idx) {
            double sum = 0;
            int num = 0;
            for (double[] value : values) {
                final double v = value[idx];
                if (!Double.isNaN(v)) {
                    sum += v;
                    num++;
                }
            }
            return num == 0 ? Double.NaN : sum / num;
        }

        private static final BigDecimal TWO = new BigDecimal(2);

        @Override
        public Price getPrice(PriceRecord priceRecord) {
            final List<Price> prices = getPrices(priceRecord);
            if (prices.isEmpty()) {
                return null;
            }
            if (prices.size() == 1) {
                return prices.get(0);
            }
            BigDecimal sum = BigDecimal.ZERO;
            DateTime dt = null;
            for (Price price : prices) {
                dt = DateUtil.max(dt, price.getDate());
                sum = sum.add(price.getValue(), Constants.MC);
            }

            final BigDecimal bd = prices.size() == 2
                    ? sum.divide(TWO, Constants.MC)
                    : sum.divide(new BigDecimal(prices.size()), Constants.MC);
            return new PriceImpl(bd, null, null, dt, null);
        }
    }

    static Term toTerm(String s) throws RecognitionException {
        final EodFormulaLexer lexer = new EodFormulaLexer(new ANTLRStringStream(s));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final EodFormulaParser parser = new EodFormulaParser(tokens);
        final EodFormulaParser.formula_return formula = parser.formula();
        final CommonTree tree = formula.getTree();
        return reduce(toTerm(tree));
    }

    private static Term reduce(Term term) {
        if (term instanceof ADF) {
            return term;
        }
        if (term instanceof NZL) {
            return reduce((NZL) term);
        }
        if (term instanceof NVL) {
            return reduce((NVL) term);
        }
        if (term instanceof AVG) {
            return reduce((AVG) term);
        }
        throw new UnsupportedOperationException("no support for: " + term.getClass());
    }

    private static Term reduce(NZL nzlTerm) {
        // just one child
        if (nzlTerm.size() == 1) {
            return reduce(nzlTerm.getChildAt(0));
        }

        // more than one child, reduce based on flatting NZL rule
        final List<Term> terms = new ArrayList<>(nzlTerm.size());
        for (Term child : nzlTerm.terms) {
            final Term term = reduce(child);
            if (term instanceof NZL) {
                addAll(terms, ((NZL) term).terms);
            }
            else {
                addTo(terms, term);
            }
        }
        return new NZL(terms);
    }

    private static Term reduce(NVL nvlTerm) {
        // just one child
        if (nvlTerm.size() == 1) {
            return reduce(nvlTerm.getChildAt(0));
        }

        // more than one child, reduce based on flatting NVL rule
        final List<Term> terms = new ArrayList<>(nvlTerm.size());
        for (Term child : nvlTerm.terms) {
            final Term term = reduce(child);
            if (term instanceof NVL) {
                addAll(terms, ((NVL) term).terms);
            }
            else {
                addTo(terms, term);
            }
        }
        return new NVL(terms);
    }

    private static void addAll(List<Term> terms, List<Term> termsToAdd) {
        for (Term term : termsToAdd) {
            addTo(terms, term);
        }
    }

    private static void addTo(List<Term> terms, Term term) {
        if (!terms.contains(term)) {
            terms.add(term);
        }
    }

    private static Term reduce(AVG avgTerm) {
        // just one child
        if (avgTerm.size() == 1) {
            return reduce(avgTerm.getChildAt(0));
        }

        // more than one child, reduce based on flatting AVG rule
        final List<Term> terms = new ArrayList<>(avgTerm.size());
        final List<NVL> nvls = new ArrayList<>(avgTerm.size());
        final List<AVG> avgs = new ArrayList<>(avgTerm.size());
        for (Term child : avgTerm.terms) {
            final Term term = reduce(child);
            if (term instanceof NVL) {
                nvls.add((NVL) term);
            }
            else if (term instanceof AVG) {
                avgs.add((AVG) term);
            }
            else {
                addTo(terms, term);
            }
        }

        reduce(terms, nvls);
        reduce(terms, avgs);
        return new AVG(terms);
    }

    private static <T extends Combi> void reduce(List<Term> terms, List<T> combis) {
        if (combis.isEmpty()) {
            return;
        }
        if (combis.size() == 1) {
            terms.add(combis.get(0));
        }
        else {
            final Map<TreeSet<Term>, List<T>> map = new HashMap<>(10);
            for (T combi : combis) {
                final TreeSet<Term> key = new TreeSet<>(combi.terms);
                map.computeIfAbsent(key, k -> new ArrayList<>(5)).add(combi);
            }
            for (Map.Entry<TreeSet<Term>, List<T>> entry : map.entrySet()) {
                if (entry.getKey().size() <= entry.getValue().size()) {
                    terms.addAll(entry.getKey());
                }
                else {
                    terms.addAll(entry.getValue());
                }
            }
        }
    }

    private static Term toTerm(CommonTree tree) {
        if (tree == null) {
            return null;
        }
        switch (tree.getText()) {
            case "ADF":
                return new ADF(Integer.parseInt(tree.getFirstChildWithType(
                        EodFormulaParser.NUM).getText()));
            case "NVL":
                final List<Term> termsNVL = new ArrayList<>(tree.getChildCount());
                for (int i = 0; i < tree.getChildCount(); i++) {
                    termsNVL.add(toTerm((CommonTree) tree.getChild(i)));
                }
                return new NVL(termsNVL);
            case "NZL":
                final List<Term> termsNZL = new ArrayList<>(tree.getChildCount());
                for (int i = 0; i < tree.getChildCount(); i++) {
                    termsNZL.add(toTerm((CommonTree) tree.getChild(i)));
                }
                return new NZL(termsNZL);
            case "AVG":
                final List<Term> termsAVG = new ArrayList<>(tree.getChildCount());
                for (int i = 0; i < tree.getChildCount(); i++) {
                    termsAVG.add(toTerm((CommonTree) tree.getChild(i)));
                }
                return new AVG(termsAVG);
            default:
                throw new UnsupportedOperationException("no support for: " + tree.getText());
        }
    }

    private Path eodMetaDataPath;

    private final AtomicReference<Map<PriceType, Formula>> ref = new AtomicReference<>();

    public void setEodMetaDataPath(String eodMetaDataPath) {
        this.eodMetaDataPath = Paths.get(eodMetaDataPath);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.eodMetaDataPath, "eodMetaDataPath property is missing");
        updateRepo();
    }

    @ManagedOperation
    public void updateRepo() {
        if (!Files.exists(this.eodMetaDataPath)) {
            LOG.warn("<updateRepo> invalid eod meta data path {}", this.eodMetaDataPath);
            return;
        }
        try (final InputStream is = Files.newInputStream(this.eodMetaDataPath, StandardOpenOption.READ)) {
            TimeTaker tt = new TimeTaker();
            final Properties prop = new Properties();
            prop.load(is);
            final EnumMap<PriceType, Formula> map = new EnumMap<>(PriceType.class);
            for (String key : prop.stringPropertyNames()) {
                if (!StringUtils.hasText(prop.getProperty(key))) {
                    continue;
                }
                final boolean found = MATCHER.reset(key).find();
                if (!found) {
                    throw new IllegalStateException("invalid rule inputs under key: " + key);
                }

                final String priceTypeName = MATCHER.group(1);
                final String quoteDefId = MATCHER.group(3);
                final PriceType priceType = PriceType.valueOf(priceTypeName);

                final Integer quoteDef;
                if (!StringUtils.hasText(quoteDefId)) {
                    quoteDef = null;
                }
                else {
                    quoteDef = Integer.parseInt(quoteDefId);
                }
                if (!map.containsKey(priceType)) {
                    map.put(priceType, new Formula());
                }
                final Formula formula = map.get(priceType);
                if (quoteDef != null) {
                    formula.addByQuoteDef(quoteDef, toTerm(prop.getProperty(key)));
                }
                else {
                    formula.setDefaultEod(toTerm(prop.getProperty(key)));
                }
            }
            this.ref.set(map);
            LOG.info("<updateRepo> read {} in {}", this.eodMetaDataPath, tt);
        } catch (Exception e) {
            LOG.error("<updateRepo> failed load term repo", e);
        }
    }

    private static final class Formula {
        private final Map<Integer, Term> byQuoteDef = new HashMap<>();

        private Term defaultEod;

        private void setDefaultEod(Term defaultEod) {
            this.defaultEod = defaultEod;
        }

        private void addByQuoteDef(int quoteDef, Term eod) {
            this.byQuoteDef.put(quoteDef, eod);
        }

        private Term getTerm(int quoteDef) {
            if (this.byQuoteDef.containsKey(quoteDef)) {
                return this.byQuoteDef.get(quoteDef);
            }
            return this.defaultEod;
        }
    }

    public Term getStandardTerm(int quoteDef, PriceType priceType) {
        final Map<PriceType, Formula> map = this.ref.get();
        return (map == null) ? null :
                (map.containsKey(priceType) ? map.get(priceType).getTerm(quoteDef) : null);
    }

    public Term getTerm(PriceType priceType, int quoteDef, String formula) {
        if (PriceType.ADF == priceType) {
            return new ADF(Integer.parseInt(formula.substring(formula.indexOf("_") + 1)));
        }

        return getStandardTerm(quoteDef, priceType);
    }

    private static final Matcher MATCHER = Pattern.compile("([A-Z]+)(\\.([0-9]+))?").matcher("");

    public static void main(String[] args) throws Exception {
        System.out.println(EodTermRepo.toTerm("NVL(ADF(41), NVL(ADF(331), AVG(ADF(80), ADF(83))))"));
        System.out.println(EodTermRepo.toTerm("NVL(ADF(41), NVL(ADF(107), AVG(NVL(ADF(30), ADF(28)), NVL(ADF(28), ADF(30)))))"));
        System.out.println(EodTermRepo.toTerm("AVG(NVL(ADF(30), ADF(28)), NVL(ADF(28), ADF(30)))"));
        System.out.println(EodTermRepo.toTerm("NVL(ADF(41), NVL(ADF(60), ADF(41)))"));

        System.out.println("+++++++++++++++++++++++");
        System.out.println(EodTermRepo.toTerm("NZL(ADF(41), NZL(ADF(331), AVG(ADF(80), ADF(83))))"));
        System.out.println(EodTermRepo.toTerm("NZL(ADF(41), NZL(ADF(107), AVG(NZL(ADF(30), ADF(28)), NZL(ADF(28), ADF(30)))))"));
        System.out.println(EodTermRepo.toTerm("AVG(NZL(ADF(30), ADF(28)), NZL(ADF(28), ADF(30)))"));
        System.out.println(EodTermRepo.toTerm("NZL(ADF(41), NZL(ADF(60), ADF(41)))"));

        System.out.println("#############################");

        final Path path = Paths.get("d:/produktion/var/data/web/eod_meta.properties");
        try (final InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
            final Properties prop = new Properties();
            prop.load(is);

            final Set<String> keys = prop.stringPropertyNames();
            final IntArrayList list = new IntArrayList();
            for (String key : keys) {
                toTerm(prop.getProperty(key)).collectVwdFieldIds(list);
            }

            final Int2IntAVLTreeMap map = new Int2IntAVLTreeMap();
            for (int i = 0; i < list.size(); i++) {
                final int adfId = list.getInt(i);
                if (!map.containsKey(adfId)) {
                    map.put(adfId, 0);
                }
                map.put(adfId, map.get(adfId) + 1);
            }

            final StringBuilder sb = new StringBuilder();
            sb.append("select qid as quoteid, day as dateVar");
            for (Int2IntMap.Entry entry : map.int2IntEntrySet()) {
                sb.append(", adf_").append(entry.getIntKey());
            }
            sb.append(" from v_eod where day = :mdp.eod.day");

            System.out.println(sb.toString());

            final List<Int2IntMap.Entry> entries = new ArrayList<>();
            entries.addAll(map.int2IntEntrySet());
            entries.sort((o1, o2) -> {
                final int valDiff = o2.getIntValue() - o1.getIntValue();
                return valDiff != 0 ? valDiff : o1.getIntKey() - o2.getIntKey();
            });

            for (Int2IntMap.Entry entry : entries) {
                System.out.println(entry);
            }
        }
    }
}
