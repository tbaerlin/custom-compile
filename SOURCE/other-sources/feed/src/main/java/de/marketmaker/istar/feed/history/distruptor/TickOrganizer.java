/*
 * WorkItemIterator.java
 *
 * Created on 17.04.13 10:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history.distruptor;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.history.TickHistoryContext;
import de.marketmaker.istar.feed.ordered.tick.TickDirectory;

/**
 * @author zzhao
 */
class TickOrganizer implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Iterator<File> itFiles;

    private final EnumSet<TickType> tickTypes;

    private final TickHistoryContext context;

    private final TickDirectory tickDirectory;

    private TickExtractor extractor;

    private Iterator<IndexItem> itIndex;

    private boolean negativeTicksPossible;

    TickOrganizer(TickDirectory tickDirectory, TreeSet<File> tickFiles, EnumSet<TickType> tickTypes,
            TickHistoryContext ctx) {
        this.tickDirectory = tickDirectory;
        this.itFiles = tickFiles.iterator();
        this.tickTypes = tickTypes;
        this.context = ctx;
    }

    private boolean extract(File tickFile) {
        IoUtils.close(this.extractor); // close last extractor

        try {
            this.extractor = new TickExtractor(tickDirectory.getTickFileReader(tickFile), tickTypes, this.context);
            this.negativeTicksPossible = fromMarketName(this.extractor.getMarketName());
            this.extractor.readVendorKeys();
            this.itIndex = this.extractor.getOaLs().iterator();
        } catch (Exception e) {
            this.logger.error("<extract> failed extracting from {}",
                    tickFile.getAbsolutePath(), e);
            return false;
        }

        if (!this.itIndex.hasNext()) {
            this.logger.warn("<extract> no vendor keys found in {}", tickFile.getAbsolutePath());
            return false;
        }

        return true;
    }

    private boolean fromMarketName(ByteString marketName) {
        return this.context.isNegativePricePossible(marketName.toString());
    }

    boolean hasWork() {
        if (null != this.itIndex && this.itIndex.hasNext()) {
            return true;
        }

        while (this.itFiles.hasNext()) {
            final File tickFile = itFiles.next();
            if (extract(tickFile)) {
                return true;
            }
        }

        return false;
    }

    void assignWork(RequestItem requestItem) {
        final IndexItem item = this.itIndex.next();
        requestItem.withSymbol(item.getSymbol(), this.extractor.getTickItemEncoding(),
                this.negativeTicksPossible);
        this.extractor.getTickData(item, requestItem);
    }

    @Override
    public void close() throws IOException {
        IoUtils.close(this.extractor);
    }
}
