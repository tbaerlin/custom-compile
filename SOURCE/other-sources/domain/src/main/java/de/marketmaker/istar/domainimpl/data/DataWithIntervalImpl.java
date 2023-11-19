package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.DataWithInterval;
import org.joda.time.ReadableInterval;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DataWithIntervalImpl<K> implements DataWithInterval<K> {
    private final K data;
    private final ReadableInterval interval;

    public DataWithIntervalImpl(K data, ReadableInterval interval) {
        this.data = data;
        this.interval = interval;
    }

    public K getData() {
        return data;
    }

    public ReadableInterval getInterval() {
        return interval;
    }
}
