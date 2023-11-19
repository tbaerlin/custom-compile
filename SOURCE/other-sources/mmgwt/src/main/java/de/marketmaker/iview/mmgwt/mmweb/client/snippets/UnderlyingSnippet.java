/*
 * NewsHeadlinesSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.FundPriceData;
import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.dmxml.PriceData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceDataType;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.FlexTableDataModelBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModelBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter.LF;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter.formatTime;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer.PERCENT;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer.PRICE;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UnderlyingSnippet extends AbstractSnippet<UnderlyingSnippet, UnderlyingSnippetView>
        implements SymbolSnippet {
    static class Class extends SnippetClass {
        public Class() {
            super("Underlying", I18n.I.underlying()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new UnderlyingSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("width", DEFAULT_SNIPPET_WIDTH); // $NON-NLS$
        }
    }

    private DmxmlContext.Block<MSCPriceData> block;

    private final boolean displayVolume;


    private UnderlyingSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.displayVolume = config.getBoolean("displayVolume", true); // $NON-NLS-0$
        this.setView(new UnderlyingSnippetView(this));

        this.block = createBlock("MSC_PriceData"); // $NON-NLS-0$
//        this.block.setParameter("symbol", config.getString("symbol", "106547.qid"));
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", "underlying(" + symbol + ")"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!block.isResponseOk()) {
            getView().update(createTableDataModel(new QuoteData(), Price.NULL_PRICE_DATA, this.displayVolume));
            return;
        }
        final MSCPriceData data = this.block.getResult();
        final TableDataModel tdm;
        switch (PriceDataType.fromDmxml(data.getPricedatatype())) {
            case STANDARD:
                tdm = createTableDataModel(data.getQuotedata(), data.getPricedata(), this.displayVolume);
                break;
            case FUND_OTC:
                tdm = createTableDataModel(data.getQuotedata(), data.getFundpricedata());
                break;
            case CONTRACT_EXCHANGE:
                // TODO
                tdm = DefaultTableDataModel.NULL;
                break;
            default:
                tdm = DefaultTableDataModel.NULL;
                break;
        }
        getView().update(tdm);
    }

    private static TableDataModel createTableDataModel(final QuoteData quoteData,
            final PriceData priceData, final boolean displayVolume) {

        final PriceData pd = Price.nonNullPriceData(priceData);

        final FlexTableDataModelBuilder builder = new FlexTableDataModelBuilder();
        builder.addRow(getKursLabel(quoteData), PRICE.render(pd.getPrice()), I18n.I.todo()); 
        builder.addRow(I18n.I.time(), formatTime(pd.getDate()), LF.formatDateShort(pd.getDate()));
        builder.addRow(I18n.I.changeNetAbbr(), Renderer.CHANGE_PRICE.render(pd.getChangeNet()), 
                Renderer.CHANGE_PERCENT.render(pd.getChangePercent()));
        if (displayVolume) {
            builder.addRow(I18n.I.bid(), PRICE.render(pd.getBid()), pd.getBidVolume()); 
            builder.addRow(I18n.I.ask(), PRICE.render(pd.getAsk()), pd.getAskVolume()); 
        }
        else {
            builder.addRow(I18n.I.bidAsk(), PRICE.render(pd.getBid()), PRICE.render(pd.getAsk())); 
        }
        builder.addRow(I18n.I.spread(), PRICE.render(pd.getSpreadNet()), PERCENT.render(pd.getSpreadPercent())); 
        builder.addRow(StringUtil.htmlBold(I18n.I.day()), "", LF.formatDateShort(pd.getDate()));  // $NON-NLS-0$
        builder.addRow(I18n.I.openClose(), PRICE.render(pd.getOpen()), PRICE.render(pd.getClose())); 
        builder.addRow(I18n.I.highLow(), PRICE.render(pd.getHighDay()), PRICE.render(pd.getLowDay())); 
        if (displayVolume) {
            builder.addRow(I18n.I.turnoverVolumeAbbr(), 
                    Renderer.TURNOVER.render(pd.getTurnoverDay()),
                    Renderer.VOLUME.renderLong(pd.getVolumeDay()));
        }
        builder.addRow(I18n.I.nrOfTradesAbbr(), "", I18n.I.todo());  // $NON-NLS-0$
        builder.addRow(StringUtil.htmlBold(I18n.I.previousClose()), "", I18n.I.todoDate());  // $NON-NLS-0$
        builder.addRow(I18n.I.close(), PRICE.render(pd.getPreviousClose()), displayVolume ? I18n.I.todoVol() : "");  // $NON-NLS-0$
        builder.addRow(StringUtil.htmlBold(I18n.I.year()), "", LF.formatDateYyyy(pd.getDate(), "--"));  // $NON-NLS-0$ $NON-NLS-1$
        builder.addRow(I18n.I.high(), PRICE.render(pd.getHighYear()), I18n.I.todoDate()); 
        builder.addRow(I18n.I.low(), PRICE.render(pd.getLowYear()), I18n.I.todoDate()); 
        return builder.getResult();
    }

    private static String getKursLabel(QuoteData quoteData) {
        if (quoteData.getCurrencyIso() != null) {
            return I18n.I.priceInCurrency(quoteData.getCurrencyIso());
        }
        return I18n.I.price(); 
    }

    private static TableDataModel createTableDataModel(final QuoteData quoteData,
            final FundPriceData fundPriceData) {
        final TableDataModelBuilder builder = new TableDataModelBuilder(8, 3);
        builder.addRow(I18n.I.redemption(), PRICE.render(fundPriceData.getRepurchasingprice()), "");  // $NON-NLS-0$
        builder.addRow(I18n.I.issuePrice2(), PRICE.render(fundPriceData.getIssueprice()), "");  // $NON-NLS-0$
        builder.addRow(I18n.I.date(), LF.formatDate(fundPriceData.getDate()), "");  // $NON-NLS-0$
        builder.addRow(I18n.I.currency(), quoteData.getCurrencyIso(), "");  // $NON-NLS-0$
        builder.addRow(I18n.I.changeNetAbbr(), Renderer.CHANGE_PRICE.render(fundPriceData.getChangeNet()), "");  // $NON-NLS-0$
        builder.addRow(I18n.I.changePercentAbbr(), Renderer.CHANGE_PERCENT.render(fundPriceData.getChangePercent()), "");  // $NON-NLS-0$
        builder.addRow(I18n.I.yearHigh(), PRICE.render(fundPriceData.getHighYear()), "");  // $NON-NLS-0$
        builder.addRow(I18n.I.yearLow(), PRICE.render(fundPriceData.getLowYear()), "");  // $NON-NLS-0$
        return builder.getResult();
    }


}
