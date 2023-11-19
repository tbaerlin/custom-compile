/*
 * MmwebServiceMethod.java
 *
 * Created on 06.09.12 15:53
 *
 * Copyright (this) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.RequestWrapper;
import de.marketmaker.istar.merger.web.StringWriterResponse;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.istar.merger.web.easytrade.StopWatchHolder;
import de.marketmaker.iview.dmxml.RequestType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebService;
import de.marketmaker.iview.mmgwt.mmweb.server.statistics.UserStatsDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.server.UserServiceImpl.SESSION_KEY_ZONENAME;

/**
 * @author oflege
 */
public class MmwebServiceMethod {
    private static final Log LOGGER = LogFactory.getLog(MmwebServiceMethod.class);

    public RequestType dmxmlRequest;

    public HttpServletRequest servletRequest;

    public final HttpSession session;

    public final Zone zone;

    public MoleculeRequest moleculeRequest;

    public boolean internalError;

    public String rawResponse;

    protected final MmwebServiceImpl service;

    private boolean withStopWatch = false;

    public MmwebServiceMethod(MmwebServiceImpl service) {
        this.service = service;
        this.servletRequest = getServletRequest();
        this.session = this.servletRequest.getSession(false);
        this.zone = resolveZone();
    }

    public MmwebResponse getData(MmwebRequest request) {
        /* === A note on the implementation ===
         * This methods's body is broken up into methods operating on a RequestContext.
         * This context is a mutable object that simply collects (references to) several
         * objects involved in the processing of a MmwebRequest.
         * The reason for this context-structure lies in the complex way in which these
         * methods may modify several of the objects in the context, e.g. wrapping a
         * HttpServletRequest in another to add zone-global parameters.
         * Methods operating on a context describe in their Javadoc, which fields of the context
         * they need to have set before invocation and which they fill themselves. Additionally,
         * they list the required fields that are (potentially) modified.
         */

        this.dmxmlRequest = request.getDmxmlRequest();
        logRequest(this.dmxmlRequest);

        if (this.withStopWatch) {
            StopWatchHolder.setStopWatch(new StopWatch()); // needed by MoleculeController
        }

        try {
            //Validate Session
            if (!isValidSession()) {
                return new MmwebResponse().withState(MmwebResponse.State.SESSION_EXPIRED);
            }

            if (!passesAuthorizationCheck()) {
                return new MmwebResponse().withState(MmwebResponse.State.SESSION_EXPIRED);
            }

            // handle statistics
            if (this.session != null && request.getUsageStatistics() != null) {
                final Long sid = (Long) this.session.getAttribute(UserStatsDao.VISIT_ID);
                this.service.insertStats(sid, request.getUsageStatistics());
            }

            // Send request
            final boolean gotResponse = sendMoleculeRequest(false);
            final MmwebResponse result = new MmwebResponse().withState(
                    gotResponse
                            ? MmwebResponse.State.OK
                            : (this.internalError
                            ? MmwebResponse.State.INTERNAL_ERROR
                            : MmwebResponse.State.SESSION_EXPIRED));

            // If successful, unmarshall response
            if (gotResponse) {
                try {
                    final ResponseType value
                            = this.service.unmarshal(this.rawResponse, ResponseType.class);
                    result.setResponseType(value);
                    XmlDebugHelper.handle(this.servletRequest, request, result, this.moleculeRequest, this.rawResponse);
                    this.service.onBeforeSend(this.session, result);
                    return result;
                } catch (Exception e) {
                    LOGGER.error("probably unmarshal error", e);
                    // fall through to return empty response with INTERNAL_ERROR
                }
            }
            final MmwebResponse errorResponse = new MmwebResponse().withState(MmwebResponse.State.INTERNAL_ERROR);
            XmlDebugHelper.handle(this.servletRequest, request, errorResponse, this.moleculeRequest, "internal error");
            return errorResponse;
        } finally {
            RequestContextHolder.setRequestContext(null);
            if (this.withStopWatch) {
                StopWatchHolder.setStopWatch(null);
            }
        }
    }

    /**
     * Checks if the validation token of the HTTP session is equal to the validation token of the current HTTP request.
     * Returns {@literal true} if no validation token is given in the request, the session is null, or the session has no validation token.
     * Returns {@literal false} only if two validation tokens are set and if these are not equal.
     *
     * IE 11 is not capable of converting UTF-8 to ISO-8859-1 automatically (if possible).
     * Hence, to support Umlauts in login names, it is necessary to encode and decode the
     * {@value de.marketmaker.iview.mmgwt.mmweb.client.MmwebService#SESSION_VALIDATOR_REQUEST_HEADER}
     * value.
     *
     * @return {@literal true} if the session is valid.
     */
    private boolean isValidSession() {
        final String clientSessionValidator
                = urlDecode(this.servletRequest.getHeader(MmwebService.SESSION_VALIDATOR_REQUEST_HEADER));
        if (clientSessionValidator != null && this.session != null) {
            final Object serverSessionValidator
                    = this.session.getAttribute(UserLoginMethod.SESSION_KEY_SESSION_VALIDATOR);
            if (serverSessionValidator != null
                    && !clientSessionValidator.equals(serverSessionValidator)) {
                return false;
            }
        }
        return true;
    }

