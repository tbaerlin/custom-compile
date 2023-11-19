package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.special.DzBankRecord;
import de.marketmaker.istar.merger.provider.DzBankRecordProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 19.10.12 13:50
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class GisStaticData extends EasytradeCommandController {

    private DzBankRecordProvider dzProvider;
    private EasytradeInstrumentProvider instrumentProvider;


    public void setDzProvider(DzBankRecordProvider dzProvider) {
        this.dzProvider = dzProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public GisStaticData() {
        super(DefaultSymbolCommand.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) throws Exception {
        SymbolCommand cmd = (SymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final List<DzBankRecord> records = this.dzProvider.getDzBankRecords(Collections.singletonList(quote.getInstrument().getId()));

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("dzrecord", records.isEmpty() ? null : records.get(0));
        return new ModelAndView("gisstaticdata", model);
    }
}
