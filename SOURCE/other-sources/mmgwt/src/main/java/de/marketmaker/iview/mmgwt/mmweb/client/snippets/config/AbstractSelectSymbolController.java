package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import de.marketmaker.iview.dmxml.BlockListType;
import de.marketmaker.iview.dmxml.MSCBasicSearchElement;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractSearchController;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebServiceAsyncProxy;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * Created on 18.10.2010 13:07:23
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public abstract class AbstractSelectSymbolController<V extends BlockListType> extends AbstractSearchController<V>
        implements SelectSymbolFormControllerInterface {

    private static final int DEFAULT_COUNT = 8;

    private SelectSymbolForm view;

    protected AbstractSelectSymbolController(String block) {
        super(null, block, DEFAULT_COUNT);
    }

    public void setView(SelectSymbolForm view) {
        this.view = view;
    }

    @Override
    protected String getViewGroup() {
        return "symbolsearch"; // $NON-NLS$
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        // unsupported
    }

    public void search(String s) {
        reset(s);
        reload();
    }

    protected abstract boolean doUpdateModel();

    protected void updateView() {
        if (this.view != null) {
            this.view.updateViewNames();
            if (this.dtm != null) {
                this.view.show(this.dtm);
            }
        }
    }

    protected QuoteWithInstrument toQuoteWithInstrument(MSCBasicSearchElement e) {
        return new QuoteWithInstrument(e.getInstrumentdata(), e.getReferenceQuotedata());
    }

    public abstract QuoteWithInstrument getResultQwi(int n);

    @Override
    protected SelectionHandler<NavItemSpec> getSelectionHandler() {
        return new SelectionHandler<NavItemSpec>() {
            @Override
            public void onSelection(SelectionEvent<NavItemSpec> event) {
                onViewChanged();
            }
        };
    }

    @Override
    protected boolean needsIndexedViewSelectionModel() {
        return true;
    }

    @Override
    public void cancelPendingRequests() {
        MmwebServiceAsyncProxy.cancelPending();
    }
}