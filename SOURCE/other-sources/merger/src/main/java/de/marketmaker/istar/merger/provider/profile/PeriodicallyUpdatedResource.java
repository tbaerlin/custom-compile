/*
 * PeriodicallyUpdatedResource.java
 *
 * Created on 23.07.12 12:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import de.marketmaker.istar.common.monitor.Resource;

/**
 * @author Markus Dick
 */
public class PeriodicallyUpdatedResource extends Resource {
    private final long period;
    volatile long lastTime;

    PeriodicallyUpdatedResource(String resourceKey, long periodInMinutes) throws Exception {
        super(resourceKey);
        period = 1000 * 60 * periodInMinutes;
        lastTime = System.currentTimeMillis();
    }

    /**
     * The time this was last modified.
     */
    @Override
    public long lastModified() {
        if(System.currentTimeMillis() - lastTime > period) {
            lastTime = System.currentTimeMillis();
            return lastTime;
        }
        return lastTime;
    }
}
