/*
 * PreferSedexQuoteStrategy.java
 *
 * Created on 6/16/14 9:20 AM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.Collections;

/**
 * @author Stefan Willenbrock
 */
public class PreferSedexQuoteStrategy extends AbstractMarketQuoteStrategy {

    public PreferSedexQuoteStrategy() {
        super(Collections.singleton("IT"), Collections.singleton("IT"));
    }

    public Type getType() {
        return Type.PREFER_SEDEX_QUOTE;
    }
}
