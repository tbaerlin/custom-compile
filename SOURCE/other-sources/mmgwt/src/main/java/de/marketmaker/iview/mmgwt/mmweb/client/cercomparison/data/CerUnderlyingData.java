package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data;

import de.marketmaker.iview.dmxml.CERDetailedStaticData;
import de.marketmaker.iview.dmxml.IdentifierData;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCPriceDataExtended;
import de.marketmaker.iview.dmxml.MSCPriceDataExtendedElement;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DecimalCellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.QwiCellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RendererContext;
import de.marketmaker.iview.mmgwt.mmweb.client.table.StringCellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 08.09.2010 14:19:44
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class CerUnderlyingData implements CerData {
    private static final String[] NAMES;

    static {
        final ArrayList<String> n = new ArrayList<String>();
        n.add(I18n.I.name());
        if(SessionData.INSTANCE.isShowIsin()) {
            n.add("ISIN"); // $NON-NLS$
        }
        if(SessionData.INSTANCE.isShowWkn()) {
            n.add("WKN"); // $NON-NLS$
        }
        n.add(I18n.I.instrumentTypeAbbr());
        n.add(I18n.I.market());
        n.add(I18n.I.currentPrice());
        n.add( I18n.I.dateTime());
        n.add(I18n.I.volume());
        n.add(I18n.I.changeNetAbbr());
        n.add(I18n.I.changePercentAbbr());
        n.add(I18n.I.bid());
        n.add(I18n.I.ask());
        n.add(I18n.I.vwap());
        n.add(I18n.I.twas());

        NAMES = n.toArray(new String[n.size()]);
    }

    private final DmxmlContext.Block<CERDetailedStaticData> blockCerStatic;
    private final DmxmlContext.Block<MSCQuoteMetadata> blockMetadata;
    private final DmxmlContext.Block<MSCPriceDataExtended> blockPriceUnderlying;


    public CerUnderlyingData(DmxmlContext.Block<CERDetailedStaticData> blockCerStatic,
                             DmxmlContext.Block<MSCQuoteMetadata> blockMetadata,
                             DmxmlContext.Block<MSCPriceDataExtended> blockPriceUnderlying) {
        this.blockCerStatic = blockCerStatic;
        this.blockMetadata = blockMetadata;
        this.blockPriceUnderlying = blockPriceUnderlying;

    }

    public CellData[] getValues() {
        final CellData[] values = new CellData[NAMES.length];
        int i = 0;
        if (this.blockMetadata.isResponseOk() && this.blockCerStatic.isResponseOk()) {
            final CERDetailedStaticData data = this.blockCerStatic.getResult();
            final List<IdentifierData> underlyings = this.blockMetadata.getResult().getUnderlying();
            if (underlyings == null || underlyings.size() <= 1) {
                final IdentifierData benchmark = data.getBenchmark();
                if (benchmark == null) {
                    values[i++] = createField(data.getMultiassetName());
                }
                else {
                    values[i++] = createQwiField(new QuoteWithInstrument(benchmark.getInstrumentdata(), benchmark.getQuotedata()));
                    if(SessionData.INSTANCE.isShowIsin()) {
                        values[i++] = createField(benchmark.getInstrumentdata().getIsin());
                    }
                    if(SessionData.INSTANCE.isShowWkn()) {
                        values[i++] = createField(benchmark.getInstrumentdata().getWkn());
                    }
                    final InstrumentTypeEnum type = InstrumentTypeEnum.valueOf(benchmark.getInstrumentdata().getType());
                    values[i++] = createField(type == null ? null : type.getDescription());

                    if (this.blockPriceUnderlying.isResponseOk() && this.blockPriceUnderlying.getResult().getElement().size() > 0) {
                        final MSCPriceDataExtendedElement priceData = this.blockPriceUnderlying.getResult().getElement().get(0);
                        values[i++] = createField(priceData.getQuotedata().getMarketName());
                        values[i++] = createPriceField(priceData.getPricedata().getPrice());
                        values[i++] = createDateField(priceData.getPricedata().getDate());
                        values[i++] = createField(priceData.getPricedata().getVolume());
                        values[i++] = createChangeNetField(priceData.getPricedata().getChangeNet());
                        values[i++] = createChangePercentField(priceData.getPricedata().getChangePercent());
                        values[i++] = createPriceField(priceData.getPricedata().getBid());
                        values[i++] = createPriceField(priceData.getPricedata().getAsk());
                        values[i++] = createPriceField(priceData.getPricedataExtended().getVwap());
                        values[i++] = createPriceField(priceData.getPricedataExtended().getTwas());
                    }
                }
            }
            else if (underlyings.size() > 1) {
                final StringBuilder names = new StringBuilder();
                for (IdentifierData underlying : underlyings) {
                    final InstrumentData instrumentData = underlying.getInstrumentdata();
                    String symbol = SessionData.INSTANCE.isShowIsin() ? instrumentData.getIsin() : instrumentData.getWkn();
                    names.append(instrumentData.getName())
                            .append(" / ")
                            .append(symbol)
                            .append("<br/>"); // $NON-NLS$

                }
                values[i++] = createField(names.toString());
            }

            fillWithEmptyValues(values, i);
        }
        return values;
    }

    private void fillWithEmptyValues(CellData[] values, int start) {
        for (int i = start; i < values.length; i++) {
            values[i] = EMPTY_CELL_DATA;
        }
    }

    public String[] getNames() {
        return NAMES;
    }

    private StringCellData createField(String text) {
        return new StringCellData(text);
    }

    private StringCellData createHtmlField(String text) {
        return new StringCellData(text, true);
    }

    private DecimalCellData createField(Long number) {
        return new DecimalCellData(Renderer.VOLUME, number == null ? null : number.toString(), CellData.Sorting.ASC);
    }

    private DecimalCellData createChangePercentField(String change) {
        return new DecimalCellData(Renderer.CHANGE_PERCENT, change, CellData.Sorting.NONE, true);
    }

    private DecimalCellData createChangeNetField(String changeNet) {
        return new DecimalCellData(Renderer.CHANGE_PRICE, changeNet, CellData.Sorting.NONE, true);
    }

    private DecimalCellData createPriceField(String price) {
        return new DecimalCellData(Renderer.PRICE, price, CellData.Sorting.ASC);
    }

    private StringCellData createDateField(String date) {
        StringBuffer sb = new StringBuffer();
        TableCellRenderers.DATE_AND_TIME.render(date, sb, new RendererContext(null, null));
        return createHtmlField(sb.toString());
    }

    private QwiCellData createQwiField(QuoteWithInstrument quoteWithInstrument) {
        return new QwiCellData(quoteWithInstrument);
    }
}
