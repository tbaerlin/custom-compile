package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.chart;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.Dimension;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DataItemFormatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CssValues;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;

/**
 * Author: umaurer
 * Created: 19.06.15
 */
public class ChartWrapper implements IsWidget {
    protected final Chart chart;

    public ChartWrapper(Series.Type type) {
        this.chart = new Chart()
                .setType(type)
                .setColors(CssValues.getChartColors())
                .setCredits(new Credits().setEnabled(false))
                .setChartTitle(null);
    }

    @Override
    public Widget asWidget() {
        return this.chart;
    }

    public void configureDefaults(final SpsChart spsChart, Dimension size) {
        final int colSpan = spsChart.getContainerConfig().getInt("colSpan", 1); // $NON-NLS$
        setChartSize(size == null ? new Dimension(colSpan * 300, 300) : size);
    }

    private void setChartSize(Dimension size) {
        this.chart.setSize(size.getWidth(), size.getHeight());
    }

    public void configure(final SpsChart spsChart, final DataItemFormatter dataItemFormatter) {
/*
        final Float minValue = spsChart.getStyleValueFloat("minValue-"); // $NON-NLS$
        if (minValue != null) {
            this.chart.setMinValue(minValue);
        }
        final Float maxValue = spsChart.getStyleValueFloat("maxValue-"); // $NON-NLS$
        if (maxValue != null) {
            this.chart.setMaxValue(maxValue);
        }
        if (spsChart.hasStyle("entryLabels")) { // $NON-NLS$
            // add labels to chart
        }
        config.lineTextFormatter(new BarChartConfig.LineTextFormatter() {
            @Override
            public String format(double value) {
                return dataItemFormatter.format(new BigDecimal(value), true);
            }
        });
*/

/*
        this.chart.getYAxis().setAxisTitleText("yyy");
*/
    }

    public <P extends SpsProperty> void drawChart(String[] seriesNames, String[] categories, Point[][] points, BindFeature<P> bindFeature) {
        if (categories != null) {
            this.chart.getXAxis().setCategories(categories);
        }
        addSeries(seriesNames, points);
    }

    void addSeries(String[] seriesNames, Point[][] points) {
        for (int i = 0; i < points.length; i++) {
            final Series series = createSeries(seriesNames[i])
                    .setPoints(points[i]);
            this.chart.addSeries(series);
        }
    }

    Series createSeries(String seriesName) {
        return this.chart.createSeries().setName(seriesName);
    }

    public Point createPoint(Number value, String label, Number date) {
        final Point point = date == null ? new Point(value) : new Point(date, value);
        if (label != null) {
            point.setName(label);
        }
        return point;
    }
}