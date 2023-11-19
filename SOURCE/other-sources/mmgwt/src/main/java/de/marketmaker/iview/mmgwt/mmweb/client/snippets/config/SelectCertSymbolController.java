package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import de.marketmaker.iview.dmxml.CERFinder;
import de.marketmaker.iview.dmxml.CERFinderElement;
import de.marketmaker.iview.dmxml.SearchTypeCount;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 18.10.2010 13:11:52
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
class SelectCertSymbolController extends AbstractSelectSymbolController<CERFinder> {

    private String category = null;

    public SelectCertSymbolController() {
        super("CER_Finder"); // $NON-NLS$
        super.setTypes(new String[] {"CER"}); // $NON-NLS$
        setWithMsc(false);
        this.block.setParameter("providerPreference", SessionData.INSTANCE.getGuiDefValue("providerPreference")); // $NON-NLS$
    }

    @Override
    protected boolean doUpdateModel() {
        this.dtm = DefaultTableDataModel.create(getResult().getElement(),
                new AbstractRowMapper<CERFinderElement>() {
                    public Object[] mapRow(CERFinderElement cerFinderElement) {
                        final QuoteWithInstrument qwi = toQuoteWithInstrument(cerFinderElement);
                        return new Object[]{
                                null,
                                qwi,
                                cerFinderElement.getInstrumentdata().getIsin(),
                                cerFinderElement.getInstrumentdata().getWkn(),
                                qwi.getQuoteData().getCurrencyIso(),
                                qwi.getQuoteData().getMarketVwd()
                        };
                    }
                });
        return true;

    }

    @Override
    protected List<SearchTypeCount> getTypecount() {
        final SearchTypeCount count = new SearchTypeCount();
        if (this.block.isResponseOk()) {
            count.setType("CER"); // $NON-NLS$
            count.setValue(this.block.getResult().getCount());
        }
        final List<SearchTypeCount> list = new ArrayList<SearchTypeCount>();
        list.add(count);
        return list;
    }

    @Override
    public QuoteWithInstrument getResultQwi(int n) {
        if (this.block.isResponseOk()) {
            final CERFinderElement element = this.block.getResult().getElement().get(n);
            return toQuoteWithInstrument(element);
        }
        return null;
    }

    public void setTypes(String[] types) {
        if (types != null && types.length > 0) {
            this.category = types[0];
        } else {
            this.category = null;
        }
    }

    protected void reset(String query) {
        this.block.removeAllParameters();
        this.block.setParameter("query", getQuery(query)); // $NON-NLS-0$
        this.getPagingFeature().resetPaging();
        this.navItemSelectionModel.setSelected(this.navItemSpecRoot.getChildren().get(0), false);
    }

    private String getQuery(String value) {
        StringBuilder sb = new StringBuilder();
        sb.append("isin@wkn@name=='").append(value).append("'"); // $NON-NLS$
        if (this.category != null) {
            sb.append(" && certificateType=='").append(this.category).append("'"); // $NON-NLS$
        }
        return sb.toString();
    }

    protected QuoteWithInstrument toQuoteWithInstrument(CERFinderElement e) {
        return new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
    }
}
