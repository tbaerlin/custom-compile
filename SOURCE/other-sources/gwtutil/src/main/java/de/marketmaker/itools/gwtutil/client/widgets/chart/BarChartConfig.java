package de.marketmaker.itools.gwtutil.client.widgets.chart;

/**
 * Author: umaurer
 * Created: 08.12.14
 */
public class BarChartConfig {
    public interface LineTextFormatter {
        String format(double value);
    }
    private static final LineTextFormatter DEFAULT_LINE_TEXT_FORMATTER = new LineTextFormatter() {
        @Override
        public String format(double value) {
            return String.valueOf(value);
        }
    };

    private boolean horizontal = false;
    private LineTextFormatter lineTextFormatter = DEFAULT_LINE_TEXT_FORMATTER;
    private int textHeight = 24;  // in pixel
    private int chartHeight = 250;
    private int minValueWidth = 10;
    private int minChartWidth = 100;
    private int valueGap = 0;
    private int entryGap = 4;
    private boolean entryLabels = false;
    private float minValue = 0;
    private float maxValue = 0;

    public void horizontal() {
        this.horizontal = true;
    }

    public BarChartConfig horizontal(boolean horizontal) {
        this.horizontal = horizontal;
        return this;
    }

    public BarChartConfig lineTextFormatter(LineTextFormatter lineTextFormatter) {
        assert lineTextFormatter != null: "lineTextFormatter must not be null";
        this.lineTextFormatter = lineTextFormatter;
        return this;
    }

    public BarChartConfig textHeight(int textHeight) {
        this.textHeight = textHeight;
        return this;
    }

    public BarChartConfig chartHeight(int chartHeight) {
        this.chartHeight = chartHeight;
        return this;
    }

    public BarChartConfig minValueWidth(int minValueWidth) {
        this.minValueWidth = minValueWidth;
        return this;
    }

    public BarChartConfig minChartWidth(int minChartWidth) {
        this.minChartWidth = minChartWidth;
        return this;
    }

    public BarChartConfig valueGap(int valueGap) {
        this.valueGap = valueGap;
        return this;
    }

    public BarChartConfig entryGap(int entryGap) {
        this.entryGap = entryGap;
        return this;
    }

    public BarChartConfig entryLabels() {
        this.entryLabels = true;
        return this;
    }

    public BarChartConfig minValue(float minValue) {
        this.minValue = minValue;
        return this;
    }

    public BarChartConfig maxValue(float maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public LineTextFormatter getLineTextFormatter() {
        return lineTextFormatter;
    }

    public int getTextHeight() {
        return textHeight;
    }

    public int getChartHeight() {
        return chartHeight;
    }

    public int getMinValueWidth() {
        return minValueWidth;
    }

    public int getMinChartWidth() {
        return minChartWidth;
    }

    public int getValueGap() {
        return valueGap;
    }

    public int getEntryGap() {
        return entryGap;
    }

    public boolean isEntryLabels() {
        return entryLabels;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }
}
