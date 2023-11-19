package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.chart;

import de.marketmaker.iview.mmgwt.mmweb.client.Dimension;

/**
 * Author: umaurer
 * Created: 19.06.15
 */
public abstract class ChartFactory {

    public static final ChartFactory POINT = new ChartFactory() {
        @Override
        ChartWrapper createChartWrapper() {
            return new LineChartWrapper(LineChartWrapper.Type.POINT);
        }
    };
    public static final ChartFactory LINE = new ChartFactory() {
        @Override
        ChartWrapper createChartWrapper() {
            return new LineChartWrapper(LineChartWrapper.Type.LINE);
        }
    };
    public static final ChartFactory AREA = new ChartFactory() {
        @Override
        ChartWrapper createChartWrapper() {
            return new LineChartWrapper(LineChartWrapper.Type.AREA);
        }
    };
    public static final ChartFactory BAR = new ChartFactory() {
        @Override
        ChartWrapper createChartWrapper() {
            return new BarChartWrapper(false);
        }
    };
    public static final ChartFactory HORIZ_BAR = new ChartFactory() {
        @Override
        ChartWrapper createChartWrapper() {
            return new BarChartWrapper(true);
        }
    };
    public static final ChartFactory PIE = new ChartFactory() {
        @Override
        ChartWrapper createChartWrapper() {
            return new PieChartWrapper(false);
        }
    };
    public static final ChartFactory DONUT = new ChartFactory() {
        @Override
        ChartWrapper createChartWrapper() {
            return new PieChartWrapper(true);
        }
    };

    abstract ChartWrapper createChartWrapper();

    public int getMaxValueCount() {
        return Integer.MAX_VALUE;
    }
}
