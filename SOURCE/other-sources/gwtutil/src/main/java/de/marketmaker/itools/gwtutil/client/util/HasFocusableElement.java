/*
 * HasFocusableElement.java
 *
 * Created on 10.09.2015 16:21
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.itools.gwtutil.client.util;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Useful for composite widgets where one of the composite's children has the focusable element.
 * @see WidgetUtil#hasFocus(Widget)
 * @author mdick
 */
public interface HasFocusableElement {
    Element getFocusableElement();
}
