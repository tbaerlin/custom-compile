/*
 * PdfSnippet.java
 *
 * Created on 17.06.2008 12:35:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.Window;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.DesktopIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Ulrich Maurer
 */
public class SymbolLinkSnippet extends AbstractSnippet<SymbolLinkSnippet, DesktopSnippetView<SymbolLinkSnippet>> implements SymbolSnippet, IsVisible {
    public static class Class extends SnippetClass {
        public Class() {
            super("SymbolLink"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new SymbolLinkSnippet(context, config);
        }
    }

    private DmxmlContext.Block<MSCQuoteMetadata> block;
    private final String checkUri;
    private final String linkUri;
    private final String iconStyle;
    private final String iconTitle;

    private final boolean debug;

    private boolean visible = false;

    private SymbolLinkSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.block = context.addBlock("MSC_QuoteMetadata"); // $NON-NLS-0$

        this.checkUri = config.getString("checkUri"); // $NON-NLS-0$
        this.linkUri = config.getString("linkUri"); // $NON-NLS-0$
        this.iconStyle = config.getString("iconStyle", "mm-desktopIcon-pdf"); // $NON-NLS-0$ $NON-NLS-1$
        this.iconTitle = config.getString("iconTitle"); // $NON-NLS-0$

        this.debug = "piaolb".equals(Window.Location.getParameter("debug")); // $NON-NLS-0$ $NON-NLS-1$

        setView(new DesktopSnippetView<SymbolLinkSnippet>(this));
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
        setVisible(false);
        getView().update(new ArrayList<DesktopIcon>(0));
    }

    public void destroy() {
        this.context.removeBlock(this.block);
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            Firebug.log("no metadata available for symbol: " + this.block.getParameter("symbol")); // $NON-NLS-0$ $NON-NLS-1$
            return;
        }

        final String wkn = this.block.getResult().getInstrumentdata().getWkn();
        final String checkUri = this.checkUri.replace("{wkn}", wkn); // $NON-NLS-0$
        final String linkUri = this.linkUri.replace("{wkn}", wkn); // $NON-NLS-0$
        
        sendCheckRequest(checkUri, new CheckCallback(){
            public void onAvailable() {
                if (debug) {
                    showDebugLink(checkUri, linkUri);
                }
                else {
                    onLinkAvailable(linkUri);
                }
                setVisible(true);
            }

            public void onError() {
                if (debug) {
                    showDebugLink(checkUri, null);
                }
                setVisible(debug);
//                setVisible(false);
            }
        });
    }

    interface CheckCallback {
        void onAvailable();
        void onError();
    }

    private native void sendCheckRequest(String checkUri, CheckCallback checkCallback) /*-{
        var img = new Image();
        img.onerror = function() {
            checkCallback.@de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolLinkSnippet.CheckCallback::onError()();
        };
        img.onload = function() {
            checkCallback.@de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolLinkSnippet.CheckCallback::onAvailable()();
        };
        img.src = checkUri;
    }-*/;

    private void onLinkAvailable(final String linkUri) {
        final List<DesktopIcon> list = new ArrayList<DesktopIcon>(1);
        list.add(new DesktopIcon(this.iconStyle, new String[]{this.iconTitle}, linkUri));
        getView().update(list);
    }

    private void showDebugLink(final String checkUri, final String linkUri) {
        final List<DesktopIcon> list = new ArrayList<DesktopIcon>(2);
        if (checkUri != null) {
            list.add(new DesktopIcon("mm-desktopIcon-html", new String[]{I18n.I.checkUrl()}, checkUri));  // $NON-NLS-0$
        }
        if (linkUri != null) {
            list.add(new DesktopIcon(this.iconStyle, new String[]{this.iconTitle}, linkUri));
        }
        getView().update(list);
    }

    public void setVisible(boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;
            this.contextController.handleVisibility();
        }
    }

    public boolean isVisible() {
        return this.visible;
    }


}
