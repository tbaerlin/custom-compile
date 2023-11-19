package de.marketmaker.itools.gwtutil.client.widgets.chart;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.itools.gwtutil.client.vml.Point;
import de.marketmaker.itools.gwtutil.client.vml.VmlCanvas;
import de.marketmaker.itools.gwtutil.client.vml.shape.Path;
import de.marketmaker.itools.gwtutil.client.vml.shape.Shape;
import de.marketmaker.itools.gwtutil.client.vml.shape.Slice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ulrich Maurer
 *         Date: 12.11.13
 */
public class VmlPieChart extends Composite implements RequiresResize, HasEntryClickHandlers<VmlPieChart.Entry, SelectableChart.Index>, HasSelectionHandlers<SelectableChart.Index>, SelectableChart {
    private final SimplePanel chartPanel = new SimplePanel();
    private List<Entry> listEntries;
    private float radius;
    private HandlerManager handlerManager = new HandlerManager(this);
    private Index selectedIndex = null;
    private List<Shape> listPathes = null;
    private int width = -1;
    private int height = -1;

    public static class Entry {
        String name;
        float value;
        Style styleNormal;
        Style styleHover;
        float angle;
        float startAngle;
        float endAngle;

        public Entry(String name, Style styleNormal, Style styleHover, float value) {
            this.name = name;
            this.value = Math.abs(value);
            this.styleNormal = styleNormal;
            this.styleHover = styleHover;
        }

        public Style getStyleNormal() {
            return styleNormal;
        }

        public Style getStyleHover() {
            return styleHover;
        }

        public String getName() {
            return name;
        }

        public float getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "name='" + name + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

    public static class Style {
        final String strokeColor;
        final double strokeWidth;
        final double strokeOpacity;
        final String fillColor;
        final double fillOpacity;

        public Style(String strokeColor, double strokeWidth, double strokeOpacity, String fillColor, double fillOpacity) {
            this.fillOpacity = fillOpacity;
            this.fillColor = fillColor;
            this.strokeOpacity = strokeOpacity;
            this.strokeWidth = strokeWidth;
            this.strokeColor = strokeColor;
        }

        public void applyTo(Shape shape) {
            shape.setStrokeColor(this.strokeColor);
            shape.setStrokeWidth(this.strokeWidth);
            shape.setStrokeOpacity(this.strokeOpacity);
            shape.setFillColor(this.fillColor);
            shape.setFillOpacity(this.fillOpacity);
        }
    }

    public VmlPieChart() {
        this.chartPanel.setStyleName("mm-pieChart");
        this.chartPanel.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                redraw();
            }
        });
        initWidget(this.chartPanel);
    }

    public void setSize(int width, int height) {
        if (width == this.chartPanel.getOffsetWidth() && height == this.chartPanel.getOffsetHeight()) {
            return;
        }
        this.width = width;
        this.height = height;
        this.chartPanel.setSize(width + "px", height + "px");
        redraw();
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<Index> handler) {
        return this.handlerManager.addHandler(SelectionEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    private void fireSelectionEvent(Index index) {
        SelectionEvent.fire(this, index);
    }

    @Override
    public HandlerRegistration addEntryClickHandler(EntryClickHandler<Entry, Index> handler) {
        return this.handlerManager.addHandler(EntryClickEvent.getType(), handler);
    }

    @Override
    public void onResize() {
        redraw();
    }

    public void drawChart(Entry... entries) {
        drawChart(Arrays.asList(entries));
    }

    public void drawChart(List<Entry> list) {
        this.listEntries = list;
        computeAngles(this.listEntries);
        redraw();
    }

    private void redraw() {
        if (!isAttached()) {
            return;
        }

        this.chartPanel.clear();

        if (this.listEntries == null) {
            return;
        }

        int width = this.width == -1 ? getOffsetWidth() : this.width;
        int height = this.height == -1 ? getOffsetHeight() : this.height;
        if (width == 0 || height == 0) {
            width = (int) CssUtil.getComputedPropertyPx(this.getElement(), "width");
            height = (int)CssUtil.getComputedPropertyPx(this.getElement(), "height");
        }
        float size = width < height ? width : height;
        this.radius = 0.45f * size;
        final VmlCanvas canvas = new VmlCanvas(width, height);
        final Point chartMid = new Point(width / 2, height / 2);

        this.listPathes = new ArrayList<>(this.listEntries.size());
        boolean singleEntry = this.listEntries.size() == 1;
        for (int i = 0; i < this.listEntries.size(); i++) {
            final Shape path = createPath(chartMid, this.listEntries.get(i), i, singleEntry);
            this.listPathes.add(path);
            canvas.add(path);
        }

        this.chartPanel.setWidget(canvas);
    }

    private void computeAngles(List<Entry> list) {
        float sum = 0f;
        for (Entry entry : list) {
            sum += entry.getValue();
        }
        float startAngle = 0;
//        float factor = (float)Math.PI * 2 / sum;
        float factor = 360f / sum;
        for (Entry entry : list) {
            entry.startAngle = startAngle;
            entry.angle = entry.getValue() * factor;
            entry.endAngle = startAngle + entry.angle;
            startAngle = entry.endAngle;
        }
    }

    private Shape createPath(Point chartMid, final Entry entry, final int entryIndex, boolean singleEntry) {
        final Index index = new Index(entryIndex, 0, true);
        final Path path;
        final Point mid;
        final Point start;
        if (singleEntry) {
            mid = chartMid;
            start = Point.onCircleDegree(mid, this.radius, entry.startAngle);
            path = new Path()
                    .moveTo(start)
                    .angleEllipseTo(mid, this.radius, entry.startAngle, entry.angle)
                    .closePath();
        }
        else {
            mid = Point.onCircleDegree(chartMid, 5, entry.startAngle + entry.angle / 2);
            path = new Slice(mid, this.radius, 90 - entry.startAngle, -entry.angle);
        }
        path.getElement().setAttribute("nr", "" + entryIndex);
        entry.getStyleNormal().applyTo(path);
        path.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                EntryClickEvent.fire(VmlPieChart.this, entry, index);
            }
        });
        path.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                setSelectedValue(index);
                fireSelectionEvent(index);
            }
        });
        path.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                final Index indexUnselect = index.withSelected(false);
                setSelectedValue(indexUnselect);
                fireSelectionEvent(indexUnselect);
            }
        });

        return path;
    }

    @Override
    public void setSelectedValue(Index index) {
        if (this.selectedIndex != null) {
            final Style styleNormal = this.listEntries.get(this.selectedIndex.getEntryIndex()).getStyleNormal();
            final Shape shape = this.listPathes.get(this.selectedIndex.getEntryIndex());
            styleNormal.applyTo(shape);
        }
        this.selectedIndex = index.isSelected() ? index : null;
        if (this.selectedIndex != null) {
            final Style styleHover = this.listEntries.get(this.selectedIndex.getEntryIndex()).getStyleHover();
            final Shape shape = this.listPathes.get(this.selectedIndex.getEntryIndex());
            styleHover.applyTo(shape);
        }
    }
}
