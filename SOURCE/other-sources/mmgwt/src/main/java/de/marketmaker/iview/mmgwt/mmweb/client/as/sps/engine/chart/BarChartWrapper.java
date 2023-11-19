package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.chart;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DataItemFormatter;
import org.moxieapps.gwt.highcharts.client.*;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;

/**
 * Author: umaurer
 * Created: 19.06.15
 */
public class BarChartWrapper extends ChartWrapper {
    public BarChartWrapper(boolean horizontal) {
        super(horizontal ? Series.Type.BAR : Series.Type.COLUMN);
    }

    @Override
    public void configure(SpsChart spsChart, DataItemFormatter dataItemFormatter) {
        final XAxisLabels xAxisLabels = new XAxisLabels().setEnabled(!spsChart.hasStyle("noAxisLabel")); // $NON-NLS$
        final Float xAxisLabelRotation = spsChart.getStyleValueFloat("xAxisLabel-rotation"); // $NON-NLS$
        if (xAxisLabelRotation != null) {
            xAxisLabels.setRotation(xAxisLabelRotation);
        }
        this.chart.getXAxis().setLabels(xAxisLabels);
        this.chart.getYAxis().setAxisTitle(null);
        this.chart.setLegend(new Legend().setEnabled(false));

        final Float maxValue = spsChart.getStyleValueFloat("maxValue"); // $NON-NLS$
        if (maxValue != null) {
            this.chart.getYAxis().setMax(maxValue);
        }

        this.chart.setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
            @Override
            public String format(ToolTipData toolTipData) {
                return toolTipData.getPointName() + "<br>" + toolTipData.getSeriesName() + ":<br><b>" + toolTipData.getPoint().toString() + "</b>"; // $NON-NLS$
            }
        }));
    }
}
