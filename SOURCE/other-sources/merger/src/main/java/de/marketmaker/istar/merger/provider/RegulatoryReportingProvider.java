/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.marketmaker.istar.domain.data.RegulatoryReportingRecord;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public interface RegulatoryReportingProvider {

  RegulatoryReportingRecord getPriceRegulatoryReportingRecord(
      RegulatoryReportingRequest reportingRequest);

  Optional<EuwaxDates> getEuwaxDates(String isin);

  class OrdsData<T> {

    private List<T> _data;

    public List<T> get_data() {
      return _data;
    }

    public void set_data(List<T> _data) {
      this._data = _data;
    }
  }

  /**
   * EUWAX dates data.
   *
   * @author zzhao
   */
  class EuwaxDates {

    /**
     * TAG020
     */
    @JsonProperty("TAG020")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate firstTradingDay;

    /**
     * TAG062
     */
    @JsonProperty("TAG062")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z")
    private LocalDateTime subscriptionBegin;

    /**
     * TAG063
     */
    @JsonProperty("TAG063")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z")
    private LocalDateTime subscriptionEnd;

    public LocalDate getFirstTradingDay() {
      return firstTradingDay;
    }

    public LocalDateTime getSubscriptionBegin() {
      return subscriptionBegin;
    }

    public LocalDateTime getSubscriptionEnd() {
      return subscriptionEnd;
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", EuwaxDates.class.getSimpleName() + "[", "]")
          .add("firstTradingDay=" + firstTradingDay)
          .add("subscriptionBegin=" + subscriptionBegin)
          .add("subscriptionEnd=" + subscriptionEnd)
          .toString();
    }
  }
}
