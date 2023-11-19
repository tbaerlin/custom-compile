package de.marketmaker.iview.mmgwt.mmweb.client.customer.wgz;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CertificateTypeEnum;

import java.util.LinkedHashMap;

/**
 * Created on 11.04.13 15:14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class WGZCertificateTypesView implements IsWidget {

    private final WGZCertificateTypesController controller;
    private final FlowPanel panel;

    public WGZCertificateTypesView(WGZCertificateTypesController controller) {
        this.controller = controller;
        this.panel = new FlowPanel();
        this.panel.addStyleName("mm-simpleHtmlView");
    }

    @Override
    public Widget asWidget() {
        return this.panel;
    }

    public void update(LinkedHashMap<CertificateTypeEnum, String> data) {
        this.panel.clear();
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        if (data == null) {
            sb.appendEscaped(I18n.I.noDataAvailable());
            this.panel.add(new HTML(sb.toSafeHtml()));
            return;
        }

        sb.appendHtmlConstant("<div class=\"internal-tool-header\">"); // $NON-NLS$
        sb.appendEscaped("WGZ BANK ").appendEscaped(I18n.I.certificates()) // $NON-NLS$
                .appendEscaped(" (").appendEscaped(I18n.I.count()).appendEscaped(")");
        sb.appendHtmlConstant("</div><div class=\"internal-tool-text\">"); // $NON-NLS$

        this.panel.add(new HTML(sb.toSafeHtml()));

        for (final CertificateTypeEnum type : data.keySet()) {
            final Label label = new Label();
            label.addStyleName("mm-link");
            label.addStyleName("internal-tool-text");
            label.setText(getDescription(type) + " (" + data.get(type) + ")");
            label.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    controller.openFinder(type);
                }
            });
            this.panel.add(label);
        }
    }

    private String getDescription(CertificateTypeEnum type) {
        if (type == CertificateTypeEnum.CERT_MBI) {
            return "MBI"; // $NON-NLS$
        }
        if (type == CertificateTypeEnum.CERT_REVERSE_CONVERTIBLE) {
            return "Aktien-/Indexanleihen"; // $NON-NLS$
        }
        if (type == CertificateTypeEnum.CERT_GUARANTEE) {
            return "Garant-Zertifikate"; // $NON-NLS$
        }
        if (type == CertificateTypeEnum.CERT_SPRINT || type == CertificateTypeEnum.CERT_SPRINTER) {
            return "Sprint-Zertifikate"; // $NON-NLS$
        }
        return type.getDescription() + "-Zertifikate"; // $NON-NLS$
    }
}