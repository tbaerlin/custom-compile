/*
 * ImgRenditestruktur.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.merger.web.easytrade.chart.ImgRenditestrukturCommand;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgRenditestruktur extends EasytradeChartController {
    public ImgRenditestruktur() {
        super(ImgRenditestrukturCommand.class, "imgRenditestruktur.png");
    }

    private String template = "imgrenditestruktur";

    public void setTemplate(String template) {
        this.template = template;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        return new ModelAndView(this.template, getDefaultModel((ImgRenditestrukturCommand) o));
    }
}
