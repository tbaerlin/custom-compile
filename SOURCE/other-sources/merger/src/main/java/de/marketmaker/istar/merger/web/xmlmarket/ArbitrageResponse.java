package de.marketmaker.istar.merger.web.xmlmarket;


import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.MarketcategoryEnum;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.instrument.InstrumentUtil;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class ArbitrageResponse extends AbstractResponse {

    private final Instrument instrument;
    private final Quote defaultQuote;
    private final List<Quote> quotes;
    private final List<PriceRecord> priceRecords;
    private final List<HighLow> highLows;

    ArbitrageResponse(Instrument instrument, List<Quote> quotes, List<PriceRecord> priceRecords, List<HighLow> highLows) {
        super(instrument.getInstrumentType());
        this.instrument = instrument;
        this.defaultQuote = resolveHomeQuote(instrument, quotes);
        this.quotes = quotes;
        this.priceRecords = priceRecords;
        this.highLows = highLows;
        if (quotes.size() != priceRecords.size()) {
            throw new IllegalStateException("quote count doesn't match priceRecord count");
        }
        if (quotes.size() != highLows.size()) {
            throw new IllegalStateException("quote count doesn't match highLows count");
        }
    }

    @Override
    public void render(PrintWriter writer) {
        createDocument().render(writer);
    }

    protected XmlDocument createDocument() {
        final XmlNode content = node("arbitrage")
                .attribute("type", OldMergerToolkit.getOriginalTypeString(type))
                .child(generated())
                .child(staticData(instrument, defaultQuote));

        content.children(createExchanges());

        return new XmlDocument()
                .child(XmlDocument.XmlDeclaration.ISO_8859_2)
                .child(content);
    }

    private List<XmlNode> createExchanges() {
        ArrayList<XmlNode> elements = new ArrayList<XmlNode>();

        if (type == InstrumentTypeEnum.IND) {
            return elements; // no quotes or exchanges for indices
        }

        for (int i = 0; i < quotes.size(); i++) {
            final PriceRecord priceRecord = priceRecords.get(i);
            if (priceRecord.equals(NullPriceRecord.INSTANCE)) {
                continue;
            }
            final Quote quote = quotes.get(i);
            final Market market = quote.getMarket();
            final MarketcategoryEnum cat = market.getMarketcategory();
            final HighLow highLow = highLows.get(i);
            elements.add(node("exchange").attribute("id", OldMergerToolkit.getMarketSymbol(quote))
                    .child(priceData(instrument, quote, priceRecord, highLow)));
        }

        return elements;
    }

    protected XmlNode staticData(Instrument instrument, Quote defaultQuote) {
        XmlNode staticData = node("staticData");
        staticData.child(node("name").cdata(instrument.getName()));
        staticData.child(node("uniqueIdentifier").text(instrument.getSymbolWkn()));
        if ((type == InstrumentTypeEnum.STK)
                || (type == InstrumentTypeEnum.FND)
                || (type == InstrumentTypeEnum.GNS)) {
            staticData.child(node("instrumentid").text(instrument.getId() + ".iid"));
        }
        staticData.child(node("symbol").text(instrument.getSymbolTicker()));
        // trouble with DE0005933931  --> gives us 593393 instead of DE0005933931
        // staticData.child(node("secondSymbol").text(instrument.getSymbolIsin()));
        staticData.child(node("secondSymbol").text(defaultQuote.getSymbolVwdsymbol()));
        staticData.child(node("currencyCode").text(emptyForNull(format(defaultQuote.getCurrency()))));
        staticData.child(node("type").text(OldMergerToolkit.getOriginalTypeString(type)));
        staticData.child(node("isin").text(instrument.getSymbolIsin()));
        if ((type == InstrumentTypeEnum.BND)) {
            staticData.child(getQuotedPerUnit(defaultQuote));
            staticData.child(getQuotedPer(defaultQuote));
        }
        return staticData;
    }

    private XmlNode priceData(Instrument instrument, Quote quote, PriceRecord price, HighLow highLow) {
        XmlNode result = null;
        switch (type) {
            case STK:   // aktie
                result = priceDataDefault(quote, price, highLow);
                break;
            case CUR:   // devise
                result = priceDataCur(quote, price, highLow);
                break;
            case FND:   // fonds
                result = priceDataFnd(quote, price, highLow);
                break;
            case BND:   // rente/anleihe  -> Rente
                result = priceDataDefault(quote, price, highLow);
                break;
            case CER:   // optionsschein/zertifikat  -> Optionsschein
                result = priceDataDefault(quote, price, highLow);
                break;
            case WNT:  // optionsschein
                result = priceDataDefault(quote, price, highLow);
                break;
            case GNS:  // genusschein
                result = priceDataDefault(quote, price, highLow);
                break;
            case IND:   // indices have no price data
                throw new IllegalArgumentException("we don't provider quote data for indices");
            case OPT:
            case FUT:
            case MER:
            case MK:
            case NON:
            case UND:
            case ZNS:
            case WEA:
            default:
                throw new IllegalArgumentException("unsupported instrument type: " + type);
        }
        return result;
    }

    private XmlNode priceDataDefault(Quote quote, PriceRecord priceRecord, HighLow highLow) {
        return node("priceData")
                .children(getOpenElements(priceRecord))
                .children(getHighElements(priceRecord))
                .children(getLowElements(priceRecord))
                .children(getCloseElements(priceRecord))
                .children(getSpotrateElements(priceRecord))
                .children(getVolumeElements(priceRecord))
                .children(getCommonElements(instrument, quote, priceRecord))
                .children(getSupplementElements(priceRecord))
                ;
    }

    private XmlNode priceDataCur(Quote quote, PriceRecord priceRecord, HighLow highLow) {
        return node("priceData")
                .children(getHighElements(priceRecord))
                .children(getLowElements(priceRecord))
                .children(getSpotrateElements(priceRecord))
                .children(getVolumeElements(priceRecord))
                .children(getCommonElements(instrument, quote, priceRecord))
                .children(getSupplementElements(priceRecord))
                ;
    }

    private XmlNode priceDataFnd(Quote quote, PriceRecord priceRecord, HighLow highLow) {
        if (InstrumentUtil.isVwdFund(quote)) {  // A0B5UZ

            final BigDecimal issuePrice = getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Ausgabe);
            final BigDecimal previousIssuePrice = getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Ausgabe_Vortag);

            return node("priceData")
                    .children(getHighLowElements(priceRecord))
                    .child(node("issuePrice").text(format(isValidPrice(issuePrice)?issuePrice:previousIssuePrice)))
                    .child(node("previousIssuePrice").text(format(previousIssuePrice)))
                    .child(node("netAssetValue").text(format(priceRecord.getPrice().getValue())))
                    .child(node("previousNetAssetValue").text(getStringFromSnap(priceRecord, VwdFieldDescription.ADF_Ruecknahme_Vortag)))

                    .child(node("lastPrice").text(format(priceRecord.getPrice().getValue())))
                    .child(node("lastDate").text(format(priceRecord.getLastClose() == null ? null :
                            priceRecord.getLastClose().getDate())))
                    .child(node("exchange").text(OldMergerToolkit.getMarketSymbol(quote)))
                    .children(getDiffElements(priceRecord))
                    .child(node("currencyCode").text(quote.getCurrency() == null ? null :
                            quote.getCurrency().getSymbolIso()))
                    ;
        }
        else {  // DE0008474503
            return node("priceData")
                    .children(getOpenElements(priceRecord))
                    .children(getHighElements(priceRecord))
                    .children(getLowElements(priceRecord))
                    .children(getCloseElements(priceRecord))
                    .children(getVolumeElements(priceRecord))
                    .children(getCommonElements(instrument, quote, priceRecord))
                    .children(getSupplementElements(priceRecord))
                    ;
        }
    }

}
