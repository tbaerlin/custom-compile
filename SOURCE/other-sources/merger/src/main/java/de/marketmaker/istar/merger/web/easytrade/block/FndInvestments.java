/*
 * FndInvestments.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.merger.web.easytrade.chart.FndInvestmentsCommand;

/**
 * Returns the url of a chart that displays the different allocations of a fund.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample symbol 4229.qid
 * @sample allokationstyp UNTERNEHMEN
 */
public class FndInvestments extends EasytradeChartController {

    public FndInvestments() {
        super(FndInvestmentsCommand.class, "fndInvestments.png");
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final FndInvestmentsCommand cmd = (FndInvestmentsCommand) o;
        return new ModelAndView("fndinvestments", getDefaultModel(cmd));
    }
}
