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

import de.marketmaker.iview.dmxml.EDGData;
import de.marketmaker.iview.dmxml.EDGRatingData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.EdgUtil;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * EdgRatingSnippet.java
 * Created on Aug 03, 2009 10:16:51 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class EdgRatingSnippet
        extends AbstractSnippet<EdgRatingSnippet, SnippetTableView<EdgRatingSnippet>>
        implements SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("EdgRating"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new EdgRatingSnippet(context, config);
        }
    }

    private DmxmlContext.Block<EDGData> edg;

    private EdgRatingSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        setView(SnippetTableView.create(this, new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn("", 0.05f, TableCellRenderers.DEFAULT), // $NON-NLS-0$
                new TableColumn(I18n.I.riskClass(), 0.55f, TableCellRenderers.DEFAULT),
                new TableColumn(I18n.I.rating1(), 0.4f, TableCellRenderers.DEFAULT_RIGHT) 
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
        final EDGRatingData rating = this.edg.getResult().getRating();
        add(list, "1", EdgUtil.getEdgRiskClassString("1"), EdgUtil.getEdgClassRating(rating, 1)); // $NON-NLS-0$ $NON-NLS-1$
        add(list, "2", EdgUtil.getEdgRiskClassString("2"), EdgUtil.getEdgClassRating(rating, 2)); // $NON-NLS-0$ $NON-NLS-1$
        add(list, "3", EdgUtil.getEdgRiskClassString("3"), EdgUtil.getEdgClassRating(rating, 3)); // $NON-NLS-0$ $NON-NLS-1$
        add(list, "4", EdgUtil.getEdgRiskClassString("4"), EdgUtil.getEdgClassRating(rating, 4)); // $NON-NLS-0$ $NON-NLS-1$
        add(list, "5", EdgUtil.getEdgRiskClassString("5"), EdgUtil.getEdgClassRating(rating, 5)); // $NON-NLS-0$ $NON-NLS-1$

        getView().update(DefaultTableDataModel.create(list));
    }

    private void add(List<Object[]> list, String risk, String riskText, String rating) {
        list.add(new Object[]{risk, riskText, rating});
    }
}
