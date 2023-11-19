package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.chart;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.pmxml.DTDiagram;
import de.marketmaker.iview.pmxml.DTPoint;
import de.marketmaker.iview.pmxml.DTSeries;
import de.marketmaker.iview.pmxml.TeeSeriesTypes;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.labels.DataLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.DataLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.PieDataLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.PiePlotOptions;

import java.util.List;

/**
 * Author: umaurer
 * Created: 20.04.15
 */
public class PieChartFactory extends ChartFactory {
    @Override
    public Chart createChart(DTDiagram diagram, boolean fullscreen) {
        final List<DTSeries> listDtSeries = diagram.getSeries();
        if (listDtSeries.isEmpty()) {
            Firebug.warn("pie chart \"" + diagram.getCaption() + "\" has no series --> no pie chart");
            return null;
        }
        if (listDtSeries.size() > 1) {
            Firebug.warn("pie chart \"" + diagram.getCaption() + "\" has more than one series --> ignore");
        }
        final DTSeries dtSeries = listDtSeries.get(0);
        final List<DTPoint> dtPoints = getDataPoints(dtSeries, diagram.isShowGroups());

        final Chart chart = createChart(Series.Type.PIE, diagram.getCaption(), fullscreen);
        chart.setReflow(false);

        double valueSum = 0d;
        final Point[] points = new Point[dtPoints.size()];
        for (int i = 0; i < dtPoints.size(); i++) {
            final DTPoint dtPoint = dtPoints.get(i);
            float value = Float.parseFloat(dtPoint.getY());
            valueSum += Math.abs(value);
            points[i] = new Point(Integer.toString(i), value);
        }

        if (valueSum == 0d) {
            Firebug.warn("pie chart series \"" + dtSeries.getTitle() + "\" sums to 0 --> no pie chart");
            return null;
        }

        final PiePlotOptions piePlotOptions = new PiePlotOptions()
                .setPieDataLabels(new PieDataLabels().setFormatter(new DataLabelsFormatter() {
                    @Override
                    public String format(DataLabelsData dataLabelsData) {
                        final int i = Integer.parseInt(dataLabelsData.getPointName());
                        final DTPoint dtPoint = dtPoints.get(i);
                        return dtPoint.getLbl();
                    }
                }));
        if (dtSeries.getSeriesType() == TeeSeriesTypes.TST_DONUT_SERIES) {
            piePlotOptions.setInnerSize(0.6);
        }

        chart
                .setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
                    @Override
                    public String format(ToolTipData toolTipData) {
                        final int i = Integer.parseInt(toolTipData.getPointName());
                        final DTPoint dtPoint = dtPoints.get(i);
                        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
                        sb.appendEscaped(dtPoint.getLbl())
                                .appendHtmlConstant("<br/><b>") // $NON-NLS$
                                .appendEscaped(dtPoint.getYText())
                                .appendHtmlConstant("</b>"); // $NON-NLS$
                        return sb.toSafeHtml().asString();
                    }
                }))
                .setPiePlotOptions(piePlotOptions);

        chart.addSeries(chart.createSeries().setName("").setPoints(points))
                .setSymbols();

        return chart;
    }
}
