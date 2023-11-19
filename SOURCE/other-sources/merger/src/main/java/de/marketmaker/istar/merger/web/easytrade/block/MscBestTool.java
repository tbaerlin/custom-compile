/*
 * MscBestTool.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.web.easytrade.EnumEditor;
import de.marketmaker.istar.merger.web.easytrade.ListHelper;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.ratios.frontend.BestToolRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.DataRecordStrategy;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscBestTool extends EasytradeCommandController {
    public final static Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);
    private EasytradeInstrumentProvider instrumentProvider;
    private RatiosProvider ratiosProvider;

    public MscBestTool() {
        super(BestToolCommand.class);
    }

    protected void initBinder(HttpServletRequest httpServletRequest,
            ServletRequestDataBinder binder) throws Exception {
        super.initBinder(httpServletRequest, binder);

        EnumEditor.register(InstrumentTypeEnum.class, binder);
        EnumEditor.register(DataRecordStrategy.Type.class, binder);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final BestToolCommand cmd = (BestToolCommand) o;
        final BestToolRatioSearchMethod searchMethod = BestToolRatioSearchMethod.create(cmd, this.ratiosProvider);
        final BestToolRatioSearchResponse ratioResponse = searchMethod.ratioSearch();
        final Map<String, Map<Object, List<BestToolRatioSearchResponse.BestToolElement>>> responseMap = ratioResponse.getResult();

        final Set<Object> columns = new TreeSet<>();
        for (final Map<Object, List<BestToolRatioSearchResponse.BestToolElement>> map : responseMap.values()) {
            for (final Object col : map.keySet()) {
                columns.add(col);
            }
        }

        final List<String> rows = new ArrayList<>(responseMap.keySet());
        rows.sort(GERMAN_COLLATOR);

        final ListResult listResult = ListResult.create(cmd, Collections.singletonList("fieldname"), "fieldname", rows.size());
        ListHelper.clipPage(cmd, rows);
        listResult.setCount(rows.size());


        final List<Long> qids = new ArrayList<>();
        final Map<String, List<List<BestToolRatioSearchResponse.BestToolElement>>> result = new LinkedHashMap<>();
        for (final String row : rows) {
            final Map<Object, List<BestToolRatioSearchResponse.BestToolElement>> map = responseMap.get(row);

            final List<List<BestToolRatioSearchResponse.BestToolElement>> bteElements = new ArrayList<>(columns.size());
            for (final Object column : columns) {
                final List<BestToolRatioSearchResponse.BestToolElement> elements = map.get(column);
                bteElements.add(elements);

                if (elements == null) {
                    continue;
                }

                for (final BestToolRatioSearchResponse.BestToolElement element : elements) {
                    qids.add(element.getQid());
                }
            }

            result.put(row, bteElements);
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("rows", rows);
        model.put("columns", columns);
        model.put("table", result);
        model.put("listinfo", listResult);

        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(qids);
        CollectionUtils.removeNulls(quotes);
        for (final Quote quote : quotes) {
            model.put(Long.toString(quote.getId()), quote);
        }

        final List<Quote> underlyingQuotes = MscFinderGroups.getUnderlyingQuotes(
            searchMethod.getPrimaryField(), null, rows, null, this.instrumentProvider);
        if (underlyingQuotes != null) {
            model.put("underlyingQuotes", underlyingQuotes);
        }

        return new ModelAndView("mscbesttool", model);
    }

    public static class BestElement implements Comparable<BestElement> {
        private final BigDecimal interval;

        private final List<BestToolRatioSearchResponse.BestToolElement> elements;

        public BestElement(BigDecimal interval,
                List<BestToolRatioSearchResponse.BestToolElement> elements) {
            this.interval = interval;
            this.elements = elements;
        }

        public BigDecimal getInterval() {
            return interval;
        }

        public List<BestToolRatioSearchResponse.BestToolElement> getElements() {
            return elements;
        }

        public int compareTo(BestElement o) {
            return this.interval.compareTo(o.interval);
        }
    }
}
