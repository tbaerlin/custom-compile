/*
 * GuiUtil.java
 *
 * Created on 24.04.2009 11:54:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class GuiUtil {
    public static LabelToolItem createToolbarTextItem(String label, String... styles) {
        final LabelToolItem result = new LabelToolItem(label);
        if (styles != null) {
            for (String style : styles) {
                result.addStyleName(style);
            }
        }
        return result;
    }

    public static ImageButton createImageButton(String iconClassDefault, String iconClassDisabled,
            String iconClassActive, String tooltip) {
        final Image imageDefault = IconImage.get(iconClassDefault).createImage();
        final Image imageDisabled = iconClassDisabled == null ? null : IconImage.get(iconClassDisabled).createImage();
        final Image imageActive = iconClassActive == null ? null : IconImage.get(iconClassActive).createImage();
        final ImageButton result = new ImageButton(imageDefault, imageDisabled, imageActive);
        Tooltip.addQtip(result, tooltip);
        return result;
    }

    public interface WidgetCallback {
        Widget getWidget();
    }

    public static ImageButton createPopupButton(String iconClassDefault, String tooltip,
            final WidgetCallback widgetCallback) {
        final ImageButton imageButton = createImageButton(iconClassDefault, null, null, tooltip);
        imageButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                final PopupPanel popupPanel = new PopupPanel(true, true);
                popupPanel.addStyleName("mm-popupPanel");
                popupPanel.add(widgetCallback.getWidget());
                popupPanel.showRelativeTo(imageButton);
                popupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
                    @Override
                    public void onClose(CloseEvent<PopupPanel> event) {
                        imageButton.setMouseOver(false);
                    }
                });
            }
        });
        return imageButton;
    }

    public static Widget createPopupAnchor(String text, String url, final String actionToken) {
        final Anchor anchor = new Anchor(text, url, "_blank"); // $NON-NLS$
        anchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                ActionPerformedEvent.fire(actionToken);
            }
        });
        final SimplePanel simplePanel = new SimplePanel();
        simplePanel.setWidget(anchor);
        return simplePanel;
    }

    public static Window createModalWindow(String title, int width, int height) {
        return createModalWindow(title, width, height, true);
    }

    public static CheckBox createCheckBox(String label) {
        final CheckBox result = new CheckBox();
        result.setFieldLabel(label);
        return result;
    }

    public static CheckBox createCheckBox(String label, String style) {
        final CheckBox result = new CheckBox();
        result.setBoxLabel("<span class=\"" + style + "\">&nbsp;&nbsp;</span>&nbsp;" + label); // $NON-NLS-0$ $NON-NLS-1$
        result.setStyleName("mm-chartcenter-checkbox"); // $NON-NLS-0$
        return result;
    }

    public static <V extends ModelData> ComboBox<V> createComboBox(String label, String style,
            ListStore<V> store) {
        final ComboBox<V> result = new ComboBox<>();
        if (style != null) {
            result.setFieldLabel("<span class=\"" + style + "\">&nbsp;&nbsp;</span>&nbsp;" + label); // $NON-NLS-0$ $NON-NLS-1$
        }
        else {
            result.setFieldLabel(label);
        }
        result.setStore(store);
        result.setTriggerAction(ComboBox.TriggerAction.ALL);
        return result;
    }

    public static <V extends ModelData> ComboBox<V> createComboBox(String label,
            ListStore<V> store) {
        return createComboBox(label, null, store);
    }

    public static <T> SimpleComboBox<T> createSimpleComboBox(String label,
            List<T> values, boolean editable) {
        SimpleComboBox<T> combo = new SimpleComboBox<>();
        combo.add(values);
        combo.setFieldLabel(label);
        combo.setEditable(editable);
        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
        return combo;
    }

    public static <V> TextField<V> createTextField(String label, int maxLength, int width) {
        final TextField<V> result = new TextField<>();
        result.setFieldLabel(label);
        result.setMaxLength(maxLength);
        result.setWidth(width);
        return result;
    }

    public static Window createModalWindow(String title, int width, int height, boolean closable) {
        return createModalWindow(title, width, height, closable, null);
    }

    public static Window createModalWindow(String title, int width, int height, boolean closable,
            final PopupPanel parentPopupPanel) {
        final Window result = new Window();
        result.setHeading(title);
        result.setHeight(height);
        result.setWidth(width);
        result.setModal(true);
        result.setResizable(false);
        result.setDraggable(false);
        result.setClosable(closable);
        if (parentPopupPanel != null) {
            result.addWindowListener(new WindowListener() {
                @Override
                public void windowShow(WindowEvent we) {
                    parentPopupPanel.setAutoHideEnabled(false);
                }

                @Override
                public void windowHide(WindowEvent we) {
                    parentPopupPanel.setAutoHideEnabled(true);
                }
            });
        }
        return result;
    }

}
