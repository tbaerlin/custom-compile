/*
 * FndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.PreferConFuturesQuoteStrategy;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * Queries futures that match given criteria.
 * <p>
 * Criteria are specified by the <code>query</code> parameter, which is composed of field predicates.
 * Note that some fields have only limited values. Those values can be queried using
 * {@see FUT_FinderMetadata}.
 * </p>
 * <p>
 * Allowed search fields can be found in the sort field lists in the response.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample query expirationDate==x_3m
 */
public class FutFinder extends AbstractFindersuchergebnis {
    public FutFinder() {
        super(Command.class);
    }

    @Override
    protected void onBind(HttpServletRequest request, Object command) throws Exception {
        super.onBind(request, command);
        Command cmd = (Command) command;
        if (cmd.getDataRecordStrategy() == null) {
            cmd.setDataRecordStrategy(PreferConFuturesQuoteStrategy.Type.PREFER_CON_FUTURES_QUOTE);
        }
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = getFields(InstrumentTypeEnum.FUT);

        final RatioSearchRequest rsr
                = createRequest(InstrumentTypeEnum.FUT, cmd.getAdditionalType(),
                cmd.getDataRecordStrategy(), fields, true, null);

        final Map<String, String> parameters = parseQuery(cmd.getQuery(), fields);
        replaceUnderlyingSymbol(parameters, cmd.isWithSiblingsForUnderlying());
        rsr.addParameters(parameters);

        final List<String> sortfields = asSortfields(fields);

        final ListResult listResult = ListResult.create(cmd, sortfields, "name", 0);
        final String sortfield
                = fields.get(RatioDataRecord.Field.valueOf(listResult.getSortedBy())).name();
        rsr.addParameter("sort1", sortfield);

        rsr.addParameter("i", Integer.toString(cmd.getOffset()));
        rsr.addParameter("n", Integer.toString(cmd.getAnzahl()));
        rsr.addParameter("sort1:D", Boolean.toString(!listResult.isAscending()));

        final RatioSearchResponse sr = this.ratiosProvider.search(rsr);

        if (!sr.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final Map<String, Object> model = createResultModel(sr, listResult, fields, isWithPrices(cmd, true), false);

        return new ModelAndView("futfinder", model);
    }
}