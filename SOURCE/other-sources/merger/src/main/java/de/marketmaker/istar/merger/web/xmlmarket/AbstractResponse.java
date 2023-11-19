package de.marketmaker.istar.merger.web.xmlmarket;


import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.data.*;
import de.marketmaker.istar.domain.instrument.*;
import de.marketmaker.istar.domainimpl.instrument.NullQuote;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.PriceRecordVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;


/**
 * base class for creating xml responses, contains some common fragments for reuse
 */
abstract class AbstractResponse implements Response {

    private static final Locale LOCALE = new Locale("en");

    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("EE MMM dd HH:mm:ss z yyyy").withLocale(LOCALE);

    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("dd.MM.yyyy").withLocale(LOCALE);

    protected static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("HH:mm:ss").withLocale(LOCALE);

    protected static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    protected final InstrumentTypeEnum type;

    AbstractResponse() {
        this.type = null;
    }

    AbstractResponse(InstrumentTypeEnum type) {
        this.type = type;
    }

    protected Quote resolveHomeQuote(Instrument instrument, List<Quote> quotes) {
        Market homeExchange = instrument.getHomeExchange();
        if (quotes.size() == 0) {
            return NullQuote.create(instrument);
        }
        for (Quote quote : quotes) {
            if (quote.getMarket().getId() == homeExchange.getId()) {
                return quote;
            }
        }
        return LbbwMarketStrategy.INSTANCE.getQuote(instrument);
    }

    protected XmlNode node(String name) {
        return new XmlNode(name);
    }

    protected XmlNode generated() {
        return node("generated").text(new DateTime().toString("dd.MM.yyyy HH:mm:ss"));
    }

