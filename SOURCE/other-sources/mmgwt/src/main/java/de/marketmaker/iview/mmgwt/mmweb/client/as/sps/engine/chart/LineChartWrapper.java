package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.chart;

import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DataItemFormatter;
import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;

/**
 * Author: umaurer
 * Created: 19.06.15
 */
public class LineChartWrapper extends ChartWrapper {
    public enum Type {
        POINT(Series.Type.SCATTER),
        LINE(Series.Type.LINE),
        AREA(Series.Type.AREA);

        private final Series.Type type;
        Type(Series.Type type) {
            this.type = type;
        }

        public Series.Type getHighchartsType() {
            return type;
        }
    }

    public LineChartWrapper(Type type) {
        super(type.getHighchartsType());
    }

    @Override
    public void configure(SpsChart spsChart, DataItemFormatter dataItemFormatter) {
        if (spsChart.isLabelIsDate()) {
            this.chart.getXAxis().setType(Axis.Type.DATE_TIME);
        }
        this.chart.getYAxis().setAxisTitle(null);
        this.chart.setLegend(new Legend().setEnabled(false));
        if (spsChart.isLabelIsDate()) {
            this.chart.setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
                @Override
                public String format(ToolTipData toolTipData) {
                    final Point point = toolTipData.getPoint();
                    final MmJsDate date = new MmJsDate(point.getX().longValue());
                    return toolTipData.getSeriesName() + "<br/>" + JsDateFormatter.formatDdmmyyyy(date) + ": <b>" + point.getText() + "</b>"; // $NON-NLS$
                }
            }));
        }
        else {
            this.chart.setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
                @Override
                public String format(ToolTipData toolTipData) {
                    final Point point = toolTipData.getPoint();
                    return toolTipData.getSeriesName() + "<br/>" + point.getText(); // $NON-NLS$
                }
            }));
        }
    }
}
