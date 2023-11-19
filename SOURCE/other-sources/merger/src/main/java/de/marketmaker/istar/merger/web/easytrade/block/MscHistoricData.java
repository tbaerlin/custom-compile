/*
 * StkKursdaten.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.vwd.EntitlementProvider;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesProvider;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.EnumEditor;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.TickDataCommand;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * Provides historical time series data for given instrument.
 * <p>
 * Note queries of type <code>VOLUME_AGGREGATION</code> is only allowed for instruments from VWD
 * feed market: <code>XEQDV</code>.
 * </p>
 * <p>
 * Based on the returned time series, outliers are marked by indexes and the value types. Supported
 * rules to determine outliers:
 * <dl>
 * <dt>5-sigma</dt>
 * <dd>If a price value's absolute deviation against its predecessor is greater than five fold
 * of the standard deviation, it is considered as an outlier.</dd>
 * </dl>
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample symbol 20664.iid
 */
public class MscHistoricData extends EasytradeCommandController {

  private HistoricConfigurationMBean historicConfigurationMBean;

  protected HistoricTimeseriesProvider historicTimeseriesProvider;

  protected HistoricTimeseriesProvider historicTimeseriesProviderEod;

  protected EasytradeInstrumentProvider instrumentProvider;

  protected IntradayProvider intradayProvider;

  protected EntitlementProvider entitlementProvider;

  public void setEntitlementProvider(EntitlementProvider entitlementProvider) {
    this.entitlementProvider = entitlementProvider;
  }

  public void setHistoricConfigurationMBean(HistoricConfigurationMBean historicConfigurationMBean) {
    this.historicConfigurationMBean = historicConfigurationMBean;
  }

  public void setHistoricTimeseriesProvider(
      HistoricTimeseriesProvider historicTimeseriesProvider) {
    this.historicTimeseriesProvider = historicTimeseriesProvider;
  }

  public void setHistoricTimeseriesProviderEod(
      HistoricTimeseriesProvider historicTimeseriesProviderEod) {
    this.historicTimeseriesProviderEod = historicTimeseriesProviderEod;
  }

