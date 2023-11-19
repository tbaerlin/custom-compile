/*
 * ArbitragePresenter.java
 *
 * Created on 17.10.13 15:56
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Markus Dick
 */
public class ArbitragePresenter implements AsyncCallback<ResponseType> {
    public final static String AS_OE_STYLE = "as-oe"; //$NON-NLS$
    public final static String ARBITRAGE_POPUP_STYLE = "arbitragePopup"; //$NON-NLS$
    private final DmxmlContext dmxmlContext;
    private final SymbolSnippet symbolSnippet;
    private final Snippet snippet;
    private final ContentPanel contentPanel;
    private final PopupPanel popupPanel;

    public ArbitragePresenter() {
        this.dmxmlContext = new DmxmlContext();
        final SnippetConfiguration sc = new SnippetConfiguration("Arbitrage") //$NON-NLS$
                .with("linkType", "MARKET_NO_LINK"); //$NON-NLS$

        this.snippet = SnippetClass.create(this.dmxmlContext, sc);
        this.symbolSnippet = (SymbolSnippet) this.snippet;

        this.contentPanel = new ContentPanel();
        this.contentPanel.setHeaderVisible(false);
        this.contentPanel.setFooter(false);

        this.popupPanel = new PopupPanel();
        this.popupPanel.addStyleName(AS_OE_STYLE);
        this.popupPanel.addStyleName(ARBITRAGE_POPUP_STYLE);
        this.popupPanel.setModal(true);
        this.popupPanel.setAutoHideEnabled(true);

        final ScrollPanel sp = new ScrollPanel();
        sp.add(this.contentPanel);
        this.popupPanel.add(sp);

        this.popupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                ArbitragePresenter.this.snippet.deactivate();
            }
        });

        this.snippet.getView().setContainer(this.contentPanel);
        this.contentPanel.layout(true);
    }

    public void show() {
        this.snippet.activate();
        this.dmxmlContext.issueRequest(this);
    }

    public void setSymbol(String symbol) {
        this.symbolSnippet.setSymbol(null, symbol, null);
    }

    public void dispose() {
        this.snippet.destroy();
    }

    private void updateViewAndShow() {
        this.snippet.updateView();
        this.contentPanel.setHeaderVisible(false);
        this.contentPanel.setFooter(false);
        this.contentPanel.layout(true);
        this.popupPanel.center();
    }

    @Override
    public void onFailure(Throwable caught) {
        updateViewAndShow();
    }

    @Override
    public void onSuccess(ResponseType result) {
        updateViewAndShow();
    }
}
