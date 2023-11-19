/*
 * MscTopProductsMetadata.java
 *
 * Created on 12.06.2012 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author Michael Lösch
 */
public class MscTopProductsMetadata implements AtomController {

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return new ModelAndView("msctopproductsmetadata", "productTypes",
                MscTopProductsCommandBuilder.ProductType.values());
    }
}
