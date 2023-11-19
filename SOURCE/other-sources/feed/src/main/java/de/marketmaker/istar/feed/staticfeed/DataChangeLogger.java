/*
 * NewKeyWriter.java
 *
 * Created on 20.09.13 14:04
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.staticfeed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataChangeListener;
import de.marketmaker.istar.feed.Vendorkey;

/**
 * Logs changes wrt. keys stored in a {@link de.marketmaker.istar.feed.FeedDataRepository}.
 * @author oflege
 */
@ManagedResource
public class DataChangeLogger implements
        FeedDataChangeListener, StaticSnapBuilder.DefinedTypeListener {

    private final Logger logger = LoggerFactory.getLogger("changed.key");

    @Override
    public void onDefinedType(FeedData data) {
        log("d", data.getVendorkey());
    }

    @Override
    public void onChange(FeedData data, ChangeType type) {
        log(getTypeMnemonic(type), data.getVendorkey());
    }

    protected void log(String change, Vendorkey vkey) {
        this.logger.info(change + " " + vkey.getType() + "." + vkey);
    }

    private String getTypeMnemonic(ChangeType type) {
        switch (type) {
            case CREATED:
                return "+";
            case DELETED:
                return "-";
            case REMOVED:
                return "x";
            case RESURRECTED:
                return "s";
            default:
                return type.name();
        }
    }

    /**
     * Logback does not rotate a log file until a new log entry is added that triggers the
     * Time/Size based log rotate policy. Schedule a call of this method to force log file rotation.
     */
    @ManagedOperation
    public void rotateNewKeysLog() {
        this.logger.info("");
    }
}
