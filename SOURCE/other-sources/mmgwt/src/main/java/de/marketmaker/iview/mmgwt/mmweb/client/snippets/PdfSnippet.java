/*
 * PdfSnippet.java
 *
 * Created on 17.06.2008 12:35:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.DesktopIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Ulrich Maurer
 */
public class PdfSnippet extends
        AbstractSnippet<PdfSnippet, DesktopSnippetView<PdfSnippet>> implements SymbolSnippet, PdfUriSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("Pdf"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PdfSnippet(context, config);
        }
    }

    private String symbol = null;

    private PdfSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        setView(new DesktopSnippetView<PdfSnippet>(this));
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.symbol = symbol;
        final List<DesktopIcon> list = new ArrayList<DesktopIcon>(1);
        list.add(new DesktopIcon("mm-desktopIcon-pdf", new String[]{I18n.I.pdfPortrait()}, PdfOptionHelper.getPdfUri(getPdfOptionSpec(), null)));  // $NON-NLS-0$
        getView().update(list);
    }

    public boolean isTopToolbarUri() {
        return true;
    }

    public String getPdfUri() {
        Firebug.log("no longer supported: PdfSnippet.getPdfUri()"); // $NON-NLS-0$
        return UrlBuilder.forPdf(getConfiguration().getString("linkFile", "stockportrait.pdf")) // $NON-NLS-0$ $NON-NLS-1$
                .add("symbol", this.symbol) // $NON-NLS-0$
                .addStyleSuffix().toURL();
    }

    public PdfOptionSpec getPdfOptionSpec() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("symbol", this.symbol); // $NON-NLS-0$
        return new PdfOptionSpec(getConfiguration().getString("linkFile", "stockportrait.pdf"), map, null); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void destroy() {
    }

    public void updateView() {
    }
}
