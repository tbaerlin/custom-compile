package de.marketmaker.itools.gwtutil.client.widgets.chart;

import com.google.gwt.dom.client.Style;
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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RequiresResize;
import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGPathElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.OMSVGTransform;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ulrich Maurer
 *         Date: 06.03.13
 */
public class PieChart extends Composite implements RequiresResize, HasEntryClickHandlers<PieChart.Entry, SelectableChart.Index>, HasSelectionHandlers<SelectableChart.Index>, SelectableChart {
    private final OMSVGDocument doc;
    private final OMSVGSVGElement svg;
    private OMSVGGElement chartGroup;
    private List<Entry> listEntries;
    private float radius;
    private HandlerManager handlerManager = new HandlerManager(this);
    private Index selectedIndex = null;
    private List<OMSVGPathElement> listPathes = null;
    private int width = -1;
    private int height = -1;

    public static class Entry {
        String name;
        float value;
        String style;
        float angle;
        float startAngle;
        float endAngle;

        public Entry(String name, float value) {
            this(name, null, value);
        }

        public Entry(String name, String style, float value) {
            this.name = name;
            this.value = Math.abs(value);
            this.style = style;
        }

        public String getStyle() {
            return style;
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
                    ", style='" + style + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

    public PieChart() {
        this.doc = OMSVGParser.currentDocument();
        this.svg = this.doc.createSVGSVGElement();

        final HTML html = new HTML();
        html.setStyleName("mm-pieChart");
        html.getElement().appendChild(this.svg.getElement());
        initWidget(html);
        html.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                redraw();
            }
        });
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        setSize(width + "px", height + "px");
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
        if (this.chartGroup != null) {
            this.svg.removeChild(this.chartGroup);
            this.chartGroup = null;
        }

        if (this.listEntries == null) {
            return;
        }

        int width = this.width == -1 ? getOffsetWidth() : this.width;
        int height = this.height == -1 ? getOffsetHeight() : this.height;
        if (width == 0 || height == 0) {
            width = (int)CssUtil.getComputedPropertyPx(this.getElement(), "width");
            height = (int)CssUtil.getComputedPropertyPx(this.getElement(), "height");
        }
        float size = width < height ? width : height;
        this.radius = 0.45f * size;
        this.svg.setWidth(Style.Unit.PX, width);
        this.svg.setHeight(Style.Unit.PX,  height);
        this.svg.setAttribute("viewBox", "" + (-width / 2) + " " + (-height / 2) + " " + width + " " + height);

        this.listPathes = new ArrayList<>(this.listEntries.size());
        this.chartGroup = this.doc.createSVGGElement();
        final OMSVGTransform transform = this.svg.createSVGTransform();
        transform.setScale(1, -1);
        this.chartGroup.getTransform().getBaseVal().appendItem(transform);
        boolean singleEntry = this.listEntries.size() == 1;
        for (int i = 0; i < this.listEntries.size(); i++) {
            final OMSVGPathElement path = createPath(this.listEntries.get(i), i, singleEntry);
            this.listPathes.add(path);
            this.chartGroup.appendChild(path);
        }
        this.svg.appendChild(this.chartGroup);
    }

    private void computeAngles(List<Entry> list) {
        float sum = 0f;
        for (Entry entry : list) {
            sum += entry.getValue();
        }
        float startAngle = 0;
        float factor = (float)Math.PI * 2 / (sum == 0f ? 1f : sum);
        for (Entry entry : list) {
            entry.startAngle = startAngle;
            entry.angle = entry.getValue() * factor;
            entry.endAngle = startAngle + entry.angle;
            startAngle = entry.endAngle;
        }
    }

    private OMSVGPathElement createPath(final Entry entry, final int entryIndex, boolean singleEntry) {
        final Index index = new Index(entryIndex, 0, true);
        final OMSVGPathElement path = this.doc.createSVGPathElement();
        path.setAttribute("nr", "" + entryIndex);
        if (entry.getStyle() != null) {
            setStyleName(path, entry.getStyle());
        }
        path.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                EntryClickEvent.fire(PieChart.this, entry, index);
            }
        });
        final SegPath segPath = new SegPath();
        final Point start = getPointByAngle(entry.startAngle, this.radius);
        if (singleEntry) {
            segPath.moveToAbs(start);
        }
        else {
            segPath.moveToAbs(0f, 0f);
            segPath.lineToAbs(start);
        }
        final Point end = getPointByAngle(entry.endAngle, this.radius);
        segPath.arcAbs(end, this.radius, this.radius, entry.angle, entry.angle >= Math.PI, entry.angle >= 2 * Math.PI);
        segPath.closePath();
        path.setAttribute("d", segPath.toString());
        final Point mid = getPointByAngle((entry.endAngle + entry.startAngle) / 2, 5);
        final OMSVGTransform transform = this.svg.createSVGTransform();
        transform.setTranslate(mid.x, mid.y);
        path.getTransform().getBaseVal().appendItem(transform);

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
            int entryIndex = this.selectedIndex.getEntryIndex();
            setStyleName(
                    this.listPathes.get(entryIndex),
                    this.listEntries.get(entryIndex).getStyle()
            );
        }
        this.selectedIndex = index.isSelected() ? index : null;
        if (this.selectedIndex != null) {
            int entryIndex = this.selectedIndex.getEntryIndex();
            setStyleName(
                    this.listPathes.get(entryIndex),
                    this.listEntries.get(entryIndex).getStyle(),
                    this.selectedIndex.getValueIndex() == -1 ? "hover" : "hover hoverValue"
            );
        }
    }

    private void setStyleName(OMSVGPathElement path, String className) {
        if (className == null) {
            path.removeAttribute("class");
        }
        else {
            path.setAttribute("class", className);
        }
    }

    private void setStyleName(OMSVGPathElement path, String className, String additionalClassName) {
        if (className == null) {
            path.setAttribute("class", additionalClassName);
        }
        else {
            path.setAttribute("class", className + " " + additionalClassName);
        }
    }

    private Point getPointByAngle(float angle, float radius) {
        return new Point(
                (float)Math.sin(angle) * radius,
                (float)Math.cos(angle) * radius
        );
    }
}