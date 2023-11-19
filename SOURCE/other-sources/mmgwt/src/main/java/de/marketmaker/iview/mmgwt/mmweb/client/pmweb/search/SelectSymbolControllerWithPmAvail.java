/*
 * SelectSymbolControllerWithPmAvail.java
 *
 * Created on 21.02.2012
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search;

import de.marketmaker.iview.dmxml.MSCBasicSearchElement;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolController;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.SearchMethods.UpdateSearchElementStateCallback;

/**
 * @author Markus Dick
 */
public class SelectSymbolControllerWithPmAvail extends SelectSymbolController {
    private List<CombinedSearchElement> combinedSearchElements = null;
    private final Set<ShellMMType> pmTypesAvailableForOrdering;

    private final UpdateSearchElementStateCallback callback = new UpdateSearchElementStateCallback() {
        @Override
        public void onSuccess(List<CombinedSearchElement> searchElements) {
            onAny(searchElements);
        }

        @Override
        public void onFailure(List<CombinedSearchElement> searchElements) {
            onAny(searchElements);
        }

        private void onAny(List<CombinedSearchElement> searchElements) {
            SelectSymbolControllerWithPmAvail.this.combinedSearchElements = searchElements;
            SelectSymbolControllerWithPmAvail.super.onResult();
        }
    };

    public SelectSymbolControllerWithPmAvail(Set<ShellMMType> pmTypesAvailableForOrdering) {
        super();
        this.pmTypesAvailableForOrdering = pmTypesAvailableForOrdering;
    }

    @Override
    protected boolean doUpdateModel() {
        this.dtm = DefaultTableDataModel.create(this.combinedSearchElements,
                new AbstractRowMapper<CombinedSearchElement>() {
                    public Object[] mapRow(CombinedSearchElement element) {
                        final QuoteWithInstrument qwi = toQuoteWithInstrument(element.getMscBasicSearchElement());

                        return new Object[]{
                                CombinedSearchElement.State.AVAILABLE.equals(element.getShellMmInfoState()),
                                qwi,
                                qwi.getInstrumentData().getIsin(),
                                qwi.getInstrumentData().getWkn(),
                                qwi.getQuoteData().getCurrencyIso(),
                                qwi.getQuoteData().getMarketVwd(),
                                element.getShellMMInfo().getTyp(),
                                element.getShellMmInfoState()
                        };
                    }
                });
        return true;
    }

    @Override
    protected void onResult() {
        if(!this.block.isResponseOk()) {
            this.combinedSearchElements = null;
            super.onResult();
            return;
        }

        issuePmSearch();
    }

    private void issuePmSearch() {
        final ArrayList<CombinedSearchElement> searchElements = new ArrayList<CombinedSearchElement>();

        for(MSCBasicSearchElement element : getResult().getElement()) {
            final CombinedSearchElement combinedSearchElement = new CombinedSearchElement(element);
            searchElements.add(combinedSearchElement);
        }

        SearchMethods.INSTANCE.updateSearchElementState(searchElements, this.pmTypesAvailableForOrdering, this.callback);
    }
}
