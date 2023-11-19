package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCStaticData;
import de.marketmaker.iview.dmxml.PFOrder;
import de.marketmaker.iview.dmxml.PortfolioPositionElement;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * A list that contains elements that associate an order with objects that provide
 * instrument and quote data for the order. Since orders may refer to data objects that are
 * no longer represented as portfolio positions in an evaluated portfolio, in that case 
 * a static data block has to be used to obtain the instrument/quote data. 
 * 
 * Created on Jun 4, 2009 2:26:21 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 */

class OrderProxyList {

    static class Element {

        private final PortfolioPositionElement ppe;

        private DmxmlContext.Block<MSCStaticData> block;

        private PFOrder order;

        private Element(PortfolioPositionElement ppe, PFOrder order) {
            this.ppe = ppe;
            this.order = order;
            this.block = null;
        }

        private Element(PFOrder order, DmxmlContext.Block<MSCStaticData> block) {
            this.ppe = null;
            this.order = order;
            this.block = block;
        }

        InstrumentData getInstrumentdata() {
            if (this.ppe != null) {
                return this.ppe.getInstrumentdata();
            }
            if (!this.block.isResponseOk()) {
                return null;
            }
            return this.block.getResult().getInstrumentdata();
        }

        QuoteData getQuotedata() {
            if (this.ppe != null) {
                return this.ppe.getQuotedata();
            }
            if (!this.block.isResponseOk()) {
                return null;
            }
            return this.block.getResult().getQuotedata();
        }

        PFOrder getOrder() {
            return order;
        }

        DmxmlContext.Block<MSCStaticData> getBlock() {
            return block;
        }
    }

    private final Map<String, DmxmlContext.Block<MSCStaticData>> blocks
            = new HashMap<String, DmxmlContext.Block<MSCStaticData>>();

    private final List<Element> elements = new ArrayList<Element>();

    private final DmxmlContext context = new DmxmlContext();

    void add(PortfolioPositionElement ppe, PFOrder order) {
        this.elements.add(createElement(ppe, order));
    }

    private Element createElement(PortfolioPositionElement ppe, PFOrder order) {
        if (ppe != null) {
            return new Element(ppe, order);
        }
        else if (this.blocks.containsKey(order.getQid())) {
            return new Element(order, this.blocks.get(order.getQid()));
        }
        else {
            return new Element(order, createBlock(order));
        }
    }

    private DmxmlContext.Block<MSCStaticData> createBlock(PFOrder order) {
        final DmxmlContext.Block<MSCStaticData> result = this.context.addBlock("MSC_StaticData"); // $NON-NLS-0$
        result.setParameter("symbol", order.getQid()); // $NON-NLS-0$
        this.blocks.put(order.getQid(), result);
        return result;
    }

    void checkData(final PortfolioController controller) {
        this.context.issueRequest(new AsyncCallback<ResponseType>() {
            public void onFailure(Throwable caught) {
                controller.doUpdateOrdersViewTable(null);
            }

            public void onSuccess(ResponseType responseType) {
                controller.doUpdateOrdersViewTable(OrderProxyList.this.getElements());
            }
        });
    }

    private List<Element> getElements() {
        return this.elements;
    }
}
