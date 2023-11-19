package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.GwtDateParser;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.CloseTimeseries;
import de.marketmaker.iview.dmxml.CloseTimeseriesElement;
import de.marketmaker.iview.dmxml.MSCHistoricData;
import de.marketmaker.iview.dmxml.OHLCTimeseries;
import de.marketmaker.iview.dmxml.OHLCTimeseriesElement;
import de.marketmaker.iview.dmxml.Timeseries;
import de.marketmaker.iview.dmxml.TimeseriesElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import org.moxieapps.gwt.highcharts.client.Point;

import java.util.List;

/**
 * Author: umaurer
 * Created: 14.01.15
 */
public class HighchartSnippet extends AbstractSnippet<HighchartSnippet, HighchartSnippetView>
        implements SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("Highchart"); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new HighchartSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("title", I18n.I.chart()); // $NON-NLS$
        }
    }

    private static final double MILLIS_PER_DAY = 1000d * 60d * 60d * 24d;

    private final DmxmlContext.Block<MSCHistoricData> block;
    private boolean refreshChart = true;
    private boolean allowAggregationAdaption = true;

    public HighchartSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("MSC_HistoricData"); // $NON-NLS$
        this.block.setParameter("type", "OHLC"); // $NON-NLS$

        setView(new HighchartSnippetView(this));
    }

    @Override
    public void destroy() {
        destroyBlock(this.block);
    }

    @Override
    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        final SnippetConfiguration conf = getConfiguration();
        conf.put("symbol", symbol); // $NON-NLS$
        conf.put("aggregation", "P90D"); // $NON-NLS$
        conf.put("type", "OHLC"); // $NON-NLS$
        this.allowAggregationAdaption = true;
        conf.remove("start"); // $NON-NLS$
        conf.remove("end"); // $NON-NLS$
        onParametersChanged();
    }

    @Override
    protected void onParametersChanged() {
        final SnippetConfiguration conf = getConfiguration();
        this.block.setParameter("symbol", conf.getString("symbol", null)); // $NON-NLS$
        final String start = conf.getString("start"); // $NON-NLS$
        this.block.setParameter("start", start == null ? "1900-01-01T00:00:00" : start); // $NON-NLS$
        this.block.setParameter("end", conf.getString("end")); // $NON-NLS$
        final String aggregation = conf.getString("aggregation"); // $NON-NLS$
        this.block.setParameter("aggregation", aggregation); // $NON-NLS$
        this.block.setParameter("type", conf.getString("type")); // $NON-NLS$
        Firebug.info("HighchartsSnippet.onParametersChanged() -> agg: " + aggregation + " refresh: " + this.refreshChart);
    }

    public void loadNewExtremes(MmJsDate start, MmJsDate end) {
        final SnippetConfiguration conf = getConfiguration();
        conf.put("start", JsDateFormatter.formatIsoDateTime(start)); // $NON-NLS$
        conf.put("end", JsDateFormatter.formatIsoDateTime(end)); // $NON-NLS$
        final String aggregation = getAggregation(start);
        conf.put("aggregation", aggregation); // $NON-NLS$
        this.refreshChart = false;
        super.ackParametersChanged();
    }

    private MmJsDate getDate(SnippetConfiguration conf, String key, MmJsDate defaultValue) {
        final String s = conf.getString(key);
        return s == null
                ? defaultValue
                : GwtDateParser.getMmJsDate(s);
    }

    private String getAggregation(MmJsDate start) {
        final SnippetConfiguration conf = getConfiguration();
        final MmJsDate end = getDate(conf, "end", new MmJsDate()); // $NON-NLS$
        final double diffDays = (double)(end.getTime() - start.getTime()) / MILLIS_PER_DAY;
        final String aggregation;
        String type = "OHLC"; // $NON-NLS$
        /*if (diffDays < 3) {
            aggregation = "P1H"; // $NON-NLS$
        }
        else*/ if (diffDays < 90) {
            aggregation = "P1D"; // $NON-NLS$
            type = "CLOSE";
        }
        else if (diffDays < 270) {
            aggregation = "P3D"; // $NON-NLS$
            type = "CLOSE";
        }
        else if (diffDays < 630) {
            aggregation = "P7D"; // $NON-NLS$
        }
        else if (diffDays < 1260) {
            aggregation = "P14D"; // $NON-NLS$
        }
        else if (diffDays < 2700) {
            aggregation = "P30D"; // $NON-NLS$
        }
        else if (diffDays < 8100) {
            aggregation = "P90D"; // $NON-NLS$
        }
        else {
            aggregation = "P180D"; // $NON-NLS$
        }
        conf.put("type", type); // $NON-NLS$
        return aggregation;
    }

    @Override
    public void updateView() {
        if (!this.block.blockChanged()) {
            return;
        }

        if (!this.block.isResponseOk()) {
            getView().clear();
            return;
        }

        final MSCHistoricData historicData = this.block.getResult();
        final Timeseries items = historicData.getItems();
        final Point[] points;
        if (items instanceof OHLCTimeseries) {
            points = toPoints(((OHLCTimeseries) items).getItem());
        }
        else if (items instanceof CloseTimeseries) {
            points = toPoints(((CloseTimeseries) items).getItem());
        }
        else {
            throw new RuntimeException("cannot handle " + items.getClass().getSimpleName()); // $NON-NLS$
        }
        if (points == null) {
            return;
        }
        getView().update(points, this.refreshChart);
        this.refreshChart = true;
        this.allowAggregationAdaption = false;
    }

    private <T extends Timeseries, E extends TimeseriesElement> Point[] toPoints(List<E> listItems) {
        final String bestAggregation = getAggregation(GwtDateParser.getMmJsDate(listItems.get(0).getDate()));
        final SnippetConfiguration conf = getConfiguration();
        final String requestedAggregation = conf.getString("aggregation"); // $NON-NLS$
        if (this.allowAggregationAdaption && listItems.size() > 2 && !bestAggregation.equals(requestedAggregation)) {
            this.allowAggregationAdaption = false;
            conf.put("aggregation", bestAggregation); // $NON-NLS$
            ackParametersChanged();
            return null;
        }
        final Point[] points = new Point[listItems.size()];
        for (int i = listItems.size() - 1; i >= 0; i--) {
            points[i] = toPoint(listItems.get(i));
        }
        return points;
    }

    private <E extends TimeseriesElement> Point toPoint(E e) {
        final MmJsDate mmJsDate = GwtDateParser.getMmJsDate(e.getDate());
        if (e instanceof OHLCTimeseriesElement) {
            return toPoint(mmJsDate, (OHLCTimeseriesElement) e);
        }
        else if (e instanceof CloseTimeseriesElement) {
            return toPoint(mmJsDate, (CloseTimeseriesElement) e);
        }
        throw new IllegalArgumentException("cannot handle " + e.getClass().getSimpleName()); // $NON-NLS$
    }

    private Point toPoint(MmJsDate mmJsDate, CloseTimeseriesElement close) {
        return new Point(mmJsDate.getTime(), toDouble(close.getClose()));
    }

    private Point toPoint(MmJsDate mmJsDate, OHLCTimeseriesElement ohlc) {
        return new Point(mmJsDate.getTime(), toDouble(ohlc.getOpen()), toDouble(ohlc.getHigh()), toDouble(ohlc.getLow()), toDouble(ohlc.getClose()));
    }

    private Double toDouble(String s) {
        return s == null ? null : Double.valueOf(s);
    }
}
