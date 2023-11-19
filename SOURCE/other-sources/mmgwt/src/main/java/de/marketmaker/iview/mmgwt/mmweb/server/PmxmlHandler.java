package de.marketmaker.iview.mmgwt.mmweb.server;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.stream.StreamSource;

import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.WebUtils;

import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.itools.pmxml.frontend.PmxmlEvent;
import de.marketmaker.itools.pmxml.frontend.PmxmlEventHandler;
import de.marketmaker.iview.mmgwt.mmweb.server.async.JobProgressListener;
import de.marketmaker.iview.mmgwt.mmweb.server.async.ServerStateChangeListener;
import de.marketmaker.iview.pmxml.PmxmlExchangeDataImpl;
import de.marketmaker.iview.pmxml.SEAsyncStateChange;
import de.marketmaker.iview.pmxml.SEMMJobProgress;
import de.marketmaker.iview.pmxml.ServerEvent;

/**
 * Created on 04.09.12 13:23
 * Copyright (c) vwd GmbH. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class PmxmlHandler extends PmxmlExchangeDataImpl implements PmxmlEventHandler {

    private List<JobProgressListener> jobProgressListeners = new ArrayList<>();

    private List<ServerStateChangeListener> serverStateChangeListeners = new ArrayList<>();

    protected boolean isEnglish() {
        return (this.enPmxml != null) && (AsLocaleResolver.EN == getLocale());
    }

    private Locale getLocale() {
        return RequestContextUtils.getLocale(ServletRequestHolder.getHttpServletRequest());
    }

    @Override
    public String getAuthToken() {
        return getAuthTokenFromSession();
    }

    public static String getAuthTokenFromSession() {
        if (ServletRequestHolder.getHttpServletRequest() == null) {
            return null;
        }
        return (String) WebUtils.getSessionAttribute(ServletRequestHolder.getHttpServletRequest(), ProfileResolver.PM_AUTHENTICATION_KEY);
    }

    public void addListener(JobProgressListener listener) {
        this.jobProgressListeners.add(listener);
    }

    public void addListener(ServerStateChangeListener listener) {
        this.serverStateChangeListeners.add(listener);
    }

    @Override
    public void processEvent(PmxmlEvent event) {
        final ServerEvent e = this.jaxb.unmarshal(new StreamSource(new ByteArrayInputStream(event.getData())), ServerEvent.class);
        if (e instanceof SEMMJobProgress) {
            final SEMMJobProgress pe = (SEMMJobProgress) e;
            logger.debug("SEMMJobProgress: " + pe.getProgress() + "% with prio " + pe.getPrio() + " on handle " + pe.getHandle());
            for (JobProgressListener listener : this.jobProgressListeners) {
                listener.onProgress(pe);
            }
        }
        else if (e instanceof SEAsyncStateChange) {
            final SEAsyncStateChange sce = (SEAsyncStateChange) e;
            logger.debug("SEAsyncStateChange: " + sce.getState());
            for (ServerStateChangeListener listener : this.serverStateChangeListeners) {
                listener.onStateChange(sce);
            }
        }
/* ignore
        else if (e instanceof SEInstanceCancel) {

        }
*/
    }

    @Override
    public void processEventStat(PmxmlEvent pmxmlEvent) {
        throw new UnsupportedOperationException();
    }
}