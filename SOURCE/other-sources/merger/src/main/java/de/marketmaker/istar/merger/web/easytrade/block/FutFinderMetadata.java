/*
 * FutFinderMetadata.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * Provides meta data and their available values that can be used in {@see FUT_Finder} for
 * searching futures.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FutFinderMetadata implements AtomController {
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return new ModelAndView("futfindermetadata");
    }
}