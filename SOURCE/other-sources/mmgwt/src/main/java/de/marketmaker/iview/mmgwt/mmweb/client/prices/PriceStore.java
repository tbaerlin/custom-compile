/*
 * PriceRegistry.java
 *
 * Created on 08.05.2009 11:22:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.prices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.BlockOrError;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.GISFinder;
import de.marketmaker.iview.dmxml.GISFinderElement;
import de.marketmaker.iview.dmxml.IMGPriceResult;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.MSCInstrumentPriceSearch;
import de.marketmaker.iview.dmxml.MSCInstrumentPriceSearchElement;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.dmxml.MSCOrderbook;
import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.dmxml.MSCPriceDataExtended;
import de.marketmaker.iview.dmxml.MSCPriceDatas;
import de.marketmaker.iview.dmxml.MSCPriceDatasElement;
import de.marketmaker.iview.dmxml.MSCTopFlop;
import de.marketmaker.iview.dmxml.TopProductsCell;
import de.marketmaker.iview.dmxml.TopProductsElement;
import de.marketmaker.iview.dmxml.TopProductsRow;
import de.marketmaker.iview.dmxml.TopProductsTable;
import de.marketmaker.iview.dmxml.PFEvaluation;
import de.marketmaker.iview.dmxml.PortfolioPositionElement;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.WLWatchlist;
import de.marketmaker.iview.dmxml.WatchlistPositionElement;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ResponseReceivedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ResponseReceivedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushData;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushOrderbook;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushPrice;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * Central place for storing prices.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceStore implements ResponseReceivedHandler, RequestCompletedHandler {

    private int priceGeneration;

    private int numPricesUpdated = 0;

    private HashSet<String> blocksWithPrices;

    public static final PriceStore INSTANCE = new PriceStore();

    /**
     * Stores prices by vwdcode
     */
    private final Map<String, Holder> prices = new HashMap<String, Holder>();

    private final Map<String, ArrayList<Holder>> holdersPerBlock
            = new HashMap<String, ArrayList<Holder>>();

    private PriceStore() {
        this.blocksWithPrices = new HashSet<String>();
        this.blocksWithPrices.add(MSCPriceData.class.getName());
        this.blocksWithPrices.add(MSCPriceDataExtended.class.getName());
        this.blocksWithPrices.add(MSCTopFlop.class.getName());
        this.blocksWithPrices.add(MSCListDetails.class.getName());
        this.blocksWithPrices.add(MSCPriceDatas.class.getName());
        this.blocksWithPrices.add(MSCInstrumentPriceSearch.class.getName());
        this.blocksWithPrices.add(WLWatchlist.class.getName());
        this.blocksWithPrices.add(PFEvaluation.class.getName());
        this.blocksWithPrices.add(IMGResult.class.getName());
        this.blocksWithPrices.add(IMGPriceResult.class.getName());
        this.blocksWithPrices.add(MSCOrderbook.class.getName());
        this.blocksWithPrices.add(TopProductsTable.class.getName());
        this.blocksWithPrices.add(GISFinder.class.getName());
    }

    public int getPriceGeneration() {
        return this.priceGeneration;
    }


    Orderbook getOrderbook(String vwdCode) {
        final PriceHolder holder = (PriceHolder) getHolder(vwdCode);
        return holder != null ? holder.getOrderbook() : null;
    }

    Price getPrice(QuoteData quotedata) {
        if (quotedata == null) {
            return null;
        }
        return getPrice(getPriceKey(quotedata));
    }

    private Price getPrice(String vwdCode) {
        final PriceHolder holder = (PriceHolder) getHolder(vwdCode);
        return holder != null ? holder.getPrice() : null;
    }

    public void removePreviousPrices() {
        for (Holder holder : this.prices.values()) {
            holder.removePrevious();
        }
    }

    public void onPush(PushData data) {
        incPriceGeneration();
        if (data != null) {
            onPushPrices(data.getPrices());
            onPushOrderbooks(data.getOrderbooks());
        }
        fireEvent(true);
    }

    private void onPushPrices(ArrayList<PushPrice> prices) {
        if (prices == null) {
            return;
        }
        for (PushPrice pushPrice : prices) {
            final PriceHolder holder = (PriceHolder) getHolder(pushPrice.getVwdCode());
            if (holder != null) {
                holder.pushUpdate(pushPrice);
                holder.setGeneration(this.priceGeneration);
            }
        }
    }

    private void onPushOrderbooks(ArrayList<PushOrderbook> orderbooks) {
        if (orderbooks == null) {
            return;
        }
        for (PushOrderbook orderbook : orderbooks) {
            final PriceHolder holder = (PriceHolder) getHolder(orderbook.getVwdCode());
            if (holder != null) {
                holder.pushUpdate(orderbook);
                holder.setGeneration(this.priceGeneration);
            }
        }
    }

    public void onResponseReceived(ResponseReceivedEvent event) {
        final MmwebResponse response = event.getResponse();
        for (BlockOrError boe : response.getResponseType().getData().getBlockOrError()) {
            if (this.blocksWithPrices.contains(boe.getClass().getName())) {
                if (this.numPricesUpdated++ == 0) {
                    incPriceGeneration();
                }
                onResponseReceived((BlockType) boe);
            }
        }
        if (this.numPricesUpdated > 0) {
            removeStalePrices();
            Firebug.log("onResponseReceived: #prices=" + this.prices.size() // $NON-NLS-0$
                    + ", #blocks=" + this.holdersPerBlock.size()); // $NON-NLS-0$
        }
    }

    private void incPriceGeneration() {
        this.priceGeneration++;
    }

    private void removeStalePrices() {
        final HashSet<String> staleKeys = new HashSet<String>();
        for (Map.Entry<String, Holder> entry : prices.entrySet()) {
            if (entry.getValue().isStale()) {
                staleKeys.add(entry.getKey());
            }
        }
        for (String key : staleKeys) {
            this.prices.remove(key);
        }
    }

    private void onResponseReceived(final BlockType block) {
        final ArrayList<Holder> updatedHolders = getUpdatedHolders(block);
        final ArrayList<String> cids
                = StringUtil.split(block.getCorrelationId(), DmxmlContext.CID_SEPARATOR.charAt(0));
        removeBlockIdsFromPrices(cids);
        addBlockIdsToUpdatedPrices(updatedHolders, cids);
        updateHoldersPerBlock(updatedHolders, cids);
    }

    private void updateHoldersPerBlock(ArrayList<Holder> updatedHolders,
                                       ArrayList<String> cids) {
        for (String cid : cids) {
            this.holdersPerBlock.put(cid, updatedHolders);
        }
    }

    private void addBlockIdsToUpdatedPrices(ArrayList<Holder> updatedHolders,
                                            ArrayList<String> cids) {
        for (Holder updatedHolder : updatedHolders) {
            for (String cid : cids) {
                updatedHolder.addBlock(cid);
            }
        }
    }

    private void removeBlockIdsFromPrices(ArrayList<String> cids) {
        for (String cid : cids) {
            final ArrayList<Holder> holders = this.holdersPerBlock.get(cid);
            if (holders != null) {
                for (Holder holder : holders) {
                    holder.removeBlock(cid);
                }
            }
        }
    }

    private ArrayList<Holder> getUpdatedHolders(BlockType block) {
        final ArrayList<Holder> result = new ArrayList<Holder>();
        if (block instanceof MSCPriceData) {
            final MSCPriceData data = (MSCPriceData) block;
            if (data.getQuotedata() != null) {
                result.add(updateHolder(Price.doCreate(data), data.getQuotedata()));
            }
        }
        else if (block instanceof MSCPriceDataExtended) {
            updateHolders(result, ((MSCPriceDataExtended) block).getElement());
        }
        else if (block instanceof MSCTopFlop) {
            updateHolders(result, ((MSCTopFlop) block).getElement());
        }
        else if (block instanceof MSCListDetails) {
            updateHolders(result, ((MSCListDetails) block).getElement());
        }
        else if (block instanceof MSCPriceDatas) {
            for (MSCPriceDatasElement element : ((MSCPriceDatas) block).getElement()) {
                if (element.getQuotedata() != null) {
                    result.add(updateHolder(Price.doCreate(element), element.getQuotedata()));
                }
            }
        }
        else if (block instanceof MSCInstrumentPriceSearch) {
            for (MSCInstrumentPriceSearchElement element : ((MSCInstrumentPriceSearch) block).getElement()) {
                if (element.getQuotedata() != null) {
                    result.add(updateHolder(Price.doCreate(element), element.getQuotedata()));
                }
            }
        }
        else if (block instanceof WLWatchlist) {
            for (WatchlistPositionElement element : ((WLWatchlist) block).getPositions().getPosition()) {
                if (element.getQuotedata() != null) {
                    result.add(updateHolder(Price.doCreate(element), element.getQuotedata()));
                }
            }
        }
        else if (block instanceof PFEvaluation) {
            for (PortfolioPositionElement element : ((PFEvaluation) block).getElements().getPosition()) {
                if (element.getQuotedata() != null) {
                    result.add(updateHolder(Price.doCreate(element), element.getQuotedata()));
                }
            }
        }
        else if (block instanceof IMGResult) {
            final IMGResult data = (IMGResult) block;
            if (data.getQuotedata() != null && (!data.getRequest().contains("currency=") // $NON-NLS-0$
                    || data.getRequest().contains("currency=" + data.getQuotedata().getCurrencyIso()))) { // $NON-NLS-0$
                result.add(updateHolder(Price.doCreate(data), data.getQuotedata()));
            }
        }
        else if (block instanceof MSCOrderbook) {
            final MSCOrderbook data = (MSCOrderbook) block;
            if (data.getQuotedata() != null) {
                result.add(updateHolder(data));
            }
        }
        else if (block instanceof TopProductsTable) {
            updateHolders(result, (TopProductsTable) block);
        }
        else if (block instanceof GISFinder) {
            for (GISFinderElement element : ((GISFinder) block).getElement()) {
                if (element.getQuotedata() != null) {
                    result.add(updateHolder(Price.doCreate(element), element.getQuotedata()));
                }
            }
        }
        return result;
    }

    private void updateHolders(ArrayList<Holder> result, TopProductsTable block) {
        for (TopProductsRow row : block.getRow()) {
            for (TopProductsCell col : row.getColumn()) {
                if (col != null) {
                    final List<TopProductsElement> items = col.getItem();
                    for (TopProductsElement item : items) {
                        if (item != null) {
                            result.add(updateHolder(Price.doCreate(item.getTopproduct()),
                                    item.getTopproduct().getQuotedata()));
                        }
                    }
                }
            }
        }
    }

    private void updateHolders(ArrayList<Holder> updatedHolders,
                               List<? extends MSCListDetailElement> elements) {
        for (MSCListDetailElement element : elements) {
            if (element.getQuotedata() != null) {
                updatedHolders.add(updateHolder(Price.doCreate(element), element.getQuotedata()));
            }
        }
    }

    private Holder updateHolder(MSCOrderbook orderbook) {
        PriceHolder result = (PriceHolder) getHolder(orderbook.getVwdcode());
        if (result != null) {
            result.update(orderbook);
        }
        else {
            result = new PriceHolder(orderbook);
            this.prices.put(orderbook.getVwdcode(), result);
        }
        result.setGeneration(this.priceGeneration);
        return result;
    }

    private Holder updateHolder(Price price, final QuoteData quotedata) {
        PriceHolder result = (PriceHolder) getHolder(quotedata);
        if (result != null) {
            result.update(price);
        }
        else {
            result = new PriceHolder(price);
            this.prices.put(getPriceKey(quotedata), result);
        }
        result.setGeneration(this.priceGeneration);
        return result;
    }

    private Holder getHolder(final QuoteData quotedata) {
        return this.prices.get(getPriceKey(quotedata));
    }

    private Holder getHolder(String vwdCode) {
        return this.prices.get(vwdCode);
    }

    private String getPriceKey(QuoteData data) {
        return data.getVwdcode();
    }

    public void onRequestCompleted(RequestCompletedEvent event) {
        if (this.numPricesUpdated > 0) {
            fireEvent(false);
            this.numPricesUpdated = 0;
        }
    }

    private void fireEvent(final boolean pushedUpdate) {
        EventBusRegistry.get().fireEvent(new PricesUpdatedEvent(pushedUpdate));
    }
}
