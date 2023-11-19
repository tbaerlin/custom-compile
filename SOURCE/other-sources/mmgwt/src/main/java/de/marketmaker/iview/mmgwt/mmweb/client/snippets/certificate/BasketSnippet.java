/*
 * PriceListSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.certificate;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.IdentifierData;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ListDetailsHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BlockPipe;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BlockPipeResult;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Lösch
 */

public class BasketSnippet extends AbstractSnippet<BasketSnippet, SnippetTableView<BasketSnippet>>
        implements SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("Basket", I18n.I.certBasket()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new BasketSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("colSpan", 3); // $NON-NLS$
        }
    }

    private DmxmlContext.Block<MSCListDetails> block;
    private final DmxmlContext.Block<MSCQuoteMetadata> metadataBlock;
    private ListDetailsHelper listDetailsHelper;
    private BlockPipe pipe;

    private BasketSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("MSC_PriceDataMulti"); // $NON-NLS-0$
        this.block.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$

        this.metadataBlock = createBlock("MSC_QuoteMetadata"); // $NON-NLS-0$

        this.pipe = new BlockPipe(this.metadataBlock, context, "symbol", true, new BlockPipeResult<MSCQuoteMetadata>() { // $NON-NLS-0$
            public String[] getResult(DmxmlContext.Block<MSCQuoteMetadata> mscQuoteMetadataBlock) {
                final List<String> symbols = new ArrayList<>();
                final List<IdentifierData> underlyings = mscQuoteMetadataBlock.getResult().getUnderlying();
                for (IdentifierData underlying : underlyings) {
                    symbols.add(underlying.getQuotedata().getQid());
                }
                return symbols.toArray(new String[symbols.size()]);
            }
        }).setNext(this.block);

        this.setView(new SnippetTableView<>(this, getListDetailsHelper().createTableColumnModel()));
    }

    ListDetailsHelper getListDetailsHelper() {
        if (this.listDetailsHelper == null) {
            this.listDetailsHelper = new ListDetailsHelper(ListDetailsHelper.LinkType.NAME,
                    getConfiguration().getBoolean("displayVolume", true), false); // $NON-NLS-0$
        }
        return this.listDetailsHelper;
    }

    public void destroy() {
        destroyBlock(this.block);
        destroyBlock(this.metadataBlock);
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.metadataBlock.setParameter("symbol", symbol); // $NON-NLS-0$
        this.pipe.issueRequest(new AsyncCallback<ResponseType>() {
            public void onSuccess(ResponseType responseType) {
                ackParametersChanged();
            }

            public void onFailure(Throwable throwable) {
            }
        });
    }

    public void updateView() {
        if (!block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        final MSCListDetails data = this.block.getResult();
        final DefaultTableDataModel dtm = getTableDataModel(data.getElement());
        getView().update(dtm);
    }

    private DefaultTableDataModel getTableDataModel(List<MSCListDetailElement> elements) {
        final ListDetailsHelper listDetailsHelper = getListDetailsHelper();
        final DefaultTableDataModel dtm = listDetailsHelper.createTableDataModel(elements.size());
        final TrendBarData tbd = TrendBarData.create(this.block.getResult());
        int row = 0;
        for (MSCListDetailElement e : elements) {
            final Price price = Price.create(e);
            listDetailsHelper.addRow(dtm, row, e.getInstrumentdata(), e.getQuotedata(), tbd, price);
            row++;
        }
        return dtm;
    }
}
