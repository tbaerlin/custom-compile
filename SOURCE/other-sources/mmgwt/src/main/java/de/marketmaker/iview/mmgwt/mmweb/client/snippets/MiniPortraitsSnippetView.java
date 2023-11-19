/*
 * MiniPortraitsSnippetView.java
 *
 * Created on 10.08.11 09:28
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QwiAndPricedata;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.FlexTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RendererContext;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSelectionViewButtons;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.MiniPortraitWidget;

/**
 * @author Michael Lösch
 */
public class MiniPortraitsSnippetView<V extends BlockType>
        extends SnippetView<AbstractMiniPortraitsSnippet<V>> {

    public enum ViewMode {
        TABLE(I18n.I.table()),
        CHART(I18n.I.charts());

        private final String text;

        public String getText() {
            return text;
        }

        ViewMode(String text) {
            this.text = text;
        }
    }

    private static final String PDF_DIVIDER_INSTRUMENTS = "@";

    private static final String PDF_DIVIDER_FIELDS = "|";

    private FlowPanel flowPanel;

    private Object[][][] additionalLines = null;

    private final List<MiniPortraitWidget> miniPortraits = new ArrayList<>();

    private final RendererContext context = new RendererContext(null, null);

    private Widget expandBtn;

    private final String originalTitle;

    private Boolean expandCollapseState = null;

    private static final String PLUS_STYLE = "x-tool-btn-plus"; // $NON-NLS$

    private static final String MINUS_STYLE = "x-tool-btn-minus"; // $NON-NLS$

    private ViewMode viewMode = ViewMode.CHART;

    private final boolean multiView;

    public MiniPortraitsSnippetView(AbstractMiniPortraitsSnippet<V> snippet) {
        super(snippet);
        this.originalTitle = snippet.getConfiguration().getString("title"); // $NON-NLS$
        this.multiView = snippet.getConfiguration().getBoolean("multiView", false); // $NON-NLS$
        setTitle(this.originalTitle);
    }

    boolean hasContainer() {
        return this.container != null;
    }

    @Override
    protected void onContainerAvailable() {
        final IndexedViewSelectionModel viewSelectionModel = this.snippet.getViewSelectionModel();
        if (viewSelectionModel != null) {
            final FloatingToolbar toolbar = getOrCreateTopToolbar();
            new ViewSelectionViewButtons(this.snippet.getViewSelectionModel(), toolbar);
        }
        if (this.multiView) {
            addViewButton(ViewMode.CHART, ViewMode.TABLE);
        }
        this.expandBtn = this.container.addHeaderTool(MINUS_STYLE, I18n.I.collapse(), headerToolWidget -> {
            final boolean visible = !flowPanel.isVisible();
            setInstrumentPanelVisible(visible, expandBtn);
        });
        if (this.flowPanel == null) {
            this.flowPanel = new FlowPanel();
        }
        this.container.setContentWidget(this.flowPanel);
        handleExpandCollapseState();
        this.snippet.onContainerAvailable();
    }

    void handleExpandCollapseState() {
        final Boolean state = Boolean.parseBoolean(SessionData.INSTANCE.getUserProperty(AppConfig.HIDE_AMAZON));
        if (this.flowPanel != null && !state.equals(this.expandCollapseState)) {
            setInstrumentPanelVisible(!state, this.expandBtn);
            this.expandCollapseState = state;
        }
    }

    private void addViewButton(final ViewMode... modes) {
        this.container.addHeaderTool("x-tool-gear", I18n.I.switchView(), // $NON-NLS$
                headerToolWidget -> onViewChange(getViewMode() == modes[0]
                ? modes[1] : modes[0]));
    }

    private void onViewChange(ViewMode mode) {
        this.viewMode = mode;
        this.snippet.updateView(true);
    }

    public ViewMode getViewMode() {
        return this.viewMode;
    }


    private void setInstrumentPanelVisible(boolean visible, Widget btn) {
        this.flowPanel.setVisible(visible);
        if (this.snippet.getViewSelectionModel() != null) {
            getOrCreateTopToolbar().setVisible(visible);
        }
        this.container.setToolIcon(btn, visible ? MINUS_STYLE : PLUS_STYLE);
        setToolTip(visible);
    }

    private void setToolTip(boolean visible) {
        this.container.setToolTooltip(this.expandBtn, visible ? I18n.I.collapse() : I18n.I.expand());
    }

    public void update(String[] head, Object[][][] additionalLines,
            List<QwiAndPricedata> qwiAndPrices) {
        this.flowPanel.clear();
        final SnippetTableWidget stw;
        final FlexTableDataModel tdm;
        if (qwiAndPrices == null || qwiAndPrices.isEmpty()) {
            stw = new SnippetTableWidget(null);
            tdm = null;
        }
        else {
            int colCount = 0;
            for (QwiAndPricedata qwiAndPrice : qwiAndPrices) {
                if (qwiAndPrice == null) {
                    continue;
                }
                colCount++;
            }
            final List<TableColumn> tabCols = new ArrayList<>(colCount++);
            tabCols.add(new TableColumn("", 0.2f, TableCellRenderers.STRING).withCellClass("mm-snippetTable-label")); // $NON-NLS$
            tdm = new FlexTableDataModel(colCount);
            int row = 0;
            tdm.setValueAt(row++, 0, I18n.I.name());
            tdm.setValueAt(row++, 0, "ISIN"); // $NON-NLS$
            tdm.setValueAt(row++, 0, I18n.I.price());
            final Object[] additionalTitles = getAdditionalTitles(additionalLines);
            for (Object additionalTitle : additionalTitles) {
                tdm.setValueAt(row++, 0, additionalTitle);
            }

            final Object[][] additionalValues = getAdditionalValues(additionalLines);
            int colCounter = 1;
            for (int i = 0, qwiAndPricesSize = qwiAndPrices.size(); i < qwiAndPricesSize; i++) {
                final QwiAndPricedata qwiAndPrice = qwiAndPrices.get(i);
                if (qwiAndPrice == null) {
                    continue;
                }
                tabCols.add(new TableColumn(head[i], 0.8f / head.length, TableCellRenderers.DEFAULT_28));
                row = 0;
                tdm.setValueAt(row++, colCounter, qwiAndPrice);
                tdm.setValueAt(row++, colCounter, qwiAndPrice.getInstrumentData().getIsin());
                tdm.setValueAt(row++, colCounter, renderPrice(qwiAndPrice));
                if (additionalValues.length > 0) {
                    final Object[] values = additionalValues[i];
                    for (Object value : values) {
                        tdm.setValueAt(row++, colCounter, value);
                    }
                }
                colCounter++;
            }
            final DefaultTableColumnModel columnModel = new DefaultTableColumnModel(
                    tabCols.toArray(new TableColumn[tabCols.size()]),
                    isHeadDefined(head));
            stw = new SnippetTableWidget(columnModel);
        }
        stw.updateData(tdm);
        this.flowPanel.add(stw);
        this.container.layout();
    }

    private String renderPrice(IMGResult imgResult) {
        if (imgResult == null) {
            return null;
        }
        return renderPrice(Price.create(imgResult), imgResult.getQuotedata());
    }

    private String renderPrice(QwiAndPricedata qwiP) {
        return renderPrice(Price.create(qwiP.getHasPricedata()), qwiP.getQuoteData());
    }

    private String renderPrice(Price price, QuoteData quote) {
        final StringBuffer sb = new StringBuffer();
        TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH.render(price, sb, this.context);
        sb.append(' ').append(quote.getCurrencyIso());
        sb.append(" (");
        TableCellRenderers.DATE_OR_TIME.render(price.getDate(), sb, this.context);
        sb.append(')');
        return sb.toString();
    }

    private boolean isHeadDefined(String[] head) {
        if (head == null || head.length == 0) return false;
        for (String s : head) {
            if (StringUtil.hasText(s)) {
                return true;
            }
        }
        return false;
    }

    private Object[][] getAdditionalValues(Object[][][] additionalLines) {
        final List<Object[]> result = new ArrayList<>();
        if (additionalLines != null) {
            for (Object[][] additionalLine : additionalLines) {
                final List<Object> values = new ArrayList<>();
                if (additionalLine != null) {
                    for (Object[] objects : additionalLine) {
                        if (objects.length > 1) {
                            values.add(objects[1]);
                        }
                    }
                }
                result.add(values.toArray(new Object[values.size()]));
            }
        }
        return result.toArray(new Object[result.size()][]);
    }


    private Object[] getAdditionalTitles(Object[][][] additionalLines) {
        List<Object> additionalTitles = new ArrayList<>();
        if (additionalLines != null) {
            for (Object[][] additionalLine : additionalLines) {
                if (additionalLine != null && additionalLine.length > 0
                        && additionalLine[0] != null && additionalLine[0].length > 0) {
                    additionalTitles.add(additionalLine[0][0]);
                    break;
                }
            }
        }
        return additionalTitles.toArray(new Object[additionalTitles.size()]);
    }

    public void update(List<IMGResult> imgResults, String[] head, Object[][][] additionalLines) {
        this.flowPanel.clear();
        this.additionalLines = additionalLines;
        if (imgResults == null || imgResults.size() == 0) {
            final HTML html = new HTML();
            html.setStyleName("mm-snippetMessage"); // $NON-NLS$
            html.setText(I18n.I.noDataAvailable());
            this.flowPanel.add(html);
        }
        else {
            this.miniPortraits.clear();
            final StringBuilder pdfParams = new StringBuilder();
            pdfParams.append(this.snippet.getConfiguration().getString("title")); // $NON-NLS$
            for (int i = 0, size = imgResults.size(); i < size; i++) {
                final IMGResult imgResult = imgResults.get(i);
                if (imgResult == null) {
                    continue;
                }
                final MiniPortraitWidget miniPortrait = new MiniPortraitWidget(head[i], "", imgResult);
                this.miniPortraits.add(miniPortrait);
                this.flowPanel.add(miniPortrait);
                updateTable(imgResult, miniPortrait, this.additionalLines == null ? null : this.additionalLines[i]);
                appendPdfParams(pdfParams, head[i], imgResult.getQuotedata().getQid(), this.additionalLines == null ? null : this.additionalLines[i]);
            }
        }
        this.container.layout();
    }

    private void appendPdfParams(StringBuilder pdfParams, String header, String qid,
            Object[][] additionalLines) {
        pdfParams.append(PDF_DIVIDER_INSTRUMENTS);
        pdfParams.append(header == null ? "" : header);
        pdfParams.append(PDF_DIVIDER_FIELDS);
        pdfParams.append(qid);
        if (additionalLines != null) {
            for (Object[] line : additionalLines) {
                pdfParams.append(PDF_DIVIDER_FIELDS);
                pdfParams.append((String) line[0]).append('=').append((String) line[1]);
            }
        }
    }

    private void updateTable(IMGResult imgResult, final MiniPortraitWidget miniPortrait,
            final Object[][] additionalLines) {
        final FlexTableDataModel dtm = new FlexTableDataModel(2);
        dtm.addValues(new Object[]{"ISIN", imgResult.getInstrumentdata().getIsin()}); // $NON-NLS$
        dtm.addValues(new Object[]{I18n.I.price(), renderPrice(imgResult)});
        if (additionalLines != null) {
            for (Object[] values : additionalLines) {
                dtm.addValues(values);
            }
        }
        miniPortrait.update(imgResult.getInstrumentdata().getName(), dtm);
    }

    public void updatePushData(List<IMGResult> imgResults) {
        if (imgResults == null) {
            return;
        }
        try {
            for (int i = 0, instrumentsSize = imgResults.size(); i < instrumentsSize; i++) {
                final IMGResult imgResult = imgResults.get(i);
                if (imgResult == null) {
                    continue;
                }
                updateTable(imgResult, this.miniPortraits.get(i), this.additionalLines == null ? null : this.additionalLines[i]);
            }
        } catch (Exception e) {
            Firebug.error("updatePushData failed", e);
        }
    }

    public void enhanceTitle(String text) {
        if (StringUtil.hasText(this.originalTitle)) {
            this.snippet.getConfiguration().put("title", this.originalTitle + AbstractMainController.INSTANCE.getHeaderSeparator() + text); // $NON-NLS$
        }
        else {
            this.snippet.getConfiguration().put("title", text);  // $NON-NLS$
        }
        reloadTitle();
    }
}