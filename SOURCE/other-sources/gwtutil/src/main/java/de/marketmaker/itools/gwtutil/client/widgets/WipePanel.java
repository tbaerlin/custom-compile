package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Transitions;

import static com.google.gwt.dom.client.Style.Position.ABSOLUTE;
import static com.google.gwt.dom.client.Style.Position.RELATIVE;
import static com.google.gwt.dom.client.Style.Unit.PCT;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 07.08.14
 */
public class WipePanel extends Composite {
    private final FlowPanel childPanel = new FlowPanel();
    private final Label wipeLabel;

    public enum FixedPosition {
        TOP("top", "height"),
        RIGHT("right", "width"),
        BOTTOM("bottom", "height"),
        LEFT("left", "width");

        final String clearProperty;
        final String transitionProperty;

        FixedPosition(String clearProperty, String transitionProperty) {
            this.clearProperty = clearProperty;
            this.transitionProperty = transitionProperty;
        }
    }

    private final FixedPosition fixedPosition;
    private final int durationMillis;

    public WipePanel(FixedPosition fixedPosition, boolean wipeInAfterAttach, int durationMillis) {
        this.fixedPosition = fixedPosition;
        this.durationMillis = durationMillis;

        if (Transitions.TRANSITION_STYLE_NAME == null) {
            this.wipeLabel = null;
            initWidget(this.childPanel);
            return;
        }

        final FlowPanel panel = new FlowPanel();
        panel.setStyleName("mm-wipePanel");

        this.wipeLabel = new Label();
        this.wipeLabel.setStyleName("mm-wipeLabel");

        final Style panelStyle = panel.getElement().getStyle();
        panelStyle.setPosition(RELATIVE);

        final Style labelStyle = this.wipeLabel.getElement().getStyle();
        labelStyle.setPosition(ABSOLUTE);
        labelStyle.setTop(0, PX);
        labelStyle.setRight(0, PX);
        labelStyle.setBottom(0, PX);
        labelStyle.setLeft(0, PX);
        labelStyle.setWidth(100, PCT);
        labelStyle.setHeight(100, PCT);

        int size = wipeInAfterAttach ? 100 : 0;

        labelStyle.clearProperty(fixedPosition.clearProperty);
        labelStyle.setProperty(fixedPosition.transitionProperty, size, PCT);
        activateTransition();

        if (wipeInAfterAttach) {
            panel.addAttachHandler(new AttachEvent.Handler() {
                @Override
                public void onAttachOrDetach(AttachEvent event) {
                    wipeIn();
                }
            });
        }
        panel.add(this.childPanel);
        panel.add(this.wipeLabel);
        initWidget(panel);
    }

    @Override
    public void setStyleName(String style) {
        super.setStyleName(style);
    }

    private void activateTransition() {
        if (this.wipeLabel == null) {
            return;
        }
        this.wipeLabel.getElement().getStyle().setProperty(Transitions.TRANSITION_STYLE_NAME, this.fixedPosition.transitionProperty + " " + this.durationMillis + "ms ease");
    }

    private void clearTransition() {
        this.wipeLabel.getElement().getStyle().clearProperty(Transitions.TRANSITION_STYLE_NAME);
    }

    public void add(Widget w) {
        this.childPanel.add(w);
    }

    public void clear() {
        this.childPanel.clear();
    }

    public void wipeIn() {
        if (this.wipeLabel == null) {
            return;
        }
        Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                final Element element = wipeLabel.getElement();
                element.getClientHeight();
                element.getStyle().setProperty(fixedPosition.transitionProperty, 0, PCT);
                return false;
            }
        }, 25);
    }

    public void wipeOut(final Transitions.TransitionEndCallback teCallback) {
        if (this.wipeLabel == null) {
            teCallback.onTransitionEnd();
            return;
        }
        this.wipeLabel.getElement().getStyle().setProperty(this.fixedPosition.transitionProperty, 100, PCT);
        Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                teCallback.onTransitionEnd();
                return false;
            }
        }, this.durationMillis);
    }
}
