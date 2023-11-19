package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.chart;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.pmxml.DTAxisSpec;
import de.marketmaker.iview.pmxml.DTDiagram;
import de.marketmaker.iview.pmxml.DTPoint;
import de.marketmaker.iview.pmxml.DTSeries;
import de.marketmaker.iview.pmxml.TeeSeriesTypes;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;

import java.util.List;

/**
 * Author: umaurer
 * Created: 20.04.15
 */
public class BarChartFactory extends ChartFactory {
    @Override
    public Chart createChart(DTDiagram diagram, boolean fullscreen) {
        final List<DTSeries> listDtSeries = diagram.getSeries();
        if (listDtSeries.isEmpty()) {
            Firebug.warn("bar chart \"" + diagram.getCaption() + "\" has no series --> no bar chart");
            return null;
        }
        if (listDtSeries.size() > 1) {
            Firebug.warn("bar chart \"" + diagram.getCaption() + "\" has more than one series --> ignore");
        }
        final DTSeries dtSeries = listDtSeries.get(0);
        final List<DTPoint> dtPoints = getDataPoints(dtSeries, diagram.isShowGroups());
        if (dtPoints.isEmpty()) {
            Firebug.warn("bar chart series \"" + dtSeries.getTitle() + "\" has no points --> no bar chart");
            return null;
        }

        final Chart chart = createChart(getChartType(dtSeries.getSeriesType()), diagram.getCaption(), fullscreen);
        chart.setReflow(false);

        final int entryCount = dtPoints.size();
        final String[] xCategories = new String[entryCount];
        final Point[] points = new Point[entryCount];
        for (int i = 0; i < entryCount; i++) {
            final DTPoint dtPoint = dtPoints.get(i);
            float value = Float.parseFloat(dtPoint.getY());
            xCategories[i] = dtPoint.getLbl();
            points[i] = new Point(value);
        }

        final List<DTAxisSpec> axes = getAxes(diagram.getAxes(), dtSeries.getAxes());
        chart.getXAxis().setCategories(xCategories);
        chart.getYAxis().setAxisTitleText(axes.get(1).getTitle());

        final Series series = chart.createSeries()
                .setName(dtSeries.getTitle())
                .setPoints(points);
        chart.addSeries(series);

        return chart;
    }

    private Series.Type getChartType(TeeSeriesTypes pmType) {
        switch (pmType) {
            case TST_BAR_SERIES:
                return Series.Type.COLUMN;
            case TST_HORIZ_BAR_SERIES:
                return Series.Type.BAR;
            default:
                return null;
        }
    }

}
