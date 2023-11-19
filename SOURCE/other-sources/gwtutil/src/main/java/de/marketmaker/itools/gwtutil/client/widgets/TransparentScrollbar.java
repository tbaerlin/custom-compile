package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.util.Transitions;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingWidget;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 07.07.14
 */
public class TransparentScrollbar extends Composite {
    private final Label scrollbar = new Label();
    private final SimplePanel panel = new SimplePanel(this.scrollbar);
    private final Transitions.FadingTransition fadingTransition;
    private double sizeFactor = -1d;
    private double currentPosition;
    private boolean firstShow = true;

    private final Timer scrollbarTimer = new Timer() {
        @Override
        public void run() {
            fadingTransition.fadeOut();
        }
    };
    private boolean enabled = true;
    private final ScrollbarDndHandler scrollbarDndHandler;

    public TransparentScrollbar(FloatingWidget floatingWidget) {
        this.panel.setStyleName("mm-scrollPanel");
        this.scrollbar.setStyleName("mm-scrollbar");
        this.fadingTransition = Transitions.createFadingTransition(this.scrollbar, 300);
        this.panel.addDomHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                show();
            }
        }, MouseMoveEvent.getType());
        this.scrollbarDndHandler = new ScrollbarDndHandler(floatingWidget);
        this.scrollbar.addMouseDownHandler(this.scrollbarDndHandler);
        this.scrollbar.addMouseMoveHandler(this.scrollbarDndHandler);
        this.scrollbar.addMouseUpHandler(this.scrollbarDndHandler);
        initWidget(this.panel);
    }

    class ScrollbarDndHandler implements MouseDownHandler, MouseMoveHandler, MouseUpHandler {
        private final FloatingWidget floatingWidget;
        private boolean dragging = false;
        private int startY;
        private double startPosition;
        private double scrollFactor = 1d;

        ScrollbarDndHandler(FloatingWidget floatingWidget) {
            this.floatingWidget = floatingWidget;
        }

        public void setScrollFactor(double scrollFactor) {
            this.scrollFactor = scrollFactor;
        }

        @Override
        public void onMouseDown(MouseDownEvent event) {
            Event.setCapture(scrollbar.getElement());
            this.startY = event.getScreenY();
            this.startPosition = currentPosition;
            this.dragging = true;
            event.preventDefault();
            show();
        }

        @Override
        public void onMouseMove(MouseMoveEvent event) {
            if (!this.dragging) {
                return;
            }
            final double d = event.getScreenY() - this.startY;
            final double p = (this.startPosition + d) / this.scrollFactor;
            this.floatingWidget.setPosition((int) -p, true);
            this.floatingWidget.updateButtonVisibility();
            event.stopPropagation();
            show();
        }

        @Override
        public void onMouseUp(MouseUpEvent event) {
            this.dragging = false;
            Event.releaseCapture(scrollbar.getElement());
        }
    }

    private void show() {
        if (!enabled) {
            return;
        }
        if (this.firstShow) {
            this.firstShow = false;
            return;
        }
        this.fadingTransition.fadeIn();
        this.scrollbarTimer.schedule(1000);
    }

    private void hide(int millis) {
        if (millis == 0) {
            this.scrollbarTimer.cancel();
            this.fadingTransition.fadeOut();
        }
        else {
            this.scrollbarTimer.schedule(millis);
        }
    }

    public void setDimensions(double maxSize, double currentSize) {
        if (maxSize <= 0 || maxSize == currentSize) {
            setEnabled(false);
            return;
        }
        setEnabled(true);
        this.sizeFactor = currentSize / maxSize;
        this.scrollbarDndHandler.setScrollFactor(this.sizeFactor);
        this.scrollbar.getElement().getStyle().setHeight(currentSize * this.sizeFactor, PX);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.panel.setVisible(enabled);
    }

    public void setPosition(int position) {
        if (this.sizeFactor == -1d) {
            return;
        }
        this.currentPosition = this.sizeFactor * position;
        this.scrollbar.getElement().getStyle().setTop(this.currentPosition, PX);
        show();
    }
}
