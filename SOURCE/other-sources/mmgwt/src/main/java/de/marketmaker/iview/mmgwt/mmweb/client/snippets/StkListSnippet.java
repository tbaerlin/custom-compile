/*
 * StkListSnippet.java
 *
 * Created on 2/13/15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.STKFinder;
import de.marketmaker.iview.dmxml.STKFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Willenbrock
 */
@NonNLS
public class StkListSnippet extends GenericListSnippet<StkListSnippet> {

    public static class Class extends SnippetClass {
        public Class() {
            super("StkList");
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new StkListSnippet(context, config);
        }
    }

    protected StkListSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config, "STK_Finder", "name");

        final String providerPreference = SessionData.INSTANCE.getGuiDefValue("providerPreference");
        this.block.setParameter("providerPreference", providerPreference);

        final SnippetTableView<StkListSnippet> snippetTableView = new SnippetTableView<>(this, new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.name(), -1f, TableCellRenderers.QUOTELINK_42, "name")}));

        this.setView(new GenericListSnippetView<>(this, snippetTableView));

        setIssuerField("issuername");
        setIssuerLabel(I18n.I.issuer());
        setTypeField("typeKey");
        setTypeLabel(I18n.I.type());
    }


    @Override
    protected boolean isResponseOk() {
        return true;
    }

    @Override
    protected boolean isMetaDataEnabled() {
        return false;
    }

    @Override
    protected void enableMetaData(boolean enabled) {
        // no metadata
    }

    @Override
    protected void updateTableData() {
        final STKFinder stkFinder = (STKFinder) this.block.getResult();
        final List<STKFinderElement> listElements = stkFinder.getElement();

        final List<Object[]> list = new ArrayList<>();
        for (STKFinderElement e : listElements) {
            final QuoteWithInstrument qwi = new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
            list.add(new Object[]{qwi});
        }

        final TableDataModel tdm = DefaultTableDataModel.create(list).withSort(stkFinder.getSort());
        if (tdm == DefaultTableDataModel.NULL) {
            setNull();
        }
        else {
            getView().update(tdm, Integer.parseInt(stkFinder.getOffset()), Integer.parseInt(stkFinder.getCount()), Integer.parseInt(stkFinder.getTotal()));
        }
    }

    @Override
    protected boolean hasSymbol(String qid) {
        if (qid == null) {
            return false;
        }
        final STKFinder bndFinder = (STKFinder) this.block.getResult();
        final List<STKFinderElement> listElements = bndFinder.getElement();
        for (STKFinderElement e : listElements) {
            if (qid.equals(e.getQuotedata().getQid())) {
                return true;
            }
        }
        return false;
    }
}
