/*
 * OHLCVSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.CloseTimeseries;
import de.marketmaker.iview.dmxml.CloseTimeseriesElement;
import de.marketmaker.iview.dmxml.DecimalField;
import de.marketmaker.iview.dmxml.FundTimeseries;
import de.marketmaker.iview.dmxml.FundTimeseriesElement;
import de.marketmaker.iview.dmxml.MSCHistoricData;
import de.marketmaker.iview.dmxml.NumberField;
import de.marketmaker.iview.dmxml.OHLCVTimeseries;
import de.marketmaker.iview.dmxml.OHLCVTimeseriesElement;
import de.marketmaker.iview.dmxml.Timeseries;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.HashMap;
import java.util.List;

/**
 * controller part for displaying a table with daily ohlcv values
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OHLCVSnippet extends AbstractSnippet<OHLCVSnippet, OHLCVSnippetView>
        implements HasValueChangeHandlers<String> {

    public static class Class extends SnippetClass {
        public Class() {
            super("OHLCV"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new OHLCVSnippet(context, config);
        }
    }

    private final DmxmlContext.Block<MSCHistoricData> block;

    private MmJsDate dateLabel = null;

    private PriceTeaserSnippet teaser;

    private HandlerManager manager;

    private OHLCVSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("MSC_HistoricData"); // $NON-NLS-0$
        this.block.setParameter("aggregation", DateTimeUtil.PeriodEnum.P1D.toString()); // $NON-NLS-0$

        final OHLCVSnippetView view = createView(config);
        onParametersChanged();
        setView(view);
    }

    private OHLCVSnippetView createView(SnippetConfiguration config) {
        final OHLCVSnippetView result = new OHLCVSnippetView(this, new MmJsDate().atMidnight().addDays(-1));

        final String type = config.getString("type");  // $NON-NLS$
        switch (type) {
            case "metals": // $NON-NLS$
                this.block.setParameters("field", METAL_FIELDS);  // $NON-NLS$
                this.block.removeParameter("type"); // $NON-NLS$
                result.setColumnModel(result.createMetalsColumnModel());
                break;
            case "FUND": // $NON-NLS$
                this.block.removeParameter("field"); // $NON-NLS$
                this.block.setParameter("type", "FUND"); // $NON-NLS$
                result.setColumnModel(result.createFundColumnModel());
                break;
            case "OHLCV": // $NON-NLS$
            default:
                this.block.removeParameter("field"); // $NON-NLS$
                this.block.setParameter("type", "OHLCV"); // $NON-NLS$
                result.setColumnModel(result.createOHLCVColumnModel());
        }
        return result;
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

    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<String> handler) {
        if (this.manager == null) {
            this.manager = new HandlerManager(this);
        }
        return this.manager.addHandler(ValueChangeEvent.getType(), handler);
    }

    public void fireEvent(GwtEvent<?> gwtEvent) {
        if (this.manager != null) {
            this.manager.fireEvent(gwtEvent);
        }
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void setSymbol(String symbol) {
        getConfiguration().put("symbol", symbol); // $NON-NLS-0$
        onParametersChanged();
    }

    protected void onParametersChanged() {
        final SnippetConfiguration conf = getConfiguration();
        final String symbol = conf.getString("symbol", null); // $NON-NLS-0$
        this.block.setParameter("start", conf.getString("start")); // $NON-NLS$
        final String end = conf.getString("end"); // $NON-NLS-0$
        if (end != null) {
            this.block.setParameter("end", end); // $NON-NLS-0$
        }
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void setTitle(String title) {
        getView().setTitle(title);
    }

    public void updateView() {
        if (!this.block.blockChanged()) {
            return;
        }

        final DefaultTableDataModel dtm;
        if (!block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        if (this.dateLabel != null) {
            getView().setDate(this.dateLabel);
            this.dateLabel = null;
        }

        final MSCHistoricData historicData = this.block.getResult();
        final Timeseries items = historicData.getItems();

        final String type = getConfiguration().getString("type");   // $NON-NLS$
        switch (type) {
            case "metals":   // $NON-NLS$
                getView().update(createTableDataModel((CloseTimeseries) items));
                break;
            case "FUND":     // $NON-NLS$
                getView().update(createTableDataModel((FundTimeseries) items));
                break;
            case "OHLCV":    // $NON-NLS$
            default:
                getView().update(createTableDataModel((OHLCVTimeseries) items));
        }
    }

    private DefaultTableDataModel createTableDataModel(CloseTimeseries timeseries) {
        final List<CloseTimeseriesElement> elements = timeseries.getItem();
        final DefaultTableDataModel dtm = new DefaultTableDataModel(elements.size(), 12);
        final FieldConverter converter = new FieldConverter();
        for (int i = elements.size() - 1, row = 0; i >= 0; i--, row++) {
            final CloseTimeseriesElement e = elements.get(i);
            converter.reset(e.getStringOrDecimalOrNumber()); // fun fact: getStringOrDecimalOrNumber returns actually a list
            dtm.setValuesAt(row, new Object[]{
                    e.getDate(),
                    converter.get(FieldConverter.Anfang),
                    converter.get(FieldConverter.Tageshoch),
                    converter.get(FieldConverter.Tagestief),
                    converter.get(FieldConverter.Schluss),
                    converter.get(FieldConverter.Umsatz_gesamt),
                    converter.get(FieldConverter.Official_Bid),
                    converter.get(FieldConverter.Official_Ask),
                    converter.get(FieldConverter.Unofficial_Bid),
                    converter.get(FieldConverter.Unofficial_Ask),
                    converter.get(FieldConverter.Interpo_Closing),
                    converter.get(FieldConverter.Prov_Evaluation)
            });
        }
        return dtm;
    }

    private DefaultTableDataModel createTableDataModel(FundTimeseries timeseries) {
        final List<FundTimeseriesElement> elements = timeseries.getItem();
        final DefaultTableDataModel dtm = new DefaultTableDataModel(elements.size(), 3);
        for (int i = elements.size() - 1, row = 0; i >= 0; i--, row++) {
            final FundTimeseriesElement e = elements.get(i);
            dtm.setValuesAt(row, new Object[]{
                    e.getDate(),
                    e.getIssuePrice(),
                    e.getRepurchasingPrice()
            });
        }
        return dtm;
    }

    private DefaultTableDataModel createTableDataModel(OHLCVTimeseries timeseries) {
        final List<OHLCVTimeseriesElement> elements = timeseries.getItem();
        final DefaultTableDataModel dtm = new DefaultTableDataModel(elements.size(), 6);
        for (int i = elements.size() - 1, row = 0; i >= 0; i--, row++) {
            final OHLCVTimeseriesElement e = elements.get(i);
            dtm.setValuesAt(row, new Object[]{
                    e.getDate(),
                    e.getOpen(),
                    e.getHigh(),
                    e.getLow(),
                    e.getClose(),
                    e.getVolume()
            });
        }
        return dtm;
    }

    void dateClicked(String date) {
        ValueChangeEvent.fire(this, date);
    }

    public void setDate(MmJsDate date) {
        final int deltaDays = getConfiguration().getInt("timerange", 50); // $NON-NLS$
        getConfiguration().put("start", JsDateFormatter.formatIsoDateTimeMidnight(new MmJsDate(date).addDays(-deltaDays))); // $NON-NLS-0$
        final String end = JsDateFormatter.formatIsoDateTimeMidnight(date);
        getConfiguration().put("end", end); // $NON-NLS-0$
        this.dateLabel = date;
        ackParametersChanged();
    }

    public boolean isEndOfDay() {
        return this.teaser != null && this.teaser.isEndOfDay();
    }


    public static final String[] METAL_FIELDS = new String[]{

            // LME fields
            "ADF_Interpo_Closing",  // 235     $NON-NLS$
            "ADF_Prov_Evaluation",  // 236     $NON-NLS$
            "ADF_Official_Ask",     // 1168    $NON-NLS$
            "ADF_Official_Bid",     // 1169    $NON-NLS$
            "ADF_Unofficial_Ask",   // 1170    $NON-NLS$
            "ADF_Unofficial_Bid",   // 1171    $NON-NLS$

            // OHLCV:
            "ADF_Anfang",           // 67      $NON-NLS$
            "ADF_Tageshoch",        // 53      $NON-NLS$
            "ADF_Tagestief",        // 63      $NON-NLS$
            "ADF_Schluss",          // 41      $NON-NLS$
            "ADF_Umsatz_gesamt",    // 83      $NON-NLS$
    };

    public static class FieldConverter {
        static final int Interpo_Closing = 235;
        static final int Prov_Evaluation = 236;
        static final int Official_Ask = 1168;
        static final int Official_Bid = 1169;
        static final int Unofficial_Ask = 1170;
        static final int Unofficial_Bid = 1171;

        static final int Anfang = 67;
        static final int Tageshoch = 53;
        static final int Tagestief = 63;
        static final int Schluss = 41;
        static final int Umsatz_gesamt = 83;

        final HashMap<Integer, String> valueMap = new HashMap<>();

        void reset(List delegate) {
            valueMap.clear();
            for (Object o : delegate) {
                if (o instanceof DecimalField) {
                    DecimalField decField = (DecimalField) o;
                    valueMap.put(Integer.valueOf(decField.getId()), decField.getValue());
                } else if (o instanceof NumberField) {
                    NumberField numField = (NumberField) o;
                    valueMap.put(Integer.valueOf(numField.getId()), numField.getValue());
                } else {
                    Firebug.info("unknown field type, object is: " + o);
                }
            }
        }

        String get(int fieldId) {
            return valueMap.get(fieldId);
        }

    }

}
