package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import de.marketmaker.iview.dmxml.MSCBasicSearch;
import de.marketmaker.iview.dmxml.MSCBasicSearchElement;
import de.marketmaker.iview.dmxml.SearchTypeCount;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;

import java.util.List;

/**
 * Created on 18.10.2010 13:11:52
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class SelectSymbolController extends AbstractSelectSymbolController<MSCBasicSearch> {
    public SelectSymbolController() {
        super("MSC_BasicSearch"); // $NON-NLS$
    }

    @Override
    protected boolean doUpdateModel() {
        this.dtm = DefaultTableDataModel.create(getResult().getElement(),
                new AbstractRowMapper<MSCBasicSearchElement>() {
                    public Object[] mapRow(MSCBasicSearchElement element) {
                        final QuoteWithInstrument qwi = toQuoteWithInstrument(element);
                        return new Object[]{
                                null,
                                qwi,
                                element.getInstrumentdata().getIsin(),
                                element.getInstrumentdata().getWkn(),
                                qwi.getQuoteData().getCurrencyIso(),
                                qwi.getQuoteData().getMarketVwd(),
                        };
                    }
                });
        return true;
    }

    @Override
    protected QuoteWithInstrument toQuoteWithInstrument(MSCBasicSearchElement e) {
        return new QuoteWithInstrument(e.getInstrumentdata(), e.getReferenceQuotedata());
    }

    @Override
    protected List<SearchTypeCount> getTypecount() {
        return getResult().getTypecount();
    }

    @Override
    public QuoteWithInstrument getResultQwi(int n) {
        if (this.block.isResponseOk()) {
            final MSCBasicSearchElement element = this.block.getResult().getElement().get(n);
            return toQuoteWithInstrument(element);
        }
        return null;
    }
}
