/*
 * InstrumentDirUpdater.java
 *
 * Created on 16.04.2010 14:24:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;

/**
 * An instrument updater assumes that the template and domain context contained in the base directory
 * and update directory are the same.
 *
 * <p>
 * This updater performs its task as following:
 * <ol>
 * <li>Append the updated instrument data to instrument data contained in the base directory</li>
 * <li>create a new index-file with info in the base index-file and update index-file</li>
 * <li>check update status: if successful, archive the base index-file and replace it in the
 * active directory with the newly created index-file. If update failed, delete the newly created
 * index-file.</li>
 * <li>Update the offset of the lately update at the reserved position in base data-file</li>
 * </ol>
 * @author zzhao
 * @since 1.2
 */
public class InstrumentDirUpdater {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File baseDir;

    private File updateDir;

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setUpdateDir(File updateDir) {
        this.updateDir = updateDir;
    }

    public void update() throws Exception {
        InstrumentSystemUtil.validateDir(this.baseDir);
        InstrumentSystemUtil.validateDir(this.updateDir);
        this.logger.info("<update> base: '" + this.baseDir.getAbsolutePath()
                + "' update: '" + this.updateDir.getAbsolutePath() + "'");
        final TimeTaker tt = new TimeTaker();

        DirInfo base = null;
        DirInfo update = null;

        try {
            base = new DirInfo(this.baseDir, false);
            update = new DirInfo(this.updateDir, true);

            final long baseSize = base.dataFile.size();

            base.dataFile.seek(baseSize);
            update.dataFile.transferTo(base.dataFile, 0L, update.dataFile.size());

            final File iolFile = mergeIOLs(base, update, baseSize);
            if (iolFile != null) {
                InstrumentSystemUtil.replace(base.indexFile, iolFile);
                writeOffsetOfLatelyUpdate(base.dataFile, baseSize);
            }
            else {
                base.dataFile.truncate(baseSize);
            }

            this.logger.info("<update> finished in " + tt);
        } finally {
            IoUtils.close(base);
            IoUtils.close(update);
        }
    }

    private void writeOffsetOfLatelyUpdate(DataFile dataFile, long offset) throws IOException {
        dataFile.seek(InstrumentDirWriter.LAST_UPDATE_OFFSET);
        dataFile.writeLong(offset);
    }

    /**
     * created a merged version of the iols of base and update. Since the IOLs from both Iterators
     * are sorted by iid, we can do a merge sort and so avoid keeping all IOLs in memory.
     */
    private File mergeIOLs(DirInfo base, DirInfo update, long offsetDiff) throws IOException {
        final File result = new File(base.indexFile.getParentFile(),
                "N_" + System.currentTimeMillis() + ".iol");

        try (InstrumentIOLIterator itBase = new InstrumentIOLIterator(base.indexFile);
             InstrumentIOLIterator itUpdate = new InstrumentIOLIterator(update.indexFile);
             IOLWriter iw = new IOLWriter(result)
        ) {
            IOL baseIOL = nextOrNull(itBase, 0);
            IOL updateIOL = nextOrNull(itUpdate, offsetDiff);
            int numReplaced = 0;

            while (baseIOL != null || updateIOL != null) {
                if (baseIOL == null) { // no more data in base, just add update
                    iw.append(updateIOL);
                    updateIOL = nextOrNull(itUpdate, offsetDiff);
                }
                else if (updateIOL == null) { // no more updates, just add base
                    iw.append(baseIOL);
                    baseIOL = nextOrNull(itBase, 0);
                }
                else { // merge based on iid
                    if (baseIOL.iid < updateIOL.iid) {
                        iw.append(baseIOL);
                        baseIOL = nextOrNull(itBase, 0);
                    }
                    else if (baseIOL.iid > updateIOL.iid) {
                        iw.append(updateIOL);
                        updateIOL = nextOrNull(itUpdate, offsetDiff);
                    }
                    else { // same iid, update overrides base
                        iw.append(updateIOL);
                        updateIOL = nextOrNull(itUpdate, offsetDiff);
                        baseIOL = nextOrNull(itBase, 0);
                        numReplaced++;
                    }
                }
            }
            this.logger.info("<mergeIOL> created file with " + iw.getNumAppended()
                    + " iols, replaced " + numReplaced);
            return result;
        } catch (IOException e) {
            this.logger.error("<mergeIOL> failed", e);
            return null;
        }
    }

    private static IOL nextOrNull(InstrumentIOLIterator it, long offsetDiff) {
        return it.hasNext() ? it.next().withOffsetIncrementedBy(offsetDiff) : null;
    }

    private static class DirInfo implements Closeable {
        private final DataFile dataFile;

        private final File indexFile;

        private DirInfo(File f, boolean isUpdateDir) throws Exception {
            this.dataFile = new DataFile(InstrumentSystemUtil.getDataFile(f), isUpdateDir);
            this.indexFile = InstrumentSystemUtil.getIndexFile(f);
        }

        public void close() throws IOException {
            this.dataFile.close();
        }
    }

}
