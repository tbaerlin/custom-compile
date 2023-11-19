/*
 * TickFileReader.java
 *
 * Created on 01.08.14 14:59
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickFiles;

import static de.marketmaker.istar.feed.ordered.tick.FileTickStore.fillData;

/**
 * To be used when many/all ticks from a single compressed tick file need to be processed.
 * Instances of this class should be obtained by calling
 * {@link de.marketmaker.istar.feed.ordered.tick.TickDirectory#getTickFileReader(String)} or
 * {@link de.marketmaker.istar.feed.ordered.tick.TickDirectory#getTickFileReader(java.io.File)}.
 * Creating an instance of this class will open the tick file and, if available, the corrections
 * file. Therefore, clients have to call {@link #close()} when they are done to clean up resources.
 * <p>
 * Ticks returned by this class will prefer corrected ticks over those stored in the
 * tdz tick file.
 *
 * @author oflege
 */
public class TickFileReader implements Closeable {

    private final File file;

    private final FileChannel fc;

    private final FileChannel correctionsFc;

    private final Map<ByteString, TickDirectory.CorrectionInfo> correctionInfos;

    TickFileReader(File f, File cf, Map<ByteString, TickDirectory.CorrectionInfo> correctionInfos) throws IOException {
        if (!FileTickStore.canHandle(f)) {
            throw new IllegalArgumentException();
        }
        this.file = f;
        this.fc = new RandomAccessFile(f, "r").getChannel();
        this.correctionInfos = correctionInfos;
        if (this.correctionInfos != null) {
            this.correctionsFc = new RandomAccessFile(cf, "r").getChannel();
        }
        else {
            this.correctionsFc = null;
        }
    }

    public File getFile() {
        return file;
    }

    public String getMarketName() {
        return TickFiles.getMarketName(this.file);
    }

    public String getMarketBaseName() {
        return TickFiles.getMarketBaseName(this.file);
    }

    public int getDay() {
        return TickFiles.getDay(this.file);
    }

    public AbstractTickRecord.TickItem.Encoding getEncoding() {
        return TickFiles.getItemType(this.file);
    }

    /**
     * Read index and invoke callback on <code>handler</code> for every entry
     * @param handler to be informed about index entries
     * @return the position (offset) of the index in the underlying tick file.
     * @throws IOException
     */
    public long readIndex(final TickFileIndexReader.IndexHandler handler) throws IOException {
        if (this.correctionInfos == null) {
            return new TickFileIndexReader(this.fc).readEntries(handler);
        }
        return new TickFileIndexReader(this.fc).readEntries((key, position, length) -> {
            TickDirectory.CorrectionInfo info = correctionInfos.get(key);
            if (info != null) {
                handler.handle(key, info.address, info.length);
            }
            else {
                handler.handle(key, position, length);
            }
        });
    }

    public byte[] readTicks(long address, int length) throws IOException {
        final ByteBuffer bb = ByteBuffer.allocate(length);
        if (TickWriter.isFileAddress(address)) {
            fillData(this.fc, address, bb);
        }
        else {
            // address does not have the FILE_ADDRESS_FLAG set, which means it refers to the
            // correstions file (see TickDirectory#CorrectionInfo). For reading, we have to
            // add that flag again to turn the address into a valid file address.
            fillData(this.correctionsFc, address | TickWriter.FILE_ADDRESS_FLAG, bb);
        }
        return bb.array();
    }

    @Override
    public void close() throws IOException {
        if (!IoUtils.close(this.fc) || !IoUtils.close(this.correctionsFc)) {
            throw new IOException();
        }
    }
}
