/*
 * ChartcenterSnippetView.java
 *
 * Created on 28.05.2008 11:24:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ExportUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;

import java.util.Map;

/**
 * @author Ulrich Maurer
 */
public class ChartcenterSnippetView extends SnippetView<ChartcenterSnippet>
        implements ErrorHandler {

    private ChartcenterForm chartcenterForm;

    private LabelField errorMessage = new LabelField();

    private Image image = new Image();
    private ImageButton btnPngExport;
    private ImageButton btnTimeseriesExport;
    private Timer btnExportTimer;
    private Panel imagePanel;
    private FocusPanel btnPanel;

    public ChartcenterSnippetView(ChartcenterSnippet snippet) {
        super(snippet);
        final SnippetConfiguration config = getConfiguration();
        final boolean asPriceEarningsChart = config.getBoolean("asPriceEarningsChart", false); // $NON-NLS-0$
        setTitle(asPriceEarningsChart ? I18n.I.priceEarningsChartAbbr() : I18n.I.chart());

        this.image.setVisible(false);

        this.errorMessage.setVisible(false);
        this.chartcenterForm = new ChartcenterForm(config, snippet);

        this.image.addErrorHandler(this);

        this.image.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                setExportButtonsVisible();
            }
        });
        this.image.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                setExportBtnsInvisible(true);
            }
        });

    }

    private void setExportButtonsVisible() {
        if (this.btnPanel == null) {
            return;
        }
        cancelExportBtnsTimer();
        if(btnPngExport != null) {
            btnPngExport.setVisible(true);
        }
        if(btnTimeseriesExport != null) {
            btnTimeseriesExport.setVisible(true);
        }
    }

    private void setExportBtnsInvisible(boolean delayed) {
        if (this.btnPanel == null) {
            return;
        }
        cancelExportBtnsTimer();
        if (delayed) {
            this.btnExportTimer = new Timer() {
                @Override
                public void run() {
                    setExportBtnsInvisible();
                }
            };
            this.btnExportTimer.schedule(100);
        }
        else {
            setExportBtnsInvisible();
        }
    }

    private void setExportBtnsInvisible() {
        this.btnPanel.setFocus(false);
        if (this.btnPngExport != null) {
            this.btnPngExport.setVisible(false);
        }
        if (this.btnTimeseriesExport != null) {
            this.btnTimeseriesExport.setVisible(false);
        }
    }

    protected void onContainerAvailable() {
        super.onContainerAvailable();

        this.container.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent attachEvent) {
                chartcenterForm.onRender();
            }
        });

        if (isPngExportAllowed()) {
            this.btnPngExport = GuiUtil.createImageButton("mm-fileType-chart-image", null, null, I18n.I.exportChartAsPngFile());  // $NON-NLS-0$
            this.btnPngExport.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    setExportBtnsInvisible(false);
                    ExportUtil.exportChartcenterPng(snippet.createParameterMap());
                }
            });

            this.btnPngExport.setVisible(false);
        }

        if (isCsvExportAllowed()) {
            this.btnTimeseriesExport = GuiUtil.createPopupButton("csv-button", I18n.I.exportCompleteTimeSeriesToFile(), // $NON-NLS$
                    new GuiUtil.WidgetCallback(){
                        public Widget getWidget() {
                            final FlowPanel panel = new FlowPanel();
                            final Map<String, String> params = snippet.createParameterMap();
                            panel.add(GuiUtil.createPopupAnchor(I18n.I.exportAsCvsFile(), getCsvUrl(params), "X_CC_CSV")); // $NON-NLS$
                            panel.add(GuiUtil.createPopupAnchor(I18n.I.exportAsXlsFile(), getXlsUrl(params), "X_CC_XLS")); // $NON-NLS$
                            return panel;
                        }
                    });
        }


        final Grid chartGrid = new Grid(1, 2);
        chartGrid.setStyleName("mm-chartcenter"); // $NON-NLS-0$

        this.imagePanel = new AbsolutePanel();
        this.imagePanel.add(this.image);
        this.imagePanel.addStyleName("mm-chartcenter");  // $NON-NLS-0$

        chartGrid.setWidget(0, 0, imagePanel);

        final HTMLTable.CellFormatter cgFormatter = chartGrid.getCellFormatter();
        cgFormatter.setStyleName(0, 0, "mm-chartcenter"); // $NON-NLS-0$

        // only one of errorMessage and image is supposed to be visible; using a single grid cell
        // and switching widgets would not work as an Image not currently attached to a widget
        // does not generate an onLoad event when setUrl is called.
        final Grid grid = new Grid(3, 1);
        grid.setStyleName("mm-chartcenter"); // $NON-NLS-0$
        grid.setWidget(0, 0, this.errorMessage);
        grid.setWidget(1, 0, chartGrid);
        grid.setWidget(2, 0, this.chartcenterForm);

        final HTMLTable.CellFormatter formatter = grid.getCellFormatter();
        formatter.setStyleName(0, 0, "mm-snippet-chart-message"); // $NON-NLS-0$
        formatter.setStyleName(1, 0, "mm-chartcenter"); // $NON-NLS-0$
        formatter.setStyleName(2, 0, "mm-chartcenter"); // $NON-NLS-0$

        this.container.setContentWidget(grid);

        updateImageSize();
    }

    private String getCsvUrl(Map<String, String> params) {
        return ExportUtil.buildTimeseriesUrl("timeseries.csv", params.get("symbol"), params.get("period"), params.get("from"), params.get("to")); // $NON-NLS$
    }

    private String getXlsUrl(Map<String, String> params) {
        return ExportUtil.buildTimeseriesUrl("timeseries.xls", params.get("symbol"), params.get("period"), params.get("from"), params.get("to")); // $NON-NLS$
    }

    public void setError(String errorMessage) {
        this.errorMessage.setText(errorMessage);
        this.errorMessage.setVisible(true);
        this.image.setVisible(false);
    }

    public void setImage(IMGResult ipr, Boolean isIntraday) {
        final String url = ChartUrlFactory.getUrl(ipr.getRequest());
        if (!url.equals(this.image.getUrl())) {
            this.image.setUrl(url);
            onLoad();
        }
        this.chartcenterForm.update(ipr, isIntraday);
    }

    public void setType(InstrumentTypeEnum type) {
        this.chartcenterForm.setType(type);
    }

    public ChartcenterForm getChartcenterForm() {
        return this.chartcenterForm;
    }

    public void onError(ErrorEvent errorEvent) {
        setError(ChartcenterSnippet.CHART_NOT_AVAILABLE);
    }

    public void onLoad() {
        this.errorMessage.setVisible(false);
        this.image.setVisible(true);
        updateImageSize();
    }

    private void updateImageSize() {
        final String width = this.snippet.getChartWidth() + "px"; // $NON-NLS-0$
        final String height = this.snippet.getChartHeight() + "px"; // $NON-NLS-0$
        this.imagePanel.setSize(width, height);
        this.image.setSize(width, height);
        addExportBtns();
    }

    private boolean isPngExportAllowed() {
        return Selector.DZBANK_WEB_INVESTOR.isAllowed() || Selector.DZBANK_WEB_INVESTOR_PUSH.isAllowed();
    }

    private boolean isCsvExportAllowed() {
        return Selector.CSV_EXPORT.isAllowed();
    }

    private void addExportBtns() {
        if (!isPngExportAllowed() && !isCsvExportAllowed()) {
            return;
        }
        this.btnPanel = new FocusPanel();
        this.btnPanel.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                cancelExportBtnsTimer();
            }
        });
        final Panel btnVerticalContainer = new VerticalPanel();
        if (this.btnPngExport != null) {
            btnVerticalContainer.add(this.btnPngExport);
        }
        if (this.btnTimeseriesExport != null) {
            btnVerticalContainer.add(this.btnTimeseriesExport);
        }
        this.btnPanel.add(btnVerticalContainer);
        this.btnPanel.addStyleName("mm-btnPngExportPanel"); // $NON-NLS$

        this.imagePanel.add(btnPanel);
    }

    private void cancelExportBtnsTimer() {
        if (btnExportTimer != null) {
            btnExportTimer.cancel();
        }
    }

    void updatePeriod() {
        this.chartcenterForm.updatePeriod();
    }

    public void updatePercent() {
        this.chartcenterForm.updatePercent();
    }

    public void updateCurrency() {
        this.chartcenterForm.updateCurrency();
    }

    public void setCompareItems(QuoteWithInstrument[] qwis) {
        this.chartcenterForm.setCompareItems(qwis);
    }
}