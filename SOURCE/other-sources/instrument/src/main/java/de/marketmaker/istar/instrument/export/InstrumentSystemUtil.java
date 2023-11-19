/*
 * InstrumentFileNames.java
 *
 * Created on 14.04.2010 16:38:14
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.IoUtils;

/**
 * Provides common instrument data directory and file related methods.
 *
 * @author zzhao
 * @since 1.2
 */
public final class InstrumentSystemUtil {

    /**
     * The name of instrument data file within an instrument data directory
     */
    public static final String FILE_NAME_DATA = "instruments.dat";

    /**
     * The name of instrument index file within an instrument data directory
     */
    public static final String FILE_NAME_IDX = "instruments.iol";

    private InstrumentSystemUtil() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    /**
     * Validates the given directory and its structure.
     *
     * @param dir a directory
     * @throws IllegalStateException if the given directory and its structure is not valid
     */
    public static void validateDir(File dir) {
        if (null == dir || !dir.exists() || !dir.canRead()) {
            throw new IllegalStateException("invalid setting of instrument data dir");
        }
        File df = getDataFile(dir);
        if (!df.exists() || !df.canRead()) {
            throw new IllegalStateException("invalid instrument data file in: " + dir.getAbsolutePath());
        }
        File idxFile = getIndexFile(dir);
        if (!idxFile.exists() || !idxFile.canRead()) {
            throw new IllegalStateException("invalid instrument index file in: " + dir.getAbsolutePath());
        }
    }

    /**
     * Gets the instrument data file within the given directory.
     *
     * @param dir an directory supposed to contain valid instrument related data.
     * @return an instrument data file
     */
    public static File getDataFile(File dir) {
        return new File(dir, FILE_NAME_DATA);
    }

    /**
     * Gets the instrument index file within the given directory.
     *
     * @param dir an directory supposed to contain valid instrument related data.
     * @return an instrument index file
     */
    public static File getIndexFile(File dir) {
        return new File(dir, FILE_NAME_IDX);
    }

    public static void transfer(DataFile source, int position, int length, DataFile target,
            ByteBuffer bb) throws IOException {
        source.seek(position);
        int remaining = length;
        while (remaining > 0) {
            bb.clear();
            if (bb.remaining() > remaining) {
                bb.limit(remaining);
            }
            source.read(bb);
            bb.flip();
            target.write(bb);
            remaining -= bb.position();
        }
    }


    public static void renameToFailException(File from, File to) throws IOException {
        if (!from.renameTo(to)) {
            throw new IllegalStateException("cannot rename '" + from.getAbsolutePath()
                    + "' to: '" + to.getAbsolutePath() + "'");
        }
    }

    public static void deleteIfExistsFailException(File f) throws IOException {
        if (f.exists()) {
            if (f.isDirectory()) {
                FileUtils.cleanDirectory(f);
                deleteFailException(f);
            }
            else {
                deleteFailException(f);
            }
        }
    }

    public static void deleteFailException(File f) throws IOException {
        if (!f.delete()) {
            throw new IllegalStateException("cannot delete: '" + f.getAbsolutePath() + "'");
        }
    }

    /**
     * Returns a directory with the given name within the given parent directory. If it does not
     * exist, it would be created. If it does exist, it could be cleaned by specifying the second
     * boolean parameter.
     *
     * @param parentDir the parent directory
     * @param name a name of directory
     * @param cleanIfExists a flag indicates whether or not to clean the named directory if it exists
     * @return a directory with the given name in the configured temporary directory.
     * @throws IOException if any occurred during creating or cleaning operation.
     */
    public static File getDir(File parentDir, String name, boolean cleanIfExists)
            throws IOException {
        File ret = new File(parentDir, name);
        if (!ret.exists() && !ret.mkdir()) {
            throw new IllegalStateException("cannot create directory: " + ret.getAbsolutePath());
        }
        else {
            if (cleanIfExists) {
                FileUtils.cleanDirectory(ret);
            }
        }

        return ret;
    }

    public static File getFile(File parentDir, String name, boolean deleteIfExists)
            throws IOException {
        File ret = new File(parentDir, name);
        if (!deleteIfExists) {
            return ret;
        }

        if (ret.exists() && !ret.delete()) {
            throw new IllegalStateException("cannot delete file: " + ret.getAbsolutePath());
        }

        return ret;
    }

    /**
     * Deletes the file to be replaced and renames the file given as replacement to the file just
     * deleted.
     *
     * @param toBeReplaced the file to be replaced.
     * @param replacement the file that would replace the file given as first parameter.
     * @throws IOException if any occurred during deletion or renaming
     * @throws IllegalStateException if either the delete or rename operation cannot
     * be performed successfully
     */
    public static void replace(File toBeReplaced, File replacement)
            throws IOException {
        InstrumentSystemUtil.deleteIfExistsFailException(toBeReplaced);
        InstrumentSystemUtil.renameToFailException(replacement, toBeReplaced);
    }

    /**
     * <b>Not thread safe on instruction context</b>
     *
     * @param dataFile
     * @return
     * @throws Exception
     */
    public static ByteBuffer readDomainContext(File dataFile) throws Exception {
        if (null == dataFile || !dataFile.exists() || !dataFile.canRead()) {
            throw new IllegalStateException("invalid instrument data file ");
        }

        DataFile df = null;
        try {
            df = new DataFile(dataFile, true);
            int contextOffset = df.readInt();
            int dataOffset = df.readInt();

            ByteBuffer bb = ByteBuffer.allocate(dataOffset - contextOffset);
            df.seek(contextOffset);
            df.read(bb);

            bb.flip();
            return bb;
        } finally {
            IoUtils.close(df);
        }
    }

    public static List<Long> getUpdatedInstrumentIds(File updateDataDir) throws Exception {
        try (InstrumentIOLIterator it = new InstrumentIOLIterator(getIndexFile(updateDataDir))) {
            List<Long> result = new ArrayList<>(it.getNumEntries());
            while (it.hasNext()) {
                result.add(it.next().getIid());
            }
            return result;
        }
    }
}
