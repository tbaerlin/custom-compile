package de.marketmaker.itools.gwtutil.client.widgets.chart;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RequiresResize;
import de.marketmaker.itools.gwtutil.client.i18n.NumberFormatterIfc;
import de.marketmaker.itools.gwtutil.client.svg.HtmlTextElement;
import de.marketmaker.itools.gwtutil.client.svg.SvgUtil;
import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGPathElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.gwt.dom.client.Style.Position.ABSOLUTE;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 12.08.15
 */
public class Gauge extends Composite implements RequiresResize {
    private static final double MIN_ANGLE = -110f;
    private static final double MAX_ANGLE = 110f;
    private static final double MIN_ANGLE_RAD = Math.toRadians(MIN_ANGLE);
    private static final double MAX_ANGLE_RAD = Math.toRadians(MAX_ANGLE);
    private static final double MIN_ANGLE_OVERFLOW = MIN_ANGLE - 10;
    private static final double MAX_ANGLE_OVERFLOW = MAX_ANGLE + 10;
    private static final double ONE_DEGREE_RAD = Math.toRadians(1);

    private final OMSVGDocument doc;
    private final OMSVGSVGElement svg;

    private int width = 250;
    private int height = 190;
    private float dialRadiusOuter = 0.4f * this.width;
    private float dialRadiusInner = 0.24f * this.width;
    private float tickTextRadius = 0.47f * this.width;

    private float chartCenterX = 0.5f * this.width;
    private float chartCenterY = 0.65f * this.height;

    private SafeHtml caption = null;

    private final double minValue;
    private String minValueString;
    private String maxValueString;
    private final double angleGradFactor;
    private final double angleRadFactor;
    private Number value = null;
    private final NumberFormatterIfc numberFormatter;

    private FlowPanel textPanel;
    private OMSVGGElement chartGroup;
    private OMSVGGElement handGroup;
    private String minOverflowStyle;
    private OMSVGElement minOverflowElement;
    private String maxOverflowStyle;
    private OMSVGElement maxOverflowElement;
    private HandAnimation handAnimation = new HandAnimation();
    private HTML valueHtml;
    private List<Tick> ticks;
    private List<Range> ranges;
    private final FlowPanel gaugePanel;

    public static double toX(double radius, double angle) {
        return Math.sin(angle) * radius;
    }

    public static double toY(double radius, double angle) {
        return Math.cos(angle) * radius;
    }

    private static double toRadius(double x, double y) {
        return Math.sqrt(x * x + y * y);
    }

    private static double toAngle(double x, double radius) {
        return Math.asin(x / radius);
    }

    public static PointRA toRA(double x, double y) {
        final double radius = toRadius(x, y);
        return new PointRA(radius, toAngle(x, radius));
    }

    public static PointXY toXY(double radius, double angle) {
        return new PointXY(toX(radius, angle), toY(radius, angle));
    }

    public static class Tick {
        private final Number value;
        private final String label;
        private String style;
        private SafeHtml tooltip;

        public Tick(Number value, String label) {
            this.value = value;
            this.label = label;
        }

        public Tick withStyle(String style) {
            this.style = style;
            return this;
        }

        public Tick withTooltip(SafeHtml tooltip) {
            this.tooltip = tooltip;
            return this;
        }
    }

    public static class Range {
        private final Number from;
        private final Number to;
        private String style;
        private SafeHtml tooltip;

        public Range(Number from, Number to) {
            this.from = from;
            this.to = to;
        }

        public Range withStyle(String style) {
            this.style = style;
            return this;
        }

        public Range withTooltip(SafeHtml tooltip) {
            this.tooltip = tooltip;
            return this;
        }
    }

    static class PointXY {
        double x;
        double y;

        public PointXY(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public PointRA toRA() {
            return Gauge.toRA(this.x, this.y);
        }

        public float getX() {
            return (float) this.x;
        }

        public float getY() {
            return (float) this.y;
        }

        public void set(PointRA ra) {
            this.x = toX(ra.radius, ra.angle);
            this.y = toY(ra.radius, ra.angle);
        }
    }

    static class PointRA {
        double radius;
        double angle;

        public PointRA(double radius, double angle) {
            this.radius = radius;
            this.angle = angle;
        }

        public PointXY toXY() {
            return Gauge.toXY(this.radius, this.angle);
        }

        public PointRA withRadius(double radius) {
            this.radius = radius;
            return this;
        }

        public PointRA withAngle(double angle) {
            this.angle = angle;
            return this;
        }

