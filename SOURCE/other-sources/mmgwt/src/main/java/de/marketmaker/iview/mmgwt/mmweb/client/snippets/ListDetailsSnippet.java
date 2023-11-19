/*
 * ListDetailsSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history.PmItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectListMenu;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.PagingPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.ErrorMessageUtil.getMessage;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ListDetailsSnippet extends AbstractSnippet<ListDetailsSnippet, ListDetailsSnippetView>
        implements PagingPanel.Handler, SymbolSnippet, PdfUriSnippet, PushRegisterHandler {

    public static class Class extends SnippetClass {
        public Class() {
            super("ListDetails", I18n.I.pricelist()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ListDetailsSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("colSpan", 3); // $NON-NLS$
        }
    }

    public static final int DEFAULT_COUNT = 50;

    private final ListDetailsHelper listDetailsHelper;

    private DmxmlContext.Block<MSCListDetails> block;

    private final boolean reduceCurrencyName;

    private DefaultTableDataModel dtm;

    private final PriceSupport priceSupport = new PriceSupport(this);

    private boolean listidChanged = true;

    private final boolean withHistoryContext;

    private ListDetailsSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("MSC_List_Details"); // $NON-NLS$
        this.block.setParameter("withOptionalMarkets", "true"); // $NON-NLS$
        this.listDetailsHelper = new ListDetailsHelper(ListDetailsHelper.LinkType.NAME, true, true);
        this.reduceCurrencyName = config.getBoolean("reduceCurrencyName", false); // $NON-NLS-0$
        this.withHistoryContext= config.getBoolean("withHistoryContext", false); // $NON-NLS$
        this.setView(new ListDetailsSnippetView(this));
        onParametersChanged();
    }

    ListDetailsHelper getListDetailsHelper() {
        return this.listDetailsHelper;
    }

    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();
        this.block.setParameter("count", config.getInt("count", DEFAULT_COUNT)); // $NON-NLS$
        this.block.setParameter("offset", config.getInt("offset", 0)); // $NON-NLS$
        this.block.setParameter("withHitCount", config.getBoolean("withHitCount", false)); // $NON-NLS$
        this.block.setParameter("sortBy", config.getString("sortBy", "name")); // $NON-NLS$ $NON-NLS-2$
        this.block.setParameter("ascending", config.getString("ascending", "true")); // $NON-NLS$ $NON-NLS-2$
        this.block.setParameter("listid", config.getString("listid", null)); // $NON-NLS$
        this.block.setParameter("period", config.getString("period", null)); // $NON-NLS$
        this.block.setParameter("marketStrategy", config.getString("marketStrategy", null)); // $NON-NLS$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        setListid(symbol);
    }

    public void setListid(String listid) {
        this.listidChanged = true;
        getConfiguration().put("listid", listid); // $NON-NLS-0$
        getConfiguration().put("offset", "0"); // $NON-NLS$
        getConfiguration().remove("marketStrategy"); // $NON-NLS$
        onParametersChanged();
    }

    void setMarketStrategy(String ms) {
        final String old = getConfiguration().put("marketStrategy", ms);// $NON-NLS$
        if (!StringUtil.equals(old, ms)) {
            getConfiguration().put("offset", "0"); // $NON-NLS$
            ackParametersChanged();
        }
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        initMarketStrategy(event);
        onParametersChanged();
    }

    private void initMarketStrategy(PlaceChangeEvent event) {
        final String ms = event.getProperty("marketStrategy"); // $NON-NLS$
        if (ms != null) {
            getConfiguration().put("marketStrategy", ms); // $NON-NLS$
        }
    }


    public void setPeriod(String period) {
        getConfiguration().put("period", period); // $NON-NLS-0$
    }

    public void ackNewOffset(int offset) {
        getConfiguration().put("offset", Integer.toString(offset)); // $NON-NLS-0$
        ackParametersChanged();
    }

    LinkListener<String> getSortLinkListener() {
        return new SortLinkSupport(this.block, new Command() {
            public void execute() {
                onSortChange();
            }
        });
    }

    private void onSortChange() {
        final SnippetConfiguration config = getConfiguration();
        config.put("sortBy", this.block.getParameter("sortBy")); // $NON-NLS$
        config.put("ascending", this.block.getParameter("ascending")); // $NON-NLS$
        config.put("offset", "0"); // $NON-NLS$
        ackParametersChanged();
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public boolean isConfigurable() {
        return true;
    }

    public void configure(Widget triggerWidget) {
        SelectListMenu.configure(this, triggerWidget, new Command() {
            @Override
            public void execute() {
                ackParametersChanged();
            }
        });
    }

    public void updateView() {
        this.priceSupport.invalidateRenderItems();
        
        if (!this.block.isResponseOk()) {
            getView().update(0, 0, 0, DefaultTableDataModel.create(getMessage(block.getError())));
            return;
        }
        final MSCListDetails ld = this.block.getResult();
        final int rows = Integer.parseInt(ld.getCount());
        final int offset = Integer.parseInt(ld.getOffset());
        final int count = Integer.parseInt(ld.getCount());
        final int total = Integer.parseInt(ld.getTotal());

        if (this.listidChanged) {
            getView().updateMarkets(ld, total, getConfiguration().getString("marketStrategy", null)); // $NON-NLS-0$
            this.listidChanged = false;
        }

        this.dtm = this.listDetailsHelper.createTableDataModel(rows).withSort(ld.getSort());

        final TrendBarData tbd = TrendBarData.create(ld);
        int row = 0;
        for (MSCListDetailElement e : ld.getElement()) {
            if (this.reduceCurrencyName) {
                StringUtil.reduceCurrencyNameLength(e.getInstrumentdata());
            }
            final Price price = Price.create(e);
            if (this.withHistoryContext) {
                this.listDetailsHelper.addRow(this.dtm, row, e.getInstrumentdata(), e.getQuotedata(), tbd, price,
                        PmItemListContext.createForPortrait(e, ld.getElement(), this.block.getResult().getName()));
            } else {
                this.listDetailsHelper.addRow(this.dtm, row, e.getInstrumentdata(), e.getQuotedata(), tbd, price);
            }
            row++;
        }
        getView().update(offset, count, total, this.dtm);
        this.priceSupport.activate();
    }

    public boolean isTopToolbarUri() {
        return true;
    }

    public PdfOptionSpec getPdfOptionSpec() {
        return new PdfOptionSpec("quotelist.pdf", createParameterMap(), "pdf_options_format"); // $NON-NLS$
    }

    private Map<String, String> createParameterMap() {
        final SnippetConfiguration config = getConfiguration();
        final Map<String, String> map = new HashMap<String, String>();
        setParameter(config, "listid", map); // $NON-NLS-0$
        setParameter(config, "sortBy", map); // $NON-NLS-0$
        setParameter(config, "ascending", map); // $NON-NLS-0$
        setParameter(config, "period", map); // $NON-NLS-0$
        setParameter(config, "marketStrategy", map); // $NON-NLS-0$
        return map;
    }

    private void setParameter(SnippetConfiguration config, String key, Map<String, String> map) {
        final String value = config.getString(key);
        if (value != null) {
            map.put(key, value);
        }
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (!event.isPushedUpdate() && this.block.isResponseOk()) {
            final MSCListDetails ld = this.block.getResult();
            final int offset = Integer.parseInt(ld.getOffset());
            final int count = Integer.parseInt(ld.getCount());
            final int total = Integer.parseInt(ld.getTotal());
            getView().update(offset, count, total, this.dtm);
        }
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            final int numAdded = event.addVwdcodes(this.block.getResult());
            if (numAdded == this.block.getResult().getElement().size()) {
                event.addComponentToReload(this.block, this);
            }
            if (numAdded > 0) {
                return getView().getRenderItems(this.dtm);
            }
        }
        return null;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }
}