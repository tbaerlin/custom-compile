/*
 * MinuteTicker.java
 *
 * Created on 22.04.13 09:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import de.marketmaker.istar.domain.data.TickType;

/**
 * @author zzhao
 */
public interface MinuteTicker {

    EnumMap<TickType, File> produceMinuteTicks(File tickDir, Map<TickType, File> workDirs)
            throws IOException;
}
