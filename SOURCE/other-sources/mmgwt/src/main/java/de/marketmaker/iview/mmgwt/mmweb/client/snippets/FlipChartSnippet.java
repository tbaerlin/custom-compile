/*
 * NewsHeadlinesSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.GuiDefsChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.GuiDefsChangedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FlipChartSnippet extends BasicChartSnippet<FlipChartSnippet, FlipChartSnippetView> {
    public static class Class extends SnippetClass {
        public Class() {
            super("FlipChart"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new FlipChartSnippet(context, config);
        }
    }

    private MultiViewSupport multiViewSupport;

    private String originalTitle;

    private boolean enhanceTitle;

    private ArrayList<String> symbols;

    private FlipChartSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        if (config.getString("period", null) == null) { // $NON-NLS-0$
            config.put("period", "P3M"); // $NON-NLS-0$ $NON-NLS-1$
        }

        loadFromConfig(config);
        setView(new FlipChartSnippetView(this));

        EventBusRegistry.get().addHandler(GuiDefsChangedEvent.getType(), new GuiDefsChangedHandler(){
            @Override
            public void onGuidefsChange(GuiDefsChangedEvent event) {
                Firebug.debug("onGuidefsChange update for " + this); // $NON-NLS-0$
                loadFromConfig(getConfiguration());
                getView().onGuidefsChange();
            }
        });
    }

    private void loadFromConfig(SnippetConfiguration config) {
        final String[] viewNames;

        final String listname = config.getString("list"); // $NON-NLS-0$
        if (StringUtil.hasText(listname)) {
            final List<QuoteWithInstrument> qwis = SessionData.INSTANCE.getList(listname);
            viewNames = new String[qwis.size()];
            this.symbols = new ArrayList<String>();
            for (int i = 0; i < qwis.size(); i++) {
                viewNames[i] = qwis.get(i).getName();
                symbols.add(qwis.get(i).getId());
            }
        }
        else {
            viewNames = config.getArray("viewNames"); // $NON-NLS-0$
            this.symbols = config.getList("symbols"); // $NON-NLS-0$
        }

        assert symbols.size() == viewNames.length;

        this.multiViewSupport = new MultiViewSupport(viewNames, null);
        this.multiViewSupport.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            public void onValueChange(ValueChangeEvent<Integer> e) {
                setSymbolForSelectedView();
                ackParametersChanged();
            }
        });

        this.originalTitle = getTitle();
        this.enhanceTitle = config.getBoolean("enhancetitle", false); // $NON-NLS-0$

        if (viewNames.length == 0) {
            Firebug.log("FlipChartSnippet: viewNames.length == 0"); // $NON-NLS-0$
        }
        else {
            setSymbolForSelectedView();
        }
    }

    private void setSymbolForSelectedView() {
        setSymbol(null, getSymbol(), null);
        if (this.enhanceTitle) {
            enhanceTitle();
        }
    }

    public MultiViewSupport getMultiViewSupport() {
        return this.multiViewSupport;
    }

    public String getSymbol() {
        return this.symbols.get(this.multiViewSupport.getSelectedView());
    }

    public String getMarketStrategy() {
        final String listname = getConfiguration().getString("list"); // $NON-NLS-0$
        if (listname == null) {
            return null;
        }
        return getMarketStrategy(listname, getSymbol());
    }

    private String getMarketStrategy(String listname, String id) {
        final JSONWrapper array = SessionData.INSTANCE.getGuiDef(listname).get("elements"); // $NON-NLS-0$
        for (int i = 0; i < array.size(); i++) {
            final String symbol = array.get(i).get("symbol").stringValue(); // $NON-NLS-0$
            if (id.equals(symbol)) {
                return array.get(i).get("marketStrategy").stringValue(); // $NON-NLS-0$
            }
        }
        return null;
    }

    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();
        this.block.setParameter("symbol", config.getString("symbol", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("symbolStrategy", config.getString("symbolStrategy", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("period", config.getString("period", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("from", config.getString("from", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("to", config.getString("to", null)); // $NON-NLS-0$ $NON-NLS-1$
    }

    private void enhanceTitle() {
        final int n = this.multiViewSupport.getSelectedView();
        getConfiguration().put("title", this.originalTitle + " " + this.multiViewSupport.getViewSpec(n).getName()); // $NON-NLS-0$ $NON-NLS-1$
    }
}
