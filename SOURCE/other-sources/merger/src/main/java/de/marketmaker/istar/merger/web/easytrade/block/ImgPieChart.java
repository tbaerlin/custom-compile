/*
 * ImgPieChart.java
 *
 * Created on 14.03.2011 20:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@MmInternal
public class ImgPieChart extends EasytradeChartController {

    public ImgPieChart() {
        super(de.marketmaker.istar.merger.web.easytrade.chart.ImgPieChart.ImgPieChartCommand.class, "piechart.png");
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final de.marketmaker.istar.merger.web.easytrade.chart.ImgPieChart.ImgPieChartCommand cmd
                = (de.marketmaker.istar.merger.web.easytrade.chart.ImgPieChart.ImgPieChartCommand) o;
        return new ModelAndView("imgpiechart", getDefaultModel(cmd));
    }
}
