/*
 * TopFlopSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.WNTFinder;
import de.marketmaker.iview.dmxml.WNTFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MostActiveWNTSnippet extends
        AbstractSnippet<MostActiveWNTSnippet, SnippetTableView<MostActiveWNTSnippet>> {

    public static class Class extends SnippetClass {
        public Class() {
            super("MostActiveWNT"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new MostActiveWNTSnippet(context, config);
        }
    }

    private static QuoteWithInstrument toQwi(WNTFinderElement e) {
        return new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
    }

    private DmxmlContext.Block<WNTFinder> block;

    protected MostActiveWNTSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("WNT_Finder"); // $NON-NLS$
        this.block.setParameter("query", config.getString("query")); // $NON-NLS$
        this.block.setParameter("sortBy", config.getString("sortBy", "averageVolume1w")); // $NON-NLS$
        this.block.setParameter("ascending", config.getString("ascending", "false")); // $NON-NLS$
        this.block.setParameter("providerPreference", SessionData.INSTANCE.getGuiDefValue("providerPreference")); // $NON-NLS$

        setView(new SnippetTableView<MostActiveWNTSnippet>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn(I18n.I.name(), 0.5f, TableCellRenderers.QUOTELINK_32), 
                        new TableColumn(I18n.I.priceValue(), 0.16f, TableCellRenderers.PRICE), 
                        new TableColumn("+/-%", 0.16f, TableCellRenderers.CHANGE_PERCENT), // $NON-NLS-0$
                        new TableColumn(I18n.I.performance1WAbbr(), 0.16f, TableCellRenderers.PERCENT) 
                })));
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        final WNTFinder result = this.block.getResult();
        final DefaultTableDataModel model = DefaultTableDataModel.create(result.getElement(),
                new AbstractRowMapper<WNTFinderElement>() {
                    public Object[] mapRow(WNTFinderElement e) {
                        final QuoteWithInstrument qwi = toQwi(e).withHistoryContext(
                                ItemListContext.createForWntPortrait(e, result.getElement(), getView().getTitle())
                        );
                        return new Object[]{
                                qwi,
                                e.getPrice(),
                                e.getChangePercent(),
                                e.getPerformance1W()
                        };
                    }
                });
        getView().update(model);
    }
}
