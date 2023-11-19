/*
 * DzPibReportLinkController.java
 *
 * Created on 28.09.2012 15:04:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

/**
 * @author Markus Dick
 */
public class DzPibDownloadLinkRenderer implements Renderer<String> {

    @Override
    public String render(String pibReportUrl) {
        SafeHtmlBuilder safeHtml = new SafeHtmlBuilder();

        if(pibReportUrl != null && !pibReportUrl.trim().isEmpty()) {
            safeHtml.appendHtmlConstant("<a href=\"" + pibReportUrl + "\" target=\"_blank\" title=\"" + I18n.I.dzBankPibTooltip() + "\">"); // $NON-NLS$
            safeHtml.append(IconImage.get("mm-icon-dzbank-pib").getSafeHtml());  //$NON-NLS$
            safeHtml.appendHtmlConstant("</a>"); // $NON-NLS$
        }
        else {
            safeHtml.appendHtmlConstant("<span title=\"" + I18n.I.dzBankPibTooltip() + "\">"); // $NON-NLS$
            safeHtml.append(IconImage.get("mm-icon-dzbank-pib").getSafeHtml()); //$NON-NLS$
            safeHtml.appendHtmlConstant("</span>"); // $NON-NLS$
        }

        return safeHtml.toSafeHtml().asString();
    }

    public static Widget createWidget(final String pibReportUrl) {
        final AbstractImagePrototype aip = IconImage.get("mm-icon-dzbank-pib");  //$NON-NLS$
        final Image image = aip.createImage();
        image.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        image.setTitle(I18n.I.dzBankPibTooltip());
        image.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open(pibReportUrl, "_blank", "");  //$NON-NLS$
            }
        });
        return image;
    }
}
