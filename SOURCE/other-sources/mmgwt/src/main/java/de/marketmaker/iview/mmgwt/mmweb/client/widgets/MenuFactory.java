package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;

import de.marketmaker.itools.gwtutil.client.util.Firebug;

/**
 * <p>
 * Creates a com.extjs.gxt.ui.client.widget.menu.Menu and adds show- and hide-Listeners
 * to prevent getting overlaid by plug-ins (e.g. Acrobat Reader).
 * </p><p>
 * An open menu is usually hidden by a plug-in (which is loaded into an iFrame).
 * Introducing a second iFrame with a higher z-index below the menu (with an even more higher z-index)
 * prevents from being hidden by the plug-in iFrame.
 * </p>
 * <p/>
 * <p/>
 * Created on 27.05.2010 14:51:15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class MenuFactory {
    private static Element iFrame = null;

    public static Menu createIFrameMenu() {
        final Menu menu = new Menu();
        menu.addListener(Events.Show, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                showIFrame(menu.getElement());
            }
        });
        menu.addListener(Events.Hide, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                hideIFrame(menu.getElement());
            }
        });
        return menu;
    }

    private static void hideIFrame(Element element) {
        getIFrame(element).getStyle().setProperty("visibility", "hidden"); // $NON-NLS-0$ $NON-NLS-1$
    }

    private static void showIFrame(Element element) {
        Firebug.log("!!execute"); // $NON-NLS-0$
        final Element eltIFrame = getIFrame(element);
        final com.google.gwt.dom.client.Style style = eltIFrame.getStyle();
        final com.google.gwt.dom.client.Style styleMenu = element.getStyle();
        style.setProperty("position", "absolute"); // $NON-NLS-0$ $NON-NLS-1$
        style.setProperty("zIndex", "999"); // $NON-NLS-0$ $NON-NLS-1$
        style.setProperty("top", styleMenu.getProperty("top")); // $NON-NLS-0$ $NON-NLS-1$
        style.setProperty("left", styleMenu.getProperty("left")); // $NON-NLS-0$ $NON-NLS-1$
        style.setProperty("width", String.valueOf(element.getOffsetWidth()) + "px"); // $NON-NLS-0$ $NON-NLS-1$
        style.setProperty("height", String.valueOf(element.getOffsetHeight()) + "px"); // $NON-NLS-0$ $NON-NLS-1$
        style.setProperty("border", "0 none"); // $NON-NLS-0$ $NON-NLS-1$
        style.setProperty("visibility", "visible"); // $NON-NLS-0$ $NON-NLS-1$
    }

    private static Element getIFrame(final Element element) {
        if (iFrame == null) {
            iFrame = DOM.createElement("iframe"); // $NON-NLS-0$
            element.getParentElement().insertFirst(iFrame);
        }
        return iFrame;
    }
}
