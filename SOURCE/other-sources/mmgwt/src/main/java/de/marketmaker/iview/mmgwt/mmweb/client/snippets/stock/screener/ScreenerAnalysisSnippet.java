/*
 * BestToolSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.screener;

import de.marketmaker.iview.dmxml.STKScreenerData;
import de.marketmaker.iview.dmxml.ScreenerAnalysisFields;
import de.marketmaker.iview.dmxml.ScreenerField;
import de.marketmaker.iview.dmxml.ScreenerFieldDate;
import de.marketmaker.iview.dmxml.ScreenerFieldDecimal;
import de.marketmaker.iview.dmxml.ScreenerFieldInteger;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Image;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ScreenerAnalysisSnippet extends
        AbstractSnippet<ScreenerAnalysisSnippet, SnippetTableView<ScreenerAnalysisSnippet>> implements
        SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("ScreenerAnalysis", I18n.I.screenerAnalysis()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ScreenerAnalysisSnippet(context, config);
        }
    }

    private DmxmlContext.Block<STKScreenerData> block;

    private ScreenerAnalysisSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("STK_ScreenerData"); // $NON-NLS$
        this.block.setParameter("language", config.getString("language", I18n.I.localeForScreener())); // $NON-NLS$

        this.setView(new SnippetTableView<>(this, ScreenerUtil.createColumnModel()));
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
    }


    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!this.block.isResponseOk() || this.block.getResult() == null || this.block.getResult().getAnalysisfields() == null) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        final ScreenerAnalysisFields af = this.block.getResult().getAnalysisfields();
        final List<Object[]> list = new ArrayList<>();
        list.add(getLineDataDate(af.getAnalysisdate()));
        list.add(getLineDataImage(af.getInterest()));
        list.add(getLineDataValue(af.getMarketCapitalization(), "$", " " + I18n.I.billionAbbr()));  // $NON-NLS$
        list.add(getLineDataImage(af.getEarningsRevisionTrend()));
        list.add(getLineDataImage(af.getValuationRating()));
        list.add(getLineDataValue(af.getGrowthToPriceEarningsRatio()));
        list.add(getLineDataValue(af.getLongtermPriceEarningsEstimate()));
        list.add(getLineDataValue(af.getLongtermGrowthEstimate(), "", "%")); // $NON-NLS
        list.add(getLineDataValue(af.getNumberOfAnalysts()));
        list.add(getLineDataValue(af.getDividendyield(), Renderer.PERCENT)); // dividend texts ???????????
        list.add(getLineDataImage(af.getTechnicalTrend()));
        list.add(getLineDataValue(af.getRelativePerformance(), Renderer.PERCENT)); // 4th star ??????????
        list.add(getLineDataImage(af.getGlobalEvaluation()));
        final TableDataModel tdm = DefaultTableDataModel.create(list);
        getView().update(tdm);
    }


    private Object[] getLineDataCommon(ScreenerField sf) {
        final Object headline = sf.getHeadline();
        final Object stars = getImage(sf.getStarImageName());
        final Object shortText = sf.getShortText();
        final Object longText = sf.getLongText();
        return new Object[]{headline, null, stars, shortText, longText};
    }


    private Object[] getLineDataImage(ScreenerField sf) {
        final Object[] values = getLineDataCommon(sf);
        values[1] = getImage(sf.getImageName());
        return values;
    }

    private Object[] getLineDataValue(ScreenerFieldDecimal sf) {
        final Object[] values = getLineDataCommon(sf);
        values[1] = sf.getValue() == null ? "" : Renderer.PRICE.render(sf.getValue());
        return values;
    }

    private Object[] getLineDataValue(ScreenerFieldDecimal sf, Renderer<String> renderer) {
        final Object[] values = getLineDataCommon(sf);
        values[1] = sf.getValue() == null ? "" : renderer.render(sf.getValue());
        return values;
    }

    private Object[] getLineDataValue(ScreenerFieldDecimal sf, String prefix, String suffix) {
        final Object value = sf.getValue() == null ? "" : prefix + Renderer.PRICE.render(sf.getValue()) + suffix;
        final Object[] values = getLineDataCommon(sf);
        values[1] = value;
        return values;
    }

    private Object[] getLineDataValue(ScreenerFieldInteger sf) {
        final Object[] values = getLineDataCommon(sf);
        values[1] = sf.getValue() == null ? null : sf.getValue();
        return values;
    }

    private Object[] getLineDataDate(ScreenerFieldDate sf) {
        final Object value = sf.getValue();
        final Object[] values = getLineDataCommon(sf);
        values[1] = value;
        return values;
    }


    private Image getImage(String imageName) {
        return ScreenerUtil.getImage(imageName);
    }
}
