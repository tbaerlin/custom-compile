/*
 * FavouritesPopup.java
 *
 * Created on 13.11.2015 11:11
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.PopupPanelFix;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingPopupPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ToolbarPopupPanel;

/**
 * @author mdick
 */
public class FavouritesPopup extends FloatingPopupPanel {
    public FavouritesPopup() {
        super(true);
        setStyleName(ToolbarPopupPanel.TOOLBAR_POPUP_STYLE);
        addStyleName("mm-favourites-popup"); // $NON-NLS$
        setWidget(new FavouritesWidget().withConfigChangedHandler()
                .withDoAfterGoto(this::hide)
                .withDoAfterRemove(() -> {
                    if (FavouriteItemsStores.isAllStoresEmpty()) {
                        this.hide();
                    }
                }));
        addAttachHandler(event -> PopupPanelFix.addFrameDummy(this));
    }

    @Override
    public void showNearby(UIObject target, Element selectedElement) {
        if (!FavouriteItemsStores.isAllStoresEmpty()) {
            super.showNearby(target, selectedElement);
            return;
        }
        Firebug.info("<FavouritesPopup.showNearby> all favourite items stores are empty. Popup will not be shown.");
    }
}
