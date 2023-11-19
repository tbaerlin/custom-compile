/*
 * FndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.EnumEditor;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.ProviderSelectionCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolListCommand;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DataRecordStrategy;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
abstract class DerivativeMatrix extends AbstractFindersuchergebnis {
    public static class Command extends SymbolListCommand implements ProviderSelectionCommand {
        private String query;

        private boolean isWithSiblingsForUnderlying = true;

        private DataRecordStrategy.Type dataRecordStrategy;

        private String providerPreference;

        @RestrictedSet("VWD,SMF")
        public String getProviderPreference() {
            return providerPreference;
        }

        public void setProviderPreference(String providerPreference) {
            this.providerPreference = providerPreference;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public boolean isWithSiblingsForUnderlying() {
            return isWithSiblingsForUnderlying;
        }

        public void setWithSiblingsForUnderlying(boolean withSiblingsForUnderlying) {
            isWithSiblingsForUnderlying = withSiblingsForUnderlying;
        }

        public DataRecordStrategy.Type getDataRecordStrategy() {
            return dataRecordStrategy;
        }

        public void setDataRecordStrategy(DataRecordStrategy.Type dataRecordStrategy) {
            this.dataRecordStrategy = dataRecordStrategy;
        }
    }

    protected DerivativeMatrix(Class cmdClass) {
        super(cmdClass);
    }


    @Override
    protected void initBinder(HttpServletRequest httpServletRequest,
            ServletRequestDataBinder binder) throws Exception {
        super.initBinder(httpServletRequest, binder);

        binder.registerCustomEditor(DataRecordStrategy.Type.class,
                new EnumEditor<>(DataRecordStrategy.Type.class));
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Quote underlyingQuote = this.instrumentProvider.getQuote(cmd);

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = getFields(getInstrumentType(), cmd.getProviderPreference());

        final RatioSearchResponse sr = search(cmd, underlyingQuote, fields);

        if (!sr.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final ListResult listResult
                = ListResult.create(cmd, Collections.singletonList("name"), "name", 0);
        final Map<String, Object> model = createResultModel(sr, listResult, fields, false, true);
        model.put("underlyingQuote", underlyingQuote);
        return new ModelAndView(getTemplate(), model);
    }

    private RatioSearchResponse search(Command cmd, Quote underlyingQuote,
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        final RatioSearchRequest rsr
                = createRequest(getInstrumentType(), null, cmd.getDataRecordStrategy());

        final Map<String, String> parameters = parseQuery(cmd.getQuery(), fields);
        parameters.put("underlyingiid", Long.toString(underlyingQuote.getInstrument().getId()));
        replaceUnderlyingSymbol(parameters, cmd.isWithSiblingsForUnderlying());
        rsr.addParameters(parameters);

        rsr.addParameter("sort1", "name");
        rsr.addParameter("i", Integer.toString(cmd.getOffset()));
        rsr.addParameter("n", Integer.toString(cmd.isDisablePaging() ? 10000 : cmd.getAnzahl()));
        rsr.addParameter("sort1:D", Boolean.FALSE.toString());

        beforeSearch(cmd, rsr);

        return this.ratiosProvider.search(rsr);
    }

    protected void beforeSearch(Command cmd, RatioSearchRequest rsr) {
        // empty
    }

    protected abstract String getTemplate();

    protected abstract InstrumentTypeEnum getInstrumentType();
}