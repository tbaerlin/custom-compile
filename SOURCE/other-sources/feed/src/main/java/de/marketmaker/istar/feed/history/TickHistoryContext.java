/*
 * HistoryContext.java
 *
 * Created on 19.05.2014 13:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.joda.time.LocalDate;

import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.domain.data.TickType;

/**
 * @author zzhao
 */
public interface TickHistoryContext {

    FileFilter getMarketFileFilter();

    boolean isNegativePricePossible(String marketName);

    boolean isRelevantSymbol(ByteString symbol);

    LocalDate getGenesis();

    int daysToReserveInPatch();

    OffsetLengthCoder getOffsetLengthCoder();

    byte[] postProcessTickData(TickType tickType, byte[] tickData) throws IOException;
}
