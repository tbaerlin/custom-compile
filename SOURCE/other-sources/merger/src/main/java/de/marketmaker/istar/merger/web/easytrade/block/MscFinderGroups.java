/*
 * FndSektorenvergleich.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domainimpl.instrument.NullQuote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.EnumEditor;
import de.marketmaker.istar.merger.web.easytrade.ListCommandWithOptionalPaging;
import de.marketmaker.istar.merger.web.easytrade.ListHelper;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.ProviderSelectionCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DataRecordStrategy;
import de.marketmaker.istar.ratios.frontend.MinMaxAvgRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.MinMaxAvgVisitor;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscFinderGroups extends AbstractFindersuchergebnis {
    private final static Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);

    private static final String MSC_NAME = "MSC";

    public MscFinderGroups() {
        super(Command.class);
    }

    protected void initBinder(HttpServletRequest httpServletRequest,
            ServletRequestDataBinder binder) throws Exception {
        super.initBinder(httpServletRequest, binder);

        binder.registerCustomEditor(InstrumentTypeEnum.class,
                new EnumEditor<>(InstrumentTypeEnum.class));
        binder.registerCustomEditor(DataRecordStrategy.Type.class,
                new EnumEditor<>(DataRecordStrategy.Type.class));
    }

    public static class Command extends ListCommandWithOptionalPaging implements
            ProviderSelectionCommand {
        private String query;

        private String primaryField;

        private String secondaryField;

        private String evaluateField;

        private InstrumentTypeEnum type;

        private String[] countItem;

        private boolean isWithSiblingsForUnderlying = true;

        private String providerPreference;

        private DataRecordStrategy.Type dataRecordStrategy;

        @RestrictedSet("VWD,SMF")
        public String getProviderPreference() {
            return providerPreference;
        }

        public void setProviderPreference(String providerPreference) {
            this.providerPreference = providerPreference;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        @NotNull
        public String getPrimaryField() {
            return primaryField;
        }

        public void setPrimaryField(String primaryField) {
            this.primaryField = primaryField;
        }

        public String getSecondaryField() {
            return secondaryField;
        }

        public void setSecondaryField(String secondaryField) {
            this.secondaryField = secondaryField;
        }

        public String getEvaluateField() {
            return evaluateField;
        }

        public void setEvaluateField(String evaluateField) {
            this.evaluateField = evaluateField;
        }

        @NotNull
        public InstrumentTypeEnum getType() {
            return type;
        }

        public void setType(InstrumentTypeEnum type) {
            this.type = type;
        }

        public String[] getCountItem() {
            return countItem;
        }

        public void setCountItem(String[] countItem) {
            this.countItem = countItem;
        }

        public boolean isWithSiblingsForUnderlying() {
            return isWithSiblingsForUnderlying;
        }

        public void setWithSiblingsForUnderlying(boolean withSiblingsForUnderlying) {
            isWithSiblingsForUnderlying = withSiblingsForUnderlying;
        }

        public DataRecordStrategy.Type getDataRecordStrategy() {
            return dataRecordStrategy;
        }

        public void setDataRecordStrategy(DataRecordStrategy.Type dataRecordStrategy) {
            this.dataRecordStrategy = dataRecordStrategy;
        }
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final InstrumentTypeEnum type = cmd.getType();

        final PermissionType pt;
        if (type == InstrumentTypeEnum.FND) {
            pt = PermissionType.FUNDDATA;
        }
        else if (type == InstrumentTypeEnum.CER) {
            pt = null;
        }
        else {
            pt = null;
        }

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = AbstractFindersuchergebnis.getFields(type, cmd.getProviderPreference(), pt);

        final RatioFieldDescription.Field primaryfield
                = AbstractFindersuchergebnis.getField(fields, cmd.getPrimaryField());
        final RatioFieldDescription.Field secondaryfield
                = AbstractFindersuchergebnis.getField(fields, cmd.getSecondaryField());
        final RatioFieldDescription.Field evaluate
                = cmd.getEvaluateField() == null ? null : fields.get(RatioDataRecord.Field.valueOf(cmd.getEvaluateField()));

        final RatioSearchRequest mmaRequest = new RatioSearchRequest(RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales());
        mmaRequest.setType(type);

        final DataRecordStrategy.Type strategy = cmd.getDataRecordStrategy();
        if (strategy != null) {
            mmaRequest.setDataRecordStrategyClass(strategy.getClazz());
        }

        mmaRequest.setVisitorClass(MinMaxAvgVisitor.class);
        final Map<String, String> mmaParameters = parseQuery(cmd.getQuery(), fields);
        replaceUnderlyingSymbol(mmaParameters, cmd.isWithSiblingsForUnderlying());
        mmaRequest.setParameters(mmaParameters);
        mmaParameters.put(MinMaxAvgVisitor.KEY_GROUP_BY, primaryfield.name() +
                (secondaryfield == null ? "" : "," + secondaryfield.name()));

        if (evaluate != null) {
            final String evFieldname = evaluate.name().toLowerCase();
            mmaParameters.put(MinMaxAvgVisitor.KEY_SOURCE, evFieldname);
        }

        final MinMaxAvgRatioSearchResponse mmaResponse
                = (MinMaxAvgRatioSearchResponse) this.ratiosProvider.search(mmaRequest);
        final Map<Integer, Map<String, Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg>>> mmaResult
                = mmaResponse.getResult();
        final Map<String, Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg>> mmaMap
                = mmaResult.get(evaluate != null ? evaluate.id() : 0);

        final Set<String> columnSet = new TreeSet<>(GERMAN_COLLATOR);
        final List<String> rows = new ArrayList<>();
        if (mmaMap != null) {
            for (final Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg> map : mmaMap.values()) {
                columnSet.addAll(map.keySet());
            }

            rows.addAll(mmaMap.keySet());
        }

        final List<String> columns = new ArrayList<>(columnSet);
        rows.sort(GERMAN_COLLATOR);

        if (cmd.getCountItem() != null) {
            rows.retainAll(new HashSet<>(Arrays.asList(cmd.getCountItem())));
            rows.add(MSC_NAME);
        }

        final ListResult listResult = ListResult.create(cmd, Collections.singletonList("fieldname"), "fieldname", rows.size());
        ListHelper.clipPage(cmd, rows);
        listResult.setCount(rows.size());

        final Map<String, List<MinMaxAvgRatioSearchResponse.MinMaxAvg>> table
                = new LinkedHashMap<>();
        if (mmaMap != null) {
            for (final String key : mmaMap.keySet()) {
                final Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg> map = mmaMap.get(key);
                if (!rows.contains(key)) { // means: this is an MSC entry
                    List<MinMaxAvgRatioSearchResponse.MinMaxAvg> mscList = table.get(MSC_NAME);

                    if (mscList == null) {
                        mscList = new ArrayList<>(Collections.<MinMaxAvgRatioSearchResponse.MinMaxAvg>nCopies(columns.size(), null));
                        table.put(MSC_NAME, mscList);
                    }

                    for (int i = 0; i < columns.size(); i++) {
                        final String column = columns.get(i);
                        final MinMaxAvgRatioSearchResponse.MinMaxAvg mma = mscList.get(i);
                        final MinMaxAvgRatioSearchResponse.MinMaxAvg org = map.get(column);

                        if (mma == null) {
                            mscList.set(i, org);
                        }
                        else {
                            mma.merge(org);
                        }
                    }
                }
                else {
                    final List<MinMaxAvgRatioSearchResponse.MinMaxAvg> mmaList = new ArrayList<>(columns.size());
                    for (final String column : columns) {
                        final MinMaxAvgRatioSearchResponse.MinMaxAvg avg = map.get(column);
                        mmaList.add(avg);
                    }

                    table.put(key, mmaList);
                }
            }
        }

        final Map<String, Object> model = new HashMap<>();

        final List<Quote> underlyingQuotes = getUnderlyingQuotes(primaryfield, secondaryfield, rows, columns, this.instrumentProvider);
        if (underlyingQuotes != null) {
            model.put("underlyingQuotes", underlyingQuotes);
        }
        model.put("rows", rows);
        model.put("columns", columns);
        model.put("table", table);
        model.put("listinfo", listResult);
        return new ModelAndView("mscfindergroups", model);
    }

    static List<Quote> getUnderlyingQuotes(RatioFieldDescription.Field primaryfield,
            RatioFieldDescription.Field secondaryfield, List<String> rows, List<String> columns,
            EasytradeInstrumentProvider instrumentProvider) {

        boolean isIsinField;
        final List<String> underlyingSymbols;
        if (isUnderlyingField(primaryfield)) {
            isIsinField = primaryfield == RatioFieldDescription.underlyingIsin;
            underlyingSymbols = isIsinField ? rows : getUnderlyingIids(rows);
        }
        else if (secondaryfield != null && isUnderlyingField(secondaryfield)) {
            isIsinField = secondaryfield == RatioFieldDescription.underlyingIsin;
            underlyingSymbols = isIsinField ? columns : getUnderlyingIids(columns);
        }
        else {
            return null;
        }

        final SymbolStrategyEnum symbolStrategy = isIsinField ? SymbolStrategyEnum.ISIN : SymbolStrategyEnum.IID;
        final List<Quote> quotes = instrumentProvider.identifyQuotes(underlyingSymbols, symbolStrategy, null, null);

        final List<String> remainingSymbols = new ArrayList<>();
        for (int i = 0; i < quotes.size(); i++) {
            if (quotes.get(i) == null) {
                remainingSymbols.add(underlyingSymbols.get(i));
            }
        }

        if (!remainingSymbols.isEmpty()) {
            final Map<String, Instrument> instruments
                    = instrumentProvider.identifyInstrument(remainingSymbols, symbolStrategy);
            for (int i = 0; i < quotes.size(); i++) {
                if (quotes.get(i) == null) {
                    final Instrument instrument = instruments.get(underlyingSymbols.get(i));
                    if (instrument != null) {
                        quotes.set(i, NullQuote.create(instrument));
                    }
                }
            }
        }

        return quotes;
    }

    private static List<String> getUnderlyingIids(List<String> items) {
        final List<String> underlyingIids = new ArrayList<>(items.size());
        for (final String item : items) {
            try {
                final long iid = Long.parseLong(item);
                underlyingIids.add(EasytradeInstrumentProvider.iidSymbol(iid));
            } catch (NumberFormatException e) {
                // ignore on purpose, since for multiassetName items, item is no long
            }
        }
        return underlyingIids;
    }

    private static boolean isUnderlyingField(RatioFieldDescription.Field field) {
        return field == RatioFieldDescription.underlyingIid
                || field == RatioFieldDescription.underlyingProductIid
                || field == RatioFieldDescription.gatrixxMultiassetName
                || field == RatioFieldDescription.underlyingIsin;
    }
}