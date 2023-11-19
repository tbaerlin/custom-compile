/*
 * OeAllocSessionContainer.java
 *
 * Created on 21.01.13 11:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.pmxml.block;

import de.marketmaker.istar.common.amqp.ServiceProviderSelection;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import de.marketmaker.iview.pmxml.AllocateOrderSessionContainerDataResponse;
import org.jdom.JDOMException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */

@MmInternal
public class OeAllocSessionContainer extends PmExchangeData {
    public static final String ORDER_SESSION_CONTAINER_HANDLE = "orc.handle";

    private JaxbHandler jaxbHandler;

    public void setJaxbHandler(JaxbHandler jaxbHandler) {
        this.jaxbHandler = jaxbHandler;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws JDOMException, IOException, PmxmlException {
        final ModelAndView modelAndView = super.doHandle(request, response, o, errors);
        final HttpSession httpSession = request.getSession();

        final String handle = (String)modelAndView.getModel().get(ORDER_SESSION_CONTAINER_HANDLE);
        httpSession.setAttribute(ORDER_SESSION_CONTAINER_HANDLE, handle);

        httpSession.setAttribute(SERVER_ID, ServiceProviderSelection.ID_FROM_LAST_REPLY.get());

        return modelAndView;
    }

    @Override
    protected ModelAndView createModelAndView(Command cmd, Map<String, Object> model, PmExchangeData.ResponseWrapper response) {
        final AllocateOrderSessionContainerDataResponse res =
                this.jaxbHandler.unmarshal(response.getRawXml(), AllocateOrderSessionContainerDataResponse.class);
        model.put(ORDER_SESSION_CONTAINER_HANDLE, res.getHandle());
        return super.createModelAndView(cmd, model, response);
    }
}