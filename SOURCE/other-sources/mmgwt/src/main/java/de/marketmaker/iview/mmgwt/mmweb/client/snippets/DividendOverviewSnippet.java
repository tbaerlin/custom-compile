/*
 * DividendOverviewSnippet.java
 *
 * Created on 27.10.2014 16:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.CorporateAction;
import de.marketmaker.iview.dmxml.DividendData;
import de.marketmaker.iview.dmxml.MSCCorporateActions;
import de.marketmaker.iview.dmxml.STKDividendData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellMetaData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CurrencyRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.DEFAULT;

/**
 * @author jkirchg
 */
public class DividendOverviewSnippet
        extends AbstractSnippet<DividendOverviewSnippet, SnippetTableView<DividendOverviewSnippet>>
        implements SymbolSnippet {

    private final DmxmlContext.Block<MSCCorporateActions> blockCorporateActions;

    private final DmxmlContext.Block<STKDividendData> blockDividendData;

    private static final MathContext MC = new MathContext(8, RoundingMode.HALF_EVEN);

    private static final TableCellRenderers.StringRenderer LABEL_RENDERER
                = new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label"); // $NON-NLS$

    public static class Class extends SnippetClass {
        public Class() {
            super("DividendOverview", I18n.I.dividend()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new DividendOverviewSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("width", DEFAULT_SNIPPET_WIDTH); // $NON-NLS$
        }
    }

    private DividendOverviewSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.blockCorporateActions = createBlock("MSC_CorporateActions"); // $NON-NLS$
        this.blockCorporateActions.setParameter("period", config.getString("period", "P10Y")); // $NON-NLS$

        this.blockDividendData = createBlock("STK_DividendData"); // $NON-NLS$

        this.setView(new SnippetTableView<>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn("", -1f, LABEL_RENDERER), // $NON-NLS$
                        new TableColumn("", -1f, DEFAULT), // $NON-NLS$
                        new TableColumn("", -1f, DEFAULT) // $NON-NLS$
                }, false)));
    }

    @Override
    public void setSymbol(InstrumentTypeEnum type, String symbol, String name,
            String... compareSymbols) {
        this.blockCorporateActions.setParameter("symbol", symbol); // $NON-NLS$
        this.blockDividendData.setParameter("symbol", symbol); // $NON-NLS$
    }

    @Override
    public void destroy() {
        destroyBlock(this.blockCorporateActions);
        destroyBlock(this.blockDividendData);
    }

    @Override
    public void updateView() {
        if (!this.blockDividendData.isResponseOk() || this.blockDividendData.getResult() == null
                || this.blockDividendData.getResult().getDividendData() == null
                || !this.blockCorporateActions.isResponseOk()
                || this.blockCorporateActions.getResult() == null
                || this.blockCorporateActions.getResult().getCorporateAction() == null) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final List<Object[]> list = new ArrayList<>();
        final List<CorporateAction> corporateActions = blockCorporateActions.getResult().getCorporateAction();
        final Map<Integer, DividendData> dividendData = toMap(blockDividendData.getResult().getDividendData());
        final int currentYear = new MmJsDate().getFullYear();

        list.add(new Object[]{
                I18n.I.dividendYield(),
                getCurrent(dividendData, currentYear, Field.YIELD),
                I18n.I.fiscalYear()});
        list.add(new Object[]{
                "",
                getAverage5y(dividendData, currentYear, Field.YIELD),
                I18n.I.averageHtmlSign() + " " + I18n.I.nYears(5)});
        list.add(new Object[]{
                I18n.I.dividendPayoutRatio(),
                getCurrent(dividendData, currentYear, Field.PAYOUT_RATIO),
                I18n.I.fiscalYear()});
        list.add(new Object[]{
                "",
                getAverage5y(dividendData, currentYear, Field.PAYOUT_RATIO),
                I18n.I.averageHtmlSign() + " " + I18n.I.nYears(5)});
        list.add(new Object[]{
                I18n.I.dividendCoverage(),
                getCurrent(dividendData, currentYear, Field.COVERAGE),
                I18n.I.fiscalYear()});
        list.add(new Object[]{
                "",
                getAverage5y(dividendData, currentYear, Field.COVERAGE),
                I18n.I.averageHtmlSign() + " " + I18n.I.nYears(5)});
        list.add(new Object[]{
                I18n.I.dividendDPSGrowth5yr(),
                getCurrent(dividendData, currentYear, Field.DPS_GROWTH_5Y),
                ""});

        CorporateAction latestDividend = getLatestDividendAction(corporateActions);
        if (latestDividend != null) {
            list.add(new Object[]{
                    I18n.I.lastDividend(),
                    Renderer.PRICE.render(latestDividend.getValue()),
                    CurrencyRenderer.DEFAULT.render(latestDividend.getCurrency())});
            list.add(new Object[]{
                    "",
                    DateRenderer.date("").render(latestDividend.getDate()),
                    I18n.I.paymentDate()});
        }

        final DefaultTableDataModel tdm = DefaultTableDataModel.create(list);

        addToolTips(list, tdm);

        getView().update(tdm);
    }

    private void addToolTips(List<Object[]> list, DefaultTableDataModel tdm) {
        for (int row = 0; row < list.size(); row++) {
            final Object[] object = list.get(row);
            if (object[0] != null && object[0] instanceof String && !((String)object[0]).isEmpty()) {
                String toolTip = null;
                String currentLabel = (String)object[0];
                if (currentLabel.equals(I18n.I.dividendYield())) {
                    toolTip = I18n.I.dividendYieldTooltip();
                }
                else if (currentLabel.equals(I18n.I.dividendPayoutRatio())) {
                    toolTip = I18n.I.dividendPayoutRatioTooltip();
                }
                else if (currentLabel.equals(I18n.I.dividendCoverage())) {
                    toolTip = I18n.I.dividendCoverageTooltip();
                }

                if (toolTip != null) {
                    final CellMetaData metaData = tdm.getMetaData(row, 0) == null
                            ? new CellMetaData()
                            : tdm.getMetaData(row, 0);
                    tdm.setMetaData(row, 0, metaData.withToolTip(toolTip));
                }
            }
        }
    }

    private Map<Integer, DividendData> toMap(List<DividendData> dividendData) {
        final HashMap<Integer, DividendData> map = new HashMap<>();
        for (DividendData item : dividendData) {
            map.put(Integer.parseInt(item.getYear()), item);
        }
        return map;
    }

    private enum Field {
        COVERAGE, DPS_GROWTH_5Y, PAYOUT_RATIO, YIELD;
    }

    private String getCurrent(Map<Integer, DividendData> dividendData, int currentYear, Field field) {
        if (dividendData != null && !dividendData.isEmpty() && dividendData.get(currentYear - 1) != null) {
            DividendData dividendDataItem = dividendData.get(currentYear - 1);
            switch(field) {
                case COVERAGE:
                    if (dividendDataItem.getDividendCoverage() != null
                            && !dividendDataItem.getDividendCoverage().isEmpty()) {
                        return Renderer.PERCENT.render(new BigDecimal(dividendDataItem.getDividendCoverage().toString()).toString());
                    }
                case DPS_GROWTH_5Y:
                    if (dividendDataItem.getDividendPerShareGrowth5Y() != null
                            && !dividendDataItem.getDividendPerShareGrowth5Y().isEmpty()) {
                        return Renderer.PERCENT.render(new BigDecimal(dividendDataItem.getDividendPerShareGrowth5Y().toString()).toString());
                    }
                case PAYOUT_RATIO:
                    if (dividendDataItem.getDividendPayoutRatio() != null
                            && !dividendDataItem.getDividendPayoutRatio().isEmpty()) {
                        return Renderer.PERCENT.render(new BigDecimal(dividendDataItem.getDividendPayoutRatio().toString()).toString());
                    }
                case YIELD:
                    if (dividendDataItem.getDividendYield() != null
                            && !dividendDataItem.getDividendYield().isEmpty()) {
                        return Renderer.PERCENT.render(new BigDecimal(dividendDataItem.getDividendYield().toString()).toString());
                    }
            }
        }
        return null;
    }

    private String getAverage5y(Map<Integer, DividendData> dividendData, int currentYear, Field field) {
        BigDecimal result = new BigDecimal(0);
        if (dividendData != null && !dividendData.isEmpty()) {
            for (int i = 1; i <= 5; i++) {
                if (dividendData.get(currentYear - i) != null) {
                    DividendData dividendDataItem = dividendData.get(currentYear - i);
                    switch(field) {
                        case COVERAGE:
                            if (dividendDataItem.getDividendCoverage() != null
                                    && !dividendDataItem.getDividendCoverage().isEmpty()) {
                                result = result.add(new BigDecimal(dividendDataItem.getDividendCoverage().toString()));
                            }
                            break;
                        case PAYOUT_RATIO:
                            if (dividendDataItem.getDividendPayoutRatio() != null
                                    && !dividendDataItem.getDividendPayoutRatio().isEmpty()) {
                                result = result.add(new BigDecimal(dividendDataItem.getDividendPayoutRatio().toString()));
                            }
                            break;
                        case YIELD:
                            if (dividendDataItem.getDividendYield() != null
                                    && !dividendDataItem.getDividendYield().isEmpty()) {
                                result = result.add(new BigDecimal(dividendDataItem.getDividendYield().toString()));
                            }
                            break;
                    }
                }
                else {
                    return null;
                }
            }
            if (result.compareTo(BigDecimal.ZERO) != 0) {
                result = result.divide(new BigDecimal(5), MC);
            }
        }
        else {
            return null;
        }
        return Renderer.PERCENT.render(result.toString());
    }

    private CorporateAction getLatestDividendAction(List<CorporateAction> corporateActions) {
        for (int i = corporateActions.size() - 1; i >= 0; i++) {
            if ("DIVIDEND".equals(corporateActions.get(i).getType())) { // $NON-NLS$
                return corporateActions.get(i);
            }
        }
        return null;
    }

}
