/*
 * SpsListSectionUtil.java
 *
 * Created on 08.12.2014 11:33
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection;

import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.List;

/**
 * @author mdick
 */
public class SpsListSectionUtil {
    private SpsListSectionUtil() {
        //do nothing
    }

    private static IconImageIcon createIcon(String iconClass, String styleName, String qtip) {
        final IconImageIcon icon = IconImage.getIcon(iconClass);
        Tooltip.addQtip(icon, qtip);
        icon.setStyleName(styleName);
        return icon;
    }

    private static String getText(String text, String defaultText) {
        if (!StringUtil.hasText(text)) {
            return defaultText;
        }
        return text;
    }

    public static boolean focusFirst(List<SpsWidget> spsWidgetList1) {
        for (SpsWidget spsWidget : spsWidgetList1) {
            if (spsWidget.focusFirst()) {
                return true;
            }
        }
        return false;
    }

    public static IconImageIcon createAddButton(String addButtonTooltip) {
        return createIcon("sps-section-add", "sps-listSection-add", // $NON-NLS$
                getText(addButtonTooltip, I18n.I.addEntry()));
    }

    public static IconImageIcon createDeleteButton(String deleteButtonTooltip) {
        return createIcon("sps-section-remove", "sps-listSection-remove", // $NON-NLS$
                getText(deleteButtonTooltip, I18n.I.deleteEntry()));
    }

    public static IconImageIcon createDeleteAllButton(String deleteAllButtonTooltip) {
        return createIcon("sps-section-remove-all", "sps-listSection-removeAll", // $NON-NLS$
                getText(deleteAllButtonTooltip, I18n.I.deleteAllEntries()));
    }
}
