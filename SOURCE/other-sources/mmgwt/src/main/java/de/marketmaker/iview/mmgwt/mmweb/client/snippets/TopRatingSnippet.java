/*
 * BestToolSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.iview.dmxml.FNDFinder;
import de.marketmaker.iview.dmxml.FNDFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TopRatingSnippet extends AbstractSnippet<TopRatingSnippet, SnippetTableView<TopRatingSnippet>>
        implements LinkListener {

    public static class Class extends SnippetClass {
        public Class() {
            super("TopRating"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new TopRatingSnippet(context, config);
        }
    }

    private DmxmlContext.Block<FNDFinder> block;
    private List<SymbolSnippet> symbolSnippets = new ArrayList<>();

    private TopRatingSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.setView(new SnippetTableView<>(this, new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.name(), 0.6f, TableCellRenderers.QUOTELINK_22), 
                new TableColumn(I18n.I.type(), 0.3f, TableCellRenderers.STRING), 
                new TableColumn(I18n.I.ratingAbbr(), 0.1f, TableCellRenderers.STRING_RIGHT) 
        })));
        this.block = createBlock("FND_Finder"); // $NON-NLS-0$
        this.block.setParameter("query", config.getString("query", "vrIssuer=='true'")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.block.setParameter("offset", "0"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("count", config.getString("count", "10")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.block.setParameter("sortBy", "ratingMorningstar"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("ascending", "false"); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void addSymbolSnippet(SymbolSnippet s) {
        this.symbolSnippets.add(s);
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public boolean isConfigurable() {
        return true;
    }

    public void updateView() {
        if (!block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        final FNDFinder result = this.block.getResult();
        final List<Object[]> list = new ArrayList<>();
        for (FNDFinderElement e : result.getElement()) {
            final QuoteWithInstrument qwi = new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
            list.add(new Object[]{
                    qwi,
                    e.getFundtype(),
                    e.getRatingMorningstar()
            });
        }
        final TableDataModel tdm = DefaultTableDataModel.create(list);
        getView().update(tdm);
    }


    public void onClick(LinkContext context, Element e) {
        for (SymbolSnippet s : this.symbolSnippets) {
            final QuoteWithInstrument qwi = (QuoteWithInstrument) context.data;
            s.setSymbol(InstrumentTypeEnum.valueOf(qwi.getInstrumentData().getType()), qwi.getQuoteData().getQid(), null);
        }
        ackParametersChanged();
    }
}
