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

/**
 * Give a descrition for all xml tags inside the raw convensys xml file.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkConvensysRawMetadata extends StkConvensysData {
    public static class Command {
        private boolean keydata = false;

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

    public StkConvensysRawMetadata() {
        super(Command.class);
    }

    private String keyMetadataXsd;

    private String portraitMetadataXsd;

    public void setKeyMetadataXsd(String keyMetadataXsd) {
        this.keyMetadataXsd = keyMetadataXsd;
    }

    public void setPortraitMetadataXsd(String portraitMetadataXsd) {
        this.portraitMetadataXsd = portraitMetadataXsd;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;


        final Map<String, Object> model = new HashMap<>();

        final String rawdata = getRawdata(null, cmd.isKeydata(), false, true);

        if (rawdata != null) {
            model.put("rawdata", rawdata);
        }

        return new ModelAndView("stkconvensysrawmetadata", model);
    }

    protected String getXsd(final boolean keydata) {
        return keydata ? this.keyMetadataXsd : this.portraitMetadataXsd;
    }
}