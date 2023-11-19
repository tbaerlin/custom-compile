/*
 * MarketsWorkspace.java
 *
 * Created on 05.04.2008 14:55:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MarketOverviewSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MarketsWorkspace extends AbstractListWorkspace<MSCListDetails, MSCListDetailElement> {
    public static final MarketsWorkspace INSTANCE = new MarketsWorkspace();

    private final Map<String, String> mapNames = new HashMap<String, String>();

    public MarketsWorkspace() {
        super(I18n.I.marketOverview(), true, true);

        final List<QuoteWithInstrument> list = SessionData.INSTANCE.getList("list_markets_workspace");  // $NON-NLS$
        if (list.isEmpty()) {
            this.block = this.context.addBlock("MSC_List_Details"); // $NON-NLS$
            this.block.setParameter("listid", SessionData.INSTANCE.getGuiDef("markets_workspace_listid").stringValue()); // $NON-NLS$
            this.block.setParameter("onlyEntitledQuotes", "true"); // $NON-NLS$
            this.block.setParameter("disablePaging", "true"); // $NON-NLS$
            this.block.setParameter("sortBy", "none"); // $NON-NLS$
        }
        else {
            for (QuoteWithInstrument qwi : list) {
                mapNames.put(qwi.getQuoteData().getQid(), qwi.getName());
            }
            this.block = this.context.addBlock("MSC_PriceDataMulti"); // $NON-NLS$
            this.block.setParameters("symbol", getSymbols(list)); // $NON-NLS$

        }
    }

    public DmxmlContext.Block<MSCListDetails> copyBlock(DmxmlContext ctx) {
        final DmxmlContext.Block<MSCListDetails> result = ctx.addBlock(this.block.getKey());
        result.addParametersFrom(this.block);
        return result;
    }

    @Override
    public String getStateKey() {
        return StateSupport.MARKETS;
    }

    private String[] getSymbols(List<QuoteWithInstrument> list) {
        final String[] symbols = new String[list.size()];
        for (int i = 0; i < symbols.length; i++) {
            symbols[i] = list.get(i).getQuoteData().getQid();
        }
        return symbols;
    }

    @Override
    protected Price getPrice(MSCListDetailElement data) {
        return Price.create(data);
    }

    @Override
    public QuoteWithInstrument getQuoteWithInstrument(MSCListDetailElement data) {
        String name = mapNames.get(data.getQuotedata().getQid());
        if (name == null) {
            name = data.getItemname();
        }
        return new QuoteWithInstrument(data.getInstrumentdata(), data.getQuotedata(), name);
    }

    protected List<MSCListDetailElement> getCurrentList() {
        return filterElements();
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            final int numAdded = event.addVwdcodes(this.block.getResult());
            if (numAdded == this.block.getResult().getElement().size()) {
                event.addComponentToReload(this.block, this.pushReloadCallback);
            }
            if (numAdded > 0) {
                return getRenderItems();
            }
        }
        return null;
    }

    @Override
    protected TableCellRenderers.PushChangeRenderer getPriceRenderer() {
        return TableCellRenderers.LAST_PRICE_COMPARE_CHANGE_PUSH;
    }

    @Override
    protected String getPriceClass() {
        return "b"; // bold // $NON-NLS-0$
    }

    /**
     * Removes L-DAX and X-DAX unless they are more recent than the DAX itself
     * @return list with L-DAX and X-DAX removed if not current
     */
    private ArrayList<MSCListDetailElement> filterElements() {
        return MarketOverviewSnippet.filterElements(this.block);
    }

    protected boolean isAutoRefreshEnabled() {
        return "true".equals(SessionData.INSTANCE.getUserProperty("mwsAutoReload")); // $NON-NLS-0$ $NON-NLS-1$
    }
}
