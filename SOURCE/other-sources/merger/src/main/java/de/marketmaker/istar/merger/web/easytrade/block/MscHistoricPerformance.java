/*
 * StkKursdaten.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscHistoricPerformance extends MscHistoricData {
    public static final class Command extends MscHistoricData.Command {
        @Override
        public boolean isBlendCorporateActions() {
            return true;
        }

        @Override
        public boolean isBlendDividends() {
            return true;
        }

        @Override
        public int getNumTrades() {
            return 0;
        }

        @Override
        public ElementDataType getType() {
            return ElementDataType.PERFORMANCE;
        }

        @Override
        public TickType getTickTypeChicago() {
            return TickType.TRADE;
        }
    }

    public MscHistoricPerformance() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws IOException {

        final Command cmd = (Command) o;
        final Quote quote = getQuote(cmd);
        final Map<String, Object> model = new MscHistoricDataMethod(this, quote, cmd).invoke();
        return new ModelAndView("mschistoricperformance", model);
    }
}
