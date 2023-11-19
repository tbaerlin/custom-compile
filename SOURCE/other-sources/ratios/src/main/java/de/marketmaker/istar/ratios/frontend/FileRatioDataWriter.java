/*
 * FileRatioDataWriter.java
 *
 * Created on 09.03.12 09:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author oflege
 */
class FileRatioDataWriter {

    static final int ENUM_FLAG = 0x40;

    static final int LOCALIZED_FLAG = 0x80;

    static final int TYPE_MASK = 0x0F;

    private static final BitSet NOT_TO_WRITE = new BitSet();

    static {
        NOT_TO_WRITE.set(RatioFieldDescription.underlyingWkn.id());
        NOT_TO_WRITE.set(RatioFieldDescription.underlyingIsin.id());
        NOT_TO_WRITE.set(RatioFieldDescription.underlyingName.id());
    }

    static final Charset UTF8 = Charset.forName("UTF-8");

    private static final int STEP = 1000;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final File file;

    private FileChannel fc = null;

    private final ByteBuffer bb = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);

    private final TypeData data;

    private final int version;

    private final Reference2IntOpenHashMap<String> enumMappings = new Reference2IntOpenHashMap<>(1 << 14);

    private final BitSet ibs;

    private final BitSet qbs;

    private long numWritten = 0;

    FileRatioDataWriter(File file, TypeData data, int version) {
        this.file = file;
        this.data = data;
        this.version = version;
        this.ibs = getFields(true);
        this.qbs = getFields(false);
        this.enumMappings.defaultReturnValue(-1);
    }

    private File fileWithSuffix(final String suffix) {
        return new File(this.file.getParent(), this.file.getName() + suffix);
    }

    private void backupPrevious() {
        if (this.file.exists()) {
            final File oldFile = fileWithSuffix(".old");
            if (oldFile.exists() && !oldFile.delete()) {
                this.logger.warn("<backupPrevious> failed to delete " + oldFile.getAbsolutePath());
            }
            if (!this.file.renameTo(oldFile)) {
                this.logger.warn("<backupPrevious> failed to rename " + this.file.getAbsolutePath()
                        + " to " + oldFile.getAbsolutePath());
            }
        }
    }

    void write() throws IOException {
        final TimeTaker tt = new TimeTaker();
        final File tmpFile = new File(this.file.getParentFile(), this.file.getName() + ".tmp");
        final int num;
        final long metaStart;
        try (FileChannel tmp = new RandomAccessFile(tmpFile, "rw").getChannel()) {
            this.fc = tmp;
            this.bb.putInt(this.version);

            num = storeDatas();
            metaStart = this.numWritten;

            // in order to deal with changes of RatioFieldDescription, we have to remember which
            // fields were used for writing: store them at the beginning of the file
            writeBitSet(this.ibs);
            writeBitSet(this.qbs);
            writeEnums();
            this.bb.putLong(metaStart);
            writeBuffer();

        } catch (Exception e) {
            if (this.file.canRead() && !this.file.delete()) {
                this.logger.error("<write> failed to delete " + this.file.getAbsolutePath());
            }
            throw (e instanceof IOException) ? (IOException) e : new IOException(e);
        }

        backupPrevious();
        if (!tmpFile.renameTo(this.file)) {
            this.logger.warn("<write> failed to rename " + tmpFile.getAbsolutePath()
                    + " to " + this.file.getAbsolutePath());
        }

        this.logger.info("<writeData> " + num + " " + this.data.getType().name() + ", "
                + this.numWritten + "/" + (this.numWritten - metaStart) + " bytes, took " + tt);
    }

    private void writeBuffer() throws IOException {
        this.bb.flip();
        this.numWritten += this.bb.remaining();
        while (this.bb.hasRemaining()) {
            this.fc.write(this.bb);
        }
        this.bb.clear();
    }

    private int storeDatas() throws Exception {
        final List<RatioData> datas = this.data.withLock(this.data::getRatioDatasCopy);

        this.bb.putInt(datas.size());
        writeBuffer();

        for (int i = 0; i < datas.size(); i += STEP) {
            final int from = i;
            final int to = Math.min(datas.size(), from + STEP);
            this.data.withLock(() -> {
                for (RatioData ratioData : datas.subList(from, to)) {
                    write(ratioData);
                }
                return null;
            });
            TimeUnit.MILLISECONDS.sleep(1);
        }
        writeBuffer();

        return datas.size();
    }

    private void write(RatioData ratioData) throws IOException {
        if (this.bb.remaining() < 0x20000) {
            writeBuffer();
        }
        final int p = this.bb.position();
        this.bb.position(p + 4);
        this.bb.putLong(ratioData.getInstrumentRatios().getId());
        write(ratioData.getInstrumentRatios(), this.ibs);
        final QuoteRatios[] qrs = ratioData.getQuoteRatios();
        this.bb.put((byte) qrs.length);
        for (QuoteRatios qr : qrs) {
            this.bb.putLong(qr.getId());
            write(qr, this.qbs);
        }
        this.bb.putInt(p, this.bb.position() - p - 4);
    }

    private void write(PropertySupported psed, BitSet bs) throws IOException {
        this.bb.putLong(psed.getTimestamp());
        for (int i = bs.nextSetBit(0); i != -1; i = bs.nextSetBit(i + 1)) {
            final RatioFieldDescription.Field f = RatioFieldDescription.getFieldById(i);
            final PropertySupport ps = psed.propertySupport(i);
            switch (f.type()) {
                case BOOLEAN:
                    writeBoolean(ps.getBoolean(psed));
                    break;
                case DATE:
                case TIME:
                    this.bb.putInt(ps.getInt(psed));
                    break;
                case DECIMAL:
                case NUMBER:
                case TIMESTAMP:
                    // optimization, since many fields tend to be undefined.
                    // size of ratio-stk.rda was reduced by 1/3 when compared against unoptimized
                    final long l = ps.getLong(psed);
                    final boolean defined = l != Long.MIN_VALUE;
                    writeBoolean(defined);
                    if (defined) {
                        this.bb.putLong(l);
                    }
                    break;
                case ENUMSET:
                    // optimization, since many fields tend to be undefined.
                    // size of ratio-stk.rda was reduced by 1/3 when compared against unoptimized
                    final BitSet bitSet = ps.getBitSet(psed);
                    writeBoolean(!bitSet.isEmpty());
                    if (!bitSet.isEmpty()) {
                        RatioEnumSet.writeTo(bitSet, this.bb);
                    }
                    break;
                case STRING:
                    if (f.isLocalized()) {
                        final int numLocales = f.getLocales().length;
                        this.bb.put((byte) numLocales);
                        for (int j = 0; j < numLocales; j++) {
                            writeString(f, ps.getString(psed, j));
                        }
                    }
                    else {
                        writeString(f, ps.getString(psed));
                    }
                    break;
            }
        }
    }

    private void writeString(RatioFieldDescription.Field f, String s) throws IOException {
        writeBoolean(s != null);
        if (s != null) {
            if (f.isEnum()) {
                int idx = this.enumMappings.getInt(s);
                if (idx == -1) {
                    this.enumMappings.put(s, idx = this.enumMappings.size());
                }
                this.bb.putInt(idx);
            }
            else {
                writeString(s);
            }
        }
    }

    private void writeBoolean(final boolean b) {
        this.bb.put((byte) (b ? 1 : 0));
    }

    private void writeEnums() throws IOException {
        this.bb.putInt(this.enumMappings.size());
        for (String s : this.enumMappings.keySet()) {
            if (this.bb.remaining() < (16 + s.length() * 2)) {
                writeBuffer();
            }
            this.bb.putInt(this.enumMappings.getInt(s));
            writeString(s);
        }
        writeBuffer();
    }

    private boolean isApplicableField(int i) {
        final RatioFieldDescription.Field f = RatioFieldDescription.getFieldById(i);
        return f != null && f.isApplicableFor(this.data.getType());
    }

    private void writeBitSet(BitSet bs) throws IOException {
        this.bb.putInt(bs.cardinality());
        for (int i = bs.nextSetBit(0); i != -1; i = bs.nextSetBit(i + 1)) {
            RatioFieldDescription.Field field = RatioFieldDescription.getFieldById(i);
            this.bb.putInt(i);
            this.bb.put((byte) getEncodedType(field));
            if (field.isLocalized()) {
                final Locale[] locales = field.getLocales();
                this.bb.put((byte) locales.length);
                for (Locale locale : locales) {
                    writeString(locale.getLanguage());
                }
            }
        }
        writeBuffer();
    }

    private void writeString(final String s) {
        final byte[] src = s.getBytes(UTF8);
        if (src.length > 1 << 16) {
            throw new IllegalArgumentException("String too long: " + s);
        }
        this.bb.putShort((short) src.length).put(src);
    }

    private static int getEncodedType(final RatioFieldDescription.Field field) {
        int result = field.type().ordinal();
        if (field.isEnum()) {
            result |= ENUM_FLAG;
        }
        if (field.isLocalized()) {
            result |= LOCALIZED_FLAG;
        }
        return result;
    }


    private BitSet getFields(boolean forInstrument) {
        final BitSet result = new BitSet(RatioFieldDescription.getMaxFieldId());
        for (int i = 1; i <= RatioFieldDescription.getMaxFieldId(); i++) {
            final RatioFieldDescription.Field f = RatioFieldDescription.getFieldById(i);
            if (isApplicableField(i) && f.isInstrumentField() == forInstrument) {
                result.set(i);
            }
        }
        if (result.intersects(NOT_TO_WRITE)) {
            result.andNot(NOT_TO_WRITE);
        }
        return result;
    }

}
