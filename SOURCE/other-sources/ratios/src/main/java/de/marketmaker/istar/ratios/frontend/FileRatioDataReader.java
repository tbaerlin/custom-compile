/*
 * FileRatioDataReader.java
 *
 * Created on 09.03.12 09:11
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * Reads ratio data from a file. If that file's version is at least 5 (i.e., it contains meta information
 * about the fields stored in that file), the following differences between the current RatioFieldDescription
 * and the one used to write the file can be resolved:
 * <ul>
 * <li>String fields may switch from enum to non-enum and vice versa</li>
 * <li>String fields may switch from localized to non-localized and vice versa.
 * In the former case, the localized string with locale index 0 will be used as new non-localized string;
 * in the other case, the non-localized string will be mapped to the one with locale index 0</li>
 * <li>the order of locales for localized strings may change, or locales may be added or removed</li>
 * </ul>
 * @author oflege
 */
class FileRatioDataReader {

    private long metaStart;

    /**
     * Information about a field stored in the file to be read; may be different from the
     * current definition of that field in the RatioFieldDescription
     */
    private static class FileField {
        private final int id;

        private final RatioFieldDescription.Type type;

        private final Locale[] locales;

        private final boolean isEnum;

        private final boolean localesAreCurrent;

        private final RatioFieldDescription.Field field;

        static FileField create(int id, RatioFieldDescription.Type type, Locale[] locales,
                boolean anEnum) {
            return new FileField(id, type, locales, anEnum);
        }

        static boolean areSameLocalesAsInField(Locale[] locales,
                RatioFieldDescription.Field field) {
            if (locales == null || field == null ||
                    field.getLocales() == null || locales.length != field.getLocales().length) {
                return false;
            }
            for (int i = 0; i < locales.length; i++) {
                if (!locales[i].getLanguage().equals(field.getLocales()[i].getLanguage())) {
                    return false;
                }
            }
            return true;
        }

        private FileField(int id, RatioFieldDescription.Type type, Locale[] locales,
                boolean anEnum) {
            this.id = id;
            this.type = type;
            this.locales = locales;
            this.isEnum = anEnum;
            this.field = RatioFieldDescription.getFieldById(id);
            this.localesAreCurrent = areSameLocalesAsInField(this.locales, this.field);
        }

        boolean isLocalized() {
            return this.locales != null;
        }

        int getLocaleIndex(int i) {
            if (this.localesAreCurrent) {
                return i;
            }
            final Locale[] fieldLocales = this.field.getLocales();
            for (int j = 0; j < fieldLocales.length; j++) {
                if (this.locales[i].getLanguage().equals(fieldLocales[j].getLanguage())) {
                    return j;
                }
            }
            return -1;
        }
    }

    private static final int MIN_CAN_READ_VERSION = 8;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FileChannel fc;

    private final InstrumentTypeEnum type;

    private final File file;

    private final Consumer<RatioData> consumer;

