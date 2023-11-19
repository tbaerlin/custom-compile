package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.util.DateWrapper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.MSCHistoricalRatios;
import de.marketmaker.iview.dmxml.MSCBasicRatiosElement;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * HistoricalPerformanceView.java
 * Created on Nov 26, 2008 11:09:59 AM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class HistoricalPerformanceView extends ContentPanel {

    private final HistoricalPerformanceController controller;
    private final Image image;
    private final SnippetConfiguration config;
    private final Text selectedInstrument;
    private final SnippetTableWidget tablePerformance;
    private final SnippetTableWidget tableHighLow;
    private String linkToChartcenter = null;

    public HistoricalPerformanceView(final HistoricalPerformanceController controller, final SnippetConfiguration config) {
        Firebug.log("HistoricalPerformanceController create"); // $NON-NLS-0$
        addStyleName("mm-contentData"); // $NON-NLS-0$
        setHeaderVisible(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);
        this.controller = controller;
        this.config = config;

        this.tablePerformance = SnippetTableWidget.create(new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.performance(), 0.33f, TableCellRenderers.STRING_CENTER), 
                new TableColumn(I18n.I.averagePrice(), 0.33f, TableCellRenderers.STRING_CENTER), 
                new TableColumn(I18n.I.turnoverAverage(), 0.33f, TableCellRenderers.STRING_CENTER) 
        }));
        this.tableHighLow = SnippetTableWidget.create(new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.high(), 0.25f, TableCellRenderers.STRING_CENTER), 
                new TableColumn(I18n.I.highDate(), 0.25f, TableCellRenderers.STRING_CENTER), 
                new TableColumn(I18n.I.low(), 0.25f, TableCellRenderers.STRING_CENTER), 
                new TableColumn(I18n.I.lowDate(), 0.25f, TableCellRenderers.STRING_CENTER) 
        }));


        FlexTable tab = new FlexTable();
        tab.setStyleName("mm-histPerformance"); // $NON-NLS-0$

        this.image = new Image();
        this.image.setStyleName("mm-chartlink"); // $NON-NLS-0$
        this.image.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (linkToChartcenter != null) {
                    PlaceUtil.goTo(linkToChartcenter);
                }
            }
        });

        this.selectedInstrument = new Text();
        this.selectedInstrument.addStyleName("mm-finder-instrumentName"); // $NON-NLS-0$

        Button btnSelectInst = new Button(I18n.I.selectInstrument(), new SelectionListener<ButtonEvent>() { 
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                showConfigView();
            }
        });
        btnSelectInst.addStyleName("mm-historicalPerformance-right"); // $NON-NLS-0$

        final DateWrapper now = new DateWrapper();

        final DateField from = new DateField();
        from.getPropertyEditor().setFormat(Formatter.FORMAT_ISO_DAY);
        from.setValue(now.add(DateWrapper.Unit.YEAR, -1).asDate());

        final DateField to = new DateField();
        to.getPropertyEditor().setFormat(Formatter.FORMAT_ISO_DAY);
        to.setValue(now.asDate());

        Label lbFrom = new Label(I18n.I.fromUpperCase());
        Label lbTo = new Label(I18n.I.to());
        Label lbChoose = new Label(I18n.I.timeSpan() + ":"); // $NON-NLS$
        Grid g = new Grid(1, 5);
        HTMLTable.CellFormatter cf = g.getCellFormatter();
        g.setStyleName("mm-histPerformance-dateselect"); // $NON-NLS-0$
        g.setWidget(0, 0, lbChoose);
        g.setWidget(0, 1, lbFrom);
        g.setWidget(0, 2, from);
        g.setWidget(0, 3, lbTo);
        cf.addStyleName(0, 4, "mm-historicalPerformance-right"); // $NON-NLS-0$
        g.setWidget(0, 4, to);

        Button btnRepaint = new Button(I18n.I.repaint(), new SelectionListener<ButtonEvent>() { 
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                controller.reload();
            }
        });
        btnRepaint.addStyleName("mm-historicalPerformance-right"); // $NON-NLS-0$

        from.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent baseEvent) {
                config.put("from", Formatter.formatDateAsISODay(from.getValue())); // $NON-NLS-0$
            }
        });
        to.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent baseEvent) {
                config.put("to", Formatter.formatDateAsISODay(to.getValue())); // $NON-NLS-0$
            }
        });

        tab.setWidget(0, 0, this.image);
        FlexTable.FlexCellFormatter fcf = tab.getFlexCellFormatter();
        fcf.setColSpan(0, 0, 2);

        tab.setWidget(1, 0, tablePerformance);
        fcf.setColSpan(1, 0, 2);

        tab.setWidget(2, 0, tableHighLow);
        fcf.setColSpan(2, 0, 2);

        tab.setWidget(3, 0, this.selectedInstrument);
        tab.setWidget(3, 1, btnSelectInst);

        tab.setWidget(4, 0, g);
        tab.setWidget(4, 1, btnRepaint);

        add(tab);
        initDd();
    }

    private void showConfigView() {
        final SnippetConfigurationView configView = new SnippetConfigurationView(this.controller);
        configView.addSelectSymbol(null);
        configView.show();
    }


    private void initDd() {
        final DropTarget dt = new DropTarget(this.selectedInstrument) {
            @Override
            protected void onDragDrop(DNDEvent dndEvent) {
                super.onDragDrop(dndEvent);
                final QuoteWithInstrument qwi = (QuoteWithInstrument) dndEvent.getData();
                controller.setSymbol(null, qwi.getId(), null);
            }
        };
        dt.setGroup("ins"); // $NON-NLS-0$
        dt.setOverStyle("drag-ok"); // $NON-NLS-0$
    }

    private void setContent() {
        this.controller.getContentContainer().setContent(this);
    }

    public void update(IMGResult ipr, MSCHistoricalRatios result) {
        if (this.controller.getContentContainer().getContent() != this) {
            setContent();
        }

        String displayName = result.getInstrumentdata().getName();
        if (result.getQuotedata().getMarketName() != null) {
            displayName += ", " + result.getQuotedata().getMarketName(); // $NON-NLS-0$
        }
        this.setInstrumentName(displayName);

        this.image.setUrl(ChartUrlFactory.getUrl(ipr.getRequest()));
        this.image.setSize(config.getString("width", "600px"), config.getString("height", "260px")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
        this.image.setVisible(true);

        final MSCBasicRatiosElement ratios = result.getData();
        List<RowData> rowsPerformance = new ArrayList<RowData>();
        rowsPerformance.add(new RowData(Renderer.CHANGE_PERCENT.render(ratios.getPerformance()),
                Renderer.PRICE_MAX2.render(ratios.getAveragePrice()) + " " + result.getQuotedata().getCurrencyIso(), // $NON-NLS-0$
                Renderer.PRICE_MAX2.render(ratios.getAverageVolume())));
        TableDataModel data = DefaultTableDataModel.createWithRowData(rowsPerformance);
        tablePerformance.updateData(data);
        tablePerformance.setStyleName("mm-histPerformance-table"); // $NON-NLS-0$

        List<RowData> rowsHighLow = new ArrayList<RowData>();

        rowsHighLow.add(new RowData(Renderer.PRICE_MAX2.render(ratios.getHigh()) + " " + result.getQuotedata().getCurrencyIso(), // $NON-NLS-0$
                DateRenderer.date(null).render(ratios.getHighDate()),
                Renderer.PRICE_MAX2.render(ratios.getLow()) + " " + result.getQuotedata().getCurrencyIso(), // $NON-NLS-0$
                DateRenderer.date(null).render(ratios.getLowDate())));
        data = DefaultTableDataModel.createWithRowData(rowsHighLow);
        tableHighLow.updateData(data);
        tableHighLow.setStyleName("mm-histHighLow-table"); // $NON-NLS-0$

        this.linkToChartcenter = PlaceUtil.getPortraitPlace(ipr.getInstrumentdata(), ipr.getQuotedata(), "C"); // $NON-NLS-0$

        doLayout();
    }

    private void setInstrumentName(String displayName) {
        this.selectedInstrument.setText(displayName);
    }
}
