/*
 * TickerStrategies.java
 *
 * Created on 29.11.13 14:20
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.KeysystemEnum;

/**
 * @author oflege
 */
public class TickerStrategies {
    private TickerStrategies() {
    }

    public static final TickerStrategy UNIFIED = new TickerStrategy() {
        @Override
        public String getTicker(Instrument instrument) {
            final String eurexTicker = instrument.getSymbol(KeysystemEnum.EUREXTICKER);
            return StringUtils.hasText(eurexTicker) ? eurexTicker : instrument.getSymbolTicker();
        }

        @Override
        public String getTicker(Quote quote) {
            return getTicker(quote.getInstrument());
        }
    };

    public static final TickerStrategy WM = new TickerStrategy() {
        @Override
        public String getTicker(Instrument instrument) {
            return instrument.getSymbolTicker();
        }

        @Override
        public String getTicker(Quote quote) {
            final String ticker = quote.getSymbolWmTicker();
            if (StringUtils.hasText(ticker)) {
                return ticker;
            }
            return getTicker(quote.getInstrument());
        }
    };

    public static final TickerStrategy DEFAULT = UNIFIED;
}
