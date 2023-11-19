/*
 * HistoryGathererI.java
 *
 * Created on 23.10.12 16:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

/**
 * @author zzhao
 */
public interface HistoryGatherer {
    void updateUnits(File dir, EnumSet<HistoryUnit> units) throws IOException;
}
