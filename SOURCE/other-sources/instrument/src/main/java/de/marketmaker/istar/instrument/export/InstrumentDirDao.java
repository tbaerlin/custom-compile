/*
 * InstrumentDirDao.java
 *
 * Created on 16.04.2010 14:24:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.instrument.protobuf.DomainContextDeserializer;
import de.marketmaker.istar.instrument.protobuf.InstrumentDeserializer;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements {@link InstrumentDao} based on access to a directory prepared by {@link InstrumentDirWriter}.
 *
 * <p>
 * Implementation referenced to InstrumentDaoFile with following differences:
 * <ul>
 * <li>accesses to one data file containing the actual template, context and instrument data
 * and another dedicated index file containing the instrument ids, offsets and lengths. The offsets
 * and lengths are position and length info into the aforementioned data file</li>
 * <li>an offset indicating where the latest instrument updates start</li>
 * <li>an extra array is introduced to hold the instruments' lengths</li>
 * <li>instrument ids provided in the data file do not need to be ascending ordered any more</li>
 * <li>offsets provided in the index file has to be in ascending order. This matches the writing order</li>
 * </ul>
 * @author zzhao
 * @since 1.2
 */
@ThreadSafe
public class InstrumentDirDao implements InstrumentDao, Closeable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File dir;

    private DataFile dataFile;

    private DomainContextImpl domainContext;

    private int[] instrumentIds;

    private long[] offsets;

    private int[] lengths;

    private long offsetOfLatelyUpdate;

    private long dataTail;

    /**
     * Creates new instance with data and index file in dir; the data file is expected to contain
     * a serialized DomainContext
     * @param dir contains data and index file
     * @throws Exception if initialization fails
     */
    public InstrumentDirDao(File dir) throws Exception {
        this(dir, null);
    }

    /**
     * Creates new instance with data and index file in dir; the data file
     * @param dir contains data and index file
     * @param domainContext if null, the data file in dir has to contain a serialized
     * DomainContext or an Exception will be thrown; if not null, it is assumed that the data
     * in the data file refers to domain objects in that context and the context in the file,
     * if any, is not deserialized.
     * @throws Exception if initialization fails
     */
    public InstrumentDirDao(File dir, DomainContextImpl domainContext) throws Exception {
        this.dir = dir;
        this.domainContext = domainContext;

        InstrumentSystemUtil.validateDir(this.dir);
        this.dataFile = new DataFile(InstrumentSystemUtil.getDataFile(this.dir), true);
        this.dataTail = this.dataFile.size();

        boolean offsetUnsignedInt = false;
        final TimeTaker tt = new TimeTaker();
        if (this.domainContext == null) {
            final int contextOffset = this.dataFile.readInt();
            if (contextOffset < InstrumentDirWriter.CONTEXT_OFFSET) {
                offsetUnsignedInt = true;
            }
            final int dataOffset = this.dataFile.readInt();
            this.offsetOfLatelyUpdate = offsetUnsignedInt
                ? this.dataFile.readInt() & 0xFFFFFFFFL
                : this.dataFile.readLong();

            this.logger.info("<init> contextOffset=" + contextOffset
                + ", dataOffset=" + dataOffset
                + ", updateOffset=" + this.offsetOfLatelyUpdate);

            final int length = dataOffset - contextOffset;
            this.domainContext = readContext(length, contextOffset);
            this.logger.info("<init> read context (" + length + " bytes) took " + tt);
        }

        readOffsets(InstrumentSystemUtil.getIndexFile(this.dir), offsetUnsignedInt);
        this.logger.info("<init> read " + this.instrumentIds.length
            + " offsets, last is " + getLastOffset() + ", took " + tt);
    }

    /**
     * Create a new instance that re-uses this object's fastMapping and instruction context
     * but reads instrument information from dir, which should contain an incremental instrument
     * update.
     */
    public InstrumentDirDao forUpdate(File dir) throws Exception {
        return new InstrumentDirDao(dir, this.domainContext);
    }

    private long getLastOffset() {
        return (this.offsets.length > 0) ? this.offsets[this.offsets.length - 1] : -1;
    }

    private void readOffsets(File idxFile, boolean offsetUnsignedInt) throws IOException {
        try (InstrumentIOLIterator it = new InstrumentIOLIterator(idxFile, offsetUnsignedInt)) {
            int numEntries = it.getNumEntries();

            this.instrumentIds = new int[numEntries];
            this.offsets = new long[numEntries];
            this.lengths = new int[numEntries];

            int counter = -1;
            while (it.hasNext()) {
                IOL iol = it.next();
                ++counter;
                this.instrumentIds[counter] = (int) iol.iid;
                this.offsets[counter] = iol.offset;
                this.lengths[counter] = iol.length;
            }
        } catch (IOException e) {
            this.logger.error("<readOffsets> failed reading instrument index file", e);
            throw e;
        }
    }

    private DomainContextImpl readContext(int length, long offset) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(length);
        fillSync(offset, buffer);
        return new DomainContextDeserializer().deserialize(buffer);
    }

    public void close() throws IOException {
        synchronized (this) {
            IoUtils.close(this.dataFile);
            this.logger.info("<close> " + InstrumentSystemUtil.getDataFile(this.dir));
            this.dataFile = null;
        }
    }

    public List<Long> getInstruments() {
        final List<Long> ids = new ArrayList<>(this.instrumentIds.length);
        for (final long id : this.instrumentIds) {
            ids.add(id);
        }

        return ids;
    }

    public int size() {
        return this.instrumentIds.length;
    }

    public Iterator<Instrument> getUpdates() {
        return new MyIterator(this.offsetOfLatelyUpdate);
    }

    public long[] validate(long[] iids) {
        final BitSet bs = new BitSet(iids.length);

        for (int i = 0; i < iids.length; i++) {
            if (getInstrumentIndex(iids[i]) < 0) {
                bs.set(i);
            }
        }
        final long[] result = new long[bs.cardinality()];
        int n = 0;
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            result[n++] = iids[i];
        }
        return result;
    }

    public Instrument getInstrument(long id) {
        int idx = getInstrumentIndex(id);
        if (idx < 0) {
            return null;
        }

        return getInstrumentAt(idx);
    }

    private int getInstrumentIndex(long id) {
        return Arrays.binarySearch(this.instrumentIds, (int) id);
    }

    private Instrument getInstrumentAt(int index) {
        final long offset = this.offsets[index];
        final int length = this.lengths[index];
        final ByteBuffer buffer = ByteBuffer.allocate(length);

        try {
            fillSync(offset, buffer);
            return readInstrument(buffer);
        } catch (Exception e) {
            this.logger.error("<getInstrument> failed for iid: " + this.instrumentIds[index]
                    + " at index: " + index + " offset: " + offset + " length: " + length, e);
            return null;
        }
    }

    private ByteBuffer fillSync(long offset, ByteBuffer bb) throws IOException {
        final int length = (int) Math.min(bb.remaining(), this.dataTail - offset);
        try {
            synchronized (this) {
                if (this.dataFile == null) {
                    throw new IOException("already closed");
                }
                this.dataFile.seek(offset);
                this.dataFile.read(bb, length);
            }
        } catch (IOException e) {
            bb.clear();
            throw e;
        }
        bb.flip();
        return bb;
    }

    private Instrument readInstrument(ByteBuffer buffer) throws IOException {
         return new InstrumentDeserializer(this.domainContext).deserialize(buffer);
    }

    public Iterator<Instrument> iterator() {
        return new MyIterator(0L);
    }

    public DomainContextImpl getDomainContext() {
        return this.domainContext;
    }

    public List<Country> getCountries() {
        return domainContext.getCountries();
    }

    public List<Currency> getCurrencies() {
        return domainContext.getCurrencies();
    }

    public List<Market> getMarkets() {
        return domainContext.getMarkets();
    }

    private class MyIterator implements Iterator<Instrument> {
        private int i = 0;

        private final ByteBuffer bb = (ByteBuffer) ByteBuffer.allocate(1024 * 1024).flip();

        private long bbOffset;

        private int[] posArray;

        private MyIterator(long fromOffset) {
            this.bbOffset = fromOffset;
            this.posArray = new int[InstrumentDirDao.this.offsets.length];
            for (int i = 0; i < this.posArray.length; i++) {
                this.posArray[i] = i;
            }
            ArraysUtil.sort(this.posArray, InstrumentDirDao.this.offsets);
            if (fromOffset > 0L) {
                int fromIdx = ArraysUtil.binarySearch(InstrumentDirDao.this.offsets, fromOffset,
                        this.posArray);
                if (fromIdx < 0) {
                    throw new IllegalStateException("Fatal: first offset of lately updates is wrong: "
                            + fromOffset + ", check instrument data file");
                }
                this.i = fromIdx;
            }
            else {
                this.i = 0;
            }
        }

        public boolean hasNext() {
            return this.i < instrumentIds.length;
        }

        public Instrument next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final long offset = InstrumentDirDao.this.offsets[posArray[this.i]];
            final int length = InstrumentDirDao.this.lengths[posArray[this.i++]];
            if (bb.position() + this.bbOffset != offset) {
                bb.position(Math.min(bb.limit(), (int) (offset - this.bbOffset)));
            }
            if (bb.remaining() < length) {
                bb.clear();
                try {
                    fillSync(offset, bb);
                    this.bbOffset = offset;
                } catch (IOException e) {
                    logger.warn("<next> failed to fill buffer from " + offset, e);
                    return null;
                }
            }
            int bbLimit = bb.limit();
            bb.limit(bb.position() + length);
            Instrument result;
            try {
                result = readInstrument(bb);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            bb.limit(bbLimit);
            return result;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static void main(String[] args) throws Exception {
        InstrumentDirDao dao = new InstrumentDirDao(new File(args[0]));
        int k = 0;
        for (Iterator<Instrument> it = dao.getUpdates(); it.hasNext() && k < 10; k++) {
            System.out.println(it.next().getId());
        }
        for (int i = 1; i < args.length; i++) {
            Instrument instrument = dao.getInstrument(Long.parseLong(args[i]));
            System.out.println(args[i]+ ": " + instrument);
        }

        int n = 0;

        for (Instrument instrument : dao) {
            if (++n > 1000) {
                break;
            }
        }

        System.out.println("<ensureDaoWorks> dao is ok");
        dao.close();
    }
}
