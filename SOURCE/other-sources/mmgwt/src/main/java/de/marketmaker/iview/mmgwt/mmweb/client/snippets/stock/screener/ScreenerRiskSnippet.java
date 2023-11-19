/*
 * BestToolSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.screener;

import de.marketmaker.iview.dmxml.STKScreenerData;
import de.marketmaker.iview.dmxml.ScreenerField;
import de.marketmaker.iview.dmxml.ScreenerFieldDecimal;
import de.marketmaker.iview.dmxml.ScreenerFieldInteger;
import de.marketmaker.iview.dmxml.ScreenerRiskFields;
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
public class ScreenerRiskSnippet extends AbstractSnippet<ScreenerRiskSnippet, SnippetTableView<ScreenerRiskSnippet>> implements SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("ScreenerRisk", I18n.I.screenerRisk()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ScreenerRiskSnippet(context, config);
        }
    }

    private DmxmlContext.Block<STKScreenerData> block;

    private ScreenerRiskSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("STK_ScreenerData"); // $NON-NLS$
        this.block.setParameter("language", config.getString("language", I18n.I.localeForScreener())); // $NON-NLS$

        this.setView(new SnippetTableView<>(this, ScreenerUtil.createColumnModel()));
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS$
    }


    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!this.block.isResponseOk() || this.block.getResult() == null || this.block.getResult().getRiskfields() == null) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        final ScreenerRiskFields rf = this.block.getResult().getRiskfields();
        final List<Object[]> list = new ArrayList<>();
        list.add(getLineDataCommon(rf.getRiskzone()));
        list.add(getLineDataValue(rf.getBearmarketfactor(), rf.getBearmarketfactorvalue()));
        list.add(getLineDataValue(rf.getBadnewsfactor(), rf.getBadnewsfactorvalue()));
        list.add(getLineDataValue(rf.getBeta(), "%")); // $NON-NLS$
        list.add(getLineDataValue(rf.getCorrelation(), Renderer.PERCENT_INT));
        list.add(getLineDataValue(rf.getValueAtRisk(), "")); // $NON-NLS$
        final TableDataModel tdm = DefaultTableDataModel.create(list);
        getView().update(tdm);
    }

    private Object[] getLineDataCommon(ScreenerField sf) {
        final Object headline = sf.getHeadline();
        final Object image = getImage(sf.getImageName());
        final Object shortText = sf.getShortText();
        final Object longText = sf.getLongText();
        return new Object[]{ headline, null, image, shortText, longText };
    }

    private Object[] getLineDataValue(ScreenerFieldDecimal sf, String suffix) {
        final Object[] values = getLineDataCommon(sf);
        values[1] = sf.getValue() == null ? "" : (Renderer.PRICE.render(sf.getValue()) + suffix); // $NON-NLS$
        return values;
    }

    private Object[] getLineDataValue(ScreenerFieldDecimal sf, Renderer<String> renderer) {
        final Object[] values = getLineDataCommon(sf);
        values[1] = sf.getValue() == null ? "" : renderer.render(sf.getValue()); // $NON-NLS$
        return values;
    }

    private Object[] getLineDataValue(ScreenerFieldInteger sf, ScreenerFieldDecimal sfd) {
        final Object[] values = getLineDataCommon(sf);
        values[1] = sfd.getValue() == null ? null : (Renderer.PRICE.render(sfd.getValue()) + "%"); // $NON-NLS$
        return values;
    }

    private Image getImage(String imageName) {
        return ScreenerUtil.getImage(imageName);
    }
}
