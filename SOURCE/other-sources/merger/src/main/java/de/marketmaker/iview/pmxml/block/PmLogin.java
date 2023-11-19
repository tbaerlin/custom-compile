/*
 * PmLogin.java
 *
 * Created on 21.01.13 11:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.pmxml.block;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import de.marketmaker.iview.pmxml.LoginResponse;
import org.jdom.JDOMException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
@MmInternal
public class PmLogin extends PmExchangeData {

    private JaxbHandler jaxbHandler;

    public void setJaxbHandler(JaxbHandler jaxbHandler) {
        this.jaxbHandler = jaxbHandler;
    }

    @Override
    protected ModelAndView createModelAndView(Command cmd, Map<String, Object> model, PmExchangeData.ResponseWrapper response) {
        final LoginResponse loginRes = this.jaxbHandler.unmarshal(response.getRawXml(), LoginResponse.class);
        model.put(ProfileResolver.PM_AUTHENTICATION_KEY, loginRes.getSessionToken());
        return super.createModelAndView(cmd, model, response);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws JDOMException, IOException, PmxmlException {
        final ModelAndView modelAndView = super.doHandle(request, response, o, errors);
        request.getSession().setAttribute(ProfileResolver.PM_AUTHENTICATION_KEY,
                modelAndView.getModel().get(ProfileResolver.PM_AUTHENTICATION_KEY));
        return modelAndView;
    }
}