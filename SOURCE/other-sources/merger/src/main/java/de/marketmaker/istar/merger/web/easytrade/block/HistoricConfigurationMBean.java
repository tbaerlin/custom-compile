package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.instrument.Quote;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

/**
 * The MBean is a singleton for the configuration of historic data.
 */
@ManagedResource
public class HistoricConfigurationMBean {

  private final Logger log = LoggerFactory.getLogger(HistoricConfigurationMBean.class);
  private Set<String> eodHistoryZones = Collections.emptySet();
  private Set<String> eodVwdFeedMarkets = Collections.emptySet();
  private boolean alwaysEod = true;

  public void setEodVwdFeedMarkets(String eodVwdFeedMarkets) {
    this.eodVwdFeedMarkets = StringUtils.commaDelimitedListToSet(eodVwdFeedMarkets);
  }

  public void setEodHistoryZones(String eodHistoryZones) {
    this.eodHistoryZones = StringUtils.commaDelimitedListToSet(eodHistoryZones);
  }

  @ManagedAttribute
  public String getEodHistoryZonesAsString() {
    return this.eodHistoryZones.toString();
  }

  @ManagedAttribute
  public boolean isAlwaysEod() {
    return alwaysEod;
  }

  @ManagedAttribute
  public String getEodVwdFeedMarketsAsString() {
    return this.eodVwdFeedMarkets.toString();
  }

  @ManagedOperation
  @ManagedOperationParameters({
      @ManagedOperationParameter(name = "alwaysEod", description = "Always use EOD historic data"),
  })
  public void setAlwaysEod(boolean alwaysEod) {
    this.alwaysEod = alwaysEod;
    this.log.info("<setAlwaysEod> alwaysEod: {}", this.alwaysEod);
  }

  public boolean isEodHistoryEnabled(Quote quote) {
    return this.alwaysEod || Objects.nonNull(quote) && this.eodVwdFeedMarkets
        .contains(quote.getSymbolVwdfeedMarket());
  }

  /**
   * If a zone is included, then all requests controlled by this class will be routed to EOD history
   * backend.
   */
  public boolean isEodZoneIncluded(String zone) {
    return this.alwaysEod || this.eodHistoryZones.contains(zone);
  }
}