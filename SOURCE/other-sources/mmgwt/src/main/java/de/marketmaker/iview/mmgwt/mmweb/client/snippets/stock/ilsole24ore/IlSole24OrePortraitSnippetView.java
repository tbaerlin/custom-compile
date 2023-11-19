/*
 * IlSole24OrePortraitSnippet.java
 *
 * Created on 29.08.2012 14:42:23
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.ilsole24ore;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.iview.dmxml.STKIlSole24OreCompanyData;
import de.marketmaker.iview.dmxml.STKVwdItCompanyRawdata;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.Desktop;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.DesktopIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DOMUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Markus Dick
 */
public class IlSole24OrePortraitSnippetView extends SnippetView<IlSole24OrePortraitSnippet> {
    private final FlowPanel panel = new FlowPanel();
    private final ContentPanel pdfcontainer = new ContentPanel();
    private final Desktop<Object> desktop = new Desktop<>(Desktop.Mode.FLOW);

    public IlSole24OrePortraitSnippetView(IlSole24OrePortraitSnippet snippet) {
        super(snippet);
        this.pdfcontainer.setTitle(I18n.I.portraitIlSole24OrePdfSecTitle());
        this.pdfcontainer.addStyleName("mm-convensys-snippet"); //$NON-NLS$
        this.pdfcontainer.setHeaderText(this.pdfcontainer.getTitle());
        this.pdfcontainer.add(this.desktop);
    }

    @Override
    protected void onContainerAvailable() {
        this.container.setHeaderVisible(false);
        this.container.setContentWidget(this.panel);
    }

    public void update(STKIlSole24OreCompanyData data, STKVwdItCompanyRawdata rawData) {
        this.panel.clear();
        this.desktop.clear();
        boolean desktopHasIcons = false;

        if (StringUtil.hasText(rawData.getPdfRequest())) {
            desktopHasIcons = true;
            this.desktop.add(createDesktopIcon(I18n.I.portraitIlSole24OrePdf(), rawData.getPdfRequest()));
        }
        if (StringUtil.hasText(rawData.getOrgPdfRequest())) {
            desktopHasIcons = true;
            this.desktop.add(createDesktopIcon(I18n.I.portraitIlSole24OreOrgPdf(), rawData.getOrgPdfRequest()));
        }

        if (desktopHasIcons) {
            this.panel.add(this.pdfcontainer);
        }

        if (StringUtil.hasText(data.getData())) {
            addHtml(data.getData());
        }
        else {
            addText(I18n.I.noData());
        }
    }

    public void setNoData() {
        this.panel.clear();
        this.desktop.clear();
        addText(I18n.I.noData());
    }

    private HTML addHtml(String html) {
        final HTML label = new HTML(html);
        label.setStyleName("mm-convensys");
        this.panel.add(label);
        return label;
    }

    private Label addText(String text) {
        final Label label = new Label(text);
        label.setStyleName("mm-convensys");
        this.panel.add(label);
        return label;
    }

    private DesktopIcon<Object> createDesktopIcon(String title, String href) {
        return new DesktopIcon<>("mm-desktopIcon-pdf", new String[]{title}, href); // $NON-NLS$
    }
}
