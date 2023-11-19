/*
 * ParameterMappingHandlerInterceptor.java
 *
 * Created on 29.01.2008 14:03:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.w3c.dom.Document;

import de.marketmaker.istar.merger.provider.profile.CounterService;

/**
 * Checks for the availability of a certain vwd access counter and denies access to a certain
 * resource if the counter does not exist. In addition to a parameter that specifies the
 * customer id, the request also has to specify a parameter "selector" that specifies the
 * selector used for counting. Typically, the selector would be defined in a zone definition.
 *  
 * @author Oliver Flege
 */
public class CounterExistsInterceptor extends HandlerInterceptorAdapter {
    private CounterService counterService;

    private final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

    private int errorCode = HttpServletResponse.SC_FORBIDDEN;

    private String errorMessage;

    private String idParameterName = "cust_id";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final XPath xpath = XPathFactory.newInstance().newXPath();

    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object o) throws Exception {
        if (isCounterAvailable(request)) {
            return true;
        }
        throw new HttpException(this.errorCode, this.errorMessage);
    }

    public void setCounterService(CounterService counterService) {
        this.counterService = counterService;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setIdParameterName(String idParameterName) {
        this.idParameterName = idParameterName;
    }

    private String getExpression(String selector) {
        return "/vwdCntAccounts/CntAccount/CounterSets/CounterSet/Counter/Selectors/Sel[@id='" + selector + "']";
    }

    private boolean isCounterAvailable(HttpServletRequest request) {
        final String cust_id = request.getParameter(this.idParameterName);
        if (!StringUtils.hasText(cust_id)) {
            return false;
        }
        final String selector = request.getParameter("selector");
        final String expression = getExpression(selector);
        return isCounterAvailable(cust_id, expression);
    }

    private boolean isCounterAvailable(String cust_id, final String expression) {
        return this.counterService.readAccount(cust_id, "", new ResponseExtractor<Boolean>() {
            @Override
            public Boolean extractData(ClientHttpResponse response) throws IOException {
                try {
                    final Document d = builderFactory.newDocumentBuilder().parse(response.getBody());
                    synchronized (xpath) {
                        return xpath.evaluate(expression, d, XPathConstants.NODE) != null;
                    }
                } catch (Exception e) {
                    logger.warn("<isCounterAvailable> failed", e);
                    return Boolean.FALSE;
                }
            }
        }) == Boolean.TRUE; // service might return null, so no auto-unboxing
    }
}
