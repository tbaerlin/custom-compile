package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.chart;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DataItemFormatter;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Style;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.labels.PieDataLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.PiePlotOptions;

import static org.moxieapps.gwt.highcharts.client.ChartTitle.VerticalAlign.MIDDLE;

/**
 * Author: umaurer
 * Created: 19.06.15
 */
public class PieChartWrapper extends ChartWrapper {
    private static final String STYLE_WITH_LABELS = "dataLabels";  // $NON-NLS$
    private static final String STYLE_WITH_LABELS_FLEX = "dataLabelsFlex";  // $NON-NLS$

    private final boolean donut;

    public PieChartWrapper(boolean donut) {
        super(Series.Type.PIE);
        this.donut = donut;
    }

    @Override
    public void configure(SpsChart spsChart, DataItemFormatter dataItemFormatter) {
        final PiePlotOptions piePlotOptions = new PiePlotOptions();
        final int colSpan = spsChart.getContainerConfig().getInt("colSpan", 1); // $NON-NLS$
        if (!(spsChart.hasStyle(STYLE_WITH_LABELS) || spsChart.hasStyle(STYLE_WITH_LABELS_FLEX) && colSpan > 1)) {
            piePlotOptions.setPieDataLabels(new PieDataLabels().setEnabled(false));
        }
        if (this.donut) {
            piePlotOptions.setInnerSize(0.6);
        }
        this.chart.setPiePlotOptions(piePlotOptions);
        this.chart.setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
            @Override
            public String format(ToolTipData toolTipData) {
                if (toolTipData.getPoint().getText() == null) {
                    return toolTipData.getPointName();
                }
                return toolTipData.getPointName() + "<br>" + toolTipData.getSeriesName() + ":<br><b>" + toolTipData.getPoint().getText() + "</b>"; // $NON-NLS$
            }
        }));
    }

    @Override
    void addSeries(String[] seriesNames, Point[][] points) {
        if (points.length == 1) {
            if (points[0].length == 0) {
                this.chart.setColors("#eee"); // $NON-NLS$
                this.chart.addSeries(createSeries(seriesNames[0]).setPoints(new Point[]{createPoint(1, I18n.I.noData(), null)}));
                return;
            }
            if (hasOnlyZeroValues(points[0])) {
                this.chart.setColors("#eee"); // $NON-NLS$
                Point[] point = points[0];
                for (int i = 0; i < point.length; i++) {
                    final String text = point[i].getText();
                    point[i] = createPoint(1, point[i].getName(), point[i].getX());
                    if (text != null) {
                        point[i].setText(text);
                    }
                }
            }
        }
        super.addSeries(seriesNames, points);
    }

    private boolean hasOnlyZeroValues(Point[] point) {
        for (Point p : point) {
            final Number y = p.getY();
            if (y != null && y.doubleValue() != 0d) {
                return false;
            }
        }
        return true;
    }

    @Override
    public <P extends SpsProperty> void drawChart(String[] seriesNames, String[] categories, Point[][] points, BindFeature<P> bindFeature) {
        super.drawChart(seriesNames, categories, points, bindFeature);
        if (!this.donut) {
            return;
        }
        else if (bindFeature == null) {
            return;
        }
        final String formattedValue = getFormattedValue(bindFeature);
        if (formattedValue == null) {
            return;
        }
        final ParsedTypeInfo pti = ((SpsLeafProperty) bindFeature.getSpsProperty()).getParsedTypeInfo();
        final String description = pti.getDisplayName();
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
//        sb.appendHtmlConstant("<div style=\"text-align: center;\">");
        if (StringUtility.hasText(description)) {
            sb.appendEscaped(description).appendHtmlConstant("<br>");
        }
        sb.appendHtmlConstant("<b>").appendEscaped(formattedValue).appendHtmlConstant("</b>");
//        sb.appendHtmlConstant("</div>");
        this.chart.setChartTitle(new ChartTitle()
                        .setText(sb.toSafeHtml().asString())
                        .setVerticalAlign(MIDDLE)
                        .setStyle(new Style().setFontSize("11px")) // $NON-NLS$
                        .setY(0)
                /*.setOption("useHTML", "true")*/); // $NON-NLS$
    }

    public <P extends SpsProperty> String getFormattedValue(BindFeature<P> bindFeature) {
        if (!(bindFeature.getSpsProperty() instanceof SpsLeafProperty)) {
            return null;
        }
        return bindFeature.getDataItemFormatter(null).withTrailingZeros(true)
                .format(((SpsLeafProperty) bindFeature.getSpsProperty()).getDataItem());
    }
}
