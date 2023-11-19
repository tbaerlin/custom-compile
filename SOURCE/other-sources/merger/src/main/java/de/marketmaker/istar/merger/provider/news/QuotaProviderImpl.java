package de.marketmaker.istar.merger.provider.news;


import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeUtils;

import java.util.Arrays;

import static java.util.concurrent.TimeUnit.SECONDS;

public class QuotaProviderImpl implements QuotaProvider {

    private int quota = 100;

    private int interval = DateTimeConstants.SECONDS_PER_HOUR;

    private Cache cache;

    @Override
    public boolean acquire(String vwdId, int amount) {
        return acquire(vwdId, amount, System.currentTimeMillis());
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public void setIntervalSeconds(int interval) {
        this.interval = interval;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    boolean acquire(String vwdId, int amount, long now) {
        long oldestTimestamp = now - SECONDS.toMillis(this.interval);

        this.cache.acquireWriteLockOnKey(vwdId);
        try {
            long[] currentTimestamps = new long[]{};
            Element elem = this.cache.get(vwdId);
            if (elem != null) {
                currentTimestamps = (long[]) elem.getValue();
            }

            long[] validTimestamps = Arrays.stream(currentTimestamps).filter(t -> (t >= oldestTimestamp)).toArray();
            if (validTimestamps.length + amount > quota) {
                return false;
            }

            long[] newTimestamps = Arrays.copyOf(validTimestamps, validTimestamps.length + amount);
            Arrays.fill(newTimestamps, validTimestamps.length, newTimestamps.length, now);

            elem = new Element(vwdId, newTimestamps);
            elem.setTimeToIdle(this.interval);
            this.cache.put(elem);
            return true;
        } finally {
            this.cache.releaseWriteLockOnKey(vwdId);
        }
    }

}
