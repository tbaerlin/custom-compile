package de.marketmaker.itools.gwtutil.client.vml.shape;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.vml.Point;
import de.marketmaker.itools.gwtutil.client.vml.VmlUtil;
import de.marketmaker.itools.gwtutil.client.vml.shape.path.AngleEllipseTo;
import de.marketmaker.itools.gwtutil.client.vml.shape.path.ClosePath;
import de.marketmaker.itools.gwtutil.client.vml.shape.path.LineTo;
import de.marketmaker.itools.gwtutil.client.vml.shape.path.MoveTo;
import de.marketmaker.itools.gwtutil.client.vml.shape.path.PathStep;

import java.util.ArrayList;
import java.util.List;

/**
 * User: umaurer
 * Date: 15.11.13
 * Time: 11:50
 */
public class Path extends Shape {
    protected final List<PathStep> steps;
    private boolean deferredDrawPending = false;

    public Path() {
        this(10);
    }

    public Path(int capacity) {
        this.steps = new ArrayList<PathStep>(capacity);
    }

    @Override
    public Element createElement() {
        final Element element = VmlUtil.createVMLElement("shape");
        element.getStyle().setPosition(Style.Position.ABSOLUTE);
        element.getStyle().setWidth(1, Style.Unit.PX);
        element.getStyle().setHeight(1, Style.Unit.PX);
        element.setPropertyString("coordsize", "1,1");
        return element;
    }

    public void add(PathStep step) {
        if (this.steps.isEmpty() && !(step instanceof MoveTo)) {
            throw new IllegalArgumentException("The first step must be an absolute MoveTo step.");
        }
        this.steps.add(step);
        deferredDraw();
    }

    void deferredDraw() {
        if (this.deferredDrawPending) {
            return;
        }
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                drawSteps();
            }
        });
        this.deferredDrawPending = true;
    }

    void drawSteps() {
        this.deferredDrawPending = false;
        final StringBuilder sb = new StringBuilder();
        for (PathStep step : this.steps) {
            sb.append(' ').append(step.getVmlString());
        }
        Firebug.debug("steps: " + sb.toString());
        getElement().setAttribute("path", sb.toString());
    }

    public Path moveTo(double x, double y) {
        return moveTo(new Point(x, y));
    }

    public Path moveTo(Point p) {
        add(new MoveTo(p));
        return this;
    }

    public Path lineTo(double x, double y) {
        return lineTo(new Point(x, y));
    }

    public Path lineTo(Point p) {
        add(new LineTo(p));
        return this;
    }

    public Path angleEllipseTo(double cx, double cy, double radius, double startAngle, double angle) {
        return angleEllipseTo(new Point(cx, cy), radius, startAngle, angle);
    }

    public Path angleEllipseTo(Point center, double radius, double startAngle, double angle) {
        add(new AngleEllipseTo(center, radius, startAngle, angle));
        return this;
    }

    public Path closePath() {
        add(new ClosePath());
        return this;
    }
}
