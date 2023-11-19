/*
 * FinderFND.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.InstrumentDateItem;
import de.marketmaker.iview.dmxml.MSCCompanyDateFinder;
import de.marketmaker.iview.dmxml.MSCCompanyDateFinderMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractSearchController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FinderCalendar extends
        AbstractFinder<MSCCompanyDateFinder, MSCCompanyDateFinderMetadata> {
    private static final List<Item> P_ALL =
            Arrays.asList(ONE_DAY, ONE_WEEK, ONE_MONTH, THREE_MONTH, SIX_MONTHS, ONE_YEAR, THREE_YEARS, FIVE_YEARS, TEN_YEARS);

    public static final FinderCalendar INSTANCE = new FinderCalendar();


    private FinderCalendar() {
        super("MSC_CompanyDateFinder", DEFAULT_PAGE_SIZE); // $NON-NLS-0$
        this.sortFields.add(new Item(I18n.I.date(), "date")); // $NON-NLS-0$
        this.sortFields.add(new Item("ISIN", "isin")); // $NON-NLS-0$ $NON-NLS-1$
        // this.sortFields.add(new Item(I18n.I.relevance(), "relevance")); // $NON-NLS-0$
        this.sortFields.add(new Item(I18n.I.name(), "name")); // $NON-NLS-0$
        this.sortFields.add(new Item(I18n.I.event(), "event")); // $NON-NLS-0$

        this.pending = new FinderFormConfig(getId(), getId());
        this.pending.put("base", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.pending.put("date", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.pending.put("date-from", getToday()); // $NON-NLS-0$
        this.pending.put("date-to", getTomorrow()); // $NON-NLS-0$
        this.pending.put("date-lb", "1d,"); // $NON-NLS-0$ $NON-NLS-1$
        this.pending.put("sort", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.pending.put("sort-item", "isin"); // $NON-NLS-0$ $NON-NLS-1$
    }

    protected ViewSpec[] getResultViewSpec() {
        return new ViewSpec[]{new ViewSpec(I18n.I.result())};
    }

    public String getId() {
        return "CAL"; // $NON-NLS-0$
    }

    public String getViewGroup() {
        return "finder-cal"; // $NON-NLS-0$
    }

    public void prepareFind(String field1, String value1, String field2, String value2) {
    }


    protected void addSections() {
        final FinderSection section = addSection("base", "", false); // $NON-NLS-0$ $NON-NLS-1$
        section.setAlwaysExpanded();
        section.setValue(true);
        final SymbolOption optionSymbol = new SymbolOption("symbol", I18n.I.instrument(), AbstractSearchController.DEFAULT_COUNT_TYPES, // $NON-NLS$
                SymbolOption.SymbolField.QID, true);
        section.add(optionSymbol);
        asInstrumentDropTarget(optionSymbol);

        section.add(new FinderFormElements.MultiListBoxOption("event", I18n.I.event(), null, "x3")); // x3 == Hauptversammlung (at least at development time ;-) $NON-NLS$
        final StartEndOption optionDate = new StartEndOption("date", "date", I18n.I.date(), "", P_ALL, DateTimeUtil.PeriodMode.FUTURE);  // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        optionDate.setValue(true);
        section.add(optionDate);
        optionDate.setDefault(new MmJsDate(), new MmJsDate().addDays(1));
    }

    private String getToday() {
        return Formatter.LF.formatDate(new Date());
    }

    private String getTomorrow() {
        return Formatter.LF.formatDate(DateTimeUtil.nowPlus(ONE_DAY.value));
    }

    protected TableDataModel createDataModel(int view) {
        final MSCCompanyDateFinder result = this.block.getResult();
        if (result.getItem().isEmpty()) {
            return DefaultTableDataModel.NULL;
        }
        return DefaultTableDataModel.create(result.getItem(), new AbstractRowMapper<InstrumentDateItem>() {
            public Object[] mapRow(InstrumentDateItem item) {
                final QuoteWithInstrument qwi = new QuoteWithInstrument(item.getInstrumentdata(), item.getQuotedata())
                        .withHistoryContext(ItemListContext.createForPortrait(item, result.getItem()));

                return new Object[]{
                        item.getDate(),
                        item.getInstrumentdata().getWkn(),
                        item.getInstrumentdata().getIsin(),
                        qwi,
                        item.getTitle()
                };
            }
        }).withSort(result.getSort());
    }

    protected AbstractFinderView createView() {
        return new FinderCalendarView(this);
    }

    protected Map<String, FinderMetaList> getMetaLists() {
        final MSCCompanyDateFinderMetadata result = this.metaBlock.getResult();
        return Collections.singletonMap("event", result.getEvent()); // $NON-NLS-0$
    }
}
