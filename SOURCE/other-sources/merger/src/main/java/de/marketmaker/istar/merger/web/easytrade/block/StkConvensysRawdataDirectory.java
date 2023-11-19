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

import de.marketmaker.istar.common.validator.NotNull;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.Pattern;
import de.marketmaker.istar.merger.provider.CompanyFundamentalsProvider;

/**
 * Provide a list of all symbols (ISINs) for which convensys data is available.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkConvensysRawdataDirectory extends EasytradeCommandController {

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

    private CompanyFundamentalsProvider companyFundamentalsProvider;

    public static class Command {
        private boolean keydata = false;

        private String referenceDate;

        /**
         * Provides only those symbols for which the profile is newer than the given referenceDate.
         * The referenceDate must have the format "yyyy-MM-dd'T'HH:mm:ss".
         * @sample 2012-06-01T00:00:00
         */
        @Pattern(regex = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")
        @NotNull
        public String getReferenceDate() {
            return referenceDate;
        }

        public void setReferenceDate(String referenceDate) {
            this.referenceDate = referenceDate;
        }

        /**
         * Specifies if the symbol list is provided for the keydata (true) or for the complete profiles (false).
         */
        public boolean isKeydata() {
            return keydata;
        }

        public void setKeydata(boolean keydata) {
            this.keydata = keydata;
        }
    }

    public StkConvensysRawdataDirectory() {
        super(Command.class);
    }

    public void setCompanyFundamentalsProvider(
            CompanyFundamentalsProvider companyFundamentalsProvider) {
        this.companyFundamentalsProvider = companyFundamentalsProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Map<String, Object> model = new HashMap<>();
        model.put("symbols", this.companyFundamentalsProvider.getConvensysRawdataDir(
                null, cmd.isKeydata(), DTF.parseDateTime(cmd.getReferenceDate())));


        return new ModelAndView("stkconvensysrawdatadirectory", model);
    }
}