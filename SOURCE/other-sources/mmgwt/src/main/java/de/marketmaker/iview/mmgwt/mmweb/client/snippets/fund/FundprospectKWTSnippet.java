/*
 * ReportsSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund;

import de.marketmaker.iview.dmxml.MSCStaticData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.DesktopIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DesktopSnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.IsVisible;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundprospectKWTSnippet extends
        AbstractSnippet<FundprospectKWTSnippet, DesktopSnippetView<FundprospectKWTSnippet>> implements
        SymbolSnippet, IsVisible {

    public static final String KWT_REPORTS_LIST = "kwt_reports_list";  // $NON-NLS-0$

    public static class Class extends SnippetClass {
        public Class() {
            super("FundprospectKWT", I18n.I.kwtFundBrochures()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new FundprospectKWTSnippet(context, config);
        }
    }

    private final DmxmlContext.Block<MSCStaticData> block;

    private boolean hasContent = false;

    private FundprospectKWTSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.setView(new DesktopSnippetView<>(this));
        this.block = context.addBlock("MSC_StaticData"); // $NON-NLS-0$
        setSymbol(InstrumentTypeEnum.FND, config.getString("symbol", null), null); // $NON-NLS-0$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void destroy() {
    }

    public void updateView() {
        this.hasContent = false;
        if (this.block.isResponseOk()) {
            final String isin = this.block.getResult().getInstrumentdata().getIsin();
            if (isin != null) {
                getView().update(getReports(isin));
            }
            else {
                getView().update(new ArrayList<DesktopIcon>());
            }
        }
    }

    private List<DesktopIcon> getReports(String isin) {
        JSONWrapper elements = SessionData.INSTANCE.getGuiDef(KWT_REPORTS_LIST);
        final List<DesktopIcon> result = new ArrayList<>();
        final JSONWrapper reports = elements.get(isin).get("children"); // $NON-NLS-0$
        if (reports == JSONWrapper.INVALID) {
            return result;
        }
        for (int i = 0; i < reports.size(); i++) {
            final String name = reports.get(i).get("name").stringValue(); // $NON-NLS-0$
            final String url = reports.get(i).get("url").stringValue(); // $NON-NLS-0$
            result.add(new DesktopIcon("mm-desktopIcon-pdf", // $NON-NLS-0$
                    new String[]{name}, url));
            this.hasContent = true;
        }
        return result;
    }

    public boolean isVisible() {
        return this.hasContent;
    }

}
