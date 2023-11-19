/*
 * PortfolioEvaluationProvider.java
 *
 * Created on 08.08.2006 14:34:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.merger.user.EvaluatedPortfolio;
import de.marketmaker.istar.merger.user.Portfolio;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface PortfolioEvaluationProvider {
    EvaluatedPortfolio evaluate(Portfolio p, boolean withEmptyPositions);
}
