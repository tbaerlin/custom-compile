/*
 * BewHistoricPriceResponse.java
 *
 * Created on 27.10.2010 15:21:13
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.SnapRecord;

/**
 * @author oflege
 */
public class BewHistoricPriceResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private final Map<String, Item> items = new HashMap<>();

    void add(String name, Item item) {
        this.items.put(name, item);
    }

    public Item getItem(String vwdcode) {
        return this.items.get(vwdcode);
    }

    public static class Item implements Serializable {
        static final long serialVersionUID = 2L;

        private final SnapRecord snapRecord;

        private final BigDecimal price;

        private final LocalDate priceDate;

        Item(SnapRecord snapRecord) {
            this(snapRecord, null, null);
        }

        Item(BigDecimal price, LocalDate priceDate) {
            this(null, price, priceDate);
        }

        private Item(SnapRecord snapRecord, BigDecimal price, LocalDate priceDate) {
            this.snapRecord = snapRecord;
            this.price = price;
            this.priceDate = priceDate;
        }

        public SnapRecord getSnapRecord() {
            return this.snapRecord;
        }

        public BigDecimal getPrice() {
            return this.price;
        }

        public LocalDate getPriceDate() {
            return this.priceDate;
        }
    }
}
