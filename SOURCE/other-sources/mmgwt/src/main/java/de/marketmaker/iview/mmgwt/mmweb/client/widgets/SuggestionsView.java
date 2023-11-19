/*
 * SuggestionsView.java
 *
 * Created on 11.08.2014 16:27
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import de.marketmaker.itools.gwtutil.client.widgets.PopupPanelFix;
import java.util.function.Supplier;

import java.util.Collection;

/**
* @author mdick
*/
public class SuggestionsView extends SuggestBox.DefaultSuggestionDisplay {
    private final Supplier<Integer> zIndexSupplier;

    public SuggestionsView() {
        this.zIndexSupplier = null;
    }

    @SuppressWarnings("unused")
    public SuggestionsView(Supplier<Integer> zIndexSupplier) {
        this.zIndexSupplier = zIndexSupplier;
    }

    @Override
    protected PopupPanel createPopup() {
        final PopupPanel p = super.createPopup();
        PopupPanelFix.addFrameDummyForSuggestPopup(p);
        return p;
    }

    public boolean isItemSelected() {
        return getCurrentSelection() != null;
    }

    @Override
    protected void showSuggestions(SuggestBox suggestBox, Collection<? extends SuggestOracle.Suggestion> suggestions, boolean isDisplayStringHTML, boolean isAutoSelectEnabled, SuggestBox.SuggestionCallback callback) {
        super.showSuggestions(suggestBox, suggestions, isDisplayStringHTML, isAutoSelectEnabled, callback);
        if(this.zIndexSupplier != null) {
            getPopupPanel().getElement().getStyle().setZIndex(this.zIndexSupplier.get());
        }
    }
}
