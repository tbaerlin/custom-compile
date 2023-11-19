/*
 * ValidationMessagePopup.java
 *
 * Created on 29.10.14
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.itools.gwtutil.client.util.Transitions;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ErrorSeverity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Not intended to be used in legacy view contexts!
 * @author umaurer
 */
public class ValidationMessagePopup {
    public enum MessageType {
        VALIDATION_FAILED("validationFailed"), // $NON-NLS$
        VALIDATION_WHEN_TYPING_FAILED("validationFailedWhenTyping"); // $NON-NLS$

        private final String value;

        MessageType(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    public static final ValidationMessagePopup I;

    private static final ArrayList<Supplier<List<String>>> severitySuppliers;

    static {
        I = new ValidationMessagePopup();
        severitySuppliers = new ArrayList<>();
        addDefaultSeveritySupplier();
        addPmxmlSeveritySupplier();
    }

    private final PopupPanel popup = new PopupPanel(false, false);

    private boolean fromHover = false;

    private final PopupColors defaultPopupColors;

    private final HashMap<String, PopupColors> popupColors = new HashMap<>();

    private static class PopupColors {
        private String backgroundColor;

        private String borderColor;

        public PopupColors(String backgroundColor, String borderColor) {
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
        }
    }

    private ValidationMessagePopup() {
        this.defaultPopupColors = getComputedPopupColor(null);

        Transitions.fadeInAfterAttach(this.popup, 600);
        this.popup.setStyleName("sps-task-error-popup");
        this.popup.addStyleName(Canvas.createIfSupported() == null ? "nocanvas" : "canvas");
    }

    public static void addSeveritySupplier(Supplier<List<String>> severitySupplier) {
        severitySuppliers.add(severitySupplier);
        I.updatePopupColors();
    }

    private static void addDefaultSeveritySupplier() {
        final MessageType[] messageTypes = MessageType.values();
        final ArrayList<String> severityValues = new ArrayList<>(messageTypes.length);
        for (MessageType messageType : messageTypes) {
            severityValues.add(messageType.value());
        }
        addSeveritySupplier(() -> severityValues);
    }

    private static void addPmxmlSeveritySupplier() {
        // This method is necessary, because we hijacked pmxml/SPS stuff for the AS/ICE
        // password change view and for the AS/ICE settings view.
        final ErrorSeverity[] values = ErrorSeverity.values();
        final ArrayList<String> severityValues = new ArrayList<>(values.length);
        for (ErrorSeverity errorSeverity : values) {
            severityValues.add(errorSeverity.value());
        }
        addSeveritySupplier(() -> severityValues);
    }

    public void forEachSeverity(Consumer<String> severityStringConsumer) {
        for (Supplier<List<String>> severitySupplier : severitySuppliers) {
            for (String severityString : severitySupplier.get()) {
                severityStringConsumer.accept(severityString);
            }
        }
    }

    private void updatePopupColors() {
        forEachSeverity(value -> this.popupColors.put(value, getComputedPopupColor(value)));
    }

    private PopupColors getComputedPopupColor(String additionalErrorStyle) {
        final Label testLabel = new Label();
        testLabel.setStyleName(additionalErrorStyle != null ? "sps-task-error-popup testLabel " + additionalErrorStyle : "sps-task-error-popup testLabel");
        RootPanel.get().add(testLabel);
        final String popupBackgroundColor = CssUtil.getComputedPropertyValue(testLabel.getElement(), "background-color"); // $NON-NLS$
        final String popupBorderColor = CssUtil.getComputedPropertyValue(testLabel.getElement(), "border-top-color"); // $NON-NLS$
        RootPanel.get().remove(testLabel);
        return new PopupColors(popupBackgroundColor, popupBorderColor);
    }

    private PopupColors getPopupColor(String errorSeverity) {
        if (!StringUtil.hasText(errorSeverity)) {
            return this.defaultPopupColors;
        }
        final PopupColors popupColors = this.popupColors.get(errorSeverity);
        if (popupColors == null) {
            return this.defaultPopupColors;
        }
        return popupColors;
    }

    public void showErrorNearby(String severityValue, SafeHtml errorMessage, final Widget widget,
            boolean fromHover) {

        if (this.popup.isShowing()) {
            if (fromHover) {
                return;
            }
            this.popup.hide();
        }

        final FlowPanel panel = new FlowPanel();
        final Canvas canvas = Canvas.createIfSupported();
        if (canvas != null) {
            canvas.setStyleName("sps-task-error-canvas");
            panel.add(canvas);
        }
        else if (severityValue != null) {
            forEachSeverity(this.popup::removeStyleName);
            this.popup.addStyleName(severityValue);
        }

        final Label label = new HTML(errorMessage);
        label.setStyleName("sps-task-error-label");
        panel.add(label);
        this.popup.setWidget(panel);
        setPopupPositionAndShow(getPopupColor(severityValue), this.popup, widget, canvas);
        this.fromHover = fromHover;
    }

    public void showErrorNearby(MessageType messageType, String errorMessage, final Widget widget,
            boolean fromHover) {
        if (!StringUtil.hasText(errorMessage)) {
            return;
        }

        showErrorNearby(messageType != null ? messageType.value() : null, SafeHtmlUtils.fromString(errorMessage), widget, fromHover);
    }

    private void setPopupPositionAndShow(PopupColors popupColors, PopupPanel popup,
            Widget nearbyWidget, Canvas canvas) {
        popup.setVisible(false);
        popup.show();
        final int clientWidth = Window.getClientWidth();
        final int widgetLeft = nearbyWidget.getAbsoluteLeft();
        final int widgetTop = nearbyWidget.getAbsoluteTop();

        popup.setPopupPosition(0, 0);
        int offsetWidth = popup.getOffsetWidth();
        int offsetHeight = popup.getOffsetHeight();

        final int left;
        if (widgetLeft - 8 + offsetWidth >= clientWidth) {
            left = clientWidth - offsetWidth;
        }
        else {
            left = widgetLeft - 8;
        }

        final int top = widgetTop - offsetHeight;
        final int hotX = widgetLeft - left + 8;
        popup.setPopupPosition(left, top);

        if (canvas != null) {
            drawBubble(popupColors, canvas, offsetWidth, offsetHeight, hotX);
        }

        popup.setVisible(true);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private void drawBubble(PopupColors popupColors, Canvas canvas, int offsetWidth,
            int offsetHeight, int hotX) {
        final int shadow = 3;
        final int borderRadius = 6;
        final int xLeft = shadow;
        final int xRight = offsetWidth - shadow;
        final int yTop = shadow;
        final int yBottom = offsetHeight - shadow - 5;
        final int hotX1 = hotX - 5;
        final int hotX2 = hotX + 5;
        final int hotY = offsetHeight - shadow;
        canvas.setCoordinateSpaceWidth(offsetWidth);
        canvas.setCoordinateSpaceHeight(offsetHeight);
        final Style canvasStyle = canvas.getElement().getStyle();
        canvasStyle.setWidth(offsetWidth, PX);
        canvasStyle.setHeight(offsetHeight, PX);
        final Context2d context = canvas.getContext2d();
        context.setLineWidth(2);
        context.setStrokeStyle(popupColors.borderColor);
        context.setFillStyle(popupColors.backgroundColor);
        context.setShadowColor("#888"); // $NON-NLS$
        context.setShadowBlur(shadow);
        context.beginPath();
        context.moveTo(hotX, hotY); // arrow
        context.lineTo(hotX1, yBottom);
        context.lineTo(xLeft + borderRadius, yBottom);
        context.quadraticCurveTo(xLeft, yBottom, xLeft, yBottom - borderRadius); // bottom left
        context.lineTo(xLeft, yTop + borderRadius);
        context.quadraticCurveTo(xLeft, yTop, xLeft + borderRadius, yTop); // top left
        context.lineTo(xRight - borderRadius, yTop);
        context.quadraticCurveTo(xRight, yTop, xRight, yTop + borderRadius); // top right
        context.lineTo(xRight, yBottom - borderRadius);
        context.quadraticCurveTo(xRight, yBottom, xRight - borderRadius, yBottom); // bottom right
        context.lineTo(hotX2, yBottom);
        context.lineTo(hotX, hotY);
        context.closePath();
        context.stroke();
        context.setShadowColor(null);
        context.setShadowBlur(0);
        context.fill();
        context.stroke();
    }

    public void hide(boolean fromHover) {
        if (fromHover && !this.fromHover) {
            return;
        }
        this.popup.hide();
    }
}
