package de.marketmaker.istar.merger.web.easytrade.moleculestatistics;

import java.util.Objects;

/**
 * This class holds the values for aggregating by vwdId, date and hour.
 */
class HourAggregationKey {

    private final String vwdId;
    private final String date;
    private final String hour;

    public HourAggregationKey(String vwdId, String date, String hour) {
        this.vwdId = vwdId != null ? vwdId : "";
        this.date = date != null ? date : "";
        this.hour = hour != null ? hour : "";
    }

    public String getVwdId() {
        return vwdId;
    }

    public String getDate() {
        return date;
    }

    public String getHour() {
        return hour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HourAggregationKey that = (HourAggregationKey) o;
        return Objects.equals(vwdId, that.vwdId) &&
                Objects.equals(date, that.date) &&
                Objects.equals(hour, that.hour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vwdId, date, hour);
    }
}
