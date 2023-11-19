/*
 * FavouritesSnippet.java
 *
 * Created on 12.11.2015 13:12
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouriteItemsStore;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouriteItemsStores;

/**
 * @author mdick
 */
public class FavouritesSnippet extends AbstractSnippet<FavouritesSnippet, FavouritesSnippetView> {
    public static class Class extends SnippetClass {
        public Class() {
            super("Favourites", I18n.I.favorites()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new FavouritesSnippet(context, config);
        }
    }

    public FavouritesSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
        this.setView(new FavouritesSnippetView(this));
        updateView();
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void configure(Widget triggerWidget) {
        final Menu menu = new Menu();

        for (FavouriteItemsStore itemStore : FavouriteItemsStores.get()) {
            final MenuItem mi = new MenuItem(itemStore.getLabel())
                    .withClickHandler(e -> {
                        setConfigured(itemStore, !isConfigured(itemStore));
                        ackParametersChanged();
                    });
            if (isConfigured(itemStore)) {
                mi.withIcon("pm-box-checked"); // $NON-NLS$
            }
            menu.add(mi);
        }

        menu.show(triggerWidget);
    }

    @Override
    public void updateView() {
        final FavouriteItemsStore[] favouriteItemsStores = FavouriteItemsStores.get();
        final ArrayList<String> configuredStores = new ArrayList<>(favouriteItemsStores.length);
        for (FavouriteItemsStore store : favouriteItemsStores) {
            if (isConfigured(store)) {
                configuredStores.add(store.getName());
            }
        }
        getView().updateView(configuredStores);
    }

    public void setConfigured(FavouriteItemsStore itemStore, boolean configured) {
        getConfiguration().put(itemStore.getName(), configured);
    }

    public boolean isConfigured(FavouriteItemsStore itemStore) {
        return getConfiguration().getBoolean(itemStore.getName(), true);
    }

    @Override
    public void destroy() {
        getView().destroyView();
    }
}
