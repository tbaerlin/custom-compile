package de.marketmaker.itools.gwtutil.client.widgets.chart;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import static com.google.gwt.dom.client.Style.Unit.PCT;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 09.01.15
 */
public class MiniBar extends Composite {
    private static final String STYLE_NAME = "mm-miniBar";
    private static final MiniBar INSTANCE = new MiniBar();
    private static final SimplePanel INSTANCE_PANEL = new SimplePanel(INSTANCE);

    private final Label bar = new Label();
    private final Label zeroNegative = new Label();
    private final Label zeroPositive = new Label();
    private final Style barStyle = bar.getElement().getStyle();

    public MiniBar() {
        this.zeroNegative.setStyleName("zero");
        this.zeroPositive.setStyleName("zero");
        final FlowPanel panel = new FlowPanel();
        panel.setStyleName(STYLE_NAME);
        panel.add(this.bar);
        panel.add(this.zeroNegative);
        panel.add(this.zeroPositive);
        initWidget(panel);
    }

    public MiniBar(double value, double minValue, double maxValue) {
        this();
        setValue(value, minValue, maxValue);
    }

    public void setValue(double value, double minValue, double maxValue) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("minValue(" + minValue + ") > maxValue(" + maxValue + ")");
        }
        if (minValue > 0d) {
            minValue = 0d;
        }
        if (maxValue < 0d) {
            maxValue = 0d;
        }
        if (minValue == maxValue) {
            maxValue = 1d;
        }
        final double range = maxValue - minValue;
        final double zeroNegPos = maxValue * 100d / range;
        final double zeroPosPos = -minValue * 100d / range;
        if (value < 0) {
            this.bar.setStyleName("bar negative");
            double widthPercent = -value * 100d / range;
            this.barStyle.setWidth(widthPercent, PCT);
            this.barStyle.clearLeft();
            this.barStyle.setRight(zeroNegPos, PCT);
        }
        else {
            this.bar.setStyleName("bar positive");
            double widthPercent = value * 100d / range;
            this.barStyle.setWidth(widthPercent, PCT);
            this.barStyle.setLeft(zeroPosPos, PCT);
            this.barStyle.clearRight();
        }
        this.zeroNegative.getElement().getStyle().setRight(zeroNegPos, PCT);
        this.zeroPositive.getElement().getStyle().setLeft(zeroPosPos, PCT);
    }

    public static String asHtml(double value, double minValue, double maxValue, Integer width) {
        if (width == null) {
            INSTANCE.getElement().getStyle().clearWidth();
        }
        else {
            INSTANCE.getElement().getStyle().setWidth(width, PX);
        }
        INSTANCE.setValue(value, minValue, maxValue);
        return INSTANCE_PANEL.getElement().getInnerHTML();
    }
}
