/*
 * StaticDataSTKSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.iview.dmxml.DDVRiskData;
import de.marketmaker.iview.dmxml.EDGData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.EdgUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * EdgRatiosSnippet.java
 * Created on Aug 03, 2009 10:16:51 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class EdgRatiosSnippet
        extends AbstractSnippet<EdgRatiosSnippet, SnippetTableView<EdgRatiosSnippet>>
        implements SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("EdgRatios"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new EdgRatiosSnippet(context, config);
        }
    }

    private DmxmlContext.Block<EDGData> edg;

    private EdgRatiosSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        setView(SnippetTableView.create(this, new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.type(), 0.4f, TableCellRenderers.DEFAULT_LABEL), 
                new TableColumn(I18n.I.value(), 0.1f, TableCellRenderers.DEFAULT_RIGHT), 
                new TableColumn("", 5, TableCellRenderers.DEFAULT), // $NON-NLS-0$
                new TableColumn(I18n.I.type(), 0.4f, TableCellRenderers.DEFAULT_LABEL), 
                new TableColumn(I18n.I.value(), 0.1f, TableCellRenderers.DEFAULT_RIGHT), 
        })));
        this.edg = EdgUtil.createBlock(config, context);
        assert this.edg != null;
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.edg.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void destroy() {
        destroyBlock(this.edg);
    }

    public void updateView() {
        if (!this.edg.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        final List<Object[]> list = new ArrayList<Object[]>();
        final DDVRiskData data = this.edg.getResult().getRiskdata();
        add(list, I18n.I.date(), data.getDdvDate(), 
                I18n.I.ddvPriceRiskNDays(10), Renderer.PRICE.render(data.getDdvPriceRisk10D())); 
        add(list, I18n.I.ddvCurrencyRiskNDays(10), Renderer.PRICE.render(data.getDdvCurrencyRisk10D()), 
                I18n.I.ddvPriceRiskNDays(250), Renderer.PRICE.render(data.getDdvPriceRisk250D())); 
        add(list, I18n.I.ddvCurrencyRiskNDays(250), Renderer.PRICE.render(data.getDdvCurrencyRisk250D()), 
                I18n.I.ddvRiskClassNDays(10), Renderer.PRICE.render(data.getDdvRiskclass10D())); 
        add(list, I18n.I.ddvDiversificationRiskNDays(10), Renderer.PRICE.render(data.getDdvDiversificationRisk10D()), 
                I18n.I.ddvTimevalueNDays(10), Renderer.PRICE.render(data.getDdvTimevalue10D())); 
        add(list, I18n.I.ddvDiversificationRiskNDays(250), Renderer.PRICE.render(data.getDdvDiversificationRisk250D()), 
                I18n.I.ddvTimevalueNDays(250), Renderer.PRICE.render(data.getDdvTimevalue250D())); 
        add(list, I18n.I.ddvInterestRiskNDays(10), Renderer.PRICE.render(data.getDdvInterestRisk10D()), 
                I18n.I.ddvVarNDays(10), Renderer.PRICE.render(data.getDdvVar10D())); 
        add(list, I18n.I.ddvInterestRiskNDays(250), Renderer.PRICE.render(data.getDdvInterestRisk250D()), 
                I18n.I.ddvVolatilityRiskNDays(10), Renderer.PRICE.render(data.getDdvVolatilityRisk10D())); 
        add(list, I18n.I.ddvIssuerRiskNDays(10), Renderer.PRICE.render(data.getDdvIssuerRisk10D()), 
                I18n.I.ddvVolatilityRiskNDays(250), Renderer.PRICE.render(data.getDdvVolatilityRisk250D())); 
        add(list, I18n.I.ddvIssuerRiskNDays(250), Renderer.PRICE.render(data.getDdvIssuerRisk250D()), "", "");  // $NON-NLS-0$ $NON-NLS-1$

        getView().update(DefaultTableDataModel.create(list));
    }

    private void add(List<Object[]> list, String meta, String data, String meta2, String data2) {
        list.add(new Object[]{meta, data, "", meta2, data2}); // $NON-NLS-0$
    }
}
