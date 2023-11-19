/*
 * TinyEdgBadge.java
 *
 * Created on 10.10.2014 11:39
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import de.marketmaker.iview.dmxml.EDGData;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.util.EdgUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * Implements a tiny EDG badge that shows the ordinary EDG badge/chart image in a pop-up dialog.
 * It is intended to be used in the narrow object info view of the AS variant.
 *
 * @author mdick
 */
@NonNLS
public class TinyEdgBadge extends Composite {
    private final ClickHandler clickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            showEdgImageAsPopup();
        }
    };

    private final HTML layout = new HTML();
    private Image edgBadge;

    public TinyEdgBadge() {
        initWidget(this.layout);

        this.layout.setStyleName("mm-tinyEdgBadge");
        this.layout.addStyleName("mm-link");

        this.layout.sinkEvents(Event.ONCLICK);
        this.layout.addHandler(this.clickHandler, ClickEvent.getType());

        setVisible(false);
    }

    public void update(EDGData edgData, Image edgBadge) {
        if(edgBadge == null || edgData == null)
        {
            setVisible(false);
            return;
        }

        final SafeHtmlBuilder sb  = new SafeHtmlBuilder()
                .appendHtmlConstant("<div class=\"mm-tinyEdgBadge-bg\">")
                .append(IconImage.get("tiny-edg-badge").getSafeHtml())
                .appendHtmlConstant("</div><div class=\"mm-tinyEdgBadge-topClasses\">")
                .append(renderTopClasses(edgData.getRating().getEdgTopClass()))
                .appendHtmlConstant("</div><div class=\"mm-tinyEdgBadge-topScore\">")
                .append(EdgUtil.getGoldenStarHtml(edgData.getRating().getEdgTopScore()))
                .appendHtmlConstant("</div>");

        this.layout.setHTML(sb.toSafeHtml());

        this.edgBadge = edgBadge;
        setVisible(true);
    }

    private void showEdgImageAsPopup() {
        if (TinyEdgBadge.this.edgBadge != null) {
            final PopupPanel edgPopup = new PopupPanel(true);
            edgPopup.setStyleName("mm-tinyEdgBadge-transparent");
            edgPopup.add(this.edgBadge);
            edgPopup.showRelativeTo(this.layout);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.layout.setVisible(visible);
    }

    private SafeHtml renderTopClasses(String topClassStr) {
        try {
            final int topClass = Integer.parseInt(topClassStr);

            final SafeHtmlBuilder sb = new SafeHtmlBuilder();
            for (int i = 1; i < 6; i++) {
                sb.append(renderTopClass(i, topClass == i));
            }
            return sb.toSafeHtml();
        }
        catch(Exception e) {
            return SafeHtmlUtils.EMPTY_SAFE_HTML;
        }
    }

    private SafeHtml renderTopClass(int topClass, boolean active) {
        if(active) {
            return SafeHtmlUtils.fromTrustedString("<div class=\"mm-tinyEdgBadge-topClass mm-tinyEdgBadge-topClass-active\">" + topClass + "</div>");
        }
        return SafeHtmlUtils.fromTrustedString("<div class=\"mm-tinyEdgBadge-topClass\">" + topClass + "</div>");
    }
}
