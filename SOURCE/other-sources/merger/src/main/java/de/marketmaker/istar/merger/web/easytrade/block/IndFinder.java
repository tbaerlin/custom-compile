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

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IndFinder extends AbstractFindersuchergebnis {
    public IndFinder() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = getFields(InstrumentTypeEnum.IND);

        final RatioSearchRequest rsr = createRequest(InstrumentTypeEnum.IND, cmd, fields);
        if (cmd.getIid() != null) {
            rsr.setInstrumentIds(Arrays.asList(cmd.getIid()));
        }

        final Map<String, String> parameters = parseQuery(FinderQueryParserSupport.ensureQuotedValues(cmd.getQuery()), fields);
        fixIids(parameters, rsr);
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

        return new ModelAndView("indfinder", model);
    }

    private void fixIids(Map<String, String> parameters, RatioSearchRequest rsr) {
        final String symbol = parameters.remove("symbol");
        if (!StringUtils.hasText(symbol)) {
            return;
        }

        final String[] symbols = symbol.split("@");
        rsr.setInstrumentIds(this.instrumentProvider.identifyInstrumentIds(symbols, SymbolStrategyEnum.AUTO));
    }
}