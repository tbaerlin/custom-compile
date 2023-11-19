/*
 * WntKursdaten.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Deprecated
public class WntKursdaten extends AbstractDerivativeKursdaten {
    protected String getTemplateName() {
        return "wntkursdaten";
    }
}
