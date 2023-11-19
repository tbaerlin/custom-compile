/*
 * InstrumentWriter.java
 *
 * Created on 01.12.2005 13:01:33
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domainimpl.DomainContext;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import de.marketmaker.istar.instrument.protobuf.DomainContextSerializer;
import de.marketmaker.istar.instrument.protobuf.InstrumentSerializer;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes instrument related data and meta data into a directory(<b>Attention: the given directory
 * would be cleaned before writing.</b>) structured as following:
 * <ol>
 * <li>instrument data file named <tt>{@value InstrumentSystemUtil#FILE_NAME_DATA}</tt>
 * <dl>
 * <dt>header(@offset 0), <em>Not present if the instrument writer in update mode</em>
 * <dd>contains 3 int values representing the offset of the domain context. the offset of
 * instrument data and the offset of the updated instrument data(reserved for appending updated
 * instrument data).
 * <dt>domain context, <em>Not present if the instrument writer in update mode</em>
 * <dd>serialized domain context
 * <dt>instrument data, <em>From offset 0 if the instrument writer in update mode</em>
 * <dd>serialized instrument data
 * </dl>
 * </li>
 * <li>instrument index file named <tt>{@value InstrumentSystemUtil#FILE_NAME_IDX}</tt>
 * <dl>
 * <dt>index
 * <dd>triple values: an instrument id of type long, an offset of type int and a length of type int,
 * sorted by instrument id, ascending.
 * </dl>
 * </li>
 * </ol>
 * To access instrument data created by this writer, please use
 * {@link InstrumentDirDao}.
 *
 * @author zzhao
 * @since 1.2
 */
public class InstrumentDirWriter implements AutoCloseable {

    public static final int LAST_UPDATE_OFFSET = 8;

    // domain context offset int, data offset int, offset of lately update long
    public static final int CONTEXT_OFFSET = Integer.BYTES + Integer.BYTES + Long.BYTES;

    public static InstrumentDirWriter create(File dir, File censorDir) throws Exception {
        return new InstrumentDirWriter(dir, false, censorDir);
    }

    public static InstrumentDirWriter createUpdateWriter(File dir,
            File censorDir) throws Exception {
        return new InstrumentDirWriter(dir, true, censorDir);
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<IOL> blocks = new ArrayList<>();

    private final File dir;

    private final DataFile dataFile;

    private int dataOffset;

    private final boolean updateMode;

    private final UpdateStatusCensor updateStatusCensor;

    private final InstrumentSerializer instrumentSerializer;

    private InstrumentDirWriter(File dir, boolean updateMode, File censorDir) throws IOException {
        if (null == dir || !dir.exists() || !dir.canWrite()) {
            throw new IllegalArgumentException("Invalid instrument file dir");
        }
        this.instrumentSerializer = new InstrumentSerializer();
        this.dir = dir;
        this.updateMode = updateMode;
        this.updateStatusCensor = new UpdateStatusCensor(censorDir);

        File df = InstrumentSystemUtil.getDataFile(this.dir);
        InstrumentSystemUtil.deleteIfExistsFailException(df);
        File idxFile = InstrumentSystemUtil.getIndexFile(this.dir);
        InstrumentSystemUtil.deleteIfExistsFailException(idxFile);

        this.dataFile = new DataFile(df, false);

        if (!updateMode) {
            this.dataFile.seek(CONTEXT_OFFSET);
        }

        this.updateStatusCensor.open(this.updateMode);
    }

    public void close() throws IOException {
        ensureContextWritten();
        writeIndex();
        writeOffsets();
        close(this.dataFile);
        this.updateStatusCensor.close();
    }

    private void close(DataFile f) {
        IoUtils.close(f);
    }

    private void writeIndex() throws IOException {
        this.blocks.sort(null);
        try (IOLWriter iw = new IOLWriter(InstrumentSystemUtil.getIndexFile(this.dir))) {
            for (IOL iol : this.blocks) {
                iw.append(iol);
            }
        } catch (IOException e) {
            this.logger.error("<writeIndex> failed", e);
            throw e;
        }
    }

    private void writeOffsets() throws IOException {
        if (!this.updateMode) {
            this.dataFile.seek(0);
            this.dataFile.writeInt(CONTEXT_OFFSET);
            this.dataFile.writeInt(this.dataOffset);
            this.dataFile.writeLong(0);
        }
    }

    public void write(DomainContext context) throws Exception {
        if (!this.updateMode) {
            if (!this.blocks.isEmpty() || this.dataOffset != 0) {
                throw new IllegalStateException("data written before context is written");
            }
            final ByteBuffer bb = ByteBuffer.wrap(
                    new DomainContextSerializer().serialize((DomainContextImpl) context));

            this.dataFile.write(bb);
            this.dataOffset = (int) this.dataFile.position();
        }
    }

    public boolean write(Instrument instrument) throws Exception {
        ensureContextWritten();

        final ByteBuffer bb = ByteBuffer.wrap(
                this.instrumentSerializer.serialize((InstrumentDp2) instrument));

        if (this.updateStatusCensor.censor(instrument.getId(), bb)) {
            this.blocks.add(new IOL(instrument.getId(), this.dataFile.position(), bb.remaining()));
            this.dataFile.write(bb);
            return true;
        }
        else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<write> instrument: " + instrument + " not changed");
            }
            return false;
        }
    }

    private void ensureContextWritten() throws IOException {
        if (!this.updateMode && this.dataOffset == 0) {
            throw new IOException("Context not written yet");
        }
    }
}