    private String urlDecode(String header) {
        try {
            return URLDecoder.decode(header, "UTF-8");
        }
        catch(Exception e) {
            return header;
        }
    }

    /**
     * <p>Requires: servletRequest, session, authentication, authenticationType.</p>
     * <p>Fills: zone, authorized.</p>
     * <p>Modifies: servletRequest</p>
     *
     * @return whether authorization is granted judging from zone info
     */
    public boolean passesAuthorizationCheck() {
        if (this.servletRequest instanceof MyHttpServletRequest) {
            return true;
        }
        if (this.session == null) {
            final Map<String, String[]> map = getAuthenticationMap(this.servletRequest.getParameterMap());
            this.servletRequest = RequestWrapper.create(this.servletRequest, this.zone.getParameterMap(map, ""));

            return !requiresSession(this.servletRequest);
        }
        return true;
    }

    private Zone resolveZone() {
        if (this.servletRequest instanceof MyHttpServletRequest) {
            return this.service.getZone("iview");
        }
        if (this.session == null) {
            return this.service.resolveZone(this.servletRequest);
        }
        return this.service.getZone((String) this.session.getAttribute(SESSION_KEY_ZONENAME));
    }

    /**
     * <p>Requires: moleculeRequest, servletRequest, zone</p>
     * <p>Fills: internalError, rawResponse</p>
     * <p>Modifies: servletRequest</p>
     *
     * @return whether the context now holds a valid rawResponse, i.e. no error occurred
     */
    protected boolean sendMoleculeRequest(boolean clearContext) {
        // Convert request
        try {
            this.moleculeRequest = this.service.toMoleculeRequest(this.dmxmlRequest);
        } catch (BadRequestException e) {
            LOGGER.warn("<sendMoleculeRequest> toMoleculeRequest failed for "
                    + this.service.asString(this.dmxmlRequest) + ": " + e);
            this.internalError = true;
            return false;
        }

        final RequestWrapper wrapper = RequestWrapper.create(this.servletRequest, getMoleculeRequestParams(),
                this.servletRequest.getContextPath() + this.zone.getName() + "/molecule.html");
        wrapper.setAttribute(MoleculeRequest.REQUEST_ATTRIBUTE_NAME, this.moleculeRequest);
        wrapper.setAttribute(MoleculeRequest.REQUEST_TYPE_ATTRIBUTE_NAME, this.dmxmlRequest);
        wrapper.setAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE, this.zone);

        Profile profile = this.service.resolveProfile(this.servletRequest);
        if (profile == null) { // dmxml docu cannot resolve profile from session
            profile = this.service.resolveProfile(wrapper);
            if (profile == null) {
                LOGGER.warn("<getData> no profile for " + this.servletRequest.getRequestURL().toString());
                return false;
            }
        }

        final StringWriterResponse swr = new StringWriterResponse();

        try {
            RequestContextHolder.setRequestContext(this.zone.getRequestContext(wrapper, profile));
            if (this.service.handleRequest(wrapper, swr)) {
                this.rawResponse = swr.toString();
                this.internalError = false;
            }
            else {
                this.internalError = true;
            }
        } catch (Exception e) {
            LOGGER.error("<getData> failed for " + this.moleculeRequest, e);
            this.internalError = true;
        } finally {
            if (clearContext) {
                RequestContextHolder.setRequestContext(null);
            }
        }
        return !this.internalError;
    }

    protected Map<String, String[]> getMoleculeRequestParams() {
        return Collections.emptyMap();
    }

    private boolean requiresSession(HttpServletRequest gwtRequest) {
        return !StringUtils.hasText(gwtRequest.getParameter(ProfileResolver.AUTHENTICATION_TYPE_KEY));
    }

    protected Map<String, String[]> getAuthenticationMap(Map<String, String[]> map) {
        final Map<String, String[]> result = new HashMap<>(map);
        putIfDefined(result, ProfileResolver.AUTHENTICATION_KEY, this.dmxmlRequest.getAuthentication());
        putIfDefined(result, ProfileResolver.AUTHENTICATION_TYPE_KEY, this.dmxmlRequest.getAuthenticationType());
        return result;
    }

    private void putIfDefined(Map<String, String[]> result, String key, String value) {
        if (StringUtils.hasText(value)) {
            result.put(key, new String[]{value});
        }
    }

    private HttpServletRequest getServletRequest() {
        final HttpServletRequest result = ServletRequestHolder.getHttpServletRequest();
        if (result != null) {
            return result;
        }
        this.withStopWatch = true;
        return new MyHttpServletRequest();
    }

    private void logRequest(RequestType request) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<logRequest> " + this.service.asString(request));
        }
    }

}
