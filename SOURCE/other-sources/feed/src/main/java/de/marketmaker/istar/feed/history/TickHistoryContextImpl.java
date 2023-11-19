/*
 * HistoryContextImpl.java
 *
 * Created on 19.05.2014 13:52
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.TickType;

/**
 * @author zzhao
 */
public class TickHistoryContextImpl implements TickHistoryContext {

    public static final LocalDate GENESIS = new LocalDate(2010, 1, 1);

    /**
     * Maximum length 16M. If ever increased, pay attention to buffers used in history gatherer:
     * @see HistoryGathererTickBase
     */
    public static final int LENGTH_BITS = 24;

    public static final String ENV_KEY_GENESIS = "genesis";

    public static final String ENV_KEY_LENGTH_BITS = "lengthBits";

    public static final String ENV_KEY_MARKETS_WITH_NEGATIVE_TICKS = "marketsWithNegativeTicks";

    public static final String ENV_KEY_MARKET_FILTER = "marketFilter";

    public static final IOFileFilter FILES_2_EXCLUDE = FileFilterUtils.makeFileOnly(
            FileFilterUtils.and(
                    FileFilterUtils.notFileFilter(FileFilterUtils.prefixFileFilter("PROF")),
                    FileFilterUtils.or(
                            FileFilterUtils.suffixFileFilter(".tda"),
                            FileFilterUtils.suffixFileFilter(".td3"),
                            FileFilterUtils.suffixFileFilter(".tdz")
                    )
            )
    );

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected IOFileFilter marketFilter = FILES_2_EXCLUDE;

    protected SymbolFilter symbolFilter;

    private Set<String> marketWithNegativeTicks = Collections.emptySet();

    private LocalDate genesis = GENESIS;

    private OffsetLengthCoder offsetLengthCoder = new OffsetLengthCoder(LENGTH_BITS);

    public void setSymbolFilter(SymbolFilter symbolFilter) {
        this.symbolFilter = symbolFilter;
    }

    public void setGenesis(LocalDate genesis) {
        this.genesis = genesis;
    }

    public void setLengthBits(int lengthBits) {
        this.offsetLengthCoder = new OffsetLengthCoder(lengthBits);
    }

    public void setMarketFilter(File propFile) throws IOException {
        if (null == propFile || !propFile.exists()) {
            return;
        }
        final TreeSet<String> set = new TreeSet<>(FileUtils.readLines(propFile, "UTF-8"));
        this.logger.info("<setMarketFilter> " + set);
        if (!set.isEmpty()) {
            this.marketFilter = FileFilterUtils.and(FILES_2_EXCLUDE,
                    FileFilterUtils.asFileFilter(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return set.contains(name.substring(0, name.indexOf("-")));
                        }
                    })
            );
        }
    }

    public void setMarketsWithNegativeTicks(File file) throws IOException {
        if (null == file || !file.exists()) {
            return;
        }
        this.marketWithNegativeTicks = new TreeSet<>(FileUtils.readLines(file, "UTF-8"));
        this.logger.info("<setMarketsWithNegativeTicks> " + this.marketWithNegativeTicks);
    }

    @Override
    public FileFilter getMarketFileFilter() {
        return this.marketFilter;
    }

    @Override
    public boolean isNegativePricePossible(String marketName) {
        return this.marketWithNegativeTicks.contains(marketName);
    }

    @Override
    public boolean isRelevantSymbol(ByteString symbol) {
        return null == this.symbolFilter || this.symbolFilter.accept(symbol);
    }

    @Override
    public LocalDate getGenesis() {
        return this.genesis;
    }

    @Override
    public int daysToReserveInPatch() {
        return HistoryUtil.daysFromBegin(getGenesis(), new LocalDate().minusMonths(13).plusDays(1));
    }

    @Override
    public OffsetLengthCoder getOffsetLengthCoder() {
        return this.offsetLengthCoder;
    }

    @Override
    public byte[] postProcessTickData(TickType tickType, byte[] tickData) throws IOException {
        return TickType.TRADE == tickType ? tickData : EntryFactory.compress(tickData);
    }

    public static TickHistoryContext fromEnv() throws Exception {
        TickHistoryContextImpl ctx = new TickHistoryContextImpl();
        ctx.setGenesis(DateUtil.yyyyMmDdToLocalDate(Integer.parseInt(
                System.getProperty(TickHistoryContextImpl.ENV_KEY_GENESIS, "20100101"))));
        ctx.setLengthBits(Integer.parseInt(
                System.getProperty(TickHistoryContextImpl.ENV_KEY_LENGTH_BITS, "24")));
        if (StringUtils.isNotBlank(
                System.getProperty(TickHistoryContextImpl.ENV_KEY_MARKETS_WITH_NEGATIVE_TICKS))) {
            ctx.setMarketsWithNegativeTicks(new File(
                    System.getProperty(TickHistoryContextImpl.ENV_KEY_MARKETS_WITH_NEGATIVE_TICKS)));
        }

        if (StringUtils.isNotBlank(System.getProperty(TickHistoryContextImpl.ENV_KEY_MARKET_FILTER))) {
            ctx.setMarketFilter(new File(
                    System.getProperty(TickHistoryContextImpl.ENV_KEY_MARKET_FILTER)));
        }

        return ctx;
    }
}
