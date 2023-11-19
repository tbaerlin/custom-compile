package de.marketmaker.istar.merger.web.easytrade.moleculestatistics;

import java.util.Objects;

public class DayAggregationKey {

    private final String vwdId;
    private final String date;

    public DayAggregationKey(String vwdId, String date) {
        this.vwdId = vwdId != null ? vwdId : "";
        this.date = date != null ? date : "";
    }

    public String getVwdId() {
        return vwdId;
    }

    public String getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DayAggregationKey that = (DayAggregationKey) o;
        return Objects.equals(vwdId, that.vwdId) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vwdId, date);
    }
}
