package de.marketmaker.istar.feed;

/**
 * Interface for classes that want to listen for changes in FeedMarketRepository
 */
public interface FeedMarketChangeListener {
    /**
     * Types of changes
     */
    enum ChangeType {
        CREATED, REMOVED
    }

    /**
     * This method is called by FeedMarketRepository if a FeedMarket instance is either
     * created or removed
     * @param market FeedMarket instance that is affected by the change
     * @param type Type of change
     */
    void onChange(FeedMarket market, ChangeType type);
}
