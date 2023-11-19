/*
 * MscRatingHistory.java
 *
 * Created on 13.09.12 09:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.rating.history.RatingHistory;
import de.marketmaker.istar.merger.provider.rating.history.RatingHistoryProvider;
import de.marketmaker.istar.merger.provider.rating.history.RatingHistoryRequest;
import de.marketmaker.istar.merger.provider.rating.history.RatingHistoryResponse;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * Retrieves rating history for a given instrument within one year.
 * <p>
 * There might be ratings provided by multiple rating agencies for a given instrument. Only
 * those ratings will be returned for which a given client is activated.
 * </p>
 * @author zzhao
 * @sample symbol 44951.qid
 */
public class MscRatingHistory extends EasytradeCommandController {

    public static class Command extends DefaultSymbolCommand {
        private boolean changesOnly = true;

        /**
         * @return If true, only rating points which differ from previous one are returned. If
         * false, all rating points are returned. Default is true.
         */
        public boolean isChangesOnly() {
            return changesOnly;
        }

        public void setChangesOnly(boolean changesOnly) {
            this.changesOnly = changesOnly;
        }
    }

    private EasytradeInstrumentProvider instrumentProvider;

    private RatingHistoryProvider ratingHistoryProvider;

    public MscRatingHistory() {
        super(Command.class);
    }

    public void setRatingHistoryProvider(RatingHistoryProvider ratingHistoryProvider) {
        this.ratingHistoryProvider = ratingHistoryProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);
        if (null == quote) {
            errors.reject("quote.unknown", "Quote with symbol: '" + cmd.getSymbol() + "' not found");
            return null;
        }
        else {
            final Instrument ins = quote.getInstrument();
            final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                    = AbstractFindersuchergebnis.getFields(ins.getInstrumentType());
            final RatingHistoryResponse resp =
                    this.ratingHistoryProvider.getRatingHistory(new RatingHistoryRequest(ins.getId()));
            if (!resp.isValid()) {
                errors.reject("server.error", "failed querying rating history");
                return null;
            }
            else {
                final Collection<RatioFieldDescription.Field> values = fields.values();
                final List<RatingHistory> histories = resp.getRatingHistories();
                final Iterator<RatingHistory> it = histories.iterator();
                while (it.hasNext()) {
                    final RatingHistory history = it.next();
                    if (!values.contains(history.getField())) {
                        it.remove();
                    }
                }

                if (!histories.isEmpty() && cmd.isChangesOnly()) {
                    histories.forEach(RatingHistory::aggregateUnchangedRatings);
                }

                final Map<String, Object> model = new HashMap<>();
                model.put("quote", quote);
                model.put("histories", histories);
                return new ModelAndView("mscratinghistory", model);
            }
        }
    }
}
