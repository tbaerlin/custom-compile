/*
 * DelegatingPageController.java
 *
 * Created on 19.08.2008 17:28:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PdfParameterSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PdfUriSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class DelegatingPageController extends AbstractPageController implements HasObjectInfoSnippet {
    protected SnippetsController delegate;

    protected DelegatingPageController(ContentContainer contentContainer) {
        super(contentContainer, null);
    }

    protected DelegatingPageController(ContentContainer contentContainer, DmxmlContext context) {
        super(contentContainer, context);
    }

    public void refresh() {
        this.delegate.refresh();
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        this.delegate.onPlaceChange(event);
    }

    @Override
    public void activate() {
        if (this.delegate == null) {
            initDelegate();
            assert delegate != null;
        }
        this.delegate.activate();
    }

    @Override
    public void deactivate() {
        this.delegate.deactivate();
    }

    protected <V> V getSnippet(String key) {
        return (V) this.delegate.getSnippet(key);
    }

    public List<Snippet> getSnippetsByConfig(String key, String value) {
        assert value != null;
        final List<Snippet> result = new ArrayList<Snippet>();
        if (this.delegate == null) {
            return result;
        }
        for (Snippet  snippet : getSnippets()) {
            if (value.equals(snippet.getConfiguration().getString(key))) {
                result.add(snippet);
            }
        }
        return result;
    }

    public List<Snippet> getSnippets() {
        return this.delegate.getSnippets();
    }

    public boolean isDelegateInitialized() {
        return this.delegate != null;
    }

    protected abstract void initDelegate();

    public String getPrintHtml() {
        return this.delegate.getPrintHtml();
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        for (Snippet snippet : getSnippets()) {
            if (snippet instanceof PdfUriSnippet) {
                final PdfUriSnippet pdfUriSnippet = (PdfUriSnippet) snippet;
                if (pdfUriSnippet.isTopToolbarUri()) {
                    return pdfUriSnippet.getPdfOptionSpec();
                }
            }
        }
        return null;
    }

    @Override
    public void addPdfPageParameters(Map<String, String> mapParameters) {
        for (Snippet snippet : getSnippets()) {
            if (snippet instanceof PdfParameterSnippet) {
                ((PdfParameterSnippet) snippet).addPdfSnippetParameters(mapParameters);
            }
        }
    }

    @Override
    public Snippet getObjectInfoSnippet() {
        final List<Snippet> snippets = getSnippetsByConfig("isObjectInfo", "true"); // $NON-NLS$
        if (!snippets.isEmpty()) {
            return snippets.get(0);
        }
        return null;
    }
}