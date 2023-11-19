/*
 * NewsHeadlinesSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.itools.gwtutil.client.util.date.GwtDateParser;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.InstrumentDateItem;
import de.marketmaker.iview.dmxml.MSCCompanyDateFinder;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModelBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CalendarSnippet extends AbstractSnippet<CalendarSnippet, CalendarSnippetView> implements ModHeightSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("DailyEvents", I18n.I.fixedDates()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new CalendarSnippet(context, config);
        }
    }

    private DmxmlContext.Block<MSCCompanyDateFinder> block;

    private CalendarSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.setView(new CalendarSnippetView(this));

        this.block = createBlock("MSC_CompanyDateFinder"); // $NON-NLS-0$
        this.block.disableRefreshOnRequest();
        this.block.setParameter("sortBy", config.getString("sortBy", "relevance")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        setDateParameter(new MmJsDate());
        this.block.setParameter("count", "100"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("offset", 0); // $NON-NLS-0$
    }

    public DefaultTableDataModel getEmptyDtm() {
        final DefaultTableDataModel dtm = new DefaultTableDataModel(1, 2);
        dtm.setValueAt(0, 0, ""); // $NON-NLS-0$
        dtm.setValueAt(0, 1, ""); // $NON-NLS-0$
        return dtm;
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    private void setDateParameter(MmJsDate date) {
        final String str = JsDateFormatter.formatIsoDay(date);
        this.block.setParameter("date", str); // $NON-NLS-0$
        this.block.setParameter("query", "date=='" + str + "'"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
    }

    public void setDate(MmJsDate date) {
        setDateParameter(date);
        this.contextController.reload();
    }

    public void updateView() {
        if (!block.isResponseOk()) {
            getView().update(getEmptyDtm());
            return;
        }
        final MSCCompanyDateFinder df = this.block.getResult();
        final int rows = Integer.parseInt(df.getCount());

        if (rows == 0) {
            getView().update(getEmptyDtm());
            return;
        }

        getView().setDate(GwtDateParser.getMmJsDate(this.block.getParameter("date"))); // $NON-NLS-0$

        final TableDataModelBuilder builder = new TableDataModelBuilder(rows, 2);

        for (InstrumentDateItem item : df.getItem()) {
            final QuoteWithInstrument qwi = new QuoteWithInstrument(item.getInstrumentdata(), item.getQuotedata())
                    .withHistoryContext(ItemListContext.createForPortrait(item, df.getItem()));
            builder.addRow(qwi, item.getTitle());
        }
        getView().update(builder.getResult());
    }

    public void setHeight(Integer height) {
        this.getView().setHeight(height);
    }
}