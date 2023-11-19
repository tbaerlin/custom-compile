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
import de.marketmaker.istar.ratios.frontend.PreferSwissQuoteStrategy;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WntFinder extends AbstractFindersuchergebnis {

    public WntFinder() {
        super(ProviderCommand.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final ProviderCommand cmd = (ProviderCommand) o;

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields =
                getFields(InstrumentTypeEnum.WNT, cmd.getProviderPreference());

        final RatioSearchRequest rsr = createRequest(InstrumentTypeEnum.WNT, cmd, fields);

        // TODO: remove after Update in mmf3
        if ("SMF".equals(cmd.getProviderPreference()) && rsr.getDataRecordStrategyClassname() == null) {
            rsr.setDataRecordStrategyClass(PreferSwissQuoteStrategy.class);
        }
       
        final Map<String, String> parameters = parseQuery(cmd.getQuery(), fields);
        replaceUnderlyingSymbol(parameters, cmd.isWithSiblingsForUnderlying());
        rsr.addParameters(parameters);

        final List<String> sortfields = asSortfields(fields);

        final ListResult listResult = ListResult.create(cmd, sortfields, "name", 0);
        final String sortfield = fields.get(RatioDataRecord.Field.valueOf(listResult.getSortedBy())).name();
        rsr.addParameter("sort1", sortfield);

        rsr.addParameter("i", Integer.toString(cmd.getOffset()));
        rsr.addParameter("n", Integer.toString(cmd.getAnzahl()));
        rsr.addParameter("sort1:D", Boolean.toString(!listResult.isAscending()));

        final RatioSearchResponse sr = this.ratiosProvider.search(rsr);

        if (!sr.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final Map<String, Object> model = createResultModel(sr, listResult, fields, isWithPrices(cmd, false), false);

        return new ModelAndView("wntfinder", model);
    }
}