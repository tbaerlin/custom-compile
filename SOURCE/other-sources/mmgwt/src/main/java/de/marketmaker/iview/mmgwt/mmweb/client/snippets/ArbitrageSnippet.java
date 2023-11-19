/*
 * PriceListSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCPriceDatas;
import de.marketmaker.iview.dmxml.MSCPriceDatasElement;
import de.marketmaker.iview.dmxml.MSCTicks;
import de.marketmaker.iview.dmxml.MSCTicksList;
import de.marketmaker.iview.dmxml.OHLCVTimeseries;
import de.marketmaker.iview.dmxml.OHLCVTimeseriesElement;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceDataType;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ArbitrageSnippet extends
        AbstractSnippet<ArbitrageSnippet, SnippetTableView<ArbitrageSnippet>>
        implements SymbolSnippet, PushRegisterHandler {

    public static ListDetailsHelper.LinkType LINK_TYPE = ListDetailsHelper.LinkType.MARKET;
    private final boolean withHistoryContext;

    private static final Comparator<String> NULL_LAST_COMPARATOR =
            (s1, s2) -> {
                if (StringUtility.isEmpty(s1)) {
                    return 1;
                } else if (StringUtility.isEmpty(s2)) {
                    return -1;
                }
                return s1.compareTo(s2);
            };

    public static class Class extends SnippetClass {
        public Class() {
            super("Arbitrage", I18n.I.arbitrage()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ArbitrageSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("colSpan", 3); // $NON-NLS$
        }
    }

    private DmxmlContext.Block<MSCPriceDatas> block;

    private DmxmlContext.Block<MSCTicksList> volumeInformation;

    private ListDetailsHelper listDetailsHelper;

    private DefaultTableDataModel dataModel;

    private final PriceSupport priceSupport = new PriceSupport(this);

    private final ListDetailsHelper.LinkType linkType;

    private ArbitrageSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        ListDetailsHelper.LinkType valueOfLinkType = LINK_TYPE;
        if (config.getString("linkType", null) != null) { // $NON-NLS-0$
            final String linkTypeName = config.getString("linkType", null); //$NON-NLS$
            try {
                valueOfLinkType = ListDetailsHelper.LinkType.valueOf(linkTypeName);
            } catch (IllegalArgumentException iae) {
                Firebug.warn("ArbitrageSnippet (id=" + this.getId() + ") has unknown linkType '" + linkTypeName + "'");
            }
        }
        this.linkType = valueOfLinkType;

        this.withHistoryContext = config.getBoolean("withHistoryContext", false); // $NON-NLS$

        this.block = createBlock("MSC_PriceDatas"); // $NON-NLS-0$
        this.block.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
        if (FeatureFlags.Feature.VWD_RELEASE_2015.isEnabled()) {
            this.volumeInformation = createBlock("MSC_TicksList"); // $NON-NLS$
            this.volumeInformation.setParameter("type", "OHLCV"); // $NON-NLS$
            this.volumeInformation.setParameter("aggregation", "P1D"); // $NON-NLS$
            this.volumeInformation.setParameter("period", "P7D"); // $NON-NLS$
        }
        onParametersChanged();

        final TableColumnModel tcm = getListDetailsHelper().createTableColumnModel();
        setView(new SnippetTableView<>(this, tcm).withPriceSupport(this.priceSupport));
        if (FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled()
                || FeatureFlags.Feature.DZ_RELEASE_2016.isEnabled()) {
            getView().setSortLinkListener(new SortLinkSupport(this.block, this::reload, true));
        }
    }

    public void reload() {
        this.contextController.reload();
    }

    ListDetailsHelper getListDetailsHelper() {
        if (this.listDetailsHelper == null) {
            this.listDetailsHelper = new ListDetailsHelper(linkType,
                    getConfiguration().getBoolean("displayVolume", true), false).withBidAskVolume(true); // $NON-NLS-0$
        }
        return this.listDetailsHelper;
    }

    public void destroy() {
        deactivate();
        destroyBlock(this.block);
    }

    public String getDropTargetGroup() {
        return DROP_TARGET_GROUP_INSTRUMENT;
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
        if (this.volumeInformation != null) {
            this.volumeInformation.setParameter("symbol", symbol); // $NON-NLS$
        }
    }

    @Override
    public void deactivate() {
        this.priceSupport.deactivate();
        this.dataModel = null;
    }

    public boolean isConfigurable() {
        return true;
    }

    public boolean notifyDrop(QuoteWithInstrument qwi) {
        if (qwi == null) {
            return false;
        }
        getConfiguration().put("title", qwi.getName()); // $NON-NLS-0$
        getConfiguration().put("symbol", qwi.getId()); // $NON-NLS-0$
        ackParametersChanged();
        return true;
    }

    public void configure(Widget triggerWidget) {
        final SnippetConfigurationView configView = new SnippetConfigurationView(this);
        configView.addSelectSymbol(null);
        configView.show();
    }

    protected void onParametersChanged() {
        this.setSymbol(null, getConfiguration().getString("symbol", null), null); // $NON-NLS$
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (this.block.isResponseOk() && !event.isPushedUpdate() &&
                !this.priceSupport.isLatestPriceGeneration()) {
            getView().update(this.dataModel);
        }
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            final int numAdded = event.addVwdcodes(this.block.getResult());
            if (numAdded > 0) {
                if (numAdded == this.block.getResult().getElement().size()) {
                    event.addComponentToReload(this.block, this);
                } else {
                    event.addComponentToReload(null, this);
                }
                return getView().getRenderItems(this.dataModel);
            }
        }
        return null;
    }

    public void updateView() {
        this.priceSupport.invalidateRenderItems();

        if (!block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final MSCPriceDatas data = this.block.getResult();
        this.dataModel = getTableDataModel(data.getInstrumentdata(), data.getElement());

        getView().update(getListDetailsHelper().withLMEFields(containsLMEPriceData(data)).createTableColumnModel(), this.dataModel);
        this.priceSupport.activate();
    }

    private DefaultTableDataModel getTableDataModel(InstrumentData instrumentdata,
                                                    List<MSCPriceDatasElement> elements) {
        final ListDetailsHelper listDetailsHelper = getListDetailsHelper();
        final DefaultTableDataModel dtm = listDetailsHelper.createTableDataModel(elements.size()).withSort(block.getResult().getSort());

        final TrendBarData tbd = TrendBarData.create(this.block.getResult());
        int row = 0;
        for (MSCPriceDatasElement e : this.sort(elements)) {
            final Price price = Price.create(e);
            listDetailsHelper.addRow(dtm, row, instrumentdata, e.getQuotedata(), tbd, price, this.withHistoryContext);
            row++;
        }
        return dtm;
    }

    private List<MSCPriceDatasElement> sort(List<MSCPriceDatasElement> elements) {

        // Intentionally separated from below to be easier removed when FeatureFlag is removed
        if (!FeatureFlags.Feature.VWD_RELEASE_2015.isEnabled()) {
            return elements;
        }
        String sortedBy = this.block.getResult().getSort().getSortedBy().getValue();
        if (StringUtility.hasText(sortedBy) && !sortedBy.equalsIgnoreCase("default")) { // $NON-NLS-0$
            return elements;
        }

        final Map<String, Long> volumePerQuote = new HashMap<>();
        String topCurrency = null;
        BigDecimal maxTurnover = BigDecimal.ZERO;
        if (this.volumeInformation.isResponseOk()) {
            for (MSCTicks mscTicks : this.volumeInformation.getResult().getBlock()) {
                List<OHLCVTimeseriesElement> items = ((OHLCVTimeseries) mscTicks.getItems()).getItem();
                Long volume = 0L;
                BigDecimal turnover = BigDecimal.ZERO;
                for (OHLCVTimeseriesElement item : items) {
                    Long volumeInItem = item.getVolume();
                    volume += (volumeInItem != null) ? volumeInItem : 0L;
                    String close = item.getClose();
                    if (close != null) {
                        turnover = turnover.add(new BigDecimal(close).multiply(new BigDecimal(volume)));
                    }
                }
                volumePerQuote.put(mscTicks.getQuotedata().getQid(), volume);
                if (turnover.compareTo(maxTurnover) > 0) {
                    maxTurnover = turnover;
                    topCurrency = mscTicks.getQuotedata().getCurrencyIso();
                }
            }
        }

        // Group by currency and sort alphabetically (null and emtpy last)
        SortedMap<String, List<MSCPriceDatasElement>> groupedByCurrency = new TreeMap<>(NULL_LAST_COMPARATOR);
        for (MSCPriceDatasElement e : elements) {
            String currencyIso = e.getQuotedata().getCurrencyIso();
            List<MSCPriceDatasElement> elementsByCurrency = groupedByCurrency.get(currencyIso);
            if (elementsByCurrency == null) {
                elementsByCurrency = new ArrayList<>();
                groupedByCurrency.put(currencyIso, elementsByCurrency);
            }
            elementsByCurrency.add(e);
        }

        // Sort currency group elements by volume
        for (List<MSCPriceDatasElement> elementsByCurrency : groupedByCurrency.values()) {
            Collections.sort(elementsByCurrency, Collections.reverseOrder(
                    (o1, o2) -> {
                        Long volume1 = volumePerQuote.get(o1.getQuotedata().getQid());
                        Long volume2 = volumePerQuote.get(o2.getQuotedata().getQid());
                        return Long.compare(
                                volume1 != null ? volume1 : -1L,
                                volume2 != null ? volume2 : -1L
                        );
                    }
            ));
        }

        List<MSCPriceDatasElement> sortedElements = new ArrayList<>();

        // Put the currency with highest turnover quote first
        if (topCurrency != null) {
            sortedElements.addAll(groupedByCurrency.remove(topCurrency));
        }

        // Append the remaining elements in alphabetical order
        for (List<MSCPriceDatasElement> elementsByCurrency : groupedByCurrency.values()) {
            sortedElements.addAll(elementsByCurrency);
        }
        return sortedElements;
    }

    private boolean containsLMEPriceData(MSCPriceDatas data) {
        if (data.getElement() instanceof ArrayList) {
            ArrayList<MSCPriceDatasElement> elements = (ArrayList<MSCPriceDatasElement>) data.getElement();
            for (MSCPriceDatasElement element : elements) {
                if (PriceDataType.LME.value().equals(element.getPricedatatype())) {
                    return true;
                }
            }
        }
        return false;
    }
}
