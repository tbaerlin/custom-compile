/*
 * MscProfessionalTrades.java
 *
 * Created on 15.05.12 11:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.IndexComposition;
import de.marketmaker.istar.domainimpl.data.NullPrice;
import de.marketmaker.istar.domainimpl.data.PriceImpl;
import de.marketmaker.istar.feed.api.FeedConnector;
import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.feed.ordered.OrderedSnapRecord;
import de.marketmaker.istar.feed.ordered.tick.TickDecompressor;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickRecordImpl;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.EntitlementQuoteProvider;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;

import static de.marketmaker.istar.feed.snap.SnapRecordUtils.getLong;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ADF_Block_Tr_Umsatz_gesamt;

/**
 * Returns the professional trades (aka block trades) for a specific day at a specific market.
 * Since the number of those rather trades is limited, there are no paging or further filtering option;
 * clients may filter the returned data as needed.
 * @author oflege
 */
public class MscProfessionalTrades extends EasytradeCommandController {
    public static class Command {
        private String market;

        private LocalDate date = new LocalDate();

        /**
         * vwd market code
         */
        @RestrictedSet("MFOX,XBRD,XEUE,XLIF,XMAT,XMON")
        public String getMarket() {
            return market;
        }

        public void setMarket(String market) {
            this.market = market;
        }

        /**
         * request block trades for this day, default is the current day
         */
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    public static class ProfessionalTrade {
        private final SnapRecord sr;

        private final LocalDate date;

        private final PriceQuality priceQuality;

        private Price blockTrade;

        private ProfessionalTrade(PriceQuality priceQuality, LocalDate date, SnapRecord sr) {
            // SnapRecordDefault[26=42020, 1270=1.17000, 1570=386, 1572=386, 1606=42020, 1776=476012.73, 1981=IM.XEQD.22.2T]
            this.priceQuality = priceQuality;
            this.date = date;
            this.sr = sr;

            this.blockTrade = doGetBlockTrade();
        }

        private DateTime getDate(VwdFieldDescription.Field timeField) {
            final SnapField field = this.sr.getField(timeField.id());
            if (!field.isDefined()) {
                return null;
            }
            return DateUtil.toDateTime(DateUtil.toYyyyMmDd(this.date), (Integer) field.getValue());
        }

        public DateTime getDate() {
            return getDate(VwdFieldDescription.ADF_Zeit);
        }

        private DateTime getBlockTradeDate() {
            return getDate(VwdFieldDescription.ADF_Block_Trade_Zeit);
        }

        public Price getBlockTrade() {
            return this.blockTrade;
        }

        private Price doGetBlockTrade() {
            final SnapField field = this.sr.getField(VwdFieldDescription.ADF_Block_Trade_Bezahlt.id());
            final BigDecimal value = field.getPrice();
            if (value == null) {
                return NullPrice.INSTANCE;
            }
            final SnapField volume = this.sr.getField(VwdFieldDescription.ADF_Block_Trade_Umsatz.id());

            return new PriceImpl(value, (volume != null && volume.isDefined()) ? getLong(volume) : null,
                    null, getBlockTradeDate(), this.priceQuality);
        }

        public Long getVolumeDay() {
            final SnapField field = this.sr.getField(ADF_Block_Tr_Umsatz_gesamt.id());
            return field.isDefined() ? getLong(field) : null;
        }

        public String getVwdcode() {
            final SnapField field = this.sr.getField(VwdFieldDescription.ADF_Symbol_vwd.id());
            return (String) field.getValue();
        }
    }

    private FeedConnector feedConnector;

