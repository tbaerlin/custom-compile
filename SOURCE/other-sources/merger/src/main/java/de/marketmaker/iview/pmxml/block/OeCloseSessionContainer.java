/*
 * OeCloseSessionContainer.java
 *
 * Created on 21.01.13 11:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.pmxml.block;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import org.jdom.JDOMException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Michael Lösch
 */
@MmInternal
public class OeCloseSessionContainer extends PmExchangeData {

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws JDOMException, IOException, PmxmlException {
        final ModelAndView modelAndView = super.doHandle(request, response, o, errors);
        request.getSession().removeAttribute(OeAllocSessionContainer.ORDER_SESSION_CONTAINER_HANDLE);
        request.getSession().removeAttribute(SERVER_ID);
        return modelAndView;
    }
}