package de.marketmaker.iview.mmgwt.mmweb.client.snippets.certificate;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.iview.dmxml.CERDetailedStaticData;
import de.marketmaker.iview.dmxml.IdentifierData;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * CerUnderlyingSnippet.java
 * Created on Aug 12, 2009 7:24:06 AM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class CerUnderlyingSnippet extends AbstractSnippet<CerUnderlyingSnippet, CerUnderlyingSnippetView>
        implements SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("certificate.Underlying"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new CerUnderlyingSnippet(context, config);
        }
    }

    public enum MODE {
        SINGLE_UNDERLYING,
        MULTI_UNDERLYING,
        MULTI_ASSET_NAME
    }

    private final PriceSnippet priceUnderlying;
    private final DmxmlContext.Block<MSCQuoteMetadata> metadataBlock;
    private final DmxmlContext.Block<CERDetailedStaticData> staticBlock;

    public CerUnderlyingSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);

        this.metadataBlock = createBlock("MSC_QuoteMetadata"); // $NON-NLS$
        this.staticBlock = createBlock("CER_DetailedStaticData"); // $NON-NLS$

        final SnippetConfiguration priceConf = new SnippetConfiguration()
                .with(configuration.getCopyOfParameters())
                .with("title", configuration.getString("priceTitle", I18n.I.underlyingPrice()))  // $NON-NLS$
                .with("symbol", SymbolSnippet.SYMBOL_UNDERLYING); // $NON-NLS$

        this.priceUnderlying = new PriceSnippet(context, priceConf);        

        final SnippetTableView<CerUnderlyingSnippet> staticDataView = SnippetTableView.create(this, new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.type(), 0.3f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn(I18n.I.value(), 0.7f, SessionData.INSTANCE.isAnonymous() ? TableCellRenderers.STRING_RIGHT : TableCellRenderers.DEFAULT_RIGHT) 
        }, false));
        final SnippetTableView<CerUnderlyingSnippet> multiUnderlyingView = SnippetTableView.create(this, new DefaultTableColumnModel(new TableColumn[]{
                createName(0.6f),
                new TableColumn("ISIN", 0.4f, TableCellRenderers.DEFAULT_RIGHT) // $NON-NLS-0$
        }));
        final SnippetTableView<CerUnderlyingSnippet> multiAssetNameView = SnippetTableView.create(this, new DefaultTableColumnModel(new TableColumn[]{
                createName(1f)                
        }, false));

        setView(new CerUnderlyingSnippetView(this, staticDataView,
                this.priceUnderlying.getView(), multiUnderlyingView, multiAssetNameView));
    }

    public void destroy() {
        destroyBlock(this.metadataBlock);
        destroyBlock(this.staticBlock);
    }

    private TableColumn createName(float width) {
        final TableCellRenderer renderer;
        if (SessionData.INSTANCE.isAnonymous()) {
            renderer = TableCellRenderers.STRING;
        } else {
            renderer = TableCellRenderers.DEFAULT;
        }
        return new TableColumn(I18n.I.name(), width, renderer); 
    }

    public void updateView() {
        this.priceUnderlying.updateView();
        final List<IdentifierData> underlyings = this.metadataBlock.getResult().getUnderlying();
        if (underlyings == null || underlyings.size() <= 1) {
            final CERDetailedStaticData data = this.staticBlock.getResult();
            final IdentifierData benchmark = data.getBenchmark();
            final List<Object[]> list = new ArrayList<>();
            if (benchmark == null) {
                list.add(new Object[]{data.getMultiassetName()});
                getView().updateView(MODE.MULTI_ASSET_NAME, DefaultTableDataModel.create(list));
            }
            else {
                list.add(new Object[]{I18n.I.name(), SessionData.INSTANCE.isAnonymous() ? 
                        benchmark.getInstrumentdata().getName() : new QuoteWithInstrument(benchmark.getInstrumentdata(), benchmark.getQuotedata())});
                if(SessionData.INSTANCE.isShowIsin()) {
                    add(list, "ISIN", benchmark.getInstrumentdata().getIsin()); // $NON-NLS-0$
                }
                if(SessionData.INSTANCE.isShowWkn()) {
                    add(list, "WKN", benchmark.getInstrumentdata().getWkn()); // $NON-NLS-0$
                }
                final InstrumentTypeEnum type = InstrumentTypeEnum.valueOf(benchmark.getInstrumentdata().getType());
                add(list, I18n.I.instrumentTypeAbbr(), type == null ? null : type.getDescription());
                getView().updateView(MODE.SINGLE_UNDERLYING, DefaultTableDataModel.create(list));
            }
        }
        else if (underlyings.size() > 1) {
            final List<Object[]> list = new ArrayList<>();
            for (IdentifierData underlying : underlyings) {
                final InstrumentData instrumentData = underlying.getInstrumentdata();
                if (SessionData.INSTANCE.isAnonymous()) {
                    list.add(new Object[]{instrumentData.getName(), instrumentData.getIsin()});
                } else {
                    list.add(new Object[]{new QuoteWithInstrument(instrumentData, underlying.getQuotedata()), instrumentData.getIsin()});
                }
            }
            getView().updateView(MODE.MULTI_UNDERLYING, DefaultTableDataModel.create(list));
        }
    }

    private void add(List<Object[]> list, String title, String value) {
        if (value != null) {
            list.add(new Object[]{title, value});
        }
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.metadataBlock.setParameter("symbol", symbol); // $NON-NLS-0$
        this.staticBlock.setParameter("symbol", symbol); // $NON-NLS-0$
        this.priceUnderlying.setSymbol(type, symbol, null);
    }
}
