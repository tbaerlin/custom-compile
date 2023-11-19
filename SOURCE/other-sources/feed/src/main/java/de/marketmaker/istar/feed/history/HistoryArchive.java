/*
 * TickHistoryController.java
 *
 * Created on 22.08.12 14:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.marketmaker.istar.feed.history.HistoryUnit.Change;
import static de.marketmaker.istar.feed.history.HistoryUnit.Patch;

/**
 * @author zzhao
 */
public abstract class HistoryArchive<T extends Comparable<T>> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Class<T> clazz;

    private SymbolRetainer<T> symbolRetainer = SymbolRetainer.YES; //FIXME: Revert and find correct solution

    private File workDir;

    protected TickHistoryContext context;

    public HistoryArchive(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void setContext(TickHistoryContext context) {
        this.context = context;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setSymbolRetainer(SymbolRetainer<T> symbolRetainer) {
        this.symbolRetainer = symbolRetainer;
    }

    public abstract EntryMerger getEntryMergerJoin(int days);

    public abstract EntryMerger getEntryMergerCompact(int days);

    public abstract String getContentType();

    public void update(HistoryUnit unit, File file) throws IOException {
        final EnumSet<HistoryUnit> units = getCurrentHistoryUnits();
        switch (unit) {
            case Day:
                onUpdateWithDay(file, units);
                break;
            case Change:
                onUpdateWithChange(file, units);
                break;
            default:
                throw new UnsupportedOperationException("no update support for: " + unit);
        }
        updateHistoryUnits(units);
    }

    private void onUpdateWithChange(File file, EnumSet<HistoryUnit> units) throws IOException {
        changeOntoPatch(file, units);
        HistoryUtil.deleteOlderFiles(file.getParentFile(), Patch);
    }

    protected abstract void onUpdateWithDay(File file, EnumSet<HistoryUnit> units)
            throws IOException;

    private EnumSet<HistoryUnit> getCurrentHistoryUnits() throws IOException {
        return HistoryUtil.loadHistoryUnits(this.workDir);
    }

    private void updateHistoryUnits(EnumSet<HistoryUnit> units) throws IOException {
        HistoryUtil.updateHistoryUnits(this.workDir, units);
    }

    private void changeOntoPatch(File file, EnumSet<HistoryUnit> units) throws IOException {
        if (units.contains(Patch)) {
            // merge onto current patch - new patch would be created
            final File base = Patch.getLatestFile(this.workDir);
            final int fromDateCP = Patch.getFromDate(base);
            final int toDateCP = Patch.getToDate(base);

            final int fromDateC = Change.getFromDate(file);
            final int toDateC = Change.getToDate(file);


            final File tmpFile = Patch.createTmpFile(getContentType(), this.workDir,
                    Math.min(fromDateCP, fromDateC), Math.max(toDateCP, toDateC));
            final PatchMerger<T> merger = new PatchMerger<>(base, file, this.clazz);
            merger.merge(tmpFile, getEntryMergerJoin(this.context.daysToReserveInPatch()));
            final File patchFile = Patch.convert(tmpFile, Patch);
            HistoryUtil.replaceFile(patchFile, tmpFile);
            HistoryUtil.reportFileOpStatus(file.delete(), "deleting " + file.getAbsolutePath());
        }
        else {
            // change would be the current patch
            final File patchFile = Change.convert(file, Patch);
            HistoryUtil.replaceFile(patchFile, file);
            units.add(Patch);
        }
    }

    protected void compactAndForward(File base, File delta, File result, int days)
            throws IOException {
        final FastForward<T> merger = new FastForward<>(base, delta, this.clazz, this.symbolRetainer);
        merger.merge(result, getEntryMergerCompact(days));
    }

    protected void fastForward(File base, File delta, File result) throws IOException {
        final FastForward<T> merger = new FastForward<>(base, delta, this.clazz, SymbolRetainer.YES);
        merger.merge(result, EntryMerger.FF);
    }

    protected void mergeHistory(File base, File delta, File patch, File result, int days)
            throws IOException {
        final HistoryMerger<T> merger = new HistoryMerger<>(base, delta,
                patch, this.clazz, this.symbolRetainer);
        merger.merge(result, getEntryMergerJoin(days));
    }
}
