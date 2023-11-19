package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.DOMUtil;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.PopupPositionCallback;

import java.util.Arrays;

/**
 * Author: umaurer
 * Created: 29.04.14
 */
public interface MessagePopup {
    public void show(Widget atWidget, String... messages);
    public void hide();

    public static class DefaultMessagePopup implements MessagePopup {
        private PopupPanel popup;
        private FlowPanel panel;
        private Widget atWidget;
        private String[] messages;

        @Override
        public void show(Widget atWidget, String... messages) {
            if (messages == null || messages.length == 0) {
                return;
            }
            if (atWidget == this.atWidget
                    && Arrays.equals(this.messages, messages)
                    && this.popup != null && this.popup.isShowing()) {
                return;
            }
            this.atWidget = atWidget;
            this.messages = messages;
            if (this.popup == null) {
                this.panel = new FlowPanel();
                this.popup = new PopupPanel(true, false);
                PopupPanelFix.addFrameDummy(this.popup);
                //noinspection GWTStyleCheck
                this.popup.setStyleName("mm-form-invalid mm-errorPopup");
                this.popup.setWidget(this.panel);
            }
            this.panel.clear();
            boolean showPopup = false;
            for (String message : messages) {
                if (message != null) {
                    this.panel.add(new Label(message));
                    showPopup = true;
                }
            }
            if (showPopup) {
                DOMUtil.setTopZIndex(this.popup);
                this.popup.setPopupPositionAndShow(new PopupPositionCallback(atWidget, this.popup));
            }

        }

        @Override
        public void hide() {
            if (this.popup != null) {
                this.popup.hide(false);
            }
        }
    }

}
