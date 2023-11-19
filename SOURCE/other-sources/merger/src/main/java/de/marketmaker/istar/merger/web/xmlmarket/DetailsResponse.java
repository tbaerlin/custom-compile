package de.marketmaker.istar.merger.web.xmlmarket;


import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordFund;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.vwd.PriceRecordVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class DetailsResponse extends MasterDataResponse {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    DetailsResponse(Instrument instrument, Quote selectedQuote, List<Quote> quotes,
                    PriceRecord selectedPriceRecord, List<PriceRecord> priceRecords) {
        super(instrument.getInstrumentType(), selectedQuote, quotes, instrument, selectedPriceRecord);
    }

    @Override
    public void render(PrintWriter writer) {
        createDocument().render(writer);
    }

    protected XmlDocument createDocument() {
        final XmlNode content = node("detailedSecurityInformation")
                .attribute("type", OldMergerToolkit.getOriginalTypeString(type))
                .child(generated())
                .child(staticData(instrument))
                .child(selectedPriceRecord==null
                        ?null
                        :ratios(selectedPriceRecord))
                .child((selectedPriceRecord==null || selectedQuote==null)
                        ?null
                        :priceData(selectedPriceRecord, selectedQuote))
                ;

        return new XmlDocument()
                .child(XmlDocument.XmlDeclaration.ISO_8859_2)
                .child(content);
    }

    // ----------- fragments

    private XmlNode priceData(PriceRecordVwd priceRecord, Quote quote) {
        if (priceRecord instanceof PriceRecordFund) {
            return priceDataFund((PriceRecordFund) priceRecord, quote);
        }
        else {
            return priceDataStandard(priceRecord, quote);
        }
    }

    private XmlNode priceDataStandard(PriceRecordVwd priceRecord, Quote quote) {

        XmlNode result = node("priceData");

        result.child(minQuotation(quote));
        MinimumQuotationSize minQuoteSize = quote.getMinimumQuotationSize();
        result.child(node("WM_quotedPer").text(minQuoteSize.getUnit().name().toLowerCase()));

        result.children(getOpenElements(priceRecord))
                .children(getHighElements(priceRecord))
                .children(getLowElements(priceRecord))
                .children(getCloseElements(priceRecord))
                .children(getSpotrateElements(priceRecord))
                .children(getVolumeElements(priceRecord))
                .child(node("ask").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Brief))))
                .child(node("bid").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Geld))));

        BigDecimal price = getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Bezahlt);
        result.child(node("price").text(format(escapePriceWithBidAsk(instrument, quote, priceRecord, price))));

        String supplement = getStringFromSnap(priceRecord, VwdFieldDescription.ADF_Bezahlt_Kurszusatz);
        result.child(node("supplement").text(emptyForNull(supplement)));

        result.child(getTimeNode(instrument, quote, priceRecord));
        result.children(getLastPriceNodes(instrument, quote, priceRecord));

        result.child(node("yearStart"))
              .child(node("exchange").text(OldMergerToolkit.getMarketSymbol(selectedQuote)))
              .children(getDiffElements(priceRecord));

        result.children(getSupplementElements(priceRecord));

        return result;
    }

    private XmlNode priceDataFund(PriceRecordFund pr, Quote quote) {
        final BigDecimal issuePrice = pr.getIssuePrice().getValue();
        final BigDecimal previousIssuePrice = pr.getPreviousIssuePrice().getValue();
        final MinimumQuotationSize minQuoteSize = quote.getMinimumQuotationSize();

        BigDecimal zwischengewinn = getPriceFromSnap(pr, VwdFieldDescription.ADF_Zwischengewinn);
        if (zwischengewinn == null) {
            zwischengewinn = getPriceFromSnap(pr, VwdFieldDescription.ADF_Zwischengewinn_Vortag);
        }

        BigDecimal immobiliengewinn = getPriceFromSnap(pr, VwdFieldDescription.ADF_Immobiliengewinn);
        if (immobiliengewinn == null) {
            immobiliengewinn = getPriceFromSnap(pr, VwdFieldDescription.ADF_Immobiliengewinn_VT);
        }

        BigDecimal aktiengewinn = getPriceFromSnap(pr, VwdFieldDescription.ADF_Aktiengewinn);
        if (aktiengewinn == null) {
            aktiengewinn = getPriceFromSnap(pr, VwdFieldDescription.ADF_Aktiengewinn_Vortag);
        }

        BigDecimal ate = getPriceFromSnap(pr, VwdFieldDescription.ADF_ATE);


        XmlNode result = node("priceData")
              .child(minQuotation(quote))
              .child(node("WM_quotedPer").text(minQuoteSize.getUnit().name().toLowerCase()));

        result.children(getHighElements(pr))
              .children(getLowElements(pr))
              .child(node("issuePrice").text(format(isValidPrice(issuePrice)?issuePrice:previousIssuePrice)))
              .child(node("previousIssuePrice").text(format(previousIssuePrice)))
              .child(node("netAssetValue").text(format(pr.getPrice().getValue())))
              .child(node("previousNetAssetValue").text(getStringFromSnap(pr, VwdFieldDescription.ADF_Ruecknahme_Vortag)))

              .child(node("interimProfit").text(format(zwischengewinn)))
              .child(node("estateProfit").text(format(immobiliengewinn)))
              .child(node("stockProfit").text(format(aktiengewinn)))
              .child(node("ate").text(format(ate)))
        ;

        result.children(getLastPriceNodes(instrument, quote, pr));

        result.child(node("yearStart"))
                .child(node("exchange").text(OldMergerToolkit.getMarketSymbol(quote)))
                .children(getDiffElements(pr));

        return result;
    }


    private XmlNode staticData(Instrument instrument) {
        XmlNode staticData = node("staticData");

        staticData.children(commonStaticData());
        switch (instrument.getInstrumentType()) {
            case STK:
                staticData.children(staticDataSTK());
                break;
            case BND:
                staticData.children(staticDataBND());
                break;
            case CER:
                staticData.children(staticDataCER());
                break;
            case FND:
                staticData.children(staticDataFND());
                break;
            case WNT:
                staticData.children(staticDataWNT());
                break;
            case GNS:
                staticData.children(staticDataGNS());
                break;
            case IND:
                staticData.children(staticDataWithWmNodes());
                break;
            default:
                logger.info("no master data for instrument type '" + instrument.getInstrumentType() + "'");
        }
        return staticData;
    }

    private List<XmlNode> commonStaticData() {
        List<XmlNode> result = new ArrayList<>();
        result.add(node("name").cdata(instrument.getName()));
        result.add(node("uniqueIdentifier").text(instrument.getSymbolWkn()));
        result.add(node("symbol").text(instrument.getSymbolTicker()));
        result.add(node("secondSymbol").text(selectedQuote.getSymbolVwdsymbol()));
        result.add(node("isin").text(instrument.getSymbolIsin()));
        return result;
    }

    // need to override since historically we have different tags in response for details request
    @Override
    protected List<XmlNode> staticDataFND() {
        List<XmlNode> result = new ArrayList<>();
        result.addAll(getWmNodes(instrument));
        result.add(node("type").text(OldMergerToolkit.getOriginalTypeString(type)));
        final Currency currency = selectedQuote.getCurrency();
        result.add(node("currency").cdata(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("currencyCode").text(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("exchange").text(OldMergerToolkit.getMarketSymbol(selectedQuote)));
        result.add(node("defaultExchange").text(OldMergerToolkit.getDefaultExchange(instrument)));
        result.add(node("exchanges").text(buildExchangesString(quotes)));

        String countryString = instrument.getCountry().getName();
        if ("Unknown".equalsIgnoreCase(countryString)) {
            countryString = "n/a";
        }
        result.add(node("country").text(countryString));

        String sectorString = masterDataFund.getSector() == null ? null : masterDataFund.getSector().getDe();
        result.add(node("industry").cdata(sectorString));

        result.add(node("marketSegment"));
        LocalizedString issuerName = masterDataFund.getIssuerName();
        result.add(node("issuer").text(issuerName==null?null:issuerName.getDe()));
        result.add(node("issueSurcharge").text(format(masterDataFund.getIssueSurcharge())));
        result.add(node("managementFee").text(format(masterDataFund.getManagementFee())));
        result.add(node("timeToMaturity").text(DATE_FORMATTER.print(instrument.getExpiration())));
        return result;
    }

    @Override
    protected List<XmlNode> getVolumeElements(PriceRecord priceRecord) {
        ArrayList<XmlNode> elements = new ArrayList<XmlNode>();
        if (priceRecord == null) {
            return elements;
        }
        // orig: getVolumeAccumulated
        elements.add(node("volumeTotal").text(zeroForNull(getStringFromSnap(priceRecord, VwdFieldDescription.ADF_Umsatz_gesamt))));
        elements.add(node("volumeAsk").text(zeroForNull(volume(priceRecord.getAsk()))));
        elements.add(node("volumeBid").text(zeroForNull(volume(priceRecord.getBid()))));
        elements.add(node("volumeTrade").text(zeroForNull(volume(priceRecord.getPrice()))));

        Long numberOfTrades = priceRecord.getNumberOfTrades();
        elements.add(node("numberOfTrades").text((numberOfTrades==null)?"0":Long.toString(numberOfTrades) ));
        elements.add(node("valueTotal").text(format(priceRecord.getTurnoverDay()==null
                        ?BigDecimal.ZERO
                        :priceRecord.getTurnoverDay())));

        return elements;
    }

}
