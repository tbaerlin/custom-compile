/*
 * MmwebServiceImpl.java
 *
 * Created on 27.02.2008 09:53:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.StringWriterResponse;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneResolver;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.iview.dmxml.Parameter;
import de.marketmaker.iview.dmxml.RequestType;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebService;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.PlaceStatistics;
import de.marketmaker.iview.mmgwt.mmweb.server.statistics.UserStatsDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Implementation of the MmwebService interface that can be used in a dmxml-1 webapp.
 * The following properties have to be set<dl>
 * <dt>moleculeController</dt><dd>Used to process requests of type RequestType</dd>
 * <dt>viewResolver</dt><dd>to render an xml representation of the model returned by the
 * moleculeController, which in turn will be unmarshalled to a ResponseType object</dd>
 * <dt>zoneResolver</dt><dd>To get access to the zone that defines additional request
 * parameters, zone name typically deducted from request authentication.</dd>
 * </dl>
 * The RemoteService methods may either be invoked by a
 * {@link GwtService} that delegates decoded
 * requests, or it may be invoked by means of jms when this object has been exported by
 * a lingo service wrapper.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@SuppressWarnings("GwtServiceNotRegistered")
public class MmwebServiceImpl extends GwtService
        implements MmwebService, InitializingBean, MmwebResponseListener {

    private AbstractController moleculeController;

    private ProfileResolver profileResolver;

    private ViewResolver viewResolver;

    private ZoneResolver zoneResolver;

    private UserStatsDao statsDao;

    private JaxbHandler jaxbHandler;

    private RequestTypeUtil requestTypeUtil;

    private List<MmwebResponseListener> listeners = Collections.emptyList();

    public void setStatsDao(UserStatsDao statsDao) {
        this.statsDao = statsDao;
    }

    public void setListeners(List<MmwebResponseListener> listeners) {
        this.listeners = listeners;
    }

    public void setJaxbHandler(JaxbHandler jaxbHandler) {
        this.jaxbHandler = jaxbHandler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.requestTypeUtil = new RequestTypeUtil(this.jaxbHandler);
    }

    public MmwebResponse getData(MmwebRequest request) {
        return new MmwebServiceMethod(this).getData(request);
    }

    public void setMoleculeController(AbstractController moleculeController) {
        this.moleculeController = moleculeController;
    }

    public void setProfileResolver(ProfileResolver profileResolver) {
        this.profileResolver = profileResolver;
    }

    public void setViewResolver(ViewResolver viewResolver) {
        this.viewResolver = viewResolver;
    }

    public void setZoneResolver(ZoneResolver zoneResolver) {
        this.zoneResolver = zoneResolver;
    }

    public MoleculeRequest toMoleculeRequest(RequestType type) {
        return this.requestTypeUtil.toMoleculeRequest(type);
    }

    String asString(RequestType type) {
        return requestTypeUtil.asString(type);
    }

    public void insertStats(long sessionId, List<PlaceStatistics> stats) {
        if (this.statsDao != null) {
            this.statsDao.insertStats(sessionId, stats);
        }
    }

    @Override
    public void onBeforeSend(HttpSession session, MmwebResponse response) {
        for (MmwebResponseListener listener : listeners) {
            listener.onBeforeSend(session, response);
        }
    }

    boolean handleRequest(HttpServletRequest request,
            StringWriterResponse response) throws Exception {
        final ModelAndView muv = moleculeController.handleRequest(request, response);
        if (response.isError()) {
            return false;
        }
        if (muv != null) { // muv == null means whole request was executed remote
            final View view
                    = this.viewResolver.resolveViewName(muv.getViewName(), Locale.getDefault());
            view.render(muv.getModel(), request, response);
        }
        return true;
    }

    Profile resolveProfile(HttpServletRequest httpServletRequest) {
        return profileResolver.resolveProfile(httpServletRequest);
    }

    public Zone resolveZone(HttpServletRequest httpServletRequest) {
        return zoneResolver.resolveZone(httpServletRequest);
    }

    public Zone getZone(String s) {
        return zoneResolver.getZone(s);
    }

    String marshal(Class<Parameter> declaredType, Parameter parameter, String elementName) {
        return jaxbHandler.marshal(declaredType, parameter, elementName);
    }

    <T> T unmarshal(String xml, Class<T> responseTypeClass) {
        return jaxbHandler.unmarshal(xml, responseTypeClass);
    }
}
