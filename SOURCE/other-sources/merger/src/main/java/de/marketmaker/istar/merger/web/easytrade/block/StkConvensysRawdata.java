/*
 * StkKennzahlenBilanz.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;

/**
 * Provides the raw companyprofile xml file as delivered by the data provider Convensys.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkConvensysRawdata extends StkConvensysData {
    public static class Command extends DefaultSymbolCommand {
        private boolean keydata = false;
        private boolean transformed = false;

        /**
         * Specifies if only the keydata (true) or the complete profile (false) is provided.
         */
        public boolean isKeydata() {
            return keydata;
        }

        public void setKeydata(boolean keydata) {
            this.keydata = keydata;
        }

        /**
         * if true return transformed files for ease of further processing
         */
        @MmInternal
        public boolean isTransformed() {
            return transformed;
        }

        public void setTransformed(boolean transformed) {
            this.transformed = transformed;
        }
    }

    public StkConvensysRawdata() {
        super(Command.class);
    }

    private String keydataXsd;

    private String portraitXsd;

    public void setKeydataXsd(String keydataXsd) {
        this.keydataXsd = keydataXsd;
    }

    public void setPortraitXsd(String portraitXsd) {
        this.portraitXsd = portraitXsd;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Map<String, Object> model = new HashMap<>();

        final Quote quote = getQuote(cmd);
        model.put("quote", quote);

        if (quote != null) {
            model.put("rawdata", getRawdata(quote.getInstrument(), cmd.isKeydata(), cmd.isTransformed(), false));
        }

        return new ModelAndView("stkconvensysrawdata", model);
    }

    protected String getXsd(final boolean keydata) {
        return keydata ? this.keydataXsd : this.portraitXsd;
    }
}