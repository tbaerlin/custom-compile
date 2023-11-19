package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Author: umaurer
 * Created: 11.02.15
 */
public class PopupPositionCenter implements PopupPanel.PositionCallback {
    private final PopupPanel popupPanel;

    public PopupPositionCenter(PopupPanel popupPanel) {
        this.popupPanel = popupPanel;
    }

    @Override
    public void setPosition(int width, int height) {
        final int windowWidth = Window.getClientWidth();
        final int windowHeight = Window.getClientHeight();
        this.popupPanel.setPopupPosition(
                (windowWidth - width) / 2,
                (windowHeight - height) / 2
        );
    }
}
