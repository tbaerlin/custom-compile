/*
 * PriceUpdateSupport.java
 *
 * Created on 02.02.2010 14:01:33
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.prices;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCOrderbook;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;

import java.util.ArrayList;

/**
 * Helps to de-/register for price updates, provides methods that encapsulate accessing
 * the {@link de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceStore#INSTANCE}.
 * Objects that need to be informed about price updates should create an instance of this
 * class and also make sure the very same object is used by its view when price data is
 * rendered.
 *
 * @author oflege
 */
public class PriceSupport implements PushRegisterHandler {
    private static int next_id = 0;

    private final PricesUpdatedHandler handler;

    private final PushRegisterHandler pushHandler;

    private HandlerRegistration registration;

    private HandlerRegistration pushRegistration;

    private int priceGeneration;

    private int id = ++next_id;

    private ArrayList<PushRenderItem> renderItems = null;

    private final ArrayList<PushRenderItem> toBeCleared = new ArrayList<PushRenderItem>();

    public PriceSupport(PricesUpdatedHandler handler) {
        this.handler = handler;
        this.pushHandler = null;
    }

    public PriceSupport(PushRegisterHandler pushHandler) {
        this.handler = pushHandler;
        this.pushHandler = pushHandler;
    }

    public boolean isActive() {
        return this.registration != null;
    }

    public void activate() {
        if (this.registration == null) {
            this.registration = addHandler(PricesUpdatedEvent.getType(), this);
        }
        if (this.pushRegistration == null && this.pushHandler != null) {
            this.pushRegistration = addHandler(PushRegisterEvent.getType(), this);
        }
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.pushHandler != null) {
            try {
                this.renderItems = this.pushHandler.onPushRegister(event);
            }
            catch (Exception e) {
                Firebug.warn("opr: " + this.pushHandler.getClass().getName(), e);
            }
        }
        return null;
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (this.renderItems != null && event.isPushedUpdate()) {
            handlePush();
        }
        //TODO: invoke only if pushed
        this.handler.onPricesUpdated(event);
        this.priceGeneration = PriceStore.INSTANCE.getPriceGeneration();
    }

    public void invalidateRenderItems() {
        this.toBeCleared.clear();
        this.renderItems = null;
    }

    private void handlePush() {
        clearItems();
        renderItems();
    }

    private void renderItems() {
        for (PushRenderItem item : this.renderItems) {
            if (isCurrent(item)) {
                item.render();
                if (item.needsToBeCleared()) {
                    this.toBeCleared.add(item);
                }
            }
        }
    }

    private void clearItems() {
        for (PushRenderItem item : this.toBeCleared) {
            if (!isCurrent(item)) {
                item.clear();
            }
        }
        this.toBeCleared.clear();
    }

    private boolean isCurrent(PushRenderItem item) {
        return item.getPushable().getGeneration() == PriceStore.INSTANCE.getPriceGeneration();
    }

    public <H extends EventHandler> HandlerRegistration addHandler(GwtEvent.Type<H> type,
                                                                   H handler) {
        return EventBusRegistry.get().addHandler(type, handler);
    }

    public void deactivate() {
        if (this.registration != null) {
            this.registration.removeHandler();
            this.registration = null;
        }
        if (this.pushRegistration != null) {
            this.pushRegistration.removeHandler();
            this.pushRegistration = null;
        }

        invalidateRenderItems();
    }

    public void updatePriceGeneration() {
        this.priceGeneration = PriceStore.INSTANCE.getPriceGeneration();
    }

    public int getPriceGeneration() {
        return priceGeneration;
    }

    public boolean isLatestPriceGeneration() {
        return this.priceGeneration == PriceStore.INSTANCE.getPriceGeneration();
    }

    /**
     * Return true iff this context's price generation is older than the generation
     * of quoteData's price (which means that the renderer using this context has not yet
     * rendered the most current price)
     *
     * @param quoteData key for price to check
     * @return true if context is not current wrt. quoteData's price
     */
    public boolean isNewerPriceAvailable(QuoteData quoteData) {
        final Price price = getCurrentPrice(quoteData);
        return (price != null) && price.getGeneration() > this.priceGeneration;
    }

    public Price getCurrentPrice(QuoteData quoteData) {
        return PriceStore.INSTANCE.getPrice(quoteData);
    }

    /**
     * Return true iff this context's price generation is older than the generation
     * of data's orderbook (which means that the renderer using this context has not yet
     * rendered the most current orderbook)
     *
     * @param data key for orderbook to check
     * @return true if context is not current wrt. data's orderbook
     */
    public boolean isNewerOrderbookAvailable(MSCOrderbook data) {
        final Orderbook orderbook = PriceStore.INSTANCE.getOrderbook(data.getVwdcode());
        return (orderbook != null) && orderbook.getGeneration() > this.priceGeneration;
    }
}
