/*
 * KurseDde.java
 *
 * Created on 27.10.2008 10:12:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.webxl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.DataBinderUtils;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class KurseDde extends AbstractDde {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final DataBinderUtils.Mapping MAPPING =
            new DataBinderUtils.Mapping().add("Xun", "xun").add("lattribs").add("lsymbols");

    protected DataBinderUtils.Mapping getParameterMapping() {
        return MAPPING;
    }

    public static class Command extends AbstractDde.Command {
        private String lsymbols;
        private String lattribs;

        @NotNull
        public String getLsymbols() {
            return lsymbols;
        }

        public void setLsymbols(String lsymbols) {
            this.lsymbols = lsymbols;
        }

        @NotNull
        public String getLattribs() {
            return lattribs;
        }

        public void setLattribs(String lattribs) {
            this.lattribs = lattribs;
        }
    }

    public KurseDde() {
        super(Command.class);
    }

    protected String getContent(Object o) {
        final Command cmd = (Command) o;

        final String[] symbols = cmd.getLsymbols().split(",");

        if (symbols.length > 50) {
            throw new IllegalStateException("too many symbols (" + symbols.length + " > 50): " + cmd.getLsymbols());
        }
        final Map<String, Quote> symbolToQuote = new LinkedHashMap<>(symbols.length);
        for (final String symbol : symbols) {
            try {
                symbolToQuote.put(symbol, this.instrumentProvider.identifyQuote(symbol, SymbolStrategyEnum.AUTO, null, null));
            }
            catch (Exception e) {
                symbolToQuote.put(symbol, null);
                this.logger.info("<doHandle> no quote for " + symbol);
            }
        }

        return getQuoteContent(cmd.getLattribs(), symbolToQuote);
    }

}

