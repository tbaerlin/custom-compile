/*
 * DelayProviderUtil.java
 *
 * Created on 31.03.11 08:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.delay;

import de.marketmaker.istar.feed.FeedData;

/**
 * @author oflege
 */
public class DelayProviderUtil {
    private DelayProviderUtil() {
    }

    /**
     * @param delayProvider a DelayProvider
     * @param data to be tested
     * @param realtime whether realtime access is requested
     * @return true, iff realtime is true or data's delay == 0, false iff realtime is false and
     * data's delay > 0, and null otherwise.
     */
    public static Boolean isRealtime(DelayProvider delayProvider, FeedData data, boolean realtime) {
        if (realtime || delayProvider == null) {
            return Boolean.TRUE;
        }
        final int delayInSeconds = delayProvider.getDelayInSeconds(data);
        if (delayInSeconds < 0) {
            return null;
        }
        return delayInSeconds == 0 ? Boolean.TRUE : Boolean.FALSE;
    }

}
