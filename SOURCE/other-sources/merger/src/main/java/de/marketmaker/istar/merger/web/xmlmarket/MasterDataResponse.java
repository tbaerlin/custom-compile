package de.marketmaker.istar.merger.web.xmlmarket;


import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.RatioFormatter;
import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.data.*;
import de.marketmaker.istar.domain.instrument.*;
import de.marketmaker.istar.domainimpl.data.NullPrice;
import de.marketmaker.istar.feed.vwd.PriceRecordVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public abstract class MasterDataResponse extends AbstractResponse {

    private final RatioFormatter ratioFormatter = new RatioFormatter();

    protected final Instrument instrument;

    protected final Quote selectedQuote;

    protected final List<Quote> quotes;

    protected final PriceRecordVwd selectedPriceRecord;

    protected MasterDataStock masterDataStock;

    protected MasterDataBond masterDataBond;

    protected MasterDataCertificate masterDataCertificate;

    protected MasterDataFund masterDataFund;

    protected MasterDataWarrant masterDataWarrant;

    private String underlyingId;

    private String underlyingType = "n/a";

    public MasterDataResponse(InstrumentTypeEnum instrumentType, Quote selectedQuote,
                              List<Quote> quotes, Instrument instrument, PriceRecord defaultPrice) {
        super(instrumentType);
        this.selectedQuote = selectedQuote;
        this.quotes = quotes;
        this.instrument = instrument;
        if (defaultPrice instanceof PriceRecordVwd) {
            this.selectedPriceRecord = (PriceRecordVwd)defaultPrice;
        } else {
            this.selectedPriceRecord = null;
        }
    }


    public void put(MasterDataStock masterDataStock) {
        this.masterDataStock = masterDataStock;
    }

    public void put(MasterDataBond masterDataBond) {
        this.masterDataBond = masterDataBond;
    }

    public void put(MasterDataCertificate masterDataCertificate) {
        this.masterDataCertificate = masterDataCertificate;
    }

    public void put(MasterDataFund masterDataFund) {
        this.masterDataFund = masterDataFund;
    }

    public void put(MasterDataWarrant masterDataWarrant) {
        this.masterDataWarrant = masterDataWarrant;
    }

    public void setUnderlyingId(String underlyingId) {
        this.underlyingId = underlyingId;
    }

    public void setUnderlyingType(String underlyingType) {
        this.underlyingType = underlyingType;
    }

    protected List<XmlNode> staticDataSTK() {
        List<XmlNode> result = new ArrayList<>();

        result.add(getQuotedPerUnit(selectedQuote));
        result.add(getQuotedPer(selectedQuote));

        result.add(node("type").text(OldMergerToolkit.getOriginalTypeString(type)));
        result.addAll(getWmNodes(instrument));

        final Currency currency = selectedQuote.getCurrency();
        result.add(node("currency").cdata(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("currencyCode").text(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("defaultExchange").text(OldMergerToolkit.getDefaultExchange(instrument)));
        result.add(node("exchanges").text(buildExchangesString(quotes)));
        result.add(node("country").text(getCountryString(instrument)));
        final LocalizedString sector = masterDataStock.getSector();
        result.add(node("industry").cdata(sector==null?"":sector.getDefault()));

        result.add(node("marketSegment").text("n/a"));
        if (instrument instanceof Stock) {
            final int date = ((Stock) instrument).getGeneralMeetingDate();
            result.add(node("generalMeetingDate").text(format((date > 0) ? DateUtil.yyyymmddToDateTime(date) : null)));
        }
        result.add(node("faceValue"));

        final BigDecimal dividend = masterDataStock.getDividend();
        final LocalDate dividendExDate = masterDataStock.getDividendExDay();
        result.add(node("estimatedDividendDate"));
        result.add(node("estimatedDividend").text(format(dividend)));
        result.add(node("dividend").text(format(dividend)));
        result.add(node("dividendDate").text(dividendExDate==null
                ?null
                :DATE_TIME_FORMATTER.print(dividendExDate.toDateTimeAtStartOfDay())));

        result.add(node("dividendCurrencyCode").text(masterDataStock.getDividendCurrency()));

        final BigDecimal returnInPercent;
        if (selectedPriceRecord != null
                && selectedPriceRecord.getPrice() != null
                && !selectedPriceRecord.getPrice().equals(NullPrice.INSTANCE)
                && dividend != null) {
            BigDecimal priceValue = selectedPriceRecord.getPrice().getValue();
            returnInPercent = dividend.divide(priceValue, MathContext.DECIMAL64).multiply(ONE_HUNDRED);
        } else {
            returnInPercent = new BigDecimal("-1");
        }

        result.add(node("dividendReturnInPercent").text(format(returnInPercent)));
        result.add(node("dividendEstimatedReturnInPercent").text(format(returnInPercent)));
        result.add(node("estimatedEarnings"));
        result.add(node("estimatedCashflow"));
        return result;
    }

    protected List<XmlNode> staticDataBND() {
        List<XmlNode> result = new ArrayList<>();

        result.add(getQuotedPerUnit(selectedQuote));
        result.add(getQuotedPer(selectedQuote));

        result.addAll(getWmNodes(instrument));
        result.add(node("type").text(OldMergerToolkit.getOriginalTypeString(type)));

        final Currency currency = selectedQuote.getCurrency();
        result.add(node("currency").cdata(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("currencyCode").text(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("defaultExchange").text(OldMergerToolkit.getDefaultExchange(instrument))); // OK
        result.add(node("exchanges").text(buildExchangesString(quotes))); //OK
        result.add(node("country").text(getCountryString(instrument)));

        result.add(node("rating").cdata("n/a"));
        final LocalizedString bondType = masterDataBond.getBondType();
        result.add(node("bondType").text(bondType==null?null:bondType.getDefault()));
        final BigDecimal issueVolume = masterDataBond.getIssueVolume();
        result.add(node("volumeOfIssues").text(issueVolume==null?null:Double.toString(issueVolume.doubleValue())));
        result.add(node("sharesPerBond"));
        result.add(node("timeToMaturity").text(DATE_FORMATTER.print(instrument.getExpiration())));
        result.add(node("callableFrom").text("n/a"));
        result.add(node("redemptionPrice").text(format(masterDataBond.getRedemptionPrice())));
        result.add(node("nominal").text(format(masterDataBond.getFaceValue())));
        result.add(node("premium"));
        result.add(node("interest").text(format(masterDataBond.getNominalInterest())));
        result.add(node("interestDate").text(format(masterDataBond.getInterestDate())));
        result.add(node("returnFormula").text(masterDataBond.getInterestCalculationMethod()));
        result.add(node("contribution"));
        return result;
    }

    protected List<XmlNode> staticDataCER() {
        List<XmlNode> result = new ArrayList<>();

        result.add(getQuotedPerUnit(selectedQuote));
        result.add(getQuotedPer(selectedQuote));

        result.add(node("optionType").text("n/a"));

        addNumberOfSharesAndWarrants(masterDataCertificate.getSubscriptionRatio(), result);

        result.add(node("symbolUnderlying").text(underlyingId));

        result.addAll(getWmNodes(instrument));   //OK
        result.add(node("type").text(OldMergerToolkit.getOriginalTypeString(type)));  //OK

        result.add(node("typeUnderlying").text(underlyingType));

        final Currency currency = selectedQuote.getCurrency();
        result.add(node("currency").cdata(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("currencyCode").text(currency == null?null:currency.getSymbolIso())); //OK
        result.add(node("strikeCurrency").text(masterDataCertificate.getCurrencyStrike())); //OK
        result.add(node("defaultExchange").text(OldMergerToolkit.getDefaultExchange(instrument)));
        result.add(node("exchanges").text(buildExchangesString(quotes))); //OK
        final LocalizedString vwdType = masterDataCertificate.getTypeVwd();
        result.add(node("warrantType").text(vwdType==null?null:vwdType.getDe()));
        result.add(node("country").text(getCountryString(instrument)));
        result.add(node("issuer").text(masterDataCertificate.getIssuerName()));
        result.add(node("timeToMaturity").text(DATE_FORMATTER.print(instrument.getExpiration())));

        if (instrument instanceof DerivativeWithStrike) {
            result.add(node("strike").text(format(((DerivativeWithStrike) instrument).getStrike())));
        } else {
            result.add(node("strike").text(format(masterDataCertificate.getStrikePrice())));
        }
        return result;
    }

    private void addNumberOfSharesAndWarrants(final BigDecimal ratio, List<XmlNode> result) {
        if (ratio != null) {
            String text = ratioFormatter.format(ratio);
            String[] strings = text.split(":");
            if (strings.length == 2) {
                result.add(node("numberOfShares").text(strings[1]));
                result.add(node("numberOfWarrants").text(strings[0]));
                return;
            }
        }
        result.add(node("numberOfShares"));
        result.add(node("numberOfWarrants"));
    }

    protected List<XmlNode> staticDataFND() {
        List<XmlNode> result = new ArrayList<>();

        result.add(getQuotedPerUnit(selectedQuote));
        result.add(getQuotedPer(selectedQuote));

        result.add(node("type").text(OldMergerToolkit.getOriginalTypeString(type)));
        result.addAll(getWmNodes(instrument));

        final Currency currency = selectedQuote.getCurrency();
        result.add(node("currency").cdata(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("currencyCode").text(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("defaultExchange").text(OldMergerToolkit.getDefaultExchange(instrument)));
        result.add(node("exchanges").text(buildExchangesString(quotes)));

        result.add(node("country").text(getCountryString(instrument)));

        String sectorString = masterDataFund.getSector() == null ? null : masterDataFund.getSector().getDe();
        if (sectorString == null || sectorString.length() == 0) {
            sectorString = "n/a";
        }
        result.add(node("industry").cdata(sectorString));

        result.add(node("marketSegment").text("n/a"));
        result.add(node("generalMeetingDate"));

        result.add(node("faceValue").text(null));

        result.add(node("estimatedDividendDate"));
        result.add(node("estimatedDividend"));
        result.add(node("dividend"));
        result.add(node("dividendDate"));

        result.add(node("dividendCurrencyCode"));

        result.add(node("dividendReturnInPercent"));
        result.add(node("dividendEstimatedReturnInPercent"));
        result.add(node("estimatedEarnings"));
        result.add(node("estimatedCashflow"));

        return result;
    }

    protected List<XmlNode> staticDataWNT() {
        List<XmlNode> result = new ArrayList<>();

        result.add(getQuotedPerUnit(selectedQuote));
        result.add(getQuotedPer(selectedQuote));

        DerivativeWithStrike dws = null;
        if (instrument instanceof Warrant || instrument instanceof Option) {
            dws = (DerivativeWithStrike) instrument;
            result.add(node("optionType").text(getOptionType(dws)));
        }

        addNumberOfSharesAndWarrants(dws != null ? dws.getSubscriptionRatio() : null, result);

        result.add(node("symbolUnderlying").text(underlyingId));
        result.addAll(getWmNodes(instrument));
        result.add(node("type").text(OldMergerToolkit.getOriginalTypeString(type)));
        result.add(node("typeUnderlying").text(underlyingType));

        final Currency currency = selectedQuote.getCurrency();
        result.add(node("currency").cdata(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("currencyCode").text(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("strikeCurrency").text(masterDataWarrant.getCurrencyStrike()));
        result.add(node("defaultExchange").text(OldMergerToolkit.getDefaultExchange(instrument)));
        result.add(node("exchanges").text(buildExchangesString(quotes)));

        result.add(node("warrantType").text("Covered Warrant"));
        result.add(node("country").text(getCountryString(instrument)));
        result.add(node("issuer").text(masterDataWarrant.getIssuerName()));
        result.add(node("timeToMaturity").text(DATE_FORMATTER.print(instrument.getExpiration())));

        if (dws != null) {
            result.add(node("strike").text(format(dws.getStrike())));
        }

        return result;
    }

    private String getOptionType(DerivativeWithStrike dws) {
        switch (dws.getType()) {
            case CALL:
                return "Call";
            case PUT:
                return "Put";
            default:
                return "n/a";
        }
    }

    private String getCountryString(Instrument instrument) {
        final Country country = instrument.getCountry();
        if (country==null || "Unknown".equalsIgnoreCase(country.getName())) {
            return "n/a";
        } else {
            return country.getName();
        }
    }

    protected List<XmlNode> staticDataGNS() {
        List<XmlNode> result = new ArrayList<>();

        result.add(getQuotedPerUnit(selectedQuote));
        result.add(getQuotedPer(selectedQuote));

        result.add(node("type").text(OldMergerToolkit.getOriginalTypeString(type)));
        result.addAll(getWmNodes(instrument));

        final Currency currency = selectedQuote.getCurrency();
        result.add(node("currency").cdata(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("currencyCode").text(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("defaultExchange").text(OldMergerToolkit.getDefaultExchange(instrument)));
        result.add(node("exchanges").text(buildExchangesString(quotes)));

        result.add(node("country").text(getCountryString(instrument)));
        result.add(node("industry").cdata("Genußschein"));
        result.add(node("marketSegment"));
        result.add(node("generalMeetingDate"));
        result.add(node("faceValue"));
        result.add(node("estimatedDividendDate"));
        result.add(node("estimatedDividend"));
        result.add(node("dividend"));
        result.add(node("dividendDate"));
        result.add(node("dividendCurrencyCode"));
        result.add(node("dividendReturnInPercent"));
        result.add(node("dividendEstimatedReturnInPercent"));
        result.add(node("estimatedEarnings"));
        result.add(node("estimatedCashflow"));
        return result;
    }

    protected List<XmlNode> staticDataSimple() {
        List<XmlNode> result = new ArrayList<>();
        result.add(node("type").text(OldMergerToolkit.getOriginalTypeString(type)));
        final Currency currency = selectedQuote.getCurrency();
        result.add(node("currency").cdata(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("currencyCode").text(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("defaultExchange").text(OldMergerToolkit.getDefaultExchange(instrument)));
        result.add(node("exchanges").text(buildExchangesString(quotes)));
        result.add(node("country").text(getCountryString(instrument)));
        return result;
    }

    protected List<XmlNode> staticDataWithWmNodes() {
        List<XmlNode> result = new ArrayList<>();
        result.add(node("type").text(OldMergerToolkit.getOriginalTypeString(type)));
        result.addAll(getWmNodes(instrument));
        final Currency currency = selectedQuote.getCurrency();
        result.add(node("currency").cdata(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("currencyCode").text(currency==null?null:currency.getSymbolIso())); //OK
        result.add(node("defaultExchange").text(OldMergerToolkit.getDefaultExchange(instrument)));
        result.add(node("exchanges").text(buildExchangesString(quotes)));
        result.add(node("country").text(getCountryString(instrument)));
        return result;
    }

    protected XmlNode ratios(PriceRecordVwd priceRecord) {
        XmlNode ratios = node("ratios");

        ratios.child(node("beta30").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Beta_1M))))
                .child(node("beta250").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Beta_1J))))
                .child(node("correlation30").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Korrel_1M))))
                .child(node("correlation250").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Korrelation_1J))));

        // seems like volatility is in percent now...
        BigDecimal volatility30 = getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Vola_1M);
        if (volatility30 != null) {
            ratios.child(node("volatility30").text(format(volatility30.divide(ONE_HUNDRED))));
        }
        else {
            ratios.child(node("volatility30"));
        }

        BigDecimal volatility250 = getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Vola_1J);
        if (volatility250 != null) {
            ratios.child(node("volatility250").text(format(volatility250.divide(ONE_HUNDRED))));
        }
        else {
            ratios.child(node("volatility250"));
        }

        if ((type == InstrumentTypeEnum.STK)
                || (type == InstrumentTypeEnum.GNS)
                || (type == InstrumentTypeEnum.FND)) {
            ratios.child(node("priceEarningsRatio").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_KGV))));
        }
        else if ( (type == InstrumentTypeEnum.WNT)
                || (type == InstrumentTypeEnum.CER)) {

            BigDecimal implVola = getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_implizite_Vola);
            if (implVola != null) {
                ratios.child(node("impliedVolatility").text(format(implVola.divide(ONE_HUNDRED))));
            }
            else {
                ratios.child(node("impliedVolatility"));
            }

            ratios.child(node("breakeven").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Break_Even))));
            ratios.child(node("delta").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Delta))));
            ratios.child(node("fairPrice").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_fairer_Wert))));
        }

        return ratios;
    }
}
