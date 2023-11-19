/*
 * FinderAnalysisView.java
 *
 * Created on 11.06.2008 13:43:14
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentViewAdapter;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FinderCalendarView extends AbstractFinderView<FinderCalendar> implements ContentContainer {

    private ContentView lastContentView;

    FinderCalendarView(FinderCalendar controller) {
        super(controller);
    }

    @Override
    protected int getNumRowsInDataGrid() {
        return 4;
    }

    @Override
    protected void initColumnModels(TableColumnModel[] columnModels) {
        columnModels[0] = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.date(), 0.1f, DATE, "date") // $NON-NLS-0$
                , new TableColumn("WKN", 0.1f, DEFAULT) // $NON-NLS-0$
                , new TableColumn("ISIN", 0.1f, DEFAULT, "isin") // $NON-NLS-0$ $NON-NLS-1$
                , new TableColumn(I18n.I.name(), 0.4f, QUOTELINK_32, "name") // $NON-NLS-0$
                , new TableColumn(I18n.I.event(), 0.4f, DEFAULT, "event") // $NON-NLS-0$
        });
    }

    @Override
    public void setContent(ContentView contentView) {
        if (this.lastContentView != null && this.lastContentView != contentView) {
            this.lastContentView.onBeforeHide();
        }
        this.lastContentView = contentView;

        this.g.setWidget(2, 0, contentView.getWidget());
    }

    @Override
    public void setContent(Widget w) {
        setContent(new ContentViewAdapter(w));
    }

    @Override
    public Widget getContent() {
        return this.lastContentView == null ? null : this.lastContentView.getWidget();
    }

    @Override
    public boolean isShowing(Widget w) {
        return this.lastContentView != null && lastContentView.getWidget() == w;
    }
}
