package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.chart;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.pmxml.DTAxisSpec;
import de.marketmaker.iview.pmxml.DTDiagram;
import de.marketmaker.iview.pmxml.DTPoint;
import de.marketmaker.iview.pmxml.DTSeries;
import de.marketmaker.iview.pmxml.TeeSeriesTypes;
import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.XAxis;

import java.util.List;

/**
 * Author: umaurer
 * Created: 15.04.15
 */
public class PointChartFactory extends ChartFactory {
    @Override
    public Chart createChart(final DTDiagram diagram, boolean fullscreen) {
        final List<DTSeries> listDtSeries = diagram.getSeries();
        final List<DTAxisSpec> axes = getAxes(diagram.getAxes(), diagram.getSeries().get(0).getAxes());
        final Chart chart = createChart(Series.Type.LINE, diagram.getCaption(), fullscreen);
        chart.setReflow(false);
        final DTAxisSpec dtXAxis = axes.get(0);
        final boolean xIsDateTime = dtXAxis.isIsDateTime();
        final XAxis xAxis = chart.getXAxis();
        if (xIsDateTime) {
            xAxis.setType(Axis.Type.DATE_TIME);
        }
        xAxis.setAxisTitleText(dtXAxis.getTitle());
        chart.getYAxis().setAxisTitleText(axes.get(1).getTitle());
        chart.setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
            @Override
            public String format(ToolTipData toolTipData) {
                final JSONObject userData = toolTipData.getPoint().getUserData();
                final int seriesId = (int) userData.get("seriesId").isNumber().doubleValue(); // $NON-NLS$
                final DTSeries dtSeries = listDtSeries.get(seriesId);
                final List<DTPoint> dtPoints = getDataPoints(dtSeries, diagram.isShowGroups());
                final int pointId = (int) userData.get("pointId").isNumber().doubleValue(); // $NON-NLS$
                final DTPoint dtPoint = dtPoints.get(pointId);
                final SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendEscaped(dtSeries.getTitle())
                        .appendHtmlConstant("<br/>") // $NON-NLS$
                        .appendEscaped(dtPoint.getXText())
                        .appendHtmlConstant(": <b>") // $NON-NLS$
                        .appendEscaped(dtPoint.getYText())
                        .appendHtmlConstant("</b>"); // $NON-NLS$
                return sb.toSafeHtml().asString();
            }
        }));
        for (int i = 0; i < listDtSeries.size(); i++) {
            final DTSeries dtSeries = listDtSeries.get(i);
            addXYDoubleSeries(chart, dtSeries, i, diagram.isShowGroups(), xIsDateTime);
        }
        return chart;
    }

    private void addXYDoubleSeries(Chart chart, DTSeries dtSeries, int seriesId, boolean showGroups, boolean xIsDateTime) {
        final Series.Type chartType = getChartType(dtSeries.getSeriesType());
        if (chartType == null) {
            Firebug.warn("PointChartFactory - cannot add series type to chart: " + dtSeries.getSeriesType());
            return;
        }
        final List<DTPoint> dtPoints = getDataPoints(dtSeries, showGroups);
        final Point[] points = new Point[dtPoints.size()];
        for (int i = 0; i < dtPoints.size(); i++) {
            final DTPoint dtPoint = dtPoints.get(i);
            final Number x = xIsDateTime ? toMillis(dtPoint.getX()) : Float.parseFloat(dtPoint.getX());
            final Number y = Double.parseDouble(dtPoint.getY());
            points[i] = new Point(x, y).setUserData(createUserData(seriesId, i));
        }
        final Series series = chart.createSeries()
                .setType(chartType)
                .setName(dtSeries.getTitle())
                .setPoints(points);
        chart.addSeries(series);
    }

    private Number toMillis(String s) {
        final double daysFrom30121899 = Double.parseDouble(s);
        final double daysFrom01011970 = daysFrom30121899 - 25569d;
        return (long)(daysFrom01011970 * DateTimeUtil.MILLIS_PER_DAY);
    }

    private Series.Type getChartType(TeeSeriesTypes pmType) {
        switch (pmType) {
            case TST_POINT_SERIES:
                return Series.Type.SCATTER;
            case TST_LINE_SERIES:
                return Series.Type.LINE;
            case TST_AREA_SERIES:
                return Series.Type.AREA;
            default:
                return null;
        }
    }

}
