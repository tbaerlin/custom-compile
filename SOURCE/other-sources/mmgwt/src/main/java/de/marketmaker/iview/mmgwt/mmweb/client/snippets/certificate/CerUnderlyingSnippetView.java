package de.marketmaker.iview.mmgwt.mmweb.client.snippets.certificate;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceSnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

/**
 * CerUnderlyingSnippetView.java
 * Created on Aug 12, 2009 7:24:41 AM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class CerUnderlyingSnippetView extends SnippetView<CerUnderlyingSnippet> {

    private final SnippetTableView<CerUnderlyingSnippet> staticUnderlying;
    private final PriceSnippetView priceUnderlying;
    private final SnippetTableView<CerUnderlyingSnippet> multiUnderlying;
    private final SnippetTableView<CerUnderlyingSnippet> multiAssetName;

    private final LayoutContainer view0;
    private final LayoutContainer view1;
    private final LayoutContainer view2;

    public CerUnderlyingSnippetView(CerUnderlyingSnippet snippet,
                                    SnippetTableView<CerUnderlyingSnippet> staticUnderlying,
                                    PriceSnippetView priceUnderlying,
                                    SnippetTableView<CerUnderlyingSnippet> multiUnderlying,
                                    SnippetTableView<CerUnderlyingSnippet> multiAssetName) {
        super(snippet);

        this.staticUnderlying = staticUnderlying;
        this.priceUnderlying = priceUnderlying;
        this.multiUnderlying = multiUnderlying;
        this.multiAssetName = multiAssetName;

        this.view0 = createSingleUnderlying();
        this.view1 = createMultiUnderlying();
        this.view2 = createMultiAssetName();
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setHeaderVisible(false);
        this.container.asWidget().addStyleName("mm-contentData"); // $NON-NLS-0$
        this.container.setContentWidget(this.view0);
    }

    private LayoutContainer createSingleUnderlying() {
        final ContentPanel staticPanel = createPanel();
        final ContentPanel pricePanel = createPanel();
        staticPanel.setHeading(getConfiguration().getString("staticTitle", I18n.I.staticDataUnderlying()));  // $NON-NLS-0$
        pricePanel.addStyleName("mm-panel-topBorder"); // $NON-NLS-0$
        this.priceUnderlying.setContainer(pricePanel);
        this.staticUnderlying.setContainer(staticPanel);
        return createContainer(staticPanel, pricePanel);
    }

    private LayoutContainer createMultiUnderlying() {
        final ContentPanel p = createPanel();
        p.setHeading(getConfiguration().getString("multiTitle", I18n.I.underlyings()));  // $NON-NLS-0$
        this.multiUnderlying.setContainer(p);
        return createContainer(p);
    }

    private LayoutContainer createMultiAssetName() {
        final ContentPanel p = createPanel();
        p.setHeading(I18n.I.underlying()); 
        this.multiAssetName.setContainer(p);
        return createContainer(p);
    }

    private LayoutContainer createContainer(ContentPanel... panels) {
        LayoutContainer container = new LayoutContainer();
        for (ContentPanel panel : panels) {
            container.add(panel);
        }
        container.setAutoHeight(true);
        container.setAutoWidth(true);
        return container;
    }

    private ContentPanel createPanel() {
        return new ContentPanel();
    }

    public void updateView(CerUnderlyingSnippet.MODE mode, TableDataModel tdm) {
        switch (mode) {
            case SINGLE_UNDERLYING:
                this.staticUnderlying.update(tdm);
                this.container.setContentWidget(this.view0);
                this.container.layout();
                break;
            case MULTI_UNDERLYING:
                this.multiUnderlying.update(tdm);
                this.container.setContentWidget(this.view1);
                this.container.layout();
                break;
            case MULTI_ASSET_NAME:
                this.multiAssetName.update(tdm);
                this.container.setContentWidget(this.view2);
                this.container.layout();
                break;
        }
    }
}
