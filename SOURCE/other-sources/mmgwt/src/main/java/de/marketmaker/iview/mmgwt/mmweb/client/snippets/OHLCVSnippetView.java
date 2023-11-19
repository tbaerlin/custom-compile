/*
 * OHLCVSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.date.DateListener;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRendererAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ExportUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.datepicker.ToolbarDateButton;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PRICE;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.VOLUME_LONG;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OHLCVSnippetView extends SnippetView<OHLCVSnippet> {
    private final LinkListener<String> listener = new LinkListener<String>() {
        public void onClick(LinkContext<String> context, Element e) {
            snippet.dateClicked(context.data);
        }
    };

    private TableColumnModel columnModel;

    private final SimplePanel panel = new SimplePanel();

    private SnippetTableWidget table;

    private ToolbarDateButton dateButton;

    private final MmJsDate initialDate;

    private final TableCellRenderer dateLinkRenderer;

    public OHLCVSnippetView(OHLCVSnippet topFlopSnippet, MmJsDate initialDate) {
        super(topFlopSnippet);
        final SnippetConfiguration config = getConfiguration();
        setTitle(config.getString("title", I18n.I.historicOhlcv()));  // $NON-NLS-0$

        this.initialDate = initialDate;
        this.panel.setWidth("100%"); // $NON-NLS-0$

        this.dateLinkRenderer = new TableCellRendererAdapter() {
            public void render(Object data, StringBuffer sb, Context context) {
                final String start = (String) data;
                context.appendLink(new LinkContext<>(listener, start), Formatter.LF.formatDate(start), null, sb);
            }

            public String getContentClass() {
                return null;
            }
        };

    }

    protected void onContainerAvailable() {
        final SnippetConfiguration config = getConfiguration();

        this.dateButton = new ToolbarDateButton(this.initialDate);
        this.dateButton.setMaxDate(new MmJsDate().atMidnight().addDays(-1));

        final FloatingToolbar tb = new FloatingToolbar();
        tb.add(this.dateButton);

        if (Selector.CSV_EXPORT.isAllowed()) {
            final ImageButton exportTSButton = GuiUtil.createPopupButton("csv-button", I18n.I.exportCompleteTimeSeriesToFile(), // $NON-NLS$
                    new GuiUtil.WidgetCallback(){
                        public Widget getWidget() {
                            final FlowPanel panel = new FlowPanel();
                            panel.add(GuiUtil.createPopupAnchor(I18n.I.exportAsCvsFile(), getCsvUrl(), getCsvActionToken()));
                            panel.add(GuiUtil.createPopupAnchor(I18n.I.exportAsXlsFile(), getXlsUrl(), getXlsActionToken()));
                            return panel;
                        }
                    });
            tb.addSeparator();
            tb.add(exportTSButton);
        }

        this.container.setTopWidget(tb);

        this.dateButton.addListener(new DateListener() {
            public void setDate(MmJsDate date) {
                snippet.setDate(date);
            }
        });
        this.snippet.setDate(this.initialDate);

        this.container.setContentWidget(this.panel);
    }

    private String getCsvActionToken() {
        return "X_TS_CSV"; // $NON-NLS$
    }

    private String getCsvUrl() {
        final String symbol = getConfiguration().getString("symbol"); // $NON-NLS$
        return ExportUtil.buildTimeseriesUrl("timeseries.csv", symbol, null, null, null); // $NON-NLS$
    }

    private String getXlsActionToken() {
        return "X_TS_XLS"; // $NON-NLS$
    }

    private String getXlsUrl() {
        final String symbol = getConfiguration().getString("symbol"); // $NON-NLS$
        return ExportUtil.buildTimeseriesUrl("timeseries.xls", symbol, null, null, null); // $NON-NLS$
    }

    public void setColumnModel(DefaultTableColumnModel columnModel) {
        this.columnModel = columnModel;
    }

    public void update(TableDataModel tdm) {
        reloadTitle();
        if (this.table == null) {
            this.table = SnippetTableWidget.create(columnModel);
            this.panel.add(this.table);
            this.container.layout();
        }

        final TableCellRenderer dateRenderer = (this.snippet.isEndOfDay() || !Selector.TIMES_AND_SALES.isAllowed())
                ? TableCellRenderers.DATE : this.dateLinkRenderer;
        this.columnModel.getTableColumn(0).setRenderer(dateRenderer);
        this.table.updateData(tdm);
    }

    public void setDate(MmJsDate date) {
        this.dateButton.setDate(date);
    }

    DefaultTableColumnModel createOHLCVColumnModel() {
        return new DefaultTableColumnModel(
                new TableColumn[]{
                        new TableColumn(I18n.I.date(), 0.2f, this.dateLinkRenderer),
                        new TableColumn(I18n.I.openPrice(), 0.15f, PRICE),
                        new TableColumn(I18n.I.high(), 0.15f, PRICE),
                        new TableColumn(I18n.I.low(), 0.15f, PRICE),
                        new TableColumn(I18n.I.close(), 0.15f, PRICE),
                        new TableColumn(I18n.I.volume(), 0.2f, VOLUME_LONG)
                }
        );
    }

    DefaultTableColumnModel createFundColumnModel() {
        return new DefaultTableColumnModel(
                new TableColumn[]{
                        new TableColumn(I18n.I.date(), 0.4f, this.dateLinkRenderer),
                        new TableColumn(I18n.I.issuePrice2(), 0.3f, PRICE),
                        new TableColumn(I18n.I.redemption(), 0.3f, PRICE)
                }
        );
    }

    DefaultTableColumnModel createMetalsColumnModel() {
        DefaultTableColumnModel result = new DefaultTableColumnModel(
                new TableColumn[]{
                        new TableColumn(I18n.I.date(), 0.08f, this.dateLinkRenderer),
                        new TableColumn(I18n.I.openPrice(), 0.08f, PRICE),
                        new TableColumn(I18n.I.high(), 0.08f, PRICE),
                        new TableColumn(I18n.I.low(), 0.08f, PRICE),
                        new TableColumn(I18n.I.close(), 0.08f, PRICE),
                        new TableColumn(I18n.I.volume(), 0.08f, PRICE),

                        new TableColumn(I18n.I.bid(), 0.08f, PRICE),  // Official
                        new TableColumn(I18n.I.ask(), 0.08f, PRICE),
                        new TableColumn(I18n.I.bid(), 0.08f, PRICE),  // Unofficial
                        new TableColumn(I18n.I.ask(), 0.08f, PRICE),
                        new TableColumn(I18n.I.evaluation2(), 0.08f, PRICE), // Interpolated Closing
                        new TableColumn(I18n.I.evaluation2(), 0.08f, PRICE), // Provisional Evaluations
                }
        );
        result.groupColumns(6,8, I18n.I.official());
        result.groupColumns(8,10, I18n.I.unofficial());
        result.groupColumns(10,11, I18n.I.interpolated());
        result.groupColumns(11,12, I18n.I.provisional());
        return result;
    }

}
