package de.marketmaker.istar.merger.web.easytrade;

/**
 * @author Ulrich Maurer
 *         Date: 05.07.12
 */
public interface HasMarketStrategy {
    /**
     * Market strategy used to select a proper quote if the given symbol identifies an instrument.
     * If not given, a customer specific default market strategy is used.
     * @return a market strategy name
     */
    String getMarketStrategy();
}
