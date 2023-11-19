/*
 * RequestParserInterceptor.java
 *
 * Created on 04.07.2006 14:43:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import de.marketmaker.istar.merger.web.HttpException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RequestParserInterceptor extends HandlerInterceptorAdapter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object o) throws Exception {

        if (!request.getRequestURI().endsWith("retrieve.xml")
                && !request.getRequestURI().endsWith("retrieve.json")) {
            return true;
        }

        if (request.getAttribute(MoleculeRequest.REQUEST_ATTRIBUTE_NAME) != null) {
            // we already have a MoleculeRequest associated with the request
            return true;
        }

        request.setCharacterEncoding("UTF-8");

        final RequestParserMethod method = new RequestParserMethod(request);
        try {
            method.invoke();
        } catch (BadRequestException e) {
            if (request.getAttribute(MoleculeRequest.REQUEST_ATTRIBUTE_NAME) == null) {
                this.logger.warn("<preHandle> bad request: " + method.requestToString()
                        + ": " + e.getMessage());
            }
            else {
                this.logger.warn("<preHandle> bad request (see molecule log for details): " + e.getMessage());
            }
            throw new HttpException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Throwable t) {
            this.logger.warn("<preHandle> failed for " + method.requestToString(), t);
            throw new HttpException(HttpServletResponse.SC_BAD_REQUEST, t.getMessage());
        }

        return true;
    }
}
