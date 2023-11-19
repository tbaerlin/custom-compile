package de.marketmaker.istar.merger.web.xmlmarket;


import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.*;
import de.marketmaker.istar.domain.instrument.*;
import de.marketmaker.istar.domain.timeseries.Timeseries;
import de.marketmaker.istar.feed.vwd.PriceRecordVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.LocalTime;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class IntradayResponse extends MasterDataResponse {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int numResults;
    private final Timeseries<TickEvent> timeseries;
    private final List<AggregatedTickImpl> ticklist;


    IntradayResponse(Instrument instrument, Quote selectedQuote, List<Quote> quotes,
                     PriceRecord selectedPriceRecord,
                     Timeseries<TickEvent> timeseries, int numResults) {
        super(instrument.getInstrumentType(), selectedQuote, quotes, instrument, selectedPriceRecord);
        this.numResults = numResults;
        this.timeseries = timeseries;
        this.ticklist = null;
    }

    IntradayResponse(Instrument instrument, Quote selectedQuote, List<Quote> quotes,
                            PriceRecord selectedPriceRecord,
                            List<AggregatedTickImpl> licklist, int numResults) {
        super(instrument.getInstrumentType(), selectedQuote, quotes, instrument, selectedPriceRecord);
        this.numResults = numResults;
        this.timeseries = null;
        this.ticklist = licklist;
    }

    @Override
    public void render(PrintWriter writer) {
        createDocument().render(writer);
    }

    protected XmlDocument createDocument() {
        final XmlNode content = node("intraday")
                .attribute("type", OldMergerToolkit.getOriginalTypeString(type))
                .child(generated())
                .child(staticData(instrument))
                .child(ratios(selectedPriceRecord))
                .child(priceData(instrument, selectedQuote, selectedPriceRecord))
                .child(ticks())
                ;

        return new XmlDocument()
                .child(XmlDocument.XmlDeclaration.ISO_8859_2)
                .child(content)
                ;
    }


    private XmlNode ticks() {
        if (timeseries != null) {
            return ticks(timeseries);
        }
        else if (ticklist != null) {
            return ticks(ticklist);
        }
        return null;
    }

    private XmlNode ticks(List<AggregatedTickImpl> ticklist) {
        XmlNode ticks = node("ticks");

        Iterator<AggregatedTickImpl> i = ticklist.iterator();
        while (i.hasNext()) {
            AggregatedTickImpl tick = i.next();
            ticks.child(node("tick")
                    .child(node("time").text(formatTime(tick.getInterval().getStart())))
                    .child(node("price").text(format(tick.getOpen())))
            );
        }

        return ticks;
    }


    private XmlNode ticks(Timeseries<TickEvent> timeseries) {
        XmlNode ticks = node("ticks");
        int count = 1;
        for (DataWithInterval<TickEvent> dataWithInterval : timeseries) {
            TickEvent data = dataWithInterval.getData();
            if (!data.isVolumePresent()) {
                continue;
            }
            final int sec = data.getTime();
            final LocalTime localTime = DateUtil.secondsInDayToLocalTime(sec);

            XmlNode tick = node("tick");
            tick.attribute("type", "de.marketmaker.merger.servlet.Intraday$Tick");
            tick.child(node("time").text(TIME_FORMATTER.print(localTime)));
            tick.child(node("price").text(format(data.isPricePresent()?PriceCoder.decode(data.getPrice()):null)));
            // crazy local inconsistency:
            DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
            dfs.setGroupingSeparator('.');
            DecimalFormat format = new DecimalFormat("###,###", dfs);
            tick.child(node("volume").text(data.isVolumePresent()
                    ? format.format(data.getVolume())
                    : null));
            tick.child(node("supplement").text(data.getSupplement()));
            ticks.firstChild(tick); // we need to reverse the timeseries

            if (count++ > numResults) {
                break;
            }
        }
        if (ticks.children.size() == 0) {
            return null;
        }
        return ticks;
    }

    protected XmlNode ratios(PriceRecordVwd priceRecord) {
        if (type != InstrumentTypeEnum.CUR
                && type != InstrumentTypeEnum.MER
                && type != InstrumentTypeEnum.IND) {
            return super.ratios(priceRecord);
        }
        else {
            return null;
        }
    }

    private XmlNode priceData(Instrument instrument, Quote quote,
                              PriceRecord priceRecord) {

        final XmlNode result = node("priceData");

        if (type != InstrumentTypeEnum.CUR
                && type != InstrumentTypeEnum.MER
                && type != InstrumentTypeEnum.IND) {
            result.child(minQuotation(quote));
            MinimumQuotationSize minQuoteSize = quote.getMinimumQuotationSize();
            result.child(node("WM_quotedPer").text(minQuoteSize.getUnit().name().toLowerCase()));
        }

        result.children(getOpenElements(priceRecord))
                .children(getHighElements(priceRecord))
                .children(getLowElements(priceRecord))
                .children(getCloseElements(priceRecord));

        result.children(getSpotrateElements(priceRecord));

        if (type != InstrumentTypeEnum.CUR
                && type != InstrumentTypeEnum.MER
                && type != InstrumentTypeEnum.IND) {
            result.children(getVolumeElements(priceRecord));
            result.child(node("ask").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Brief))));
            result.child(node("bid").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Geld))));
        }

        BigDecimal price = getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Bezahlt);
        result.child(node("price").text(format(escapePriceWithBidAsk(instrument, quote, priceRecord, price))));

        if (type != InstrumentTypeEnum.BND
                && type != InstrumentTypeEnum.CUR
                && type != InstrumentTypeEnum.MER
                && type != InstrumentTypeEnum.IND) {
            String supplement = getStringFromSnap(priceRecord, VwdFieldDescription.ADF_Bezahlt_Kurszusatz);
            result.child(node("supplement").text(emptyForNull(supplement)));
        }

        result.child(getTimeNode(instrument, quote, priceRecord));
        result.children(getLastPriceNodes(instrument, quote, priceRecord));

        result.child(node("yearStart"))
                .child(node("exchange").text(OldMergerToolkit.getMarketSymbol(selectedQuote)))
                .children(getDiffElements(priceRecord));

        if (type != InstrumentTypeEnum.CUR
                && type != InstrumentTypeEnum.MER
                && type != InstrumentTypeEnum.IND) {
            result.children(getSupplementElements(priceRecord));
        }
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
            case CUR:
                staticData.children(staticDataSimple());
                break;
            case MER:
                staticData.children(staticDataSimple());
                break;
            case IND:
                staticData.children(staticDataSimple());
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

    @Override
    protected List<XmlNode> getLowElements(PriceRecord priceRecord) {
        ArrayList<XmlNode> elements = new ArrayList<XmlNode>();
        if (priceRecord == null) {
            return elements;
        }
        elements.add(node("low").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Tagestief))));
        elements.add(node("previousLow").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Tagestief_Vortag))));
        elements.add(node("lowYear").text(format(getPriceFromSnap(priceRecord, VwdFieldDescription.ADF_Jahrestief))));
        elements.add(node("lowYearDate").text(format(priceRecord.getLowYear().getDate())));
        return elements;
    }

    @Override
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
        return elements;
    }

    @Override
    protected List<XmlNode> getVolumeElements(PriceRecord priceRecord) {
        ArrayList<XmlNode> elements = new ArrayList<XmlNode>();
        // orig: getVolumeAccumulated
        elements.add(node("volumeTotal").text(zeroForNull(getStringFromSnap(priceRecord, VwdFieldDescription.ADF_Umsatz_gesamt))));
        if (priceRecord == null) {
            return elements;
        }
        elements.add(node("volumeAsk").text(zeroForNull(volume(priceRecord.getAsk()))));
        elements.add(node("volumeBid").text(zeroForNull(volume(priceRecord.getBid()))));
        elements.add(node("volumeTrade").text(zeroForNull(volume(priceRecord.getPrice()))));

        Long numberOfTrades = priceRecord.getNumberOfTrades();
        elements.add(node("numberOfTrades").text((numberOfTrades==null)?"0":Long.toString(numberOfTrades) ));
        elements.add(node("valueTotal").text(format(priceRecord.getTurnoverDay()==null?BigDecimal.ZERO:priceRecord.getTurnoverDay())));

        return elements;
    }

}
