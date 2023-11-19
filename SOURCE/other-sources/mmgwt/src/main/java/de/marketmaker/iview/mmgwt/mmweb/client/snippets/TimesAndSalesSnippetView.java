/*
 * TimesAndSalesSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.SimplePanel;

import de.marketmaker.itools.gwtutil.client.util.date.DateListener;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.Separator;
import de.marketmaker.iview.dmxml.OHLCVTimeseriesElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.datepicker.ToolbarDateButton;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TimesAndSalesSnippetView extends SnippetView<TimesAndSalesSnippet> {
    private final LinkListener<OHLCVTimeseriesElement> listener = new LinkListener<OHLCVTimeseriesElement>() {
        public void onClick(LinkContext<OHLCVTimeseriesElement> context, Element e) {
            snippet.goDown(context.data);
        }
    };

    private final TableCellRenderer timeLinkRenderer = new TableCellRenderer() {
        public void render(Object data, StringBuffer sb, Context context) {
            final OHLCVTimeseriesElement e = (OHLCVTimeseriesElement) data;
            context.appendLink(new LinkContext<>(listener, e), snippet.getTimeLabel(e.getDate()), null, sb);
        }

        public boolean isPushRenderer() {
            return false;
        }

        public String getContentClass() {
            return null;
        }
    };

    private final TableColumnModel columnModelOhlcv
            = new DefaultTableColumnModel(new TableColumn[]{
            new TableColumn(I18n.I.time(), 0.3f, timeLinkRenderer),
            new TableColumn(I18n.I.openPrice(), 0.1f, TableCellRenderers.PRICE),
            new TableColumn(I18n.I.high(), 0.1f, TableCellRenderers.PRICE),
            new TableColumn(I18n.I.low(), 0.1f, TableCellRenderers.PRICE),
            new TableColumn(I18n.I.close(), 0.1f, TableCellRenderers.PRICE),
            new TableColumn(I18n.I.volume(), 0.2f, TableCellRenderers.VOLUME_LONG),
            new TableColumn(I18n.I.ticks(), 0.1f, TableCellRenderers.STRING_RIGHT)
    });

    private final TableColumnModel columnModelTicks
            = new DefaultTableColumnModel(new TableColumn[]{
            new TableColumn(I18n.I.time(), 0.5f, TableCellRenderers.FULL_TIME_LEFT),
            new TableColumn(I18n.I.price(), 0.2f, TableCellRenderers.PRICE),
            new TableColumn("", 0.1f, TableCellRenderers.STRING), // $NON-NLS-0$
            new TableColumn(I18n.I.volume(), 0.2f, TableCellRenderers.STRING_RIGHT), // don't use TableCellRenderers.VOLUME -> display exact value
    });

    private final TableColumnModel columnModelTicksLme
            = new DefaultTableColumnModel(new TableColumn[]{
            new TableColumn(I18n.I.time(), 0.4f, TableCellRenderers.FULL_TIME_LEFT),
            new TableColumn(I18n.I.price(), 0.2f, TableCellRenderers.PRICE),
            new TableColumn("", 0.1f, TableCellRenderers.STRING), // $NON-NLS-0$
            new TableColumn(I18n.I.volume(), 0.1f, TableCellRenderers.STRING_RIGHT), // don't use TableCellRenderers.VOLUME -> display exact value
            new TableColumn(I18n.I.lmeField(), 0.2f, TableCellRenderers.STRING),
    });

    private SnippetTableWidget twOhlcv;

    private SnippetTableWidget twTicks;

    private SnippetTableWidget twTicksLme;

    private final SimplePanel panel = new SimplePanel();

    private ToolbarDateButton dateButton;

    private Button upButton;


    public TimesAndSalesSnippetView(TimesAndSalesSnippet snippet) {
        super(snippet);
        setTitle(getConfiguration().getString("title", I18n.I.timesAndSalesAbbrWithoutSpace()));  // $NON-NLS-0$

        this.panel.setWidth("100%"); // $NON-NLS-0$
    }

    protected void onContainerAvailable() {
        final MmJsDate now = new MmJsDate().atMidnight();
        this.dateButton = new ToolbarDateButton(now);
        this.dateButton.setMaxDate(now);
        this.dateButton.setMinDate(new MmJsDate(now).addDays(-100));

        this.upButton = Button.icon("x-tbar-page-prev") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        snippet.goUp();
                    }
                })
                .build();
        this.upButton.setEnabled(false);

        final FloatingToolbar tb = new FloatingToolbar();
        tb.add(this.dateButton);
        tb.addSeparator();
        tb.add(this.upButton);
        this.container.setTopWidget(tb);

        this.dateButton.addListener(new DateListener() {
            public void setDate(MmJsDate date) {
                snippet.setDate(date);
            }
        });

        this.container.setContentWidget(this.panel);
    }

    void updateOHLCV(TableDataModel tdm) {
        if (this.twOhlcv == null) {
            this.twOhlcv = SnippetTableWidget.create(columnModelOhlcv);
        }
        this.twOhlcv.updateData(tdm);
        setWidgetIfNotSet(this.twOhlcv);
    }

    void updateTicks(TableDataModel tdm, boolean isLme) {
        SnippetTableWidget tw;
        // TODO: only show additional columns if data exists
        if (isLme) {
            if (twTicksLme == null) {
                this.twTicksLme = SnippetTableWidget.create(columnModelTicksLme);
            }
            tw = this.twTicksLme;
        }
        else {
            if (this.twTicks == null) {
                this.twTicks = SnippetTableWidget.create(columnModelTicks);
            }
            tw = this.twTicks;
        }
        tw.updateData(tdm);
        setWidgetIfNotSet(tw);
    }

    private void setWidgetIfNotSet(final SnippetTableWidget w) {
        if (this.panel.getWidget() != w) {
            this.panel.setWidget(w);
        }
    }

    void setDate(MmJsDate date) {
        this.dateButton.setDate(date);
    }

    public void disableUp() {
        if (this.upButton != null) {
            this.upButton.setEnabled(false);
        }
    }

    public void enableUp() {
        if (this.upButton != null) {
            this.upButton.setEnabled(true);
        }
    }
}