    private EntitlementQuoteProvider entitlementQuoteProvider;

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public MscProfessionalTrades() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIndexCompositionProvider(ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setEntitlementQuoteProvider(EntitlementQuoteProvider entitlementQuoteProvider) {
        this.entitlementQuoteProvider = entitlementQuoteProvider;
    }

    public void setFeedConnector(FeedConnector feedConnector) {
        this.feedConnector = feedConnector;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        checkPermission(Selector.OCTOPUS_DATA);

        final Command cmd = (Command) o;
        final List<ProfessionalTrade> trades = getTrades(cmd);

        final Map<String, Object> model = new HashMap<>();
        model.put("trades", trades);

        final List<String> optionClasses = getClasses("octopus_enoc_" + cmd.getMarket());
        final List<String> futureClasses = getClasses("octopus_future_" + cmd.getMarket());

        model.put("optionClasses", optionClasses);
        model.put("futureClasses", futureClasses);

        return new ModelAndView("mscprofessionaltrades", model);
    }

    private List<String> getClasses(final String listname) {
        IndexCompositionResponse response
                = this.indexCompositionProvider.getIndexCompositionByName(listname);
        if (!response.isValid()) {
            return Collections.emptyList();
        }

        final IndexComposition idSet = response.getIndexComposition();
        HashSet<Long> qids = new HashSet<>(idSet.getQids());
        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(qids);
        final Set<String> symbols = new HashSet<>();
        for (final Quote quote : quotes) {
            if (quote != null) {
                qids.remove(quote.getId());
                final String vwdcode = quote.getSymbolVwdcode();
                if (vwdcode != null) {
                    symbols.add(vwdcode.substring(0, vwdcode.indexOf(".")));
                }
            }
        }
        if (!qids.isEmpty()) {
            this.logger.warn("<getClasses> unknown qids for " + listname + ": " + qids);
        }

        final List<String> classes = new ArrayList<>(symbols);
        classes.sort(null);
        return classes;
    }

    private List<ProfessionalTrade> getTrades(Command cmd) {
        final int day = DateUtil.toYyyyMmDd(cmd.getDate());

        final String key = "1." + cmd.getMarket() + ".PROF";
        final PriceQuality pq = getPriceQuality(key);
        if (pq != PriceQuality.REALTIME && pq != PriceQuality.DELAYED) {
            return Collections.emptyList();
        }

        IntradayRequest intradayRequest = new IntradayRequest();
        IntradayRequest.Item item = new IntradayRequest.Item(key, pq == PriceQuality.REALTIME);
        item.setRetrieveTicks(day);
        intradayRequest.add(item);

        IntradayResponse intradayResponse = this.feedConnector.getIntradayData(intradayRequest);
        IntradayResponse.Item responseItem = intradayResponse.getItem(key);

        if (responseItem == null) {
            return Collections.emptyList();
        }

        TickRecord tr = responseItem.getTickRecord();
        if (!(tr instanceof TickRecordImpl)) {
            return Collections.emptyList();
        }

        TickRecordImpl impl = (TickRecordImpl) tr;
        AbstractTickRecord.TickItem tickItem = impl.getItem(day);
        if (tickItem == null) {
            return Collections.emptyList();
        }

        if (tickItem.getEncoding() == AbstractTickRecord.TickItem.Encoding.TICK3
                || tickItem.getEncoding() == AbstractTickRecord.TickItem.Encoding.TICKZ) {
            return decodeTrades(pq, cmd.getDate(), tickItem, impl.getLastTickDateTime());
        }
        this.logger.warn("<getTrades> cannot handle type " + tickItem.getEncoding() + " for " + key);
        return Collections.emptyList();
    }

    private List<ProfessionalTrade> decodeTrades(PriceQuality pq, LocalDate date,
            AbstractTickRecord.TickItem tickItem,
            DateTime lastTickDateTime) {
        final List<ProfessionalTrade> result = new ArrayList<>();

        for (TickDecompressor.Element e : new TickDecompressor(tickItem)) {
            // TODO: ensure sr is not after lastTickDateTime (if that is not null)
            OrderedSnapRecord sr = new OrderedSnapRecord(e.getData().getAsByteArray(), 0);
            final ProfessionalTrade pt = new ProfessionalTrade(pq, date, sr);
            if (isValid(pt)) {
                result.add(pt);
            }
        }
        return result;
    }

    private boolean isValid(ProfessionalTrade pt) {
        if (pt == NullPrice.INSTANCE) {
            return false;
        }

        final BigDecimal price = pt.getBlockTrade().getValue();
        final Long volume = pt.getBlockTrade().getVolume();
        if ((price == null || price.compareTo(BigDecimal.ZERO) == 0)
                && (volume == null || volume == 0L)) {
            return false;
        }
        return true;
    }

    private PriceQuality getPriceQuality(String key) {
        final Quote eQuote = this.entitlementQuoteProvider.getQuote(key);
        return RequestContextHolder.getRequestContext().getProfile().getPriceQuality(eQuote);
    }
}