    private ByteBuffer bb = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);

    private final List<String> enumList = new ArrayList<>(1 << 14);

    private List<FileField> iff;

    private List<FileField> qff;

    FileRatioDataReader(File file, InstrumentTypeEnum type,
            Consumer<RatioData> consumer) {
        this.file = file;
        this.type = type;
        this.consumer = consumer;
    }

    void read() throws Exception {

        try (final RandomAccessFile tmp = new RandomAccessFile(this.file, "r")) {
            this.logger.info("<read> from {}", this.file.getAbsolutePath());

            this.fc = tmp.getChannel();

            clearAndFill(4);
            final int version = this.bb.getInt();
            if (version < MIN_CAN_READ_VERSION) {
                throw new IOException("<read> file version " + version + " < " + MIN_CAN_READ_VERSION);
            }

            this.fc.position(this.file.length() - 8);
            clearAndFill(8);
            this.metaStart = this.bb.getLong();
            final int metaSize = (int) (this.file.length() - this.metaStart);
            if (this.bb.capacity() < metaSize) {
                this.bb = ByteBuffer.allocate(metaSize).order(ByteOrder.LITTLE_ENDIAN);
            }
            this.fc.position(this.metaStart);
            clearAndFill(metaSize);

            this.iff = readFileFields();
            this.qff = readFileFields();
            readEnums();

            this.fc.position(4);
            this.bb.clear().flip();
            compactAndFill();

            final int size = this.bb.getInt();
            for (int i = 0; i < size; i++) {
                final RatioData rd = readRatioData();
                this.consumer.accept(rd);
            }
            this.logger.info("<read> succeeded for {}, read={}", this.type.name(), size);
        }
    }

    private void clearAndFill(int num) throws IOException {
        this.bb.clear();
        fillBuffer(num);
    }

    private void compactAndFill() throws IOException {
        this.bb.compact();
        final int num = (int) Math.min(this.metaStart - this.fc.position(), this.bb.remaining());
        if (num == 0) {
            throw new IOException();
        }
        fillBuffer(num);
    }

    private void fillBuffer(int num) throws IOException {
        this.bb.limit(this.bb.position() + num);
        int n = num;
        while (n > 0) {
            final int numRead = this.fc.read(this.bb);
            if (numRead <= 0) {
                throw new EOFException();
            }
            n -= numRead;
        }
        this.bb.flip();
    }

    private RatioData readRatioData() throws IOException {
        if (this.bb.remaining() < 4) {
            compactAndFill();
        }
        final int length = this.bb.getInt();
        if (this.bb.remaining() < length) {
            compactAndFill();
        }
        final long id = this.bb.getLong();
        final InstrumentRatios ir = InstrumentRatios.create(this.type, id);
        read(ir, this.iff);
        final QuoteRatios[] qrs = new QuoteRatios[readByte()];
        for (int i = 0; i < qrs.length; i++) {
            qrs[i] = ir.createQuoteRatios(this.bb.getLong());
            read(qrs[i], this.qff);
        }
        return new RatioData(ir, qrs);
    }

    private List<FileField> readFileFields() throws IOException {
        final List<FileField> result = new ArrayList<>();
        final int n = this.bb.getInt();
        for (int i = 0; i < n; i++) {
            result.add(readField());
        }
        return result;
    }

    private FileField readField() throws IOException {
        final int id = this.bb.getInt();

        final int encodedType = readByte();
        return FileField.create(id,
                RatioFieldDescription.Type.values()[encodedType & FileRatioDataWriter.TYPE_MASK],
                readLocales(encodedType),
                (encodedType & FileRatioDataWriter.ENUM_FLAG) != 0);
    }

    private Locale[] readLocales(int encodedType) throws IOException {
        if ((encodedType & FileRatioDataWriter.LOCALIZED_FLAG) == 0) {
            return null;
        }
        final Locale[] result = new Locale[readByte()];
        for (int k = 0; k < result.length; k++) {
            result[k] = new Locale(readUTF());
        }
        return result;
    }

    private String readUTF() {
        final byte[] bytes = new byte[this.bb.getShort() & 0xFFFF];
        this.bb.get(bytes);
        return new String(bytes, FileRatioDataWriter.UTF8);
    }

    private boolean readBoolean() {
        return this.bb.get() != 0;
    }

    private int readByte() {
        return this.bb.get() & 0xFF;
    }

    private void readEnums() throws IOException {
        final int num = this.bb.getInt();
        this.enumList.addAll(Collections.nCopies(num, ""));
        for (int i = 0; i < num; i++) {
            final int idx = this.bb.getInt();
            this.enumList.set(idx, readUTF());
        }
        this.logger.info("<readEnums> {} for {}", this.enumList.size(), this.type);
    }

    private void read(PropertySupported ps, List<FileField> ffs) throws IOException {
        ps.setTimestamp(this.bb.getLong());
        for (FileField ff : ffs) {
            final PropertySupport propSupp = (ff.field != null) ? ps.propertySupport(ff.id) : null;
            switch (ff.type) {
                case BOOLEAN:
                    final boolean b = readBoolean();
                    if (propSupp != null) {
                        propSupp.set(ps, b);
                    }
                    break;
                case DATE:
                case TIME:
                    final int j = this.bb.getInt();
                    if (propSupp != null) {
                        propSupp.set(ps, j);
                    }
                    break;
                case DECIMAL:
                case NUMBER:
                case TIMESTAMP:
                    if (readBoolean()) {
                        final long l = this.bb.getLong();
                        if (propSupp != null) {
                            propSupp.set(ps, l);
                        }
                    }
                    break;
                case ENUMSET:
                    if (readBoolean()) {
                        final BitSet bitSet = RatioEnumSet.read(this.bb);
                        if (propSupp != null) {
                            propSupp.set(ps, bitSet);
                        }
                    }
                    break;
                case STRING:
                    if (ff.isLocalized()) {
                        final int numLocales = readByte();
                        for (int k = 0; k < numLocales; k++) {
                            final String s = readString(ff, propSupp);
                            if (s != null && propSupp != null) {
                                if (ff.field.isLocalized()) {
                                    final int localeIndex = ff.getLocaleIndex(k);
                                    if (localeIndex != -1) {
                                        propSupp.set(ps, localeIndex, s);
                                    }
                                }
                                else if (k == 0) {
                                    // field is no longer localized, use 1st localized as new unlocalized
                                    propSupp.set(ps, -1, s);
                                }
                            }
                        }
                    }
                    else {
                        final String s = readString(ff, propSupp);
                        if (s != null && propSupp != null) {
                            if (ff.field.isLocalized()) {
                                // field switched from unlocalized to localized, use unloc. value for 1st locale
                                propSupp.set(ps, 0, s);
                            }
                            else {
                                propSupp.set(ps, -1, s);
                            }
                        }
                    }
                    break;
            }
        }
    }

    private String readString(FileField ff, PropertySupport ps) {
        if (!readBoolean()) {
            return null;
        }
        if (ff.isEnum) {
            if (ff.field == null) {
                // field is deleted, read bb and proceed
                this.bb.getInt();
                return null;
            }

            // field was enum when it was written, so value can be retrieved from enumList
            return EnumFlyweightFactory.intern(ff.field.id(), this.enumList.get(this.bb.getInt()));
        }
        final String result = readUTF();
        if (ps != null && ff.field != null && ff.field.isEnum()) {
            // field was no enum but is now, so intern the value before returning it
            return EnumFlyweightFactory.intern(ff.field.id(), result);
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("FileRatioDataReader {rda(relative to product dir)} {type}");
            System.exit(1);
        }
        final File file1 = new File(LocalConfigProvider.getProductionBaseDir(), args[0]);
        final TypeData mer = new TypeData(InstrumentTypeEnum.valueOf(args[1].toUpperCase()));
        new FileRatioDataReader(file1, mer.getType(), mer).read();
    }
}
