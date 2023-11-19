/*
 * ImgRenditestruktur.java
 *
 * Created on 10.08.2015 10:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.merger.web.easytrade.chart.ImgPortfolioHistoryCommand;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgPortfolioHistory extends EasytradeChartController {
    public ImgPortfolioHistory() {
        super(ImgPortfolioHistoryCommand.class, "imgPortfolioHistory.png");
    }

    private String template = "imgportfoliohistory";

    public void setTemplate(String template) {
        this.template = template;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        return new ModelAndView(this.template, getDefaultModel((ImgPortfolioHistoryCommand) o));
    }
}
