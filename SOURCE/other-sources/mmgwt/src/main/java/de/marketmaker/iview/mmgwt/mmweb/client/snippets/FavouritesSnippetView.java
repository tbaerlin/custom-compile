/*
 * FavouritesSnippetView.java
 *
 * Created on 12.11.2015 13:12
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.List;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardSnippetErrorUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ScrollPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouriteItemsStore;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouriteItemsStores;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouritesWidget;

/**
 * @author mdick
 */
public class FavouritesSnippetView extends SnippetView<FavouritesSnippet> {
    private final FavouritesWidget favouritesWidget = new FavouritesWidget()
            .withConfigChangedHandler()
            .withSafeHtmlIfAllStoresEmpty(DashboardSnippetErrorUtil.getErrorHtml(
                    I18n.I.noDataAvailable(), I18n.I.noDataAvailable()));

    final ScrollPanel layout = new ScrollPanel(this.favouritesWidget);

    public FavouritesSnippetView(FavouritesSnippet snippet) {
        super(snippet);
        setTitle(getTitleFromConfig());
        this.layout.addStyleName("mm-snippet-div");
    }

    @Override
    protected void onContainerAvailable() {
        getContainer().setContentWidget(this.layout);
    }

    public void updateView(List<String> configuredStores) {
        this.favouritesWidget.setConfiguredStores(configuredStores, false);
        this.favouritesWidget.update();

        if (configuredStores.size() == 1) {
            for (FavouriteItemsStore store : FavouriteItemsStores.get()) {
                if (StringUtil.equals(configuredStores.get(0), store.getName())) {
                    setTitle(getTitleFromConfig() + " - " + store.getLabel()); // $NON-NLS$
                }
            }
        }
    }

    protected void destroyView() {
        this.favouritesWidget.release();
    }

    public String getTitleFromConfig() {
        return getConfiguration().getString("title");  // $NON-NLS$
    }
}
