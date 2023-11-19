/*
 * WidgetUtil.java
 *
 * Created on 08.01.2009 11:12:37
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.HandlesAllFocusEvents;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;

import static de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget.PUSH_ATTRIBUTE_NAME;
import static de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget.PUSH_ATTRIBUTE_VALUE;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WidgetUtil {

    private static final HandlesAllFocusEvents BORDER_FOCUS_LISTENER = new HandlesAllFocusEvents() {
        public void onFocus(FocusEvent focusEvent) {
            ((Widget) (focusEvent.getSource())).addStyleName("mm-form-focus"); // $NON-NLS-0$
        }

        public void onBlur(BlurEvent blurEvent) {
            ((Widget) (blurEvent.getSource())).removeStyleName("mm-form-focus"); // $NON-NLS-0$
        }
    };

    public static void applyFormStyling(final Widget... widgets) {
        for (Widget widget : widgets) {
            if (widget instanceof HasAllFocusHandlers || widget instanceof HasFocusHandlers && widget instanceof HasBlurHandlers) {
                ((HasFocusHandlers) widget).addFocusHandler(BORDER_FOCUS_LISTENER);
                ((HasBlurHandlers) widget).addBlurHandler(BORDER_FOCUS_LISTENER);
            }
            widget.setStyleName("mm-form-field"); // $NON-NLS-0$
        }
    }

    public static ArrayList<Element> getPushedElements(Widget w, String... tagNames) {
        final ArrayList<Element> result = new ArrayList<>();
        for (String tagName : tagNames) {
            final NodeList<Element> tds = w.getElement().getElementsByTagName(tagName);
            for (int i = 0; i < tds.getLength(); i++) {
                if (PUSH_ATTRIBUTE_VALUE.equals(tds.getItem(i).getAttribute(PUSH_ATTRIBUTE_NAME))) {
                    result.add(tds.getItem(i));
                }
            }
        }
        return result;
    }

}
