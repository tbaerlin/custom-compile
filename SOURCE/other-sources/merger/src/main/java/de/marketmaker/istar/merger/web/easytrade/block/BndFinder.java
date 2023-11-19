/*
 * FndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Arrays;
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
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * Queries bonds that match given criteria.
 * <p>
 * Criteria are specified by the <code>query</code> parameter, which is composed of field predicates.
 * Note that some fields have only limited values. Those values can be queried using
 * {@see BND_FinderMetadata}.
 * </p>
 * <p>
 * Allowed search fields can be found in the sort field lists in the response.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample query isin='DE0007'
 */
public class BndFinder extends AbstractFindersuchergebnis {

    public static class Command extends AbstractFindersuchergebnis.Command {
        private boolean withDetailedSymbol;

        public boolean isWithDetailedSymbol() {
            return withDetailedSymbol;
        }

        public void setWithDetailedSymbol(boolean withDetailedSymbol) {
            this.withDetailedSymbol = withDetailedSymbol;
        }
    }

    public BndFinder() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields = getFields(InstrumentTypeEnum.BND);

        final RatioSearchRequest rsr = createRequest(InstrumentTypeEnum.BND, cmd, fields);
        if (cmd.getIid() != null && cmd.getIid().length > 0) {
            rsr.setInstrumentIds(Arrays.asList(cmd.getIid()));
        }
        rsr.setWithDetailedSymbol(cmd.isWithDetailedSymbol());

        final Map<String, String> parameters = parseQuery(cmd.getQuery(), fields);
        rsr.addParameters(parameters);

        rsr.addParameter("i", Integer.toString(cmd.getOffset()));
        rsr.addParameter("n", Integer.toString(cmd.getAnzahl()));

        final List<String> sortfields = asSortfields(fields);
        final ListResult listResult = ListResult.create(cmd, sortfields, "name", 0);

        addSorts(rsr, listResult, fields);

        final RatioSearchResponse sr = this.ratiosProvider.search(rsr);

        if (!sr.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final Map<String, Object> model = createResultModel(sr, listResult, fields, isWithPrices(cmd, false), false);

        return new ModelAndView("bndfinder", model);
    }
}