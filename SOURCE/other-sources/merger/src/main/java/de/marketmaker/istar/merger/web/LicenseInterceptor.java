/*
 * GimLicenseInterceptor.java
 *
 * Created on 23.02.15 14:05
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.w3c.dom.Document;

import de.marketmaker.istar.merger.provider.profile.AccessCounter;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;

/**
 * @author oflege
 */
public class LicenseInterceptor extends HandlerInterceptorAdapter {

    private static final String EXPRESSION = "/vwdCntAccounts/CntAccount/Header/Status/text()";

    private final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

    private final XPath xpath = XPathFactory.newInstance().newXPath();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AccessCounter accessCounter;

    private String appId;

    private String ident;

    private String customerParameter;

    private String userParameter;

    public LicenseInterceptor(AccessCounter accessCounter) {
        this.accessCounter = accessCounter;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public void setCustomerParameter(String customerParameter) {
        this.customerParameter = customerParameter;
    }

    public void setUserParameter(String userParameter) {
        this.userParameter = userParameter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {

        String customer = getParameter(request, this.customerParameter);
        String user = getParameter(request, this.userParameter);

        String status = getLicenseStatus(customer, user);

        if (!"1".equals(status)) {
            if ("-2".equals(status)) {
                throw new NoLicenseException("Invalid customer id: '" + status + "'");
            }
            if ("-10".equals(status)) {
                throw new NoLicenseException("No licence available: '" + status + "'");
            }
            throw new NoLicenseException("Invalid licence: status code= '" + status + "'");
        }
        return true;
    }

    protected String getLicenseStatus(String customer, String user) {
        return this.accessCounter.countAccess(this.appId,
                customer, user, this.ident, "1", (ClientHttpResponse chr) -> {
                    try {
                        if (chr.getStatusCode() != HttpStatus.OK) {
                            return "SC" + chr.getStatusCode();
                        }
                        final Document d = builderFactory.newDocumentBuilder().parse(chr.getBody());
                        synchronized (xpath) {
                            return (String) xpath.evaluate(EXPRESSION, d, XPathConstants.STRING);
                        }
                    } catch (Exception e) {
                        logger.warn("<isCounterAvailable> failed", e);
                        return null;
                    }
                });
    }

    protected String getParameter(HttpServletRequest request, final String name) {
        String result = request.getParameter(name);
        if (!StringUtils.hasText(result)) {
            throw new BadRequestException("Parameter '" + name + "' is missing");
        }
        return result;
    }

    public static void main(String[] args) {
        AccessCounter ac = new AccessCounter();
        ac.setRestTemplate(new RestTemplate());
        ac.setBaseUri("http://vwd-ent:1969/vwdCounter.asmx/"); // Prod
        ac.setBaseUri("http://vwd-ent:1973/vwdCounter.asmx/"); // Test

        LicenseInterceptor li = new LicenseInterceptor(ac);
        li.setAppId("86");
        li.setIdent("2463");
        System.out.println(li.getLicenseStatus("genoid", "playerid"));
    }
}
