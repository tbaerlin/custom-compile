package de.marketmaker.istar.merger.web.xmlmarket;


import de.marketmaker.istar.common.util.XmlUtil;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

class SearchResultResponse extends AbstractResponse {

    private Map<String, String> parameterMap;
    private List<Instrument> elements;


    SearchResultResponse(Map<String, String> parameterMap, List<Instrument> elements) {
        this.parameterMap = parameterMap;
        this.elements = elements;
    }


    @Override
    public void render(PrintWriter writer) {
        createDocument().render(writer);
    }

    protected XmlDocument createDocument() {
        final XmlNode content = node("searchResult")
                .child(generated())
                .child(parameters(parameterMap))
                ;

        XmlNode result = node("result");
        for (Instrument instrument : elements) {
            final Profile profile = RequestContextHolder.getRequestContext().getProfile();
            final List<Quote> quotes = ProfiledInstrument.quotesWithPrices(instrument, profile);
            Quote defaultQuote = resolveHomeQuote(instrument, quotes);
            result.child(node("resultItem").child(staticData(instrument, defaultQuote, quotes)));
        }

        content.child(result);

        return new XmlDocument()
                .child(XmlDocument.XmlDeclaration.ISO_8859_2)
                .child(content);
    }

    private XmlNode staticData(Instrument instrument, Quote defaultQuote, List<Quote> quotes) {
        XmlNode staticData = node("staticData");

        staticData.child(node("name").cdata(instrument.getName()));
        staticData.child(node("uniqueIdentifier").text(instrument.getSymbolWkn()));
        staticData.child(node("instrumentid").text(instrument.getId() + ".iid"));
        staticData.child(node("symbol").text(instrument.getSymbolTicker()));
        staticData.child(node("secondSymbol").text(defaultQuote.getSymbolVwdsymbol()));
        final InstrumentTypeEnum type = instrument.getInstrumentType();
        final String typestring = OldMergerToolkit.getOriginalTypeString(type);
        staticData.child(node("type").text(typestring));

        staticData.children(getWmNodes(instrument));

        staticData.child(node("defaultExchange").text(OldMergerToolkit.getMarketSymbol(defaultQuote)));
        staticData.child(node("exchanges").text(buildExchangesString(quotes)));

        staticData.child(node("currencyCode").text(defaultQuote.getCurrency().getSymbolIso()));
        staticData.child(node("isin").text(instrument.getSymbolIsin()));

        return staticData;
    }

}
