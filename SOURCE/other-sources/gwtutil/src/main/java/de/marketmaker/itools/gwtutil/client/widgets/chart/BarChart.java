package de.marketmaker.itools.gwtutil.client.widgets.chart;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;

import java.util.Arrays;
import java.util.List;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 15.12.14
 */
public class BarChart extends Composite implements RequiresResize, HasSelectionHandlers<SelectableChart.Index>, SelectableChart {
    private static final BarChartConfig CONFIG = new BarChartConfig();

    private BarChartConfig config = CONFIG;

    private final HandlerManager handlerManager = new HandlerManager(this);
    private final FlowPanel panel = new FlowPanel();
    private Label[][] bars;
    private HTMLTable.CellFormatter entryLabelFormatter;
    private Index selectedChild = null;

    // the model
    private List<Entry> listEntries;

    public BarChart() {
        this.panel.setStyleName("mm-barChart");
        this.panel.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                redraw();
            }
        });
        initWidget(this.panel);
    }

    public static class Value {
        String style; // actually this is the css class
        float value;
        String tooltip;

        public Value(float value) {
            this(null, value);
        }

        public Value(String style, float value) {
            this.style = style;
            this.value = value;
        }

        public Value withTooltip(String tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public String getTooltip() {
            return tooltip;
        }

        @Override
        public String toString() {
            return "Value{" +
                    "style='" + style + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

    public static class Entry {
        String name;
        String style;
        Value[] values;

        public Entry(String name, Value... values) {
            this(name, null, values);
        }

        public Entry(String name, String style, Value... values) {
            this.name = name;
            this.style = style;
            this.values = values;
        }

        public Entry(String name, float... values) {
            this(name, null, values);
        }

        public Entry(String name, String style, float... values) {
            this.name = name;
            this.style = style;
            this.values = new Value[values.length];
            for (int i = 0; i < values.length; i++) {
                this.values[i] = new Value(values[i]);
            }
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "name='" + name + '\'' +
                    ", style='" + style + '\'' +
                    ", values=" + Arrays.toString(values) +
                    '}';
        }
    }


    public BarChartConfig config() {
        this.config = new BarChartConfig();
        return this.config;
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<Index> handler) {
        return handlerManager.addHandler(SelectionEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    private void fireSelectionEvent(Index index) {
        SelectionEvent.fire(this, index);
    }

    @Override
    public void onResize() {
        redraw();
    }

    // this might override the size
    public void drawChart(Entry... entries) {
        drawChart(Arrays.asList(entries));
    }

    public void drawChart(List<Entry> list) {
        this.listEntries = list;
        if (list != null && !list.isEmpty()) {
            final int valuesPerEntry = list.get(0).values.length;
            for (Entry entry : list) {
                if (entry.values.length != valuesPerEntry) {
                    throw new IllegalArgumentException("BarChart <drawChart> unexpected values count (" + entry.values.length + " != " + valuesPerEntry + ")");
                }
            }
        }
        redraw();
    }

    class CoordinateSystem {
        final double minValue;
        final double maxValue;
        final double factor;
        final double zeroBottom;
        final double zeroTop;
        final int valueWidth;
        final int entryWidth;
        final int chartWidth;
        final double sectionHeight;
        int labelWidth = 0;

        public CoordinateSystem(List<Entry> listEntries) {
            float minValue = config.getMinValue();
            float maxValue = config.getMaxValue();
            for (Entry entry : listEntries) {
                for (Value value : entry.values) {
                    if (value.value > maxValue) {
                        maxValue = value.value;
                    }
                    else if (value.value < minValue) {
                        minValue = value.value;
                    }
                }
            }
            if (minValue == maxValue) {
                maxValue = 1f;
            }
            this.minValue = minValue;
            this.maxValue = maxValue;
            double delta = maxValue - minValue;
            this.factor = delta / (config.getChartHeight() - 2);       // 1 px margin at top and bottom
            final double zeroY = 1d + maxValue / this.factor; // 1 px margin at top and bottom
            this.zeroBottom = -0.5d + config.getChartHeight() - zeroY;
            this.zeroTop = -0.5d + zeroY;
            final double maxSectionCount = config.getChartHeight() / config.getTextHeight();
            final double minSectionHeight = delta / maxSectionCount;
            this.sectionHeight = computeBestSectionHeight(minSectionHeight);

            final int valuesPerEntry = listEntries.get(0).values.length;
            final int valueGapWidthPerEntry = config.getValueGap() * (valuesPerEntry - 1);
            final int minEntryWidth = config.getMinValueWidth() * valuesPerEntry + valueGapWidthPerEntry + config.getEntryGap();
            final int minChartWidth = minEntryWidth * listEntries.size();
            if (minChartWidth > config.getMinChartWidth()) {
                this.valueWidth = config.getMinValueWidth();
                this.entryWidth = minEntryWidth;
                this.chartWidth = minChartWidth;
            }
            else {
                final double entryWidth = config.getMinChartWidth() / listEntries.size();
                this.valueWidth = (int) Math.ceil((entryWidth - config.getEntryGap() - valueGapWidthPerEntry) / valuesPerEntry);
                this.entryWidth = this.valueWidth * valuesPerEntry + valueGapWidthPerEntry + config.getEntryGap();
                this.chartWidth = this.entryWidth * listEntries.size();
            }
        }

        public void checkLabelWidth(int width) {
            if (width > this.labelWidth) {
                this.labelWidth = width;
            }
        }

        public int getLabelWidth() {
            return labelWidth;
        }

        public double getMinValue() {
            return minValue;
        }

        public double getMaxValue() {
            return maxValue;
        }

        public double getZeroBottom() {
            return zeroBottom;
        }

        public double getZeroTop() {
            return zeroTop;
        }

        public double getSectionHeight() {
            return sectionHeight;
        }

        public double getHeight(double value) {
            return value / this.factor + 1;
        }

        public double getLinePos(double value) {
            return this.zeroBottom + value / this.factor;
        }

        public int getChartWidth() {
            return chartWidth;
        }

        public int getEntryPos(int entryId) {
            return entryId * this.entryWidth;
        }

        public int getValuePos(int valueId) {
            return config.getEntryGap() / 2 + valueId * (this.valueWidth + config.getValueGap());
        }

        public int getEntryWidth() {
            return this.entryWidth;
        }

        public int getValueWidth() {
            return valueWidth;
        }
    }

    interface AlignmentHandler {
        String getStyle();
        void setLinePosition(Label line, Label label, double pos, boolean asTopLabel);
        void setTop(Style style, double top);
        void setBottom(Style style, double bottom);
        void setLeft(Style style, double left);
        void setWidth(Style style, double width);
        void setHeight(Style style, double height);
        int getOffsetWidth(Widget widget);
        void setPanelLines(FlowPanel panel);
        void setPanelBars(FlowPanel panel);
        void setEntryLabels(Widget widget);
        void setPixelSize(Widget widget, int labelWidth, int chartWidth, int chartHeight);
    }

    class VerticalHandler implements AlignmentHandler {
        private Label topLabel;
        private FlowPanel panelBars;
        private FlowPanel panelLines;
        private Widget widgetEntryLabels = null;

        @Override
        public String getStyle() {
            return "mm-vertical";
        }

        @Override
        public void setLinePosition(Label line, Label label, double pos, boolean asTopLabel) {
            final Style styleLine = line.getElement().getStyle();
            styleLine.setBottom(pos, PX);

            final Style styleLabel = label.getElement().getStyle();
            styleLabel.setLeft(0, PX);
            styleLabel.setBottom(pos, PX);
            if (asTopLabel) {
                this.topLabel = label;
            }
        }

        @Override
        public void setTop(Style style, double top) {
            style.setTop(top, PX);
        }

        @Override
        public void setBottom(Style style, double bottom) {
            style.setBottom(bottom, PX);
        }

        @Override
        public void setLeft(Style style, double left) {
            style.setLeft(left, PX);
        }

        @Override
        public void setWidth(Style style, double width) {
            style.setWidth(width, PX);
        }

        @Override
        public void setHeight(Style style, double height) {
            style.setHeight(height, PX);
        }

        @Override
        public int getOffsetWidth(Widget widget) {
            return widget.getOffsetWidth();
        }

        @Override
        public void setPanelLines(FlowPanel panelLines) {
            this.panelLines = panelLines;
        }

        @Override
        public void setPanelBars(FlowPanel panelBars) {
            this.panelBars = panelBars;
        }

        public void setEntryLabels(Widget widget) {
            this.widgetEntryLabels = widget;
        }

        @Override
        public void setPixelSize(Widget widget, int labelWidth, int chartWidth, int chartHeight) {
            final int width = labelWidth + chartWidth;
            widget.setPixelSize(width, chartHeight);
            int offset = -this.topLabel.getElement().getOffsetTop();
            if (offset < 0) {
                offset = 0;
            }

            final Style styleLines = this.panelLines.getElement().getStyle();
            final Style styleBars = this.panelBars.getElement().getStyle();

            final int entryLabelHeight;
            if (this.widgetEntryLabels == null) {
                entryLabelHeight = 0;
            }
            else {
                entryLabelHeight = this.widgetEntryLabels.getOffsetWidth() + 3;
                final Style styleEntryLabels = this.widgetEntryLabels.getElement().getStyle();
                styleEntryLabels.setTop(chartHeight + offset + entryLabelHeight, PX);
                styleEntryLabels.setLeft(labelWidth, PX);
            }

            widget.setPixelSize(width, chartHeight + offset + entryLabelHeight);
            styleLines.setTop(offset, PX);
            styleLines.setHeight(chartHeight, PX);
            styleBars.setTop(offset, PX);
            styleBars.setWidth(width, PX);
            styleBars.setHeight(chartHeight, PX);
        }
    };

    class HorizontalHandler implements AlignmentHandler {
        private Label topLabel;
        private FlowPanel panelBars;
        private FlowPanel panelLines;
        private Widget widgetEntryLabels = null;

        @Override
        public String getStyle() {
            return "mm-horizontal";
        }

        @Override
        public void setLinePosition(Label line, Label label, double pos, boolean asTopLabel) {
            final Style styleLine = line.getElement().getStyle();
            styleLine.setLeft(pos, PX);

            final Style styleLabel = label.getElement().getStyle();
            styleLabel.setTop(0, PX);
            styleLabel.setLeft(pos + 2, PX);
            if (asTopLabel) {
                this.topLabel = label;
            }
        }

        @Override
        public void setTop(Style style, double top) {
            style.setRight(top, PX);
        }

        @Override
        public void setBottom(Style style, double bottom) {
            style.setLeft(bottom, PX);
        }

        @Override
        public void setLeft(Style style, double left) {
            style.setTop(left, PX);
        }

        @Override
        public void setWidth(Style style, double width) {
            style.setHeight(width, PX);
        }

        @Override
        public void setHeight(Style style, double height) {
            style.setWidth(height, PX);
        }

        @Override
        public int getOffsetWidth(Widget widget) {
            return widget.getOffsetHeight();
        }

        @Override
        public void setPanelLines(FlowPanel panelLines) {
            this.panelLines = panelLines;
        }

        @Override
        public void setPanelBars(FlowPanel panelBars) {
            this.panelBars = panelBars;
        }

        public void setEntryLabels(Widget widget) {
            this.widgetEntryLabels = widget;
        }

        @SuppressWarnings("SuspiciousNameCombination")
        @Override
        public void setPixelSize(Widget widget, int labelWidth, int chartWidth, int chartHeight) {
            final int width = chartWidth + labelWidth;
            widget.setPixelSize(chartHeight, width);
            int offset = this.topLabel.getElement().getOffsetLeft() + this.topLabel.getElement().getOffsetWidth() + 1 - chartHeight;
            if (offset < 0) {
                offset = 0;
            }

            final Style styleLines = this.panelLines.getElement().getStyle();
            final Style styleBars = this.panelBars.getElement().getStyle();

            final int entryLabelHeight;
            if (this.widgetEntryLabels == null) {
                entryLabelHeight = 0;
            }
            else {
                entryLabelHeight = this.widgetEntryLabels.getOffsetWidth() + 3;
                final Style styleEntryLabels = this.widgetEntryLabels.getElement().getStyle();
                styleEntryLabels.setTop(labelWidth, PX);
                styleEntryLabels.setLeft(1, PX);
                styleLines.setLeft(entryLabelHeight, PX);
                styleBars.setLeft(entryLabelHeight, PX);
            }

            widget.setPixelSize(chartHeight + offset + entryLabelHeight, width);
            styleLines.setWidth(chartHeight + offset, PX);
            styleBars.setWidth(chartHeight, PX);
            styleBars.setHeight(width, PX);
        }
    };

    private void redraw() {
        if (!this.panel.isAttached()) {
            return;
        }
        else if (!isVisible()) {
            return;
        }

        clear();

        if (this.listEntries.isEmpty()) {
            // TODO: handle 0 entries
            return;
        }
        if (this.listEntries.get(0).values.length == 0) {
            // TODO: handle 0 values
            return;
        }

        final AlignmentHandler ah = config.isHorizontal() ? new HorizontalHandler() : new VerticalHandler();
        final CoordinateSystem cs = new CoordinateSystem(this.listEntries);
        addLines(ah, cs, this.panel);
        addBars(ah, cs, this.panel);
        if (config.isEntryLabels()) {
            addEntryLabels(ah, cs, this.panel);
        }
        ah.setPixelSize(this.panel, cs.getLabelWidth(), cs.getChartWidth(), config.getChartHeight());
    }

    private void clear() {
        this.panel.clear();
        // TODO
    }

    private void addLines(AlignmentHandler ah, CoordinateSystem cs, FlowPanel panel) {
        final FlowPanel panelLines = new FlowPanel();
        panelLines.setStyleName("mm-lines");
        panelLines.addStyleName(config.isHorizontal() ? "mm-horizontal" : "mm-vertical");
        panel.add(panelLines);
        ah.setPanelLines(panelLines);

        final double sectionHeight = cs.getSectionHeight();
        double value = 0;
        while (value <= cs.getMaxValue()) {
            addLine(ah, cs, panelLines, value, true);
            value += sectionHeight;
        }
        value = -sectionHeight;
        while (value > cs.getMinValue()) {
            addLine(ah, cs, panelLines, value, false);
            value -= sectionHeight;
        }
    }

    private void addLine(AlignmentHandler ah, CoordinateSystem cs, FlowPanel panel, double value, boolean asTopLabel) {
        final double linePos = cs.getLinePos(value);
        final Label line = new Label();
        line.setStyleName("mm-line");

        final String labelText = config.getLineTextFormatter().format(value);
        final Label label = new Label(labelText);
        label.setStyleName("mm-label");

        panel.add(line);
        panel.add(label);

        ah.setLinePosition(line, label, linePos, asTopLabel);
        cs.checkLabelWidth(ah.getOffsetWidth(label));
    }

    private static final double raster[] = {1d, 2.5d, 5d, 10d};

    public static double computeBestSectionHeight(double minSectionHeight) {
        double zeros = (int) (Math.log10(minSectionHeight));
        double pow = Math.pow(10d, zeros);
        double normalizedRange = minSectionHeight / pow;  // must be in [1..10]
        for (double f : raster) {
            if (normalizedRange < f) {
                return f * pow;
            }
        }
        return pow; // famous last words: this should never happen
    }

    private void addBars(AlignmentHandler ah, CoordinateSystem cs, FlowPanel panel) {
        final FlowPanel panelBars = new FlowPanel();
        panelBars.setStyleName("mm-bars");
        panelBars.addStyleName(config.isHorizontal() ? "mm-horizontal" : "mm-vertical");
        final int valuesPerEntry = this.listEntries.get(0).values.length;
        if (valuesPerEntry == 1) {
            panelBars.addStyleName("singleValue");
        }
        ah.setLeft(panelBars.getElement().getStyle(), cs.getLabelWidth());
        panel.add(panelBars);
        ah.setPanelBars(panelBars);

        final int entryCount = this.listEntries.size();
        this.bars = new Label[entryCount][];
        for (int entryId = 0; entryId < entryCount; entryId++) {
            final Entry entry = this.listEntries.get(entryId);
            this.bars[entryId] = new Label[valuesPerEntry];
            panelBars.add(createEntry(ah, cs, entry, entryId));
        }
    }

    private FlowPanel createEntry(AlignmentHandler ah, CoordinateSystem cs, Entry entry, int entryId) {
        final FlowPanel panelEntry = new FlowPanel();
        panelEntry.setStyleName("mm-entry");
        if (entry.style != null) {
            panelEntry.addStyleName(entry.style);
        }
        final Style styleEntry = panelEntry.getElement().getStyle();
        ah.setLeft(styleEntry, cs.getEntryPos(entryId));
        ah.setWidth(styleEntry, cs.getEntryWidth());

        for (int valueId = 0; valueId < entry.values.length; valueId++) {
            panelEntry.add(createBar(ah, cs, entry.values[valueId], entryId, valueId));
        }
        return panelEntry;
    }

    private Label createBar(AlignmentHandler ah, CoordinateSystem cs, Value value, int entryId, int valueId) {
        final Label bar = new Label();
        bar.setStyleName("mm-bar");
        if (value.style != null) {
            bar.addStyleName(value.style);
        }
        final Style style = bar.getElement().getStyle();
        ah.setLeft(style, cs.getValuePos(valueId));
        ah.setWidth(style, cs.getValueWidth());

        if (value.value == 0f) {
            ah.setBottom(style, cs.getLinePos(0));
            bar.addStyleName("mm-bar-0");
        }
        else if (value.value > 0f) {
            ah.setBottom(style, cs.getZeroBottom());
            ah.setHeight(style, cs.getHeight(value.value));
        }
        else {
            ah.setTop(style, cs.getZeroTop());
            ah.setHeight(style, cs.getHeight(-value.value));
        }

        if (value.getTooltip() != null) {
            Tooltip.addQtip(bar, value.getTooltip());
        }

        final Index indexOver = new Index(entryId, valueId, true);
        final Index indexOut = new Index(entryId, valueId, false);
        bar.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                setSelectedValue(indexOver);
                fireSelectionEvent(indexOver);
            }
        });
        bar.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                setSelectedValue(indexOut);
                fireSelectionEvent(indexOut);
            }
        });
        this.bars[entryId][valueId] = bar;
        return bar;
    }

    private void addEntryLabels(AlignmentHandler ah, CoordinateSystem cs, FlowPanel panel) {
        final int entryCount = this.listEntries.size();
        final Grid table = new Grid(entryCount, 1);
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("mm-entryLabels");
        table.addStyleName(ah.getStyle());
        final HTMLTable.CellFormatter formatter = table.getCellFormatter();
        for (int entryId = 0; entryId < entryCount; entryId++) {
            final Entry entry = listEntries.get(entryId);
            table.setHTML(entryId, 0, StringUtility.toHtmlLines(entry.name));
            final Style style = formatter.getElement(entryId, 0).getStyle();
            style.setHeight(cs.getEntryWidth(), PX);
        }
        panel.add(table);
        ah.setEntryLabels(table);
        this.entryLabelFormatter = formatter;
    }

    @Override
    public void setSelectedValue(Index index) {
        if (this.bars == null) {
            return;
        }

        if (this.selectedChild != null) {
            if (entryLabelFormatter != null) {
                this.entryLabelFormatter.getElement(this.selectedChild.getEntryIndex(), 0).removeClassName("entryHover");
            }
            if (this.selectedChild.getValueIndex() == -1) {
                removeClassName("barHover", this.bars[this.selectedChild.getEntryIndex()]);
            }
            else {
                removeClassName("barHover", this.bars[this.selectedChild.getEntryIndex()][this.selectedChild.getValueIndex()]);
            }
        }
        this.selectedChild = index.isSelected() ? index : null;
        if (this.selectedChild != null) {
            if (entryLabelFormatter != null) {
                this.entryLabelFormatter.getElement(this.selectedChild.getEntryIndex(), 0).addClassName("entryHover");
            }
            if (this.selectedChild.getValueIndex() == -1) {
                addClassName("barHover", this.bars[this.selectedChild.getEntryIndex()]);
            }
            else {
                addClassName("barHover", this.bars[this.selectedChild.getEntryIndex()][this.selectedChild.getValueIndex()]);
            }
        }
    }

    private void addClassName(String style, Widget... labels) {
        for (Widget label : labels) {
            label.addStyleName(style);
        }
    }

    private void removeClassName(String style, Widget... labels) {
        for (Widget label : labels) {
            label.removeStyleName(style);
        }
    }
}
