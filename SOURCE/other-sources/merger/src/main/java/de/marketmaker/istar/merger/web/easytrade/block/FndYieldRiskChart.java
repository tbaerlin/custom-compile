/*
 * FndYieldRiskChart.java
 *
 * Created on 10.12.2007 12:57:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

import de.marketmaker.istar.merger.web.easytrade.chart.BaseImgSymbolCommand;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndYieldRiskChart extends EasytradeChartController {
    public FndYieldRiskChart() {
        super(BaseImgSymbolCommand.class, "fndyieldrisk.png");
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final BaseImgSymbolCommand c = (BaseImgSymbolCommand) o;
        return new ModelAndView("fndyieldrisk", getDefaultModel(c));        
    }
}
