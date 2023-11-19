/*
 * GisResearchContentFlagsPostProcessor.java
 *
 * Created on 24.04.14 15:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.domain.instrument.ContentFlags;

/**
 * Processes content flags as written by
 * <code>de.marketmaker.istar.merger.provider.gisresearch.GisResearchContentFlagsWriter</code>.
 *
 * @author oflege
 */
public class GisResearchContentFlagsPostProcessor implements ContentFlagsPostProcessor,
        InitializingBean {

    public static long encodeIidAndFlag(long iid, int flags) {
        return (iid << 8) + flags;
    }

    private static long decodeIid(long encoded) {
        return (encoded >> 8);
    }

    private static long decodeFlags(long encoded) {
        return (encoded & 0xF) << ContentFlags.Flag.ResearchDzHM1.ordinal();
    }

    private File flagsFile;

    private LongBuffer lb;

    private long currentIid;

    private long flags;

    public void setFlagsFile(File flagsFile) {
        this.flagsFile = flagsFile;
    }

    void setLb(LongBuffer lb) {
        this.lb = lb;
    }

    @Override
    public void postProcessFlags(long iid, long qid, long[] flags) {
        while (currentIid < iid) {
            if (!lb.hasRemaining()) {
                this.currentIid = Long.MAX_VALUE;
                return;
            }
            long iidAndFlags = this.lb.get();
            this.currentIid = decodeIid(iidAndFlags);
            this.flags = decodeFlags(iidAndFlags);
        }
        if (this.currentIid == iid) {
            flags[0] |= this.flags;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.lb == null) {
            this.lb = ByteBuffer.wrap(FileCopyUtils.copyToByteArray(this.flagsFile)).asLongBuffer();
        }
    }
}
