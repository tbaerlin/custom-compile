package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * User: umaurer
 * Date: 21.10.13
 * Time: 13:45
 */
public class PopupPanelFix {
    private static final PopupPanelFixImpl FIX = GWT.create(PopupPanelFixImpl.class);

    public static void addFrameDummy(Widget widget) {
        FIX.addFrameDummy(widget.getElement());
    }

    public static void addFrameDummy(PopupPanel popupPanel) {
        FIX.addFrameDummy(popupPanel.getElement());
    }

    public static void addFrameDummyForSuggestPopup(PopupPanel popupPanel) {
        FIX.addFrameDummyForSuggestPopup(popupPanel.getElement());
    }
}