  public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
    this.instrumentProvider = instrumentProvider;
  }

  public void setIntradayProvider(IntradayProvider intradayProvider) {
    this.intradayProvider = intradayProvider;
  }

  HistoricTimeseriesProvider getHistoricTimeseriesProvider() {
    return historicTimeseriesProvider;
  }

  HistoricTimeseriesProvider getHistoricTimeseriesProviderEod() {
    return historicTimeseriesProviderEod;
  }

  IntradayProvider getIntradayProvider() {
    return intradayProvider;
  }

  EntitlementProvider getEntitlementProvider() {
    return entitlementProvider;
  }

  public static class Command extends TickDataCommand {

    private boolean blendCorporateActions = true;

    private boolean blendDividends = false;

    private boolean endAsReferenceDate = true;

    private int numTrades;

    private String currency;

    private boolean fillNightlyGapWithChicagoData = false;

    private boolean withIntraday = false;

    private String outlierRule;

    private String[] field;

    private String baseField;

    private boolean functionalFill;

    /**
     * @return rule to determine outliers. If not set, no outliers are marked.
     */
    @RestrictedSet("5-sigma")
    public String getOutlierRule() {
      return outlierRule;
    }

    public void setOutlierRule(String outlierRule) {
      this.outlierRule = outlierRule;
    }

    /**
     * @sample CLOSE
     */
    @Override
    @RestrictedSet("CLOSE,OHLC,OHLCV,FUND,PERFORMANCE,VOLUME_AGGREGATION")
    public ElementDataType getType() {
      return super.getType();
    }

    /**
     * @return if corporate actions to be considered.
     */
    public boolean isBlendCorporateActions() {
      return blendCorporateActions;
    }

    public void setBlendCorporateActions(boolean blendCorporateActions) {
      this.blendCorporateActions = blendCorporateActions;
    }

    /**
     * @return if dividends to be considered.
     */
    public boolean isBlendDividends() {
      return blendDividends;
    }

    public void setBlendDividends(boolean blendDividends) {
      this.blendDividends = blendDividends;
    }

    /**
     * @return Whether to use end date as reference date for corporate actions calculations.
     */
    public boolean isEndAsReferenceDate() {
      return endAsReferenceDate;
    }

    public void setEndAsReferenceDate(boolean endAsReferenceDate) {
      this.endAsReferenceDate = endAsReferenceDate;
    }

    /**
     * @return currency used to query historical time series prices.
     */
    public String getCurrency() {
      return currency;
    }

    public void setCurrency(String currency) {
      this.currency = currency;
    }

    /**
     * @return restrict the number of time series price items, if type is not
     * <code>PERFORMANCE</code>. If not set all price items aggregated are returned.
     */
    public int getNumTrades() {
      return numTrades;
    }

    public void setNumTrades(int numTrades) {
      this.numTrades = numTrades;
    }

    /**
     * @return whether the queried time series should include intra-day prices.
     */
    public boolean isWithIntraday() {
      return withIntraday;
    }

    public void setWithIntraday(boolean withIntraday) {
      this.withIntraday = withIntraday;
    }

    /**
     * @return whether the queried time series should fill gaps w/ chicago in the night before the
     * pm update is ready.
     */
    @MmInternal
    public boolean isFillNightlyGapWithChicagoData() {
      return fillNightlyGapWithChicagoData;
    }

    public void setFillNightlyGapWithChicagoData(boolean fillNightlyGapWithChicagoData) {
      this.fillNightlyGapWithChicagoData = fillNightlyGapWithChicagoData;
    }

    /**
     * Name or number of vwd feed fields that are also stored in tick-by-tick data and that should
     * be returned in addition to the "normal" ticks. Ignored if aggregated ticks are requested.<br>
     * Data may be available for the following fields:
     * <ul>
     * <li>ADF_Anfang</li>
     * <li>ADF_Anzahl_Handel</li>
     * <li>ADF_Auktion</li>
     * <li>ADF_Auktion_Umsatz</li>
     * <li>ADF_Ausgabe</li>
     * <li>ADF_Indicative_Price</li>
     * <li>ADF_Indicative_Qty</li>
     * <li>ADF_Kassa</li>
     * <li>ADF_Kassa_Kurszusatz</li>
     * <li>ADF_Mittelkurs</li>
     * <li>ADF_NAV</li>
     * <li>ADF_Notierungsart</li>
     * <li>ADF_Open_Interest</li>
     * <li>ADF_Rendite</li>
     * <li>ADF_Rendite_Brief</li>
     * <li>ADF_Rendite_Geld</li>
     * <li>ADF_Rendite_ISMA</li>
     * <li>ADF_Ruecknahme</li>
     * <li>ADF_Schluss</li>
     * <li>ADF_Settlement</li>
     * <li>ADF_Tageshoch</li>
     * <li>ADF_Tagestief</li>
     * <li>ADF_Umsatz_gesamt</li>
     * <li>ADF_Volatility</li>
     * </ul>
     * For symbols at market <tt>LME</tt>, data may also be available for these additional fields:
     * <ul>
     * <li>ADF_Benchmark
     * <li>ADF_Interpo_Closing
     * <li>ADF_Official_Ask
     * <li>ADF_Official_Bid
     * <li>ADF_Prov_Evaluation
     * <li>ADF_Unofficial_Ask
     * <li>ADF_Unofficial_Bid
     * <li>ADF_VWAP
     * </ul>
     *
     * @sample ADF_Rendite
     */
    public String[] getField() {
      return this.field;
    }

    public void setField(String[] field) {
      if (field.length == 1 && field[0].contains(",")) {
        this.field = field[0].split(",");
      } else {
        this.field = field;
      }
    }

    /**
     * vwd field(s) on which the returned prices will be based.
     * <p>
     * <ul>
     * <li>single vwd field identified by ID or name</li>
     * <li>multiple vwd fields with following format:
     * <code>
     * O=ADF_XXX,H=ADF_YYY,L=ADF_ZZZ,C=ADF_ID
     * </code>
     * in case of type=OHLC, means filling open-slot with prices from ADF_XXX, high-slot with
     * prices from ADF_YYY and so on.
     * </li>
     * </ul>
     * </p>
     */
    public String getBaseField() {
      return baseField;
    }

    /**
     * Indicates whether to fill missing values given in baseField with functional prices from other
     * related fields.
     */
    @MmInternal
    public boolean isFunctionalFill() {
      return functionalFill;
    }

    public void setBaseField(String baseField) {
      this.baseField = baseField;
    }

    public void setFunctionalFill(boolean functionalFill) {
      this.functionalFill = functionalFill;
    }
  }

  public MscHistoricData() {
    super(Command.class);
  }

  protected MscHistoricData(Class aClass) {
    super(aClass);
  }

  protected void initBinder(HttpServletRequest httpServletRequest,
      ServletRequestDataBinder binder) throws Exception {
    super.initBinder(httpServletRequest, binder);

    EnumEditor.register(TickDataCommand.Format.class, binder);
    EnumEditor.register(TickDataCommand.ElementDataType.class, binder);
    EnumEditor.register(TickImpl.Type.class, binder);
  }

  protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
      Object o, BindException errors) throws IOException {

    final Command cmd = (Command) o;
    final Quote quote = getQuote(cmd);
    if (!InstrumentUtil.isVwdFund(quote) && cmd.getType() == TickDataCommand.ElementDataType.FUND) {
      throw new BadRequestException("type FUND only for issuer data");
    }

    final Map<String, Object> model = createModel(request, cmd, quote);
    return new ModelAndView("mschistoricdata", model);
  }

  protected Map<String, Object> createModel(HttpServletRequest request, Command cmd,
      Quote quote) throws IOException {
    final Zone zone = (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);
    final boolean eodEnabled = this.historicConfigurationMBean.isEodHistoryEnabled(quote) ||
        this.historicConfigurationMBean.isEodZoneIncluded(zone.getName());

    AbstractHistoricDataMethod method = eodEnabled
        ? new MscEodMethod(this, quote, cmd)
        : new MscHistoricDataMethod(this, quote, cmd);

    final Map<String, Object> result = method.invoke();
    result.put("eod", eodEnabled);
    return result;
  }

  protected boolean isEodModel(Map<String, Object> m) {
    return Boolean.TRUE.equals(m.get("eod"));
  }

  Quote getQuote(SymbolCommand cmd) {
    return this.instrumentProvider.getQuote(cmd);
  }
}
