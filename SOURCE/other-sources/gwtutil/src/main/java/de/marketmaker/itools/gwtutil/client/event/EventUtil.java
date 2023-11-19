package de.marketmaker.itools.gwtutil.client.event;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

/**
 * Author: umaurer
 * Created: 12.09.14
 */
public class EventUtil {
    public static boolean keyCodeIn(DomEvent event, int... codes) {
        return keyCodeIn(event.getNativeEvent(), codes);
    }

    public static boolean keyCodeIn(NativeEvent event, int... codes) {
        final int keyCode = event.getKeyCode();
        for (int code : codes) {
            if (code == keyCode) {
                return true;
            }
        }
        return false;
    }

    public static boolean keyCombinationIn(DomEvent event, KeyCombination... kcs) {
        return keyCombinationIn(event.getNativeEvent(), kcs);
    }

    public static boolean keyCombinationIn(NativeEvent event, KeyCombination... kcs) {
        for (KeyCombination kc : kcs) {
            if (kc.matches(event)) {
                return true;
            }
        }
        return false;
    }

    public static boolean charCodeIn(KeyPressEvent event, char... codes) {
        final char charCode = event.getCharCode();
        for (char code : codes) {
            if (code == charCode) {
                return true;
            }
        }
        return false;
    }

    public static HandlerRegistration addClickHandler(Widget widget, ClickHandler handler) {
        return widget instanceof HasClickHandlers
                ? ((HasClickHandlers) widget).addClickHandler(handler)
                : widget.addDomHandler(handler, ClickEvent.getType());
    }
}
