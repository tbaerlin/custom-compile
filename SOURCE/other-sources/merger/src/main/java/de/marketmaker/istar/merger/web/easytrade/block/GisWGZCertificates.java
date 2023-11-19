/*
 * GisIpoInstruments.java
 *
 * Created on 24.10.2008 13:56:55
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.WGZCertificateData;
import de.marketmaker.istar.merger.provider.CustomerDataDelegateProvider;

/**
 * Provides WGZ certificates structure overview.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class GisWGZCertificates implements AtomController {
    private CustomerDataDelegateProvider provider;

    public void setProvider(CustomerDataDelegateProvider provider) {
        this.provider = provider;
    }

    public ModelAndView handleRequest(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws Exception {
        final List<WGZCertificateData> data = this.provider.getWGZCertificateData();

        final Map<String, Object> model = new HashMap<>();
        model.put("data", data);
        return new ModelAndView("giswgzcertificates", model);
    }
}