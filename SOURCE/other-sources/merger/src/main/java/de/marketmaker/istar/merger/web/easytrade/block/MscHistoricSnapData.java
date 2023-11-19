/*
 * StkKursdaten.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.AggregatedTickImpl;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.NoDataException;
import de.marketmaker.istar.merger.provider.historic.data.AggregatedValueImpl;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;

import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.FND;
import static de.marketmaker.istar.merger.web.easytrade.TickDataCommand.ElementDataType.FUND;
import static de.marketmaker.istar.merger.web.easytrade.TickDataCommand.ElementDataType.OHLCV;

/**
 * MSC_HistoricSnapData will forward the request to MSC_HistoricData with a date range
 * from given date -1 year to the given date. It will then check the results reverse in time
 * i.e. newest to oldest and return the first value where at least one of the following fields
 * has been defined:
 * <ul>
 *     <li>Open</li>
 *     <li>High</li>
 *     <li>Low</li>
 *     <li>Close</li>
 *     <li>Volume</li>
 * </ul>
 * For FUND only Close and High will be evaluated.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscHistoricSnapData extends MscHistoricData {
    public static final class Command extends DefaultSymbolCommand {
        private DateTime date;

        private boolean blendCorporateActions = true;

        private boolean blendDividends = false;

        private TickImpl.Type tickType = TickImpl.Type.TRADE;

        private boolean inferYieldBasedQuote = false;

        public DateTime getDate() {
            return this.date != null ? this.date : new DateTime();
        }

        public void setDate(DateTime date) {
            this.date = date;
        }

        public boolean isBlendCorporateActions() {
            return blendCorporateActions;
        }

        public void setBlendCorporateActions(boolean blendCorporateActions) {
            this.blendCorporateActions = blendCorporateActions;
        }

        public boolean isBlendDividends() {
            return blendDividends;
        }

        public void setBlendDividends(boolean blendDividends) {
            this.blendDividends = blendDividends;
        }

        public TickImpl.Type getTickType() {
            return tickType;
        }

        public void setTickType(TickImpl.Type tickType) {
            this.tickType = tickType;
        }

        public boolean isInferYieldBasedQuote() {
            return inferYieldBasedQuote;
        }

        public void setInferYieldBasedQuote(boolean inferYieldBasedQuote) {
            this.inferYieldBasedQuote = inferYieldBasedQuote;
        }
    }

    public MscHistoricSnapData() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws IOException {

        final Command cmd = (Command) o;
        final Quote quote = getQuote(cmd);

        final Map<String, Object> model = buildModel(request, cmd, quote);

        if (model == null) {
            throw new NoDataException(quote.getId() + ".qid at " + cmd.getDate());
        }

        model.put("quote", quote);
        model.put("tickType", TickType.TRADE.name());

        return new ModelAndView("mschistoricsnapdata", model);
    }

    private Map<String, Object> buildEodStandardModel(Map<String, Object> hm) {
        @SuppressWarnings({"unchecked"})
        final List<AggregatedValueImpl> ticks = (List<AggregatedValueImpl>) hm.get("trades");
        if (ticks != null) {
            for (int i = ticks.size(); i-- > 0; ) {
                final AggregatedValueImpl at = ticks.get(i);
                if (isDefined(at)) {
                    return createStandardModel(at);
                }
            }
        }
        return null;
    }

    private Map<String, Object> buildStandardModel(Map<String, Object> hm) {
        @SuppressWarnings({"unchecked"})
        final List<AggregatedTickImpl> ticks = (List<AggregatedTickImpl>) hm.get("trades");
        if (ticks != null) {
            for (int i = ticks.size(); i-- > 0; ) {
                final AggregatedTickImpl at = ticks.get(i);
                if (isDefined(at)) {
                    return createStandardModel(at);
                }
            }
        }
        return null;
    }

    private Map<String, Object> createStandardModel(AggregatedTickImpl at) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("aggregate", at);
        return result;
    }

    private Map<String, Object> createStandardModel(AggregatedValueImpl at) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("aggregate", at);
        return result;
    }

    private boolean isDefined(AggregatedTickImpl at) {
        return at.getClose() != null
                || at.getOpen() != null
                || at.getHigh() != null
                || at.getLow() != null
                || at.getVolume() != null;
    }

    private boolean isDefined(AggregatedValueImpl at) {
        return at.getClose() != null
                || at.getOpen() != null
                || at.getHigh() != null
                || at.getLow() != null
                || at.getVolume() != null;
    }

    private Map<String, Object> buildEodFundModel(Map<String, Object> hm) {
        @SuppressWarnings({"unchecked"})
        final List<AggregatedValueImpl> fundTicks = (List<AggregatedValueImpl>) hm.get("fundTs");
        if (fundTicks != null) {
            for (int i = fundTicks.size(); i-- > 0; ) {
                final AggregatedValueImpl at = fundTicks.get(i);
                if (at.getRedemptionPrice() != null || at.getIssuePrice() != null) {
                    return createFundModel(at.getRedemptionPrice(), at.getIssuePrice(), at.getInterval().getStart());
                }
            }
        }
        return null;
    }

    private Map<String, Object> buildFundModel(Map<String, Object> hm) {
        @SuppressWarnings({"unchecked"})
        final List<AggregatedTickImpl> fundTicks = (List<AggregatedTickImpl>) hm.get("fundTs");
        if (fundTicks != null) {
            for (int i = fundTicks.size(); i-- > 0; ) {
                final AggregatedTickImpl at = fundTicks.get(i);
                if (at.getClose() != null || at.getHigh() != null) {
                    return createFundModel(at.getClose(), at.getHigh(), at.getInterval().getStart());
                }
            }
        }
        return null;
    }

    private Map<String, Object> createFundModel(BigDecimal rp, BigDecimal ip, final DateTime date) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("issuePrice", ip);
        result.put("repurchasingPrice", rp);
        result.put("date", date);
        return result;
    }

    private Map<String, Object> buildModel(HttpServletRequest request,Command cmd, Quote quote) throws IOException {
        Map<String, Object> model = createModel(request, cmd, quote);

        final boolean eod = isEodModel(model);

        if (quote.getInstrument().getInstrumentType() == FND) {
            return eod ? buildEodFundModel(model) : buildFundModel(model);
        }
        else {
            return eod ? buildEodStandardModel(model) : buildStandardModel(model);
        }
    }

    private Map<String, Object> createModel(HttpServletRequest request,
            Command cmd, Quote quote) throws IOException {
        final MscHistoricData.Command hc = new MscHistoricData.Command();
        hc.setBlendCorporateActions(cmd.isBlendCorporateActions());
        hc.setBlendDividends(cmd.isBlendDividends());
        hc.setInferTickType(cmd.isInferYieldBasedQuote());
        hc.setSymbol(cmd.getSymbol());
        hc.setSymbolStrategy(cmd.getSymbolStrategy());
        hc.setTickType(cmd.getTickType());
        hc.setStart(cmd.getDate().minusYears(1));
        hc.setEnd(cmd.getDate());
        hc.setType(quote.getInstrument().getInstrumentType() == FND ? FUND : OHLCV);
        return createModel(request, hc, quote);
    }
}