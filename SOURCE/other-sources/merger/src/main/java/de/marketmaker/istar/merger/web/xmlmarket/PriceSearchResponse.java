package de.marketmaker.istar.merger.web.xmlmarket;


import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.*;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

class PriceSearchResponse extends AbstractResponse {

    private final Map<String, String> parameterMap;
    private final Instrument instrument;
    private final List<Quote> quotes;
    private final List<PriceRecord> priceRecords;

    public PriceSearchResponse(Map<String, String> parameterMap,
                               Instrument instrument, List<Quote> quotes, List<PriceRecord> priceRecord) {
        this.parameterMap = parameterMap;
        this.instrument = instrument;
        this.quotes = quotes;
        this.priceRecords = priceRecord;
    }

    @Override
    public void render(PrintWriter writer) {
        createDocument().render(writer);
    }

    protected XmlDocument createDocument() {
        final XmlNode content = node("arbitrageSearch")
                .child(generated())
                .child(parameters(parameterMap))
                ;

        XmlNode result = node("result");

        for (int i = 0; i<quotes.size(); i++ ) {
            Quote quote = quotes.get(i);
            PriceRecord priceRecord = priceRecords.get(i);
            XmlNode resultItem = node("resultItem");
            resultItem.child(staticData(instrument, quote));
            resultItem.child(priceData(instrument, quote, priceRecord));
            result.child(resultItem);
        }

        content.child(result);

        return new XmlDocument()
                .child(XmlDocument.XmlDeclaration.ISO_8859_2)
                .child(content);
    }


    protected XmlNode staticData(Instrument instrument, Quote quote) {
        XmlNode staticData = node("staticData");
        staticData.child(node("name").cdata(instrument.getName()));
        staticData.child(node("uniqueIdentifier").text(instrument.getSymbolWkn()));
        staticData.child(node("instrumentid").text(instrument.getId() + ".iid"));
        staticData.child(node("symbol").text(instrument.getSymbolTicker()));
        staticData.child(node("secondSymbol").text(quote.getSymbolVwdsymbol()));
        staticData.child(node("type").text(OldMergerToolkit.getOriginalTypeString(instrument.getInstrumentType())));
        DetailedInstrumentType detailedInstrument = instrument.getDetailedInstrumentType();
        staticData.child(node("WM_GD195_ID").text(detailedInstrument.getSymbol(KeysystemEnum.WM_GD195_ID)));
        String string = detailedInstrument.getSymbol(KeysystemEnum.GD664);
        if (string == null) {
            string = instrument.getSymbolWmGd664();
        }
        staticData.child(node("WM_GD664").text(string));
        staticData.child(node("WM_GD195_NAME").text(detailedInstrument.getSymbol(KeysystemEnum.WM_GD195_NAME)));
        InstrumentTypeEnum type = instrument.getInstrumentType();
        String typestring = type.getName(Language.de);
        // String typestring = OldMergerToolkit.getOriginalTypeString(type);
        staticData.child(node("WM_type").text(typestring));
        staticData.child(node("defaultExchange").text(OldMergerToolkit.getDefaultExchange(instrument)));
        staticData.child(node("exchanges").text(buildExchangesString(quotes)));
        staticData.child(node("currencyCode").text(emptyForNull(format(quote.getCurrency()))));
        staticData.child(node("isin").text(instrument.getSymbolIsin()));
        return staticData;
    }


    protected XmlNode priceData(Instrument instrument, Quote quote, PriceRecord priceRecord) {
        XmlNode result = node("priceData");

        result.child(minQuotation(quote));
        MinimumQuotationSize minQuoteSize = quote.getMinimumQuotationSize();
        result.child(node("WM_quotedPer").text(minQuoteSize.getUnit().name().toLowerCase()));

        BigDecimal price = getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Bezahlt);
        result.child(node("price").text(format(escapePriceWithBidAsk(instrument, quote, priceRecord, price))));

        DateTime time = null;

        Integer bezahltZeit = getInt(priceRecord, VwdFieldDescription.ADF_Bezahlt_Zeit);
        Integer bezahltDatum = getInt(priceRecord, VwdFieldDescription.ADF_Bezahlt_Datum);
        if (time == null && bezahltZeit != null && bezahltDatum != null
                && bezahltZeit > 0 && bezahltDatum > 0) {
            time = DateUtil.toDateTime(bezahltDatum, bezahltZeit);
        }

        Integer geldZeit = getInt(priceRecord, VwdFieldDescription.ADF_Geld_Zeit);
        Integer geldDatum = getInt(priceRecord, VwdFieldDescription.ADF_Datum);
        if (time == null && geldZeit != null && geldDatum != null
                && geldZeit > 0 && geldDatum > 0) {
            time = DateUtil.toDateTime(geldDatum, geldZeit);
        }

        Integer briefZeit = getInt(priceRecord, VwdFieldDescription.ADF_Brief_Zeit);
        Integer briefDatum = getInt(priceRecord, VwdFieldDescription.ADF_Datum);
        if (time == null && briefZeit != null && briefDatum != null
                && briefZeit > 0 && briefDatum > 0) {
            time = DateUtil.toDateTime(briefDatum, briefZeit);
        }

        result.child(node("time").text(time==null?null:TIME_FORMATTER.print(time)));

        result.children(getLastPriceNodes(instrument, quote, priceRecord));
        result.child(node("exchange").text(OldMergerToolkit.getMarketSymbol(quote)));
        result.children(getDiffElements(priceRecord));
        return result;
    }


}