    protected XmlNode parameters(Map<String, String> map) {
        XmlNode result = node("parameters");
        for (Map.Entry<String, String> elem : map.entrySet()) {
            result.child(node(elem.getKey()).cdata(elem.getValue()));
        }
        return result;
    }

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
        elements.add(node("previousVolumeTotal").text(zeroForNull(getStringFromSnap(priceRecord, VwdFieldDescription.ADF_Umsatz_gesamt_Vortag))));
        return elements;
    }

    protected List<XmlNode> getSupplementElements(PriceRecord priceRecord) {
        ArrayList<XmlNode> elements = new ArrayList<XmlNode>();
        if (priceRecord == null) {
            return elements;
        }
        elements.add(node("priceSupplement").text(emptyForNull(getStringFromSnap(priceRecord, VwdFieldDescription.ADF_Bezahlt_Kurszusatz))));
        elements.add(node("openSupplement").text(emptyForNull(getStringFromSnap(priceRecord, VwdFieldDescription.ADF_Anfang_Kurszusatz))));
        elements.add(node("highSupplement").text(emptyForNull(getStringFromSnap(priceRecord, VwdFieldDescription.ADF_Tageshoch_Kurszusatz))));
        elements.add(node("lowSupplement").text(emptyForNull(getStringFromSnap(priceRecord, VwdFieldDescription.ADF_Tagestief_Kurszusatz))));
        elements.add(node("closeSupplement").text(emptyForNull(getStringFromSnap(priceRecord, VwdFieldDescription.ADF_Schluss_Kurszusatz))));
        elements.add(node("bidSupplement").text(emptyForNull(supplement(priceRecord.getBid()))));
        elements.add(node("askSupplement").text(emptyForNull(supplement(priceRecord.getAsk()))));
        elements.add(node("previousOpenSupplement").text(emptyForNull(supplement(priceRecord.getPreviousOpen()))));
        elements.add(node("previousHighSupplement").text(emptyForNull(supplement(priceRecord.getPreviousHighDay()))));
        elements.add(node("previousLowSupplement").text(emptyForNull(supplement(priceRecord.getPreviousLowDay()))));
        // elements.add(node("previousCloseSupplement").text(emptyForNull(getStringFromSnap(priceRecord, VwdFieldDescription.ADF_Schluss_Vortag_Kurszusatz))));
        elements.add(node("previousCloseSupplement").text(emptyForNull(supplement(priceRecord.getPreviousClose()))));
        return elements;
    }

    protected List<XmlNode> getWmNodes(Instrument instrument) {
        ArrayList<XmlNode> elements = new ArrayList<XmlNode>();
        DetailedInstrumentType detailedInstrument = instrument.getDetailedInstrumentType();
        elements.add(node("WM_GD195_ID").text(detailedInstrument.getSymbol(KeysystemEnum.WM_GD195_ID)));
        String string = detailedInstrument.getSymbol(KeysystemEnum.GD664);
        if (string == null) {
            string = instrument.getSymbolWmGd664();
        }
        elements.add(node("WM_GD664").text(string));
        elements.add(node("WM_GD195_NAME").text(detailedInstrument.getSymbol(KeysystemEnum.WM_GD195_NAME)));
        InstrumentTypeEnum type = instrument.getInstrumentType();
        if (type != null) {
            elements.add(node("WM_type").text(type.getName(Language.de)));
        }
        return elements;
    }

    protected List<XmlNode> getSpotrateElements(PriceRecord priceRecord) {
        ArrayList<XmlNode> elements = new ArrayList<>();
        BigDecimal spotrate = getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Kassa);
        if (spotrate == null) {
            spotrate = getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Mittelkurs);
        }
        elements.add(node("spotrate").text(format(spotrate)));
        elements.add(node("previousSpotrate").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Kassa_Vortag))));
        return elements;
    }

    protected List<XmlNode> getOpenElements(PriceRecord priceRecord) {
        ArrayList<XmlNode> elements = new ArrayList<XmlNode>();
        if (priceRecord == null) {
            return elements;
        }
        elements.add(node("open").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Anfang))));
        elements.add(node("previousOpen").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Anfang_Vortag))));
        return elements;
    }

    protected List<XmlNode> getHighLowElements(PriceRecord priceRecord) {
        ArrayList<XmlNode> elements = new ArrayList<XmlNode>();
        if (priceRecord == null) {
            return elements;
        }
        elements.add(node("highYear").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Jahreshoch))));
        elements.add(node("highYearDate").text(format(priceRecord.getHighYear().getDate())));
        elements.add(node("lowYear").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Jahrestief))));
        elements.add(node("lowYearDate").text(format(priceRecord.getLowYear().getDate())));
        return elements;
    }

    protected List<XmlNode> getHighElements(PriceRecord priceRecord) {
        ArrayList<XmlNode> elements = new ArrayList<XmlNode>();
        if (priceRecord == null) {
            return elements;
        }
        elements.add(node("high").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Tageshoch))));
        elements.add(node("previousHigh").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Tageshoch_Vortag))));
        elements.add(node("highYear").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Jahreshoch))));
        elements.add(node("highYearDate").text(format(priceRecord.getHighYear().getDate())));
        return elements;
    }

    // note there is an inconsistency in the original implementation:
    // "lowLast" is used in FND, "previousLow" for anything else, but only in arbitrage, intraday request has previousLow
    protected List<XmlNode> getLowElements(PriceRecord priceRecord) {
        ArrayList<XmlNode> elements = new ArrayList<XmlNode>();
        if (priceRecord == null) {
            return elements;
        }
        elements.add(node("low").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Tagestief))));
        if (type == InstrumentTypeEnum.FND) {
            elements.add(node("lowLast").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Tagestief_Vortag))));
        }
        else {
            elements.add(node("previousLow").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Tagestief_Vortag))));
        }
        elements.add(node("lowYear").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Jahrestief))));
        elements.add(node("lowYearDate").text(format(priceRecord.getLowYear().getDate())));
        return elements;
    }

    protected List<XmlNode> getCloseElements(PriceRecord priceRecord) {
        ArrayList<XmlNode> elements = new ArrayList<XmlNode>();
        if (priceRecord == null) {
            return elements;
        }
        elements.add(node("close").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Schluss))));
        elements.add(getCloseDate(priceRecord));
        elements.add(node("previousClose").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Schluss_Vortag))));
        elements.add(getPreviousCloseDate(priceRecord));
        return elements;
    }

    // "supplement" is only provided for STK, CER, WNT, GNS
    // "exchange" is only provided for FND
    protected List<XmlNode> getCommonElements(Instrument instrument, Quote quote,
            PriceRecord priceRecord) {
        ArrayList<XmlNode> elements = new ArrayList<XmlNode>();
        elements.add(node("ask").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Brief))));
        elements.add(node("bid").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Geld))));

        BigDecimal price = getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Bezahlt);
        elements.add(node("price").text(format(escapePriceWithBidAsk(instrument, quote, priceRecord, price))));

        if (type == InstrumentTypeEnum.STK
                || type == InstrumentTypeEnum.CER
                || type == InstrumentTypeEnum.WNT
                || type == InstrumentTypeEnum.GNS) {
            String supplement = getStringFromSnap(priceRecord, VwdFieldDescription.ADF_Bezahlt_Kurszusatz);
            elements.add(node("supplement").text(emptyForNull(supplement)));
        }

        elements.add(getTimeNode(instrument, quote, priceRecord));
        elements.addAll(getLastPriceNodes(instrument, quote, priceRecord));
        if (type == InstrumentTypeEnum.FND) {
            elements.add(node("exchange").text(OldMergerToolkit.getMarketSymbol(quote)));
        }
        elements.addAll(getDiffElements(priceRecord));

        elements.add(node("currencyCode").text(quote.getCurrency().getSymbolIso()));

        return elements;
    }

    protected List<XmlNode> getLastPriceNodes(Instrument instrument, Quote quote,
            PriceRecord pr) {
        final List<XmlNode> elements = new ArrayList<>();
        if (pr == null) {
            return elements;
        }

        elements.add(node("lastPrice").text(format(escapePriceWithBidAsk(instrument, quote, pr, pr.getPrice().getValue()))));

        final DateTime date = isValidPrice(pr.getPrice().getValue()) ? pr.getPrice().getDate() : pr.getDate();

        elements.add(node("lastTime").text(getLastTimeStr(date)));
        elements.add(node("lastDate").text(date == null ? null : DATE_TIME_FORMATTER.print(date)));
        return elements;
    }


    protected List<XmlNode> getDiffElements(PriceRecord priceRecord) {
        ArrayList<XmlNode> elements = new ArrayList<XmlNode>();
        if (priceRecord == null) {
            return elements;
        }

        elements.add(node("differenceAbs").text(format(priceRecord.getChangeNet())));
        elements.add(node("differenceRel").text(format(priceRecord.getChangePercent() == null
                ? null // needed for HSH3GP
                : priceRecord.getChangePercent().multiply(ONE_HUNDRED))));
        return elements;
    }


    protected XmlNode getQuotedPer(Quote quote) {
        String minQuotSizeString = null;
        MinimumQuotationSize minQuotSize = quote.getMinimumQuotationSize();
        if (minQuotSize != null) {
            minQuotSizeString = minQuotSize.getUnit().name();
        }
        return node("quotedPer").text(minQuotSizeString);
    }

    protected XmlNode getQuotedPerUnit(Quote quote) {
        String minQuotSizeString = null;
        MinimumQuotationSize minQuotSize = quote.getMinimumQuotationSize();
        if (minQuotSize != null) {
            minQuotSizeString = minQuotSize.getUnit().name();
        }
        return node("quotedPerUnit").text(Boolean.toString("UNIT".equals(minQuotSizeString)));
    }

    protected XmlNode minQuotation(Quote quote) {
        XmlNode result = node("minimumQuotation");
        MinimumQuotationSize minQuote = quote.getMinimumQuotationSize();
        if (minQuote.getNumber() != null) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("0.0", dfs);
            df.setGroupingUsed(false);
            result.child(node("number").text(df.format(minQuote.getNumber())));
            result.child(node("unit").text(minQuote.getUnit().name().toLowerCase()));
            String currency = minQuote.getCurrency().getSymbolIso();
            currency = currency == null ? "n/a" : currency;
            result.child(node("currency").text(currency));
        }
        return result;
    }

    // this is broken for HSH3GP
    protected XmlNode getTimeNode(Instrument instrument, Quote quote, PriceRecord priceRecord) {
        DateTime dateTime = getTimeFromSnap(priceRecord, VwdFieldDescription.ADF_Boersenzeit);
        if (dateTime == null) {
            dateTime = getTimeFromSnap(priceRecord, VwdFieldDescription.ADF_Zeit);
        }

        if (dateTime == null) {
            dateTime = getTimeFromSnap(priceRecord, VwdFieldDescription.ADF_Geld_Zeit);
        }

        if (dateTime == null) {
            dateTime = getTimeFromSnap(priceRecord, VwdFieldDescription.ADF_Brief_Zeit);
        }

        return node("time").text(dateTime == null ? "00:00:00" : formatTime(dateTime));
    }

    protected XmlNode getCloseDate(PriceRecord priceRecord) {
        DateTime date = null;
        // FIXME: this is probably not what we want (!= "Optionsschein")
        if ((type != InstrumentTypeEnum.WNT)
                && (type != InstrumentTypeEnum.CER)) {
            date = getDateFromSnap(priceRecord, VwdFieldDescription.ADF_Handelsdatum);
        }
        if (date == null) {
            date = getDateFromSnap(priceRecord, VwdFieldDescription.ADF_Schluss_Datum);
        }
        return node("closeDate").text(date == null ? null : DATE_TIME_FORMATTER.print(date));
    }

    protected XmlNode getPreviousCloseDate(PriceRecord priceRecord) {
        DateTime date = getDateFromSnap(priceRecord, VwdFieldDescription.ADF_Schluss_Vortagesdatum);
        if (date == null) {
            date = getDateFromSnap(priceRecord, VwdFieldDescription.ADF_Vorheriges_Datum);
        }
        return node("previousCloseDate").text(date == null ? null : DATE_TIME_FORMATTER.print(date));
    }


    protected String getLastTimeStr(DateTime date) {
        if (date == null) {
            return "VT";
        }
        if (date.isBefore(DateTime.now().withMillisOfDay(0))) {
            return TIME_FORMATTER.print(date) + " (VT)";
        }
        return TIME_FORMATTER.print(date);
    }

    protected BigDecimal escapePriceWithBidAsk(Instrument instrument, Quote quote,
            PriceRecord priceRecord, BigDecimal price) {
        if (isValidPrice(price)) {
            return price;
        }

        if (instrument.getInstrumentType() != InstrumentTypeEnum.WNT
                || OldMergerToolkit.GERMAN_OS_PLACES.contains(OldMergerToolkit.getMarketSymbol(quote))) {
            return null;
        }

        final BigDecimal bid = getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Geld);
        final BigDecimal ask = getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Brief);

        if (!isValidPrice(ask) && !isValidPrice(bid)) {
            return null;
        }

        if (isValidPrice(ask) && isValidPrice(bid)) {
            return new BigDecimal(0).add(bid).add(ask).divide(new BigDecimal(2));
        }

        return isValidPrice(ask) ? ask : bid;
    }

    // return empty string instead of null
    protected String emptyForNull(String string) {
        return string == null ? "" : string;
    }

    protected String zeroForNull(String string) {
        return string == null ? "0" : string;
    }

    protected String getStringFromSnap(PriceRecord priceRecord, VwdFieldDescription.Field field) {
        if (!(priceRecord instanceof PriceRecordVwd)) {
            return null;
        }
        PriceRecordVwd priceRecordVwd = (PriceRecordVwd) priceRecord;
        SnapField snapField = priceRecordVwd.getSnapRecord().getField(field.id());
        if (snapField instanceof NullSnapField) {
            return null;
        }
        Object value = snapField.getValue();
        if (value instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) value;
            return bigDecimal.setScale(4, BigDecimal.ROUND_HALF_UP).toPlainString();
        }
        return SnapRecordUtils.getString(snapField);
    }

    protected BigDecimal getPriceFromSnap(PriceRecord priceRecord,
            VwdFieldDescription.Field field) {
        if (!(priceRecord instanceof PriceRecordVwd)) {
            return null;
        }
        PriceRecordVwd priceRecordVwd = (PriceRecordVwd) priceRecord;
        SnapField snapField = priceRecordVwd.getSnapRecord().getField(field.id());
        if (snapField instanceof NullSnapField) {
            return null;
        }
        BigDecimal result = snapField.getPrice();
        if (!isValidPrice(result)) {
            return null;
        }
        return result;
    }

    protected Integer getInt(PriceRecord priceRecord, VwdFieldDescription.Field field) {
        if (!(priceRecord instanceof PriceRecordVwd)) {
            return null;
        }
        PriceRecordVwd priceRecordVwd = (PriceRecordVwd) priceRecord;
        SnapField snapField = priceRecordVwd.getSnapRecord().getField(field.id());
        if (snapField instanceof NullSnapField) {
            return null;
        }
        return SnapRecordUtils.getInt(snapField);
    }

    protected DateTime getTimeFromSnap(PriceRecord priceRecord, VwdFieldDescription.Field field) {
        if (!(priceRecord instanceof PriceRecordVwd)) {
            return null;
        }
        PriceRecordVwd priceRecordVwd = (PriceRecordVwd) priceRecord;
        SnapField snapField = priceRecordVwd.getSnapRecord().getField(field.id());
        if (snapField instanceof NullSnapField) {
            return null;
        }
        if (!snapField.getType().isTime()) {
            return null;
        }

        int date = SnapRecordUtils.getDate(priceRecordVwd.getSnapRecord());
        return toDateTime(date, SnapRecordUtils.getInt(snapField));
    }

    protected DateTime getDateFromSnap(PriceRecord priceRecord, VwdFieldDescription.Field field) {
        if (!(priceRecord instanceof PriceRecordVwd)) {
            return null;
        }
        PriceRecordVwd priceRecordVwd = (PriceRecordVwd) priceRecord;
        SnapField snapField = priceRecordVwd.getSnapRecord().getField(field.id());
        if (snapField instanceof NullSnapField) {
            return null;
        }
        if (!snapField.getType().isDate()) {
            return null;
        }

        return toDateTime(SnapRecordUtils.getInt(snapField), 0);
    }

    protected DateTime toDateTime(int date, int time) {
        return (date > 0 && time >= 0) ? DateUtil.toDateTime(date, time) : null;
    }


    // ----------- formatter

    protected String format(Currency currency) {
        if (currency == null) {
            return null;
        }
        else {
            return currency.getSymbolIso();
        }
    }

    protected String formatTime(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        else {
            return TIME_FORMATTER.print(dateTime);
        }
    }

    protected String format(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        else {
            return DATE_TIME_FORMATTER.print(dateTime);
        }
    }

    protected String format(Long price) {
        if (price == null) {
            return null;
        }
        else {
            return Long.toString(price);
        }
    }

    protected String format(BigDecimal number) {
        if (number == null) {
            return null;
        }
        else {
            return number.setScale(4, BigDecimal.ROUND_HALF_UP).toPlainString();
        }
    }

    protected boolean isValidPrice(BigDecimal number) {
        return ((number != null) && (number.compareTo(BigDecimal.ZERO) != 0));
    }

    protected String volume(Price price) {
        if (price == null || price.getVolume() == null) {
            return null;
        }
        else {
            return price.getVolume().toString();
        }
    }

    protected String supplement(Price price) {
        if (price == null || price.getSupplement() == null) {
            return null;
        }
        else {
            return price.getSupplement();
        }
    }

    protected String buildExchangesString(List<Quote> quotes) {
        StringBuilder sb = new StringBuilder();
        quotes.sort(QuoteSorter.MARKET_SORTER);

        sb.append("[");
        boolean addComma = false;
        String lastSymbol, symbol = null;
        for (Quote quote : quotes) {
            lastSymbol = symbol;
            symbol = OldMergerToolkit.getMarketSymbol(quote);
            if (symbol.equals(lastSymbol)) {
                continue;
            }
            if (addComma) {
                sb.append(", ");
            }
            else {
                addComma = true;
            }
            sb.append(symbol);
        }
        sb.append("]");

        return sb.toString();
    }

}
