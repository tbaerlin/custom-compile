package de.marketmaker.itools.gwtutil.client.widgets.notification;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import de.marketmaker.itools.gwtutil.client.widgets.PopupPanelFix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * User: umaurer
 * Date: 20.06.13
 * Time: 15:13
 */
public class NotificationBox extends Composite implements NotificationHandler {
    private final Label label = new Label();
    private final PopupPanel popupPanel = new PopupPanel(false, false);
    private final FlowPanel flowPanel = new FlowPanel();
    private final Map<Integer, MessageWidget> mapWidgets = new HashMap<Integer, MessageWidget>();
    private final I18nCallback i18nCallback;
    private int widgetCount = 0;

    public interface I18nCallback {
        String nNotifications(int count);
    }

    public NotificationBox(I18nCallback i18nCallback) {
        this.i18nCallback = i18nCallback;
        Notifications.I.addNotificationHandler(this);
        this.label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (widgetCount > 0) {
                    flipPopupVisibility();
                }
            }
        });
        initWidget(this.label);

        final Image deleteAll = new Image("clear.cache.gif");
        deleteAll.setStyleName("delete-all");
        deleteAll.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                deleteAllNotifications();
            }
        });
        this.flowPanel.add(deleteAll);

        final Image close = new Image("clear.cache.gif");
        close.setStyleName("close-popup");
        close.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                flipPopupVisibility();
            }
        });
        this.flowPanel.add(close);

        this.popupPanel.setWidget(this.flowPanel);
        this.popupPanel.setStyleName("mm-noteBox-popup");
        this.popupPanel.setVisible(false);
        PopupPanelFix.addFrameDummy(this.popupPanel);
    }

    private void deleteAllNotifications() {
        for (MessageWidget messageWidget : new ArrayList<>(this.mapWidgets.values())) {
            messageWidget.close();
        }
    }

    @Override
    public void onNotification(NotificationEvent event) {
        final NotificationMessage message = event.getMessage();
        NotificationMessage.State state = event.getRequestedState();
        switch (state) {
            case DEFAULT:
            case HIDDEN:
            case VISIBLE:
            case FORCE_HIDDEN:
                MessageWidget widget = this.mapWidgets.get(message.getId());
                if (widget == null) {
                    widget = new MessageWidget(message);
                    this.mapWidgets.put(message.getId(), widget);
                    this.flowPanel.insert(widget, 2);
                    this.widgetCount++;
                    if (event.getRequestedState() != NotificationMessage.State.HIDDEN) {
                        showPopup();
                        state = NotificationMessage.State.DEFAULT;
                    }
                }
                if (state == NotificationMessage.State.VISIBLE) {
                    showPopup();
                    state = NotificationMessage.State.DEFAULT;
                }
                if (state == NotificationMessage.State.FORCE_HIDDEN) {
                    hidePopup();
                    state = NotificationMessage.State.HIDDEN;
                }
                widget.setProgress(message.getProgress());
                break;
            case DELETED:
                final MessageWidget widgetToDelete = this.mapWidgets.remove(message.getId());
                this.flowPanel.remove(widgetToDelete);
                this.widgetCount--;
                break;
        }
        if (this.widgetCount == 0) {
            this.popupPanel.setVisible(false);
            this.label.setText("");
            this.label.setStyleName("");
        }
        else {
            this.label.setText(this.i18nCallback.nNotifications(this.widgetCount));
            this.label.setStyleName("mm-noteBox");
        }
        message.setState(state);
    }

    private void showPopup() {
        this.popupPanel.setVisible(false);
        this.popupPanel.show();
        final Style style = this.popupPanel.getElement().getStyle();
        style.setProperty("top", "auto");
        style.setProperty("right", "0");
        style.setProperty("bottom", this.label.getOffsetHeight() + "px");
        style.setProperty("left", "auto");
        this.popupPanel.setVisible(true);
/*
        this.popupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                popupPanel.setPopupPosition(10, 10);
            }
        });
*/
    }

    private void hidePopup() {
        this.popupPanel.setVisible(false);
    }

    class MessageWidget extends Composite {
        private final FlowPanel panel = new FlowPanel();
        private final NotificationMessage message;
        private NotificationProgress progress;
        private final Image close;

        MessageWidget(final NotificationMessage message) {
            this.message = message;
            this.panel.setStyleName("mm-noteBox-message");
            final boolean withProgress = !Double.isNaN(message.getProgress());

            this.close = new Image("clear.cache.gif");
            this.close.setStyleName("close");
            this.close.setVisible(!withProgress || message.isCancellableProgress());
            this.close.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    close();
                }
            });
            this.panel.add(this.close);

            final Label lblHeader = new HTML(message.getHeader());
            lblHeader.setStyleName("header");
            this.panel.add(lblHeader);

            this.progress = new NotificationProgress();
            this.progress.setVisible(withProgress);
            this.panel.add(this.progress);

            final Widget widget = message.getWidget();
            if (widget != null) {
                this.panel.add(widget);
            }

            initWidget(this.panel);
        }

        public void close() {
            Notifications.I.fireState(this.message, NotificationMessage.State.DELETED);
        }

        public void setProgress(double progress) {
            final boolean withProgress = !Double.isNaN(progress);
            if (withProgress) {
                this.close.setVisible(progress >= 1d || message.isCancellableProgress());
                this.progress.setProgress(progress);
            }
        }
    }

    private void flipPopupVisibility() {
        if (this.popupPanel.isVisible()) {
            this.popupPanel.setVisible(false);
        }
        else {
            showPopup();
        }
    }
}
