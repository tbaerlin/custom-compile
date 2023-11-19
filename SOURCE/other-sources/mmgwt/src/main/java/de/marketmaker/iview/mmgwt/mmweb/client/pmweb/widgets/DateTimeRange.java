/*
 * DateTimeRange.java
 *
 * Created on 23.03.2015 12:20
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets;

import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
@NonNLS
public class DateTimeRange {
    private final MmJsDate begin;
    private final MmJsDate end;

    public DateTimeRange(MmJsDate start, MmJsDate end) {
        this.begin = start;
        this.end = end;
    }

    public boolean hasBegin() {
        return this.begin != null;
    }

    public boolean hasEnd() {
        return this.end != null;
    }

    public MmJsDate getBegin() {
        return this.begin;
    }

    public MmJsDate getEnd() {
        return this.end;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateTimeRange)) return false;

        final DateTimeRange that = (DateTimeRange) o;

        if (end != null ? !end.equals(that.end) : that.end != null) return false;
        if (begin != null ? !begin.equals(that.begin) : that.begin != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = begin != null ? begin.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DateTimeRange{" +
                "begin=" + begin +
                ", end=" + end +
                '}';
    }
}