        public float getRadius() {
            return (float) this.radius;
        }

        public float getAngleGrad() {
            return (float) Math.toDegrees(this.angle);
        }
    }

    public Gauge(Number minValue, Number maxValue, NumberFormatterIfc numberFormatter) {
        this.minValue = minValue.doubleValue();
        this.minValueString = numberFormatter.format(minValue);
        this.maxValueString = numberFormatter.format(maxValue);
        this.angleGradFactor = (MAX_ANGLE - MIN_ANGLE) / (maxValue.doubleValue() - this.minValue);
        this.angleRadFactor = (MAX_ANGLE_RAD - MIN_ANGLE_RAD) / (maxValue.doubleValue() - this.minValue);
        this.numberFormatter = numberFormatter;
        this.doc = OMSVGParser.currentDocument();
        this.svg = this.doc.createSVGSVGElement();

        this.gaugePanel = new FlowPanel();
        this.gaugePanel.setStyleName("mm-gaugeChart");
        this.gaugePanel.getElement().appendChild(this.svg.getElement());
        this.textPanel = new FlowPanel();
        this.textPanel.setStyleName("mm-gauge-textPanel");
        this.gaugePanel.add(this.textPanel);
        initWidget(this.gaugePanel);
        this.gaugePanel.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    redraw();
                }
            }
        });
    }

    public void setCaption(SafeHtml caption) {
        this.caption = caption;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        setSize(width + "px", height + "px");
    }

    public void clearTicks() {
        this.ticks = null;
        redraw();
    }

    public void addTicks(Tick... ticks) {
        if (this.ticks == null) {
            this.ticks = new ArrayList<>(Arrays.asList(ticks));
        }
        else {
            this.ticks.addAll(Arrays.asList(ticks));
        }
        redraw();
    }

    public void clearRanges() {
        this.ranges = null;
        redraw();
    }

    public void addRanges(Range... ranges) {
        if (this.ranges == null) {
            this.ranges = new ArrayList<>(Arrays.asList(ranges));
        }
        else {
            this.ranges.addAll(Arrays.asList(ranges));
        }
        redraw();
    }

    public void setMinOverflowStyle(String minOverflowStyle) {
        this.minOverflowStyle = minOverflowStyle;
        redraw();
    }

    public void setMaxOverflowStyle(String maxOverflowStyle) {
        this.maxOverflowStyle = maxOverflowStyle;
        redraw();
    }

    public void set(String minValueString, String maxValueString, List<Range> ranges, List<Tick> ticks, String minOverflowStyle, String maxOverflowStyle) {
        this.ranges = ranges;
        this.ticks = ticks;
        this.minOverflowStyle = minOverflowStyle;
        this.maxOverflowStyle = maxOverflowStyle;
        if (minValueString != null) {
            this.minValueString = minValueString;
        }
        if (maxValueString != null) {
            this.maxValueString = maxValueString;
        }
        redraw();
    }

    @Override
    public void onResize() {
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

        int width = this.width == -1 ? getOffsetWidth() : this.width;
        int height = this.height == -1 ? getOffsetHeight() : this.height;
        if (width == 0 || height == 0) {
            width = (int) CssUtil.getComputedPropertyPx(this.getElement(), "width");
            height = (int)CssUtil.getComputedPropertyPx(this.getElement(), "height");
        }

        this.gaugePanel.setPixelSize(width, height);

        this.svg.setWidth(PX, width);
        this.svg.setHeight(PX, height);
        this.chartCenterX = 0.5f * width;
        this.chartCenterY = 0.65f * height;

        this.dialRadiusOuter = 0.4f * width;
        this.dialRadiusInner = 0.22f * width;
        this.tickTextRadius = this.dialRadiusOuter + 20;
        final float dialRadiusMid = (this.dialRadiusOuter + this.dialRadiusInner) / 2;
        final float handRadiusTop = 0.25f * width;

        this.svg.setAttribute("viewBox", "" + (-this.chartCenterX) + " " + (-chartCenterY) + " " + width + " " + height);

        this.textPanel.clear();
        this.chartGroup = this.doc.createSVGGElement();

        if (this.caption != null) {
            this.textPanel.add(new HtmlTextElement(0, 0.4f * this.dialRadiusOuter, this.width, 40, this.chartCenterX, this.chartCenterY, centeredText(this.caption)).withStyle("mm-gauge-caption"));
        }

        this.chartGroup.appendChild(this.minOverflowElement = createMinOverflow(this.dialRadiusOuter, this.dialRadiusInner));
        this.chartGroup.appendChild(this.maxOverflowElement = createMaxOverflow(this.dialRadiusOuter, this.dialRadiusInner));

        this.chartGroup.appendChild(createArc(this.dialRadiusOuter, this.dialRadiusInner, MIN_ANGLE_RAD, MAX_ANGLE_RAD, "mm-gauge-dial-back-glow"));
        this.chartGroup.appendChild(createArc(this.dialRadiusOuter, this.dialRadiusInner, MIN_ANGLE_RAD, MAX_ANGLE_RAD, "mm-gauge-dial-back"));

        if (this.ranges != null) {
            this.chartGroup.appendChild(createRanges(this.ranges));
        }

        if (this.ticks != null) {
            this.chartGroup.appendChild(createTicks(this.ticks));
        }

        this.textPanel.add(createExtremeText(centeredText(this.minValueString), dialRadiusMid, MIN_ANGLE_RAD, 80, 40));
        this.textPanel.add(createExtremeText(centeredText(this.maxValueString), dialRadiusMid, MAX_ANGLE_RAD, 80, 40));

        this.handGroup = addHand(this.chartGroup, handRadiusTop);
        this.valueHtml = createValueHtml(SafeHtmlUtils.EMPTY_SAFE_HTML, 0, 0.2f * this.dialRadiusOuter, 140, 60);
        this.textPanel.add(this.valueHtml);
        if (this.value != null) {
            setValue(this.value);
        }

        this.svg.appendChild(this.chartGroup);
    }

    private OMSVGPathElement createArc(double rOuter, double rInner, double fromAngleRad, double toAngleRad, String styleName) {
        final OMSVGPathElement path = createArc(rOuter, rInner, fromAngleRad, toAngleRad);
        path.setClassNameBaseVal(styleName);
        return path;
    }

    private OMSVGElement createMinOverflow(double rOuter, double rInner) {
        final OMSVGGElement g = this.doc.createSVGGElement();
        g.setClassNameBaseVal("mm-gauge-overflow-group");

        final String style = this.minOverflowStyle == null ? "mm-gauge-overflow" : ("mm-gauge-overflow " + this.minOverflowStyle);

        g.appendChild(createArc(rOuter, rInner, MIN_ANGLE_RAD - ONE_DEGREE_RAD * 10, MIN_ANGLE_RAD - ONE_DEGREE_RAD * 9, style));
        g.appendChild(createArc(rOuter, rInner, MIN_ANGLE_RAD - ONE_DEGREE_RAD * 8, MIN_ANGLE_RAD - ONE_DEGREE_RAD * 6, style));
        g.appendChild(createArc(rOuter, rInner, MIN_ANGLE_RAD - ONE_DEGREE_RAD * 5, MIN_ANGLE_RAD - ONE_DEGREE_RAD * 2, style));

        return g;
    }

    private OMSVGElement createMaxOverflow(double rOuter, double rInner) {
        final OMSVGGElement g = this.doc.createSVGGElement();
        g.setClassNameBaseVal("mm-gauge-overflow-group");

        final String style = this.maxOverflowStyle == null ? "mm-gauge-overflow" : ("mm-gauge-overflow " + this.maxOverflowStyle);

        g.appendChild(createArc(rOuter, rInner, MAX_ANGLE_RAD + ONE_DEGREE_RAD * 2, MAX_ANGLE_RAD + ONE_DEGREE_RAD * 5, style));
        g.appendChild(createArc(rOuter, rInner, MAX_ANGLE_RAD + ONE_DEGREE_RAD * 6, MAX_ANGLE_RAD + ONE_DEGREE_RAD * 8, style));
        g.appendChild(createArc(rOuter, rInner, MAX_ANGLE_RAD + ONE_DEGREE_RAD * 9, MAX_ANGLE_RAD + ONE_DEGREE_RAD * 10, style));

        return g;
    }

    private OMSVGPathElement createArc(double rOuter, double rInner, double fromAngleRad, double toAngleRad) {
        final OMSVGPathElement path = this.doc.createSVGPathElement();
        final SegPath segPath = new SegPath();
        if (fromAngleRad > toAngleRad) {
            double h = fromAngleRad;
            fromAngleRad = toAngleRad;
            toAngleRad = h;
        }
        final boolean largeArgFlag = toAngleRad - fromAngleRad > Math.PI;
        final PointRA ra = new PointRA(rOuter, fromAngleRad);
        final PointXY p = ra.toXY();
        segPath.moveToAbs(p.getX(), -p.getY());
        p.set(ra.withAngle(toAngleRad));
        segPath.arcAbs(p.getX(), -p.getY(), ra.getRadius(), ra.getRadius(), 0, largeArgFlag, true);
        p.set(ra.withRadius(rInner));
        segPath.lineToAbs(p.getX(), -p.getY());
        p.set(ra.withAngle(fromAngleRad));
        segPath.arcAbs(p.getX(), -p.getY(), ra.getRadius(), ra.getRadius(), 0, largeArgFlag, false);
        segPath.closePath();
        path.setAttribute("d", segPath.toString());
        return path;
    }

    private HtmlTextElement createExtremeText(SafeHtml text, double radius, double angleRad, int width, int height) {
        final double angleDiff;
        final float textAngle;
        if (angleRad > 0) {
            angleDiff = ONE_DEGREE_RAD;
            textAngle = (float)(Math.toDegrees(angleRad) - 90);
        }
        else {
            angleDiff = -ONE_DEGREE_RAD;
            textAngle = (float)(Math.toDegrees(angleRad) + 90);
        }
        final PointRA ra = new PointRA(radius, angleRad + angleDiff);
        final PointXY p = ra.toXY();
        return new HtmlTextElement(p.getX(), -p.getY(), width, height, this.chartCenterX, this.chartCenterY, text).withRotation(textAngle);
    }

    private OMSVGElement createRanges(List<Range> ranges) {
        final OMSVGGElement g = this.doc.createSVGGElement();
        g.setClassNameBaseVal("mm-gauge-ranges");
        for (Range range : ranges) {
            final OMSVGElement rangeElt = createRange(range);
            if (rangeElt != null) {
                g.appendChild(rangeElt);
            }
        }
        return g;
    }

    private OMSVGElement createRange(Range range) {
        double fromRad = getAngleRad(range.from.doubleValue());
        double toRad = getAngleRad(range.to.doubleValue());
        if (fromRad > MAX_ANGLE_RAD) {
            return null;
        }
        if (fromRad < MIN_ANGLE_RAD) {
            fromRad = MIN_ANGLE_RAD;
        }
        if (toRad < MIN_ANGLE_RAD) {
            return null;
        }
        if (toRad > MAX_ANGLE_RAD) {
            toRad = MAX_ANGLE_RAD;
        }
        final OMSVGPathElement element = createArc(this.dialRadiusOuter, this.dialRadiusInner
                , fromRad, toRad
                , range.style == null ? "mm-gauge-range" : ("mm-gauge-range " + range.style));
        if (range.tooltip != null) {
            element.setAttribute(Tooltip.ATT_QTIP_LABEL, range.tooltip.asString());
        }
        return element;
    }

    private OMSVGElement createTicks(List<Tick> ticks) {
        final OMSVGGElement g = this.doc.createSVGGElement();
        g.setClassNameBaseVal("mm-gauge-ticks");
        for (Tick tick : ticks) {
            g.appendChild(createTick(tick));
        }
        return g;
    }

    private OMSVGElement createTick(Tick tick) {
        final OMSVGGElement g = this.doc.createSVGGElement();
        g.setClassNameBaseVal("mm-gauge-tick");
        final PointRA ra = new PointRA(this.tickTextRadius, getAngleRad(tick.value.doubleValue()));

        final OMSVGPathElement tickHand = createTickHand();
        SvgUtil.setRotation(this.svg, tickHand, ra.getAngleGrad(), 0, 0);
        g.appendChild(tickHand);
        if (tick.tooltip != null) {
            tickHand.setAttribute(Tooltip.ATT_QTIP_LABEL, tick.tooltip.asString());
        }
        if (tick.style != null) {
            g.addClassNameBaseVal(tick.style);
        }

        final PointXY p = ra.toXY();
        this.textPanel.add(new HtmlTextElement(p.getX(), -p.getY(), 160, 20, this.chartCenterX, this.chartCenterY, centeredText(tick.label)).withRotation(ra.getAngleGrad()));
        return g;
    }

    private OMSVGPathElement createTickHand() {
        final OMSVGPathElement path = this.doc.createSVGPathElement();
        final SegPath segPath = new SegPath();
        segPath.moveToRel(-8, -this.dialRadiusOuter);
        segPath.curveToCubicRel(16, 0, -1, -7, 17, -7);
        segPath.curveToCubicRel(-16, 0, -3, 21, -13, 21);
        segPath.closePath();
        path.setAttribute("d", segPath.toString());
        return path;
    }

    private OMSVGGElement addHand(OMSVGGElement parent, float r) {
        final OMSVGGElement g = this.doc.createSVGGElement();

        final OMSVGPathElement pathGlow = createHandPath(r);
        pathGlow.setClassNameBaseVal("mm-gauge-hand-glow");
        g.appendChild(pathGlow);

        final OMSVGPathElement path = createHandPath(r);
        path.setClassNameBaseVal("mm-gauge-hand");
        g.appendChild(path);
        parent.appendChild(g);

        return g;
    }

    private OMSVGPathElement createHandPath(float r) {
        final OMSVGPathElement path = this.doc.createSVGPathElement();
        final SegPath segPath = new SegPath();
        final float handCircleR = 0.08f * this.dialRadiusOuter;
        final float pointerWidth = 0.02f * this.dialRadiusOuter;
        segPath.moveToRel(-0.001f, -handCircleR);
        segPath.arcRel(0.002f, 0, handCircleR, handCircleR, 0, true, false);
        segPath.closePath();

        segPath.moveToAbs(-handCircleR * 0.6f, 0);
        segPath.lineToHorizontalRel(handCircleR * 1.2f);
        segPath.lineToRel(-handCircleR * 0.6f + pointerWidth, -r);
        segPath.arcRel(-2 * pointerWidth, 0, pointerWidth, pointerWidth, 0, false, false);
        segPath.closePath();
        path.setAttribute("d", segPath.toString());
        return path;
    }

    private HTML createValueHtml(SafeHtml safeHtml, float x, float y, float width, float height) {
        final HTML html = new HTML(safeHtml);
        html.setStyleName("mm-gauge-value");
        final Style style = html.getElement().getStyle();
        style.setPosition(ABSOLUTE);
        style.setLeft(this.chartCenterX + x - width / 2, PX);
        style.setTop(this.chartCenterY + y, PX);
        style.setWidth(width, PX);
        style.setHeight(height, PX);
        return html;
    }

    private SafeHtml centeredText(String text) {
        return new SafeHtmlBuilder().appendHtmlConstant("<div style=\"text-align: center\">").appendEscaped(text).appendHtmlConstant("</div>").toSafeHtml();
    }

    private SafeHtml centeredText(SafeHtml text) {
        return new SafeHtmlBuilder().appendHtmlConstant("<div style=\"text-align: center\">").append(text).appendHtmlConstant("</div>").toSafeHtml();
    }

    public void setValue(Number value) {
        this.value = value;
        if (!isAttached()) {
            return;
        }
        double angle = getAngleGrad(value.doubleValue());

        if (angle < MIN_ANGLE) {
            if (angle < MIN_ANGLE_OVERFLOW) {
                angle = MIN_ANGLE_OVERFLOW;
            }
            this.minOverflowElement.getStyle().setVisibility(Style.Visibility.VISIBLE);
            this.maxOverflowElement.getStyle().setVisibility(Style.Visibility.HIDDEN);
        }
        else if (angle > MAX_ANGLE) {
            if (angle > MAX_ANGLE_OVERFLOW) {
                angle = MAX_ANGLE_OVERFLOW;
            }
            this.minOverflowElement.getStyle().setVisibility(Style.Visibility.HIDDEN);
            this.maxOverflowElement.getStyle().setVisibility(Style.Visibility.VISIBLE);
        }
        else {
            this.minOverflowElement.getStyle().setVisibility(Style.Visibility.HIDDEN);
            this.maxOverflowElement.getStyle().setVisibility(Style.Visibility.HIDDEN);
        }
        this.handAnimation.setValue(angle);
        this.valueHtml.setHTML(centeredText(this.numberFormatter.format(this.value)));
    }

    private double getAngleGrad(double value) {
        return (value - this.minValue) * this.angleGradFactor + MIN_ANGLE;
    }

    private double getAngleRad(double value) {
        return (value - this.minValue) * this.angleRadFactor + MIN_ANGLE_RAD;
    }

    class HandAnimation extends Animation {
        double from = 0f;
        double to;
        double current = 0f;

        @Override
        protected void onUpdate(double progress) {
            this.current = this.from + (this.to - this.from) * progress;
            SvgUtil.setRotation(svg, handGroup, (float)this.current, 0, 0);
        }

        public void setValue(double to) {
            this.from = this.current;
            this.to = to;
            this.cancel();
            run(600);
        }
    }
}
