/*
 * ImpliedVolatilities.java
 *
 * Created on 14.12.11 13:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.util.Iterator;

/**
 * @author oflege
 */
public interface ImpliedVolatilities {
    public interface Item {
        String getType();

        BigDecimal getStrike();

        LocalDate getMaturity();

        BigDecimal getImpliedVolatility();
    }

    public interface Daily {
        LocalDate getDay();

        Iterator<Item> getItems();
    }

    long getUnderlyingId();

    Iterator<Daily> getDailies();
}
