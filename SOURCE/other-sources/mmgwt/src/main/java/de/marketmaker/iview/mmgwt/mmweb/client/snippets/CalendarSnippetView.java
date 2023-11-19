/*
 * NewsHeadlinesSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import de.marketmaker.itools.gwtutil.client.util.date.DateListener;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.VwdPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.datepicker.ToolbarDateButton;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CalendarSnippetView extends SnippetView<CalendarSnippet> {
    private TableColumnModel columnModel;

    private SnippetTableWidget tw;

    private ToolbarDateButton dateButton;

    protected final Panel panel;

    private ScrollPanel sp;

    private Panel printPanel;

    public CalendarSnippetView(CalendarSnippet calendarSnippet) {
        super(calendarSnippet);
        setTitle(I18n.I.fixedDates());

        final TableColumn[] columns = new TableColumn[2];
        columns[0] = new TableColumn(I18n.I.companys(), 100);
        columns[0].setRenderer(new TableCellRenderers.QuoteLinkRenderer(16, "-")); // $NON-NLS-0$

        columns[1] = new TableColumn(I18n.I.event(), 160);
        columns[1].setRenderer(new TableCellRenderers.MaxLengthStringRenderer(100, "-")); // $NON-NLS-0$

        this.columnModel = new DefaultTableColumnModel(columns);

        this.panel = new VerticalPanel();
        this.panel.setWidth("100%"); // $NON-NLS-0$

        this.printPanel = new SimplePanel();
        this.printPanel.setStyleName("mm-calendar-date-invisible"); // $NON-NLS-0$
    }

    protected void onContainerAvailable() {
        super.onContainerAvailable();

        final MmJsDate defaultDate = new MmJsDate().atMidnight();
        this.dateButton = new ToolbarDateButton(defaultDate);
        final FloatingToolbar tb = new FloatingToolbar();
        tb.add(this.dateButton);

        if (Selector.PAGES_VWD.isAllowed()) {
            tb.addFill();
            tb.add(Button.text(I18n.I.dayPreview())
                    .clickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            PlaceUtil.goTo(VwdPageController.KEY + "/9127"); // $NON-NLS$
                        }
                    })
                    .build());
        }

        this.container.setTopWidget(tb);
        this.container.setContentWidget(this.panel);

        this.dateButton.addListener(new DateListener() {
            public void setDate(MmJsDate date) {
                snippet.setDate(date);
            }
        });

        this.snippet.setDate(defaultDate);
    }

    void update(TableDataModel dtm) {
        if (this.tw == null) {
            this.tw = SnippetTableWidget.create(this.columnModel);
//            add(this.tw);

            this.panel.add(this.printPanel);

            final String height = getConfiguration().getString("height", null); // $NON-NLS-0$
            if (height != null) {
                this.sp = new ScrollPanel(this.tw);
                this.sp.setHeight(height);
                this.panel.add(this.sp);
            }
            else {
                this.panel.add(this.tw);
            }
        }
        this.tw.updateData(dtm);
    }

    void setDate(MmJsDate date) {
        final String sDate = JsDateFormatter.formatDdmmyyyy(date);
        this.dateButton.setText(sDate);
        final LabelField lb = new LabelField(sDate);
        this.printPanel.clear();
        this.printPanel.add(lb);
    }

    public void setHeight(Integer height) {
        if (this.sp != null && height != null) {
            this.sp.setHeight(height + "px");  // $NON-NLS$
        }
    }
}
