package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.core.client.Callback;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.widgets.highcharts.Highcharts;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.Dimension;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DataItemFormatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CssValues;
import de.marketmaker.iview.pmxml.MM;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Pane;
import org.moxieapps.gwt.highcharts.client.PaneBackground;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;

import java.math.BigDecimal;

/**
 * Author: umaurer
 * Created: 07.04.15
 */
public class SpsGauge extends SpsBoundWidget<SimplePanel, SpsLeafProperty> implements NoValidationPopup {
    public static final Dimension DEFAULT_SIZE = new Dimension(300, 150);
    private final SimplePanel panel = new SimplePanel();
    private final String titleText;
    private final double minValue;
    private final double maxValue;
    private final double minOverflow;
    private final double maxOverflow;
    private final double defaultValue;
    private final DataItemFormatter formatter;
    private Chart chart;
    private Dimension size = DEFAULT_SIZE;

    public SpsGauge(String titleText, double minValue, double maxValue, double defaultValue, DataItemFormatter formatter) {
        this.titleText = titleText;
        this.minValue = minValue;
        this.maxValue = maxValue;
        final double o = (this.maxValue - this.minValue) / 40;
        this.minOverflow = this.minValue - o;
        this.maxOverflow = this.maxValue + o;
        this.defaultValue = defaultValue;
        this.formatter = formatter;
    }

    public void setSize(Dimension size) {
        this.size = size == null ? DEFAULT_SIZE : size;
    }

    @Override
    public void onPropertyChange() {
        if (this.chart != null) {
            final MM dataItem = getBindFeature().getSpsProperty().getDataItem();
            this.chart.getSeries()[0].setPoints(new Point[]{new Point(this.formatter.format(dataItem), getDisplayValue(this.formatter.value(dataItem)))});
        }
        else {
            Highcharts.initialize(Highcharts.Type.SOLIDGAUGE, new Callback<Void, Exception>() {
                @Override
                public void onFailure(Exception e) {
                    Notifications.add(I18n.I.error(), I18n.I.spsErrorHighchartsInitFailed(e.getMessage()));
                }

                @Override
                public void onSuccess(Void aVoid) {
                    createChart();
                    onPropertyChange();
                }
            });
        }
    }

    private Number getDisplayValue(BigDecimal value) {
        final double d = value.doubleValue();
        return d > this.maxOverflow
                ? this.maxOverflow
                : d < this.minOverflow
                ? this.minOverflow
                : value;
    }

    private void createChart() {
        this.chart = new Chart()
                .setOption("/chart/type", "solidgauge") // $NON-NLS$
                .setMarginRight(10)
                .setColors(CssValues.getChartColors())
                .setChartTitleText(this.titleText)
                .setToolTip(new ToolTip().setEnabled(false))
                .setCredits(new Credits().setEnabled(false))
                .setSize(this.size.getWidth(), this.size.getHeight());
        this.chart.setPane(
                new Pane()
                        .setStartAngle(-90).setEndAngle(90)
                        .setCenter("50%", "85%") // $NON-NLS$
                        .setOption("size", "160%") // $NON-NLS$
                        .setBackground(
                                new PaneBackground()
                                        .setOption("shape", "arc") // $NON-NLS$
                                        .setBackgroundColor("#eee") // $NON-NLS$
                                        .setInnerRadius("55%") // $NON-NLS$
                                        .setOuterRadius("100%") // $NON-NLS$
                        )
        );
        final YAxisLabels yAxisLabels = new YAxisLabels().setY(12);
        if (this.formatter.isPercent()) {
            yAxisLabels.setFormatter(new AxisLabelsFormatter() {
                @Override
                public String format(AxisLabelsData data) {
                    return data.getValueAsString() + "%";
                }
            });
        }
        this.chart.getYAxis()
                .setMin(this.minValue).setMax(this.maxValue)
                .setOption("stops", createGaugeStops()) // $NON-NLS$
                .setOption("tickPositions", createArray(this.minValue, this.maxValue)) // $NON-NLS$
                .setLineWidth(0)
                .setMinorTickInterval(null)
                .setTickWidth(0)
                .setLabels(yAxisLabels);
        final Series series = this.chart.createSeries()
                .setPoints(new Point[]{new Point("", this.defaultValue)})
                .setOption("dataLabels/borderWidth", 0) // $NON-NLS$
                .setOption("dataLabels/useHTML", true) // $NON-NLS$
                .setOption("dataLabels/format", "<div style=\"font-size:12px\">{point.name}</div>") // $NON-NLS$
                ;
        this.chart.addSeries(series);
        this.panel.setWidget(this.chart);
    }

    private JSONArray createGaugeStops() {
        final JSONArray stops = new JSONArray();
        final String[] colors = CssValues.getChartColors();
        double tick = 1d / (colors.length + 1);
        for (int i = 0; i < colors.length; i++) {
            addStop(stops, tick * i, colors[i]);
        }
        return stops;
    }

    private void addStop(JSONArray stops, double value, String color) {
        final JSONArray stop = new JSONArray();
        stop.set(0, new JSONNumber(value));
        stop.set(1, new JSONString(color));
        stops.set(stops.size(), stop);
    }

    private JSONArray createArray(double... values) {
        final JSONArray array = new JSONArray();
        for (int i = 0; i < values.length; i++) {
            array.set(i, new JSONNumber(values[i]));
        }
        return array;
    }

    @Override
    protected SimplePanel createWidget() {
        return this.panel;
    }
}
