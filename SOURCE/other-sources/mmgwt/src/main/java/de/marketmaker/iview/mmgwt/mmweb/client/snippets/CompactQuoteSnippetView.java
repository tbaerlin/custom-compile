/*
 * NewsHeadlinesSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CompactQuoteSnippetView extends SnippetView<CompactQuoteSnippet> implements MarketSelectionButton.Callback {

    private TableColumnModel columnModel;

    private MarketSelectionButton marketButton;

    private Image idImage;

    private Image hImage;

    private final SimplePanel panel;

    final Panel printPanel;

    private SnippetTableWidget tw;

    private Button linkButton;

    private Label symbolsItem;

    private Grid grid;

    public CompactQuoteSnippetView(CompactQuoteSnippet snippet) {
        super(snippet);
        setTitle(I18n.I.compactQuote()); 

        this.columnModel = snippet.getTableColumnModel();

        this.panel = new SimplePanel();
        this.panel.setWidth("100%"); // $NON-NLS-0$
        this.printPanel = new SimplePanel();
        this.printPanel.setStyleName("mm-compactQuote-invisible"); // $NON-NLS-0$
    }

    protected void onContainerAvailable() {
        final FloatingToolbar tb = new FloatingToolbar();
        this.marketButton = new MarketSelectionButton(this, 12);
        tb.add(this.marketButton.asWidget());

        tb.addSeparator();
        this.linkButton = Button.icon("mm-compactQuoteJumpPortrait") // $NON-NLS$
                .tooltip(I18n.I.tooltipGotoPortrait())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        CompactQuoteSnippetView.this.snippet.goToPortrait();
                    }
                })
                .build();
        this.linkButton.setEnabled(false);
        tb.add(this.linkButton);

        tb.addSeparator();
        this.symbolsItem = tb.addLabel("");

        this.container.setTopWidget(tb);
        this.container.setContentWidget(this.panel);
    }

    public void disableQuotesMenu() {
        this.marketButton.setEnabled(false);
    }

    public void update(String msg) {
        this.panel.setWidget(new Label(msg));
        this.tw = null;
        this.idImage = null;
        this.hImage = null;
        this.linkButton.setEnabled(false);
    }

    public void updateQuote(String qid) {
        this.snippet.updateQuote(qid);
    }

    public void updateQuotesMenu(List<QuoteData> quotedata, QuoteData selected) {
        this.marketButton.updateQuotesMenu(quotedata, selected);
    }

    void updateSymbols(String wkn, String isin) {
        final StringBuilder sb = new StringBuilder();
        if (wkn != null && SessionData.INSTANCE.isShowWkn()) {
            sb.append("WKN ").append(wkn); // $NON-NLS-0$
        }
        if (isin != null && SessionData.INSTANCE.isShowIsin()) {
            sb.append(sb.length() > 0 ? " | " : "").append("ISIN ").append(isin); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        }
        setSymbolsItem(sb.toString());
    }

    void update(TableDataModel dtm, TableColumnModel tcm, String idRequest, String hRequest) {
        final VerticalPanel vp = new VerticalPanel();
        this.columnModel = tcm;
        if (this.tw == null) {
            this.tw = SnippetTableWidget.create(this.columnModel);
            this.idImage = new Image();
            this.hImage = new Image();
            vp.add(this.printPanel);
            vp.add(this.tw);

            this.grid = new Grid(2, 2);
            this.grid.setCellPadding(0);
            this.grid.setCellSpacing(0);
            this.grid.setStyleName("mm-compactQuoteChart"); // $NON-NLS-0$
            this.grid.setWidth("100%"); // $NON-NLS-0$
            this.grid.setText(0, 0, I18n.I.chartTitleIntraday()); 
            this.grid.setWidget(1, 0, this.idImage);
            this.grid.setText(0, 1, I18n.I.nYears(1)); 
            this.grid.setWidget(1, 1, this.hImage);
            vp.add(this.grid);
            this.panel.setWidget(vp);
        }

        this.tw.updateData(dtm);
        if (idRequest != null) {
            this.idImage.setUrl(ChartUrlFactory.getUrl(idRequest));
            this.grid.setWidget(1, 0, this.idImage);
        }
        else {
            this.grid.setWidget(1, 0, new HTML(I18n.I.notActivatedTwoline())); 
        }
        if (hRequest == null) {
            this.grid.setText(1, 1, "");
        }
        else {
            this.grid.setWidget(1, 1, this.hImage);
            this.hImage.setUrl(ChartUrlFactory.getUrl(hRequest));
        }
        this.linkButton.setEnabled(true);
    }

    private void setSymbolsItem(String symbols) {
        this.symbolsItem.setText(symbols);
        final Label lb = new Label(symbols);
        this.printPanel.clear();
        this.printPanel.add(lb);
    }

    public ArrayList<PushRenderItem> getRenderItems(TableDataModel tdm) {
        return this.tw.getRenderItems(tdm);
    }
}
