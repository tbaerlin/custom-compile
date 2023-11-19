package de.marketmaker.itools.gwtutil.client.util;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Author: umaurer
 * Created: 31.10.14
 */
public class MultiWidgetFocusSupport implements HasFocusHandlers, HasBlurHandlers {
    private final Label dummyWidget = new Label();

    private FocusHandler focusHandler = new FocusHandler() {
        @Override
        public void onFocus(FocusEvent event) {
            dummyWidget.fireEvent(event);
        }
    };
    private BlurHandler blurHandler = new BlurHandler() {
        @Override
        public void onBlur(BlurEvent event) {
            dummyWidget.fireEvent(event);
        }
    };

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return this.dummyWidget.addDomHandler(handler, BlurEvent.getType());
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return this.dummyWidget.addDomHandler(handler, FocusEvent.getType());
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.dummyWidget.fireEvent(event);
    }

    public void add(Widget... widgets) {
        for (Widget widget : widgets) {
            if (widget instanceof HasFocusHandlers) {
                ((HasFocusHandlers) widget).addFocusHandler(this.focusHandler);
            }
            else {
                widget.addDomHandler(this.focusHandler, FocusEvent.getType());
            }
            if (widget instanceof HasBlurHandlers) {
                ((HasBlurHandlers) widget).addBlurHandler(this.blurHandler);
            }
            else {
                widget.addDomHandler(this.blurHandler, BlurEvent.getType());
            }
        }
    }


    public static HandlerRegistration addFocusHandler(FocusHandler handler, HasFocusHandlers... widgets) {
        final HandlerRegistration[] hrs = new HandlerRegistration[widgets.length];
        for (int i = 0; i < widgets.length; i++) {
            hrs[i] = widgets[i] == null ? null : widgets[i].addFocusHandler(handler);
        }
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                for (HandlerRegistration hr : hrs) {
                    if (hr != null) {
                        hr.removeHandler();
                    }
                }
            }
        };
    }

    public static HandlerRegistration addBlurHandler(BlurHandler handler, HasBlurHandlers... widgets) {
        final HandlerRegistration[] hrs = new HandlerRegistration[widgets.length];
        for (int i = 0; i < widgets.length; i++) {
            hrs[i] = widgets[i] == null ? null : widgets[i].addBlurHandler(handler);
        }
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                for (HandlerRegistration hr : hrs) {
                    if (hr != null) {
                        hr.removeHandler();
                    }
                }
            }
        };
    }
}
