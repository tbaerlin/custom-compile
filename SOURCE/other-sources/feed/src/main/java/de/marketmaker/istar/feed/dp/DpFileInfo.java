/*
 * DpFileStatistics.java
 *
 * Created on 20.08.2002 12:49:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Captures information about a DpFile.
 * This is a <em>public</em> class because it is used by jmx management components.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ThreadSafe
public class DpFileInfo implements Serializable {
    static final long serialVersionUID = -3658205145406356904L;

    //SimpleDateFormat is not thread-safe, we have to synchronize access to it
    @GuardedBy("this")
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");

    private final String inputFile;

    private final long created;

    @GuardedBy("this")
    private long lastUpdate;

    @GuardedBy("this")
    private long lastUpdateTime;

    public DpFileInfo(String inputFile) {
        this.inputFile = inputFile;
        this.created = System.currentTimeMillis();
    }

    public DpFileInfo(DpFileInfo toCopy) {
        synchronized (toCopy) {
            this.inputFile = toCopy.inputFile;
            this.created = toCopy.created;
            this.lastUpdate = toCopy.lastUpdate;
            this.lastUpdateTime = toCopy.lastUpdateTime;
        }
    }

    public synchronized String toString() {
        return "inputFile: " + this.inputFile
                + ", created: " + getCreatedStr()
                + ", lastUpdate: " + getLastUpdateStr()
                + ", took: " + this.lastUpdateTime + "ms";
    }

    public synchronized String getLastUpdateStr() {
        return (this.lastUpdate) != 0 ? this.sdf.format(new Date(this.lastUpdate)) : "n/a";
    }

    public synchronized String getCreatedStr() {
        return this.sdf.format(new Date(this.created));
    }

    public long getCreated() {
        return created;
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getInputFileName() {
        final int p = this.inputFile.indexOf("dp2in/");
        if (p > 0) {
            return this.inputFile.substring(p + "dp2in/".length());
        }
        return this.inputFile.substring(this.inputFile.lastIndexOf('/') + 1);
    }

    public synchronized long getLastUpdate() {
        return lastUpdate;
    }

    public synchronized long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public synchronized void setUpdate(long lastUpdate, long lastUpdateTime) {
        this.lastUpdate = lastUpdate;
        this.lastUpdateTime = lastUpdateTime;
    }

}
