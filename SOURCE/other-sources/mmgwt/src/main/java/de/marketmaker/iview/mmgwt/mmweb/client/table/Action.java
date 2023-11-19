/*
 * Action.java
 *
 * Created on 07.01.2009 15:56:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * An action that is shown in a certain table cell.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Action {
    public static Action ACKNOWLEDGE = new Action("Ok", "mm-small-ok", I18n.I.confirmReception());  // $NON-NLS$
    public static Action EDIT = new Action("Edit", "mm-small-edit", I18n.I.edit());  // $NON-NLS$
    public static Action DELETE = new Action("Del", "mm-small-remove", I18n.I.delete());  // $NON-NLS$

    private String text;
    private String icon;
    private String tooltip;

    public static Action textAction(String text, String tooltip) {
        return new Action(text, null, tooltip);
    }

    public static Action iconAction(String icon, String tooltip) {
        return new Action(null, icon, tooltip);
    }

    private Action(String text, String icon, String tooltip) {
        this.text = text;
        this.icon = icon;
        this.tooltip = tooltip;
    }


    public String getText() {
        return text;
    }

    public String getIcon() {
        return icon;
    }

    public String getTooltip() {
        return tooltip;
    }
}
