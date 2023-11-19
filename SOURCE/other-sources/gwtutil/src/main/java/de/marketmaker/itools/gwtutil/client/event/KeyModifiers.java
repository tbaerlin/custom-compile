package de.marketmaker.itools.gwtutil.client.event;

import com.google.gwt.dom.client.NativeEvent;

/**
 * Author: umaurer
 * Created: 29.08.14
 */
public class KeyModifiers {
    public static boolean isNone(NativeEvent event) {
        return !(event.getShiftKey() || event.getCtrlKey() || event.getAltKey() || event.getMetaKey());
    }

    public static boolean isCtrl(NativeEvent event) {
        return event.getCtrlKey()
                && !(event.getShiftKey() || event.getAltKey() || event.getMetaKey());
    }

    public static boolean isShift(NativeEvent event) {
        return event.getShiftKey()
                && !(event.getCtrlKey() || event.getAltKey() || event.getMetaKey());
    }
    public static boolean isCtrlShift(NativeEvent event) {
        return event.getShiftKey() && event.getCtrlKey()
                && !(event.getAltKey() || event.getMetaKey());
    }
    public static boolean isAltShift(NativeEvent event) {
        return event.getShiftKey() && event.getAltKey()
                && !(event.getCtrlKey() || event.getMetaKey());
    }
    public static boolean isCtrlAltShift(NativeEvent event) {
        return event.getShiftKey() && event.getCtrlKey() && event.getAltKey()
                && !event.getMetaKey();
    }

    public static KeyCombination.Factory ctrl() {
        return new KeyCombination.Factory().ctrl();
    }

    public static KeyCombination.Factory shift() {
        return new KeyCombination.Factory().shift();
    }

    public static KeyCombination.Factory alt() {
        return new KeyCombination.Factory().alt();
    }

    public static KeyCombination.Factory meta() {
        return new KeyCombination.Factory().meta();
    }
}