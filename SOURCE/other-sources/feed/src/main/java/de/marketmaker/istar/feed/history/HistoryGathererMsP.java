/*
 * DayTicker.java
 *
 * Created on 20.08.12 14:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.joda.time.Interval;

import de.marketmaker.istar.common.util.IoUtils;

import static de.marketmaker.istar.feed.history.HistoryUnit.Month;

/**
 * @author zzhao
 */
public class HistoryGathererMsP<T extends Comparable<T>> extends HistoryGathererBase<T> {

    private final List<HistoryReader<T>> yearReaders;

    private HistoryReader<T> monthReader;

    public HistoryGathererMsP(Class<T> clazz, boolean patchInMem) {
        super(clazz, patchInMem);
        this.monthReader = new HistoryReader<>(clazz, Month);
        this.yearReaders = new ArrayList<>();
    }

    public List<HistoryReader<T>> getYearReaders() {
        return yearReaders;
    }

    public HistoryReader<T> getMonthReader() {
        return monthReader;
    }

    @Override
    public void updateUnits(File dir, EnumSet<HistoryUnit> units) throws IOException {
        super.updateUnits(dir, units);
        if (units.contains(Month)) {
            setMonthFile(dir);
        }
    }

    private void setMonthFile(File dir) throws IOException {
        final Interval curMonthInterval = this.monthReader.getInterval();
        if (null == curMonthInterval) {
            final List<File> files = Month.getRelevantFilesDistinctMonth(dir);
            if (!files.isEmpty()) {
                this.monthReader.setFile(files.get(0));
                for (int i = 1; i < files.size(); i++) {
                    final HistoryReader<T> reader = new HistoryReader<>(this.clazz, Month);
                    reader.setFile(files.get(i));
                    this.yearReaders.add(reader);
                    if (this.yearReaders.size() >= 12) {
                        break;
                    }
                }
            }
        }
        else {
            final File file = Month.getLatestFile(dir);
            final Interval interval = Month.getInterval(file);
            if (curMonthInterval.getStart().equals(interval.getStart())) {
                this.monthReader.setFile(file);
            }
            else {
                if (interval.getStart().isBefore(curMonthInterval.getEnd())) {
                    throw new IllegalStateException("current month: " + curMonthInterval + "," +
                            " latest month file: " + interval);
                }
                this.yearReaders.add(0, this.monthReader);
                if (this.yearReaders.size() > 12) {
                    final HistoryReader<T> reader = this.yearReaders.remove(this.yearReaders.size() - 1);
                    reader.close();
                }
                this.monthReader = new HistoryReader<>(this.clazz, Month);
                this.monthReader.setFile(file);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        IoUtils.close(this.monthReader);
        for (HistoryReader<T> reader : this.yearReaders) {
            IoUtils.close(reader);
        }
        super.destroy();
    }
}
