/*
 * Orderbook.java
 *
 * Created on 05.03.2010 15:10:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.prices;

import java.util.List;

import de.marketmaker.iview.dmxml.MSCOrderbook;
import de.marketmaker.iview.dmxml.OrderbookItem;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushOrderbook;

/**
 * @author oflege
 */
public class Orderbook implements Pushable {
    public static final int ORDERBOOK_DEPTH = 20;

    private int generation;

    public static class Item {
        private String price;
        private String volume;

        public String getPrice() {
            return price;
        }

        public String getVolume() {
            return volume;
        }

        private void reset() {
            this.price = null;
            this.volume = null;
        }
    }

    private Item[] askItems = new Item[ORDERBOOK_DEPTH];

    private Item[] bidItems = new Item[ORDERBOOK_DEPTH];

    private String date;

    private int askSize;

    private int bidSize;

    public static Orderbook getFor(MSCOrderbook data) {
        return PriceStore.INSTANCE.getOrderbook(data.getVwdcode());
    }

    Orderbook(MSCOrderbook data) {
        update(data);
    }

    void setGeneration(int generation) {
        this.generation = generation;
    }

    public int getGeneration() {
        return this.generation;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getAskSize() {
        return this.askSize;
    }

    public int getBidSize() {
        return this.bidSize;
    }

    public Item getAskItem(int i) {
        return this.askItems[i];
    }

    public Item getBidItem(int i) {
        return this.bidItems[i];
    }

    public void copyFrom(PushOrderbook orderbook) {
        this.askSize = orderbook.getAskPrices().length;
        this.bidSize = orderbook.getBidPrices().length;
        copyFrom(true, orderbook.getAskPrices(), orderbook.getAskVolumes());
        copyFrom(false, orderbook.getBidPrices(), orderbook.getBidVolumes());
        if (orderbook.getDate() != null) {
            this.date = orderbook.getDate();
        }
    }

    private void copyFrom(boolean ask, String[] prices, String[] volumes) {
        Item[] items = ask ? this.askItems : this.bidItems;

        for (int i = 0; i < prices.length; i++) {
            if (items[i] == null) {
                items[i] = new Item();
            }
            if (prices[i] != null) {
                items[i].price = prices[i];
            }
            if (volumes[i] != null) {
                items[i].volume = volumes[i];
            }
        }

        int limit = ask ? this.askSize : this.bidSize;
        for (int i = prices.length; i < limit; i++) {
            items[i].reset();
        }

        if (ask) {
            this.askSize = prices.length;
        }
        else {
            this.bidSize = prices.length;
        }
    }


    public void update(MSCOrderbook data) {
        update(data.getAsk(), this.askItems, this.askSize);
        update(data.getBid(), this.bidItems, this.bidSize);
        this.askSize = data.getAsk().size();
        this.bidSize = data.getBid().size();
        this.date = data.getDate();
    }

    private void update(List<OrderbookItem> itemList, Item[] items, int size) {
        for (int i = 0; i < itemList.size(); i++) {
            if (items[i] == null) {
                items[i] = new Item();
            }
            final OrderbookItem item = itemList.get(i);
            items[i].price = item.getPrice();
            items[i].volume = item.getVolume();
        }
        for (int i = itemList.size(); i < size; i++) {
            items[i].reset();
        }
    }
}
