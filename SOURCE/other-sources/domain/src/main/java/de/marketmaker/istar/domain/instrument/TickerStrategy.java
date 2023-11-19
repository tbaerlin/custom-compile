/*
 * TickerStrategy.java
 *
 * Created on 29.11.13 14:18
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

/**
 * @author oflege
 */
public interface TickerStrategy {
    String getTicker(Instrument instrument);

    String getTicker(Quote quote);
}
