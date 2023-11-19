package de.marketmaker.istar.merger.web.easytrade.multiplex;

import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created on 06.05.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class DocmanAuthTarget extends RemoteTarget implements InitializingBean {
    private String key;

    private String fixedAuthentication;

    private String fixedAuthenticationType;

    private boolean hasAuthConfig;

    @Required
    public void setKey(String key) {
        this.key = key;
    }

    @Required
    public void setFixedAuthentication(String fixedAuthentication) {
        this.fixedAuthentication = fixedAuthentication;
    }

    @Required
    public void setFixedAuthenticationType(String fixedAuthenticationType) {
        this.fixedAuthenticationType = fixedAuthenticationType;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.hasAuthConfig = StringUtils.hasText(this.key)
                && StringUtils.hasText(this.fixedAuthentication)
                && StringUtils.hasText(this.fixedAuthenticationType);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<afterPropertiesSet>" +
                            toLogParam("key", this.key) +
                            toLogParam("fixedAuthentication", this.fixedAuthentication) +
                            toLogParam("fixedAuthenticationType", this.fixedAuthenticationType)
            );
        }

        this.logger.info("<afterPropertiesSet> hasAuthConfig=" + this.hasAuthConfig);
    }

    private static String toLogParam(String param, Object value) {
        return " " + param + "=\"" + value + "\"";
    }

    @Override
    public boolean supports(Zone zone, MoleculeRequest.AtomRequest atom) {
        return atom.getName().startsWith("DOC_") || atom.getName().endsWith("@DOC");
    }

    @Override
    public Future<List<ModelAndView>> handleMixedRequest(final HttpServletRequest request, final HttpServletResponse response, final Zone zone, final MoleculeRequest mr, ExecutorService executorService) throws Exception {
        checkConfig();
        //TODO: REMOVE _HACK_
        hackAtomNames(mr);
        //TODO: END _HACK_
        return super.handleMixedRequest(request, response, zone, mr, executorService);
    }

    private void hackAtomNames(MoleculeRequest mr) {
        final List<MoleculeRequest.AtomRequest> ars = mr.getAtomRequests();
        for (MoleculeRequest.AtomRequest ar : ars) {
            if (ar.getName().endsWith("@DOC")) {
                ar.set_HACK_Name(ar.getName().replaceAll("@DOC$", ""));
            }
        }
    }

    @Override
    public ModelAndView handleStraightRequest(HttpServletRequest request, HttpServletResponse response, Zone zone, MoleculeRequest mr) throws Exception {
        checkConfig();
        //TODO: REMOVE _HACK_
        hackAtomNames(mr);
        //TODO: END _HACK_
        return super.handleStraightRequest(request, response, zone, mr);
    }

    private void checkConfig() {
        if (!this.hasAuthConfig) {
            throw new IllegalStateException("no valid authentication configured");
        }
    }

    @Override
    protected void prepareRequestType(MoleculeRequest mr, HttpSession session) {
        mr.setKey(this.key);
        mr.setAuthenticationType(this.fixedAuthenticationType);
        mr.setAuthentication(this.fixedAuthentication);
    }
}
