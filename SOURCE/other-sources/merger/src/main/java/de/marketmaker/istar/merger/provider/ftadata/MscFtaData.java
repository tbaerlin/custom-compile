/*
 * FTAData.java
 *
 * Created on 5/17/13 1:54 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.ftadata;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.ErrorUtils;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Willenbrock
 */
public class MscFtaData extends EasytradeCommandController {
    private EasytradeInstrumentProvider instrumentProvider;

    private FTADataProvider ftaDataProvider;

    public MscFtaData() {
        super(DefaultSymbolCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setFtaDataProvider(FTADataProvider ftaDataProvider) {
        this.ftaDataProvider = ftaDataProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws Exception {
        final DefaultSymbolCommand command = (DefaultSymbolCommand) o;
        final Quote quote = this.instrumentProvider.getQuote(command);

        if (quote == null) {
            ErrorUtils.rejectSymbol(command.getSymbol(), errors);
            return null;
        }

        final FTADataRequest ftaRequest = new FTADataRequest(quote.getInstrument().getId());
        final FTADataResponse ftaResponse = this.ftaDataProvider.getFTAData(ftaRequest);

        if (!ftaResponse.isValid()) {
            errors.reject("fta.requestfailed", "invalid request response");
            return null;
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("masterdata", ftaResponse.getMasterData());
        model.put("metadata", ftaResponse.getMetaData());
        model.put("commentary", ftaResponse.getCommentary());
        return new ModelAndView("mscftadata", model);
    }
}
