/*
 * PriceListSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import de.marketmaker.itools.gwtutil.client.util.date.GwtDateParser;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.DecimalField;
import de.marketmaker.iview.dmxml.MSCTicks;
import de.marketmaker.iview.dmxml.OHLCVTimeseries;
import de.marketmaker.iview.dmxml.OHLCVTimeseriesElement;
import de.marketmaker.iview.dmxml.TicksPriceElement;
import de.marketmaker.iview.dmxml.TicksTimeseries;
import de.marketmaker.iview.dmxml.TicksTimeseriesElement;
import de.marketmaker.iview.dmxml.Timeseries;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TimesAndSalesSnippet extends AbstractSnippet<TimesAndSalesSnippet, TimesAndSalesSnippetView>
        implements IsVisible, PushRegisterHandler, ValueChangeHandler<String> {

    public static class Class extends SnippetClass {
        public Class() {
            super("TimesAndSales"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new TimesAndSalesSnippet(context, config);
        }
    }

    public static final Map<String, String> METAL_FIELDS = new HashMap();
    static {
        // TODO: i18n
        METAL_FIELDS.put("235", I18n.I.interpoClosing());  // $NON-NLS$
        METAL_FIELDS.put("236", I18n.I.provEvaluation());  // $NON-NLS$
        METAL_FIELDS.put("1168", I18n.I.ask() + I18n.I.officialSuffix()); // $NON-NLS$
        METAL_FIELDS.put("1169", I18n.I.bid() + I18n.I.officialSuffix()); // $NON-NLS$
        METAL_FIELDS.put("1170", I18n.I.bid() + I18n.I.unOfficialSuffix()); // $NON-NLS$
        METAL_FIELDS.put("1171", I18n.I.ask() + I18n.I.unOfficialSuffix()); // $NON-NLS$
    }

    private class LevelData {
        private final DateTimeUtil.PeriodEnum aggregation;
        private final String end;
        private final String start;

        LevelData(String start, String end, DateTimeUtil.PeriodEnum aggregation) {
            this.start = start;
            this.end = end;
            this.aggregation = aggregation;
        }
    }

    private DateTimeUtil.PeriodEnum aggregation;

    private final DmxmlContext.Block<MSCTicks> block;

    private MmJsDate dateLabel = null;

    private List<LevelData> levelData = new ArrayList<>();

    private boolean isLme = false;

    private boolean isVisible = false;

    private PriceTeaserSnippet teaser;

    private final PriceSupport priceSupport = new PriceSupport(this);

    private TimesAndSalesSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.isLme = "metals".equals(config.getString("type")); // $NON-NLS-0$ // $NON-NLS-1$
        this.block = createBlock("MSC_Ticks"); // $NON-NLS-0$
        onParametersChanged();

        setView(new TimesAndSalesSnippetView(this));
        }

    @Override
    public void onControllerInitialized() {
        final String teaserId = getConfiguration().getString("teaserId", null); // $NON-NLS-0$
        if (teaserId != null) {
            this.teaser = (PriceTeaserSnippet) this.contextController.getSnippet(teaserId);
            if (this.teaser == null) {
                throw new IllegalArgumentException("snippet not found: " + teaserId); // $NON-NLS-0$
            }
        }
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void onValueChange(ValueChangeEvent<String> e) {
        setDate(GwtDateParser.getMmJsDate(e.getValue()));
    }

    public void setDate(MmJsDate date) {
        doSetDate(date, true);
    }

    public void setSymbol(String symbol) {
        getConfiguration().put("symbol", symbol); // $NON-NLS-0$
        doSetDate(new MmJsDate(), false);
        onParametersChanged();
    }

    public void setTitle(String title) {
        getView().setTitle(title);
    }

    public void updateView() {
        this.priceSupport.invalidateRenderItems();

        this.isVisible = false;
        if (this.teaser != null && this.teaser.isEndOfDay()) {
            this.isVisible = false;
            return;
        }
        if (this.dateLabel != null) {
            getView().setDate(this.dateLabel);
            this.dateLabel = null;
        }
        if (this.levelData.size() == 1) {
            getView().enableUp();
        }

        if (!block.isResponseOk()) {
            getView().updateOHLCV(DefaultTableDataModel.NULL);
            return;
        }
        final Timeseries timeseries = this.block.getResult().getItems();
        DefaultTableDataModel dtm;
        if (timeseries instanceof OHLCVTimeseries) {
            dtm = getTableDataModel((OHLCVTimeseries) timeseries);
            getView().updateOHLCV(dtm);
        }
        else {
            dtm = getTableDataModel((TicksTimeseries) timeseries);
            getView().updateTicks(dtm, this.isLme);
        }
        this.isVisible = !("FONDS".equals(this.block.getResult().getQuotedata().getMarketVwd())); // $NON-NLS-0$
        this.priceSupport.activate();
    }

    protected void onParametersChanged() {
        this.block.setParameter("symbol", getConfiguration().getString("symbol")); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("aggregation", getConfiguration().getString("aggregation")); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("start", getConfiguration().getString("start")); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("end", getConfiguration().getString("end")); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("type", getAggregationType()); // $NON-NLS-0$

        if (displayLmeData()) {
            // Retrieve additional LME fields
            this.block.setParameter("tickType", "TRADE"); // $NON-NLS-0$ $NON-NLS-1$
            this.block.setParameters("field", METAL_FIELDS.keySet().toArray(new String[METAL_FIELDS.size()])); // $NON-NLS-0$
        }
    }

    void goDown(String start, String end, DateTimeUtil.PeriodEnum smallerAggregation) {
        this.levelData.add(new LevelData(getConfiguration().getString("start"), // $NON-NLS-0$
                getConfiguration().getString("end"), this.aggregation)); // $NON-NLS-0$
        getConfiguration().put("start", start); // $NON-NLS-0$
        getConfiguration().put("end", end); // $NON-NLS-0$
        getConfiguration().put("aggregation", smallerAggregation.toString()); // $NON-NLS-0$
        this.aggregation = smallerAggregation;
        ackParametersChanged();
    }

    void goUp() {
        final TimesAndSalesSnippet.LevelData data = this.levelData.remove(this.levelData.size() - 1);
        if (this.levelData.size() == 0) {
            getView().disableUp();
        }
        getConfiguration().put("start", data.start); // $NON-NLS-0$
        getConfiguration().put("end", data.end); // $NON-NLS-0$
        getConfiguration().put("aggregation", data.aggregation.toString()); // $NON-NLS-0$
        this.aggregation = data.aggregation;
        ackParametersChanged();
    }

    private void doSetDate(MmJsDate date, boolean ackChange) {
        final String start = JsDateFormatter.formatIsoDateTimeMidnight(date);
        getConfiguration().put("start", start); // $NON-NLS-0$
        final String end = JsDateFormatter.formatIsoDateTimeMidnight(new MmJsDate(date).addDays(1));
        getConfiguration().put("end", end); // $NON-NLS-0$
        getConfiguration().put("aggregation", DateTimeUtil.PeriodEnum.PT30M.toString()); // $NON-NLS-0$
        getConfiguration().put("type", "OHLCV"); // $NON-NLS-0$ $NON-NLS-1$

        this.aggregation = DateTimeUtil.PeriodEnum.PT30M;
        this.dateLabel = date;

        this.levelData.clear();
        getView().disableUp();

        if (ackChange) {
            ackParametersChanged();
        }
    }

    private String formatPeriod(String start, Date dateEnd) {
        switch (this.aggregation) {
            case PT30M:
            case PT1M:
                return Formatter.formatTimeHhmm(start) + " - " + Formatter.formatTimeHhmm(dateEnd); // $NON-NLS-0$
            case PT5S:
                return Formatter.formatTime(start) + " - " + Formatter.formatTime(dateEnd); // $NON-NLS-0$
            default:
                return Formatter.formatTime(start);
        }
    }

    private String getAggregationType() {
        return "PT0S".equals(getConfiguration().getString("aggregation")) ? "TICK" : "OHLCV"; // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
    }

    private DateTimeUtil.PeriodEnum getSmallerAggregation(long count) {
        if (count <= 60) {
            return DateTimeUtil.PeriodEnum.PT0S;
        }
        switch (this.aggregation) {
            case PT30M:
                return DateTimeUtil.PeriodEnum.PT1M;
            case PT1M:
                return DateTimeUtil.PeriodEnum.PT5S;
            default:
                return DateTimeUtil.PeriodEnum.PT0S;
        }
    }

    private DefaultTableDataModel getTableDataModel(TicksTimeseries timeseries) {
        final List<TicksTimeseriesElement> elements = timeseries.getItem();
        final DefaultTableDataModel dtm = new DefaultTableDataModel(elements.size(), displayLmeData() ? 5 : 4);
        for (int i = elements.size() - 1, row = 0; i >= 0; i--, row++) {
            final TicksTimeseriesElement e = elements.get(i);
            if (displayLmeData()) {
                if (e.getTrade() != null) { // trade
                    final TicksPriceElement trade = e.getTrade();
                    dtm.setValuesAt(row, new Object[]{
                            e.getDate(),
                            trade.getPrice(),
                            trade.getSupplement(),
                            getVolumeString(trade),
                            "" // $NON-NLS-0$
                    });
                }
                else { // LME field
                    final List<Serializable> fields = e.getStringOrDecimalOrNumber();
                    final Map<String, String> lmeFields = getLMEFields(fields);
                    if (!lmeFields.isEmpty()) {
                        for (String id : lmeFields.keySet()) {
                            dtm.setValuesAt(row, new Object[] {
                                    e.getDate(),
                                    lmeFields.get(id),
                                    "", // $NON-NLS-0$
                                    "", // $NON-NLS-0$
                                    METAL_FIELDS.get(id)
                            });
                            row++;
                        }
                        row--;
                    }
                }
            }
            else {
                if (e.getTrade() != null) {
                    final TicksPriceElement trade = e.getTrade();
                    dtm.setValuesAt(row, new Object[]{
                            e.getDate(),
                            trade.getPrice(),
                            trade.getSupplement(),
                            getVolumeString(trade)
                    });
                }
            }
        }
        return dtm;
    }

    private boolean displayLmeData() {
        return this.isLme && "TICK".equals(getAggregationType()); // $NON-NLS-0$
    }

    private String getVolumeString(TicksPriceElement trade) {
        return trade.getVolume() != null ? trade.getVolume().toString() : null;
    }

    private Map<String, String> getLMEFields(List fields) {
        Map<String, String> lmeFields = new HashMap<>();
        for (String id : METAL_FIELDS.keySet()) {
            String value = getDecimal(fields, id);
            if (value != null && !value.isEmpty()) {
                lmeFields.put(id, value);
            }
        }
        return lmeFields;
    }

    private String getDecimal(List fields, String id) {
        for (Object field : fields) {
            if (field instanceof DecimalField) {
                if (id.equals(((DecimalField) field).getId())) {
                    return ((DecimalField) field).getValue().toString();
                }
            }
        }
        return "";
    }

    private DefaultTableDataModel getTableDataModel(OHLCVTimeseries timeseries) {
        final List<OHLCVTimeseriesElement> elements = timeseries.getItem();
        final DefaultTableDataModel dtm = new DefaultTableDataModel(elements.size(), 7);
        for (int i = elements.size() - 1, row = 0; i >= 0; i--, row++) {
            final OHLCVTimeseriesElement e = elements.get(i);
            dtm.setValuesAt(row, new Object[]{
//                    getWidget(e.getDate(), e.getCount()),
                    e,
                    e.getOpen(),
                    e.getHigh(),
                    e.getLow(),
                    e.getClose(),
                    e.getVolume(),
                    e.getCount()
            });
        }
        return dtm;
    }

    String getTimeLabel(final String start) {
        final Date dateStart = Formatter.parseISODate(start);
        final Date dateEnd = new Date(dateStart.getTime() + this.aggregation.getMillis());
        return formatPeriod(start, dateEnd);
    }

    void goDown(OHLCVTimeseriesElement e) {
        final Date dateStart = Formatter.parseISODate(e.getDate());
        final Date dateEnd = new Date(dateStart.getTime() + this.aggregation.getMillis());
        final String end = Formatter.formatDateAsISODate(dateEnd);
        final DateTimeUtil.PeriodEnum smallerAggregation = getSmallerAggregation(e.getCount());
        goDown(e.getDate(), end, smallerAggregation);
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            // todo: check price quality if push is allowed
            event.addComponentToReload(null, this);
        }
        return null;
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        // empty, we just care about reloads
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }
}
