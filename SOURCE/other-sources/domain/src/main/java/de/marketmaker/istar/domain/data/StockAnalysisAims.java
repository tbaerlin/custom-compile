/*
 * StockAnalysisAims.java
 *
 * Created on 27.09.2006 22:09:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface StockAnalysisAims {
    BigDecimal getMinimum();
    BigDecimal getMaximum();
    String getCurrency();
}
