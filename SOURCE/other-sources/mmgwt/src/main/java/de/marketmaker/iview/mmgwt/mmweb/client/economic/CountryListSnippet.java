/*
 * PdfSnippet.java
 *
 * Created on 17.06.2008 12:35:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.economic;

import de.marketmaker.iview.dmxml.MERItemsMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Ulrich Maurer
 */
public class CountryListSnippet extends AbstractSnippet<CountryListSnippet, CountryListSnippetView> {
    public static class Class extends SnippetClass {
        public Class() {
            super("economic.CountryList", I18n.I.countries()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new CountryListSnippet(context, config);
        }
    }

    protected DmxmlContext.Block<MERItemsMetadata> block;

    private TypeListSnippet typeListSnippet = null;

    private CountryListSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("MER_ItemsMetadata"); // $NON-NLS-0$
        setView(new CountryListSnippetView(this));
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            getView().setMessage(I18n.I.noDataAvailable(), false); 
        }
        else if (this.block.isEnabled()) {
            getView().update(this.block.getResult().getCountry().getElement());
            this.block.disable(); // countries never change, so don't request again
        }
        
        onCountrySelectionChanged();
    }

    public void onControllerInitialized() {
        this.typeListSnippet = (TypeListSnippet) super.contextController.getSnippet("tl"); // $NON-NLS-0$
    }

    protected void onCountrySelectionChanged() {
        this.typeListSnippet.setCountries(getView().getChecked());
    }

}
