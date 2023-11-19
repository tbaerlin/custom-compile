/*
 * PdfSnippet.java
 *
 * Created on 17.06.2008 12:35:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.ChartIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.DesktopIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ulrich Maurer
 */
public class ChartListSnippet extends
        AbstractSnippet<ChartListSnippet, DesktopSnippetView<ChartListSnippet>> {
    public static class Class extends SnippetClass {
        public Class() {
            super("ChartList"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ChartListSnippet(context, config);
        }
    }

    private ChartListSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        setView(new DesktopSnippetView<ChartListSnippet>(this));
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        SnippetConfiguration config = getConfiguration();

        final String currency = event.getHistoryToken().get("cur"); // $NON-NLS-0$
        final String listId = config.getString("listid"); // $NON-NLS-0$

        final List<QuoteWithInstrument> listQid = SessionData.INSTANCE.getList(listId);
        final List<DesktopIcon> icons = new ArrayList<DesktopIcon>(listQid.size());
        for (QuoteWithInstrument qwi : listQid) {
            String additionalParams = qwi.getName() != null && qwi.getName().endsWith("(Ask)") ? "ask=true" : null; // $NON-NLS-0$ $NON-NLS-1$
            if (InstrumentTypeEnum.valueOf(qwi.getInstrumentData().getType()) != InstrumentTypeEnum.CUR && currency != null) {
                additionalParams = additionalParams + "&currency=" + currency; // $NON-NLS-0$
            }
            icons.add(new ChartIcon(qwi, "P1D", currency, additionalParams, // $NON-NLS-0$
                    config.getString("chartWidth", "200"), config.getString("chartHeight", "150") // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
            ));
        }
        getView().update(icons);
    }

    public void destroy() {
    }

    public void updateView() {
    }
}
