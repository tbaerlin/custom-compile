/*
 * OpenGisPortalXhrBasicAuthPreFlightMethod.java
 *
 * Created on 29.03.2016 12:06
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz;

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.HttpBasicAuthCredentials;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ShutdownEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ShutdownHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.logging.Logger;
import de.marketmaker.iview.mmgwt.mmweb.client.util.MD5Util;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author mdick
 */
@SuppressWarnings("Duplicates")
public class OpenGisPortalXhrBasicAuthPreFlightMethod implements OpenGisPortalCommand {
    public enum PortalUrl {
        TEST("/gisportal-test/index.php"), // $NON-NLS$
        PROD("/gisportal/index.php");  // $NON-NLS$

        private final String portalBaseUrl;

        PortalUrl(String portalBaseUrl) {
            this.portalBaseUrl = portalBaseUrl;
        }

        public String url() {
            return this.portalBaseUrl;
        }
    }

    private static final String SIMPLE_NAME = OpenGisPortalXhrBasicAuthPreFlightMethod.class.getSimpleName();

    private static final String KEY = "c2eafee8016c9ebde44f6e752a05d736"; // $NON-NLS$

    private static HandlerRegistration shutdownHandlerRegistration;

    private final Consumer<String> errorHandler;

    private final Supplier<HttpBasicAuthCredentials> basicAuthCredentialsSupplier;

    private final HandlerManager eventBus;

    private final String portalBaseUrl;

    private final Logger logger;

    public OpenGisPortalXhrBasicAuthPreFlightMethod(PortalUrl activePortalUrl,
            Supplier<HttpBasicAuthCredentials> basicAuthCredentialsSupplier,
            HandlerManager eventBus, Consumer<String> errorHandler,
            Logger logger) {
        this.errorHandler = errorHandler;
        this.basicAuthCredentialsSupplier = basicAuthCredentialsSupplier;
        this.eventBus = eventBus;
        this.portalBaseUrl = activePortalUrl.url();
        this.logger = logger;
    }

    static class GisPortalShutdownHandler implements ShutdownHandler {
        private final Logger logger;
        private final String portalBaseUrl;

        public GisPortalShutdownHandler(String portalBaseUrl, Logger logger) {
            this.portalBaseUrl = portalBaseUrl;
            this.logger = logger;
        }

        @Override
        public void onShutdown(ShutdownEvent event) {
            this.logger.debug("<" + SIMPLE_NAME + ".GisPortalShutdownHandler.onShutdown>");
            final Frame frame = new Frame(getLogoutLink(this.portalBaseUrl) + "&logout=1"); // $NON-NLS$
            frame.setSize("1px", "1px"); // $NON-NLS$
            RootPanel.get().add(frame);
        }
    }

    @Override
    public void execute() {
        this.logger.debug("<" + SIMPLE_NAME + ".execute>");

        final RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, encodeUrl(this.portalBaseUrl, false));
        trySetUserAndPassword(rb);
        rb.setIncludeCredentials(true);
        rb.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                int statusCode = response.getStatusCode();
                if (statusCode == Response.SC_OK) {
                    openGisPortalWindow();
                }
                else {
                    errorHandler.accept(I18n.I.gisPortalErrorBasicAuth(statusCode, response.getStatusText()));
                }
            }

            @Override
            public void onError(Request request, Throwable throwable) {
                errorHandler.accept(I18n.I.gisPortalErrorOther());
            }
        });

        try {
            rb.send();
        } catch (Exception e) {
            this.errorHandler.accept(I18n.I.gisPortalErrorOther());
        }
    }

    private void trySetUserAndPassword(RequestBuilder rb) {
        if (this.basicAuthCredentialsSupplier != null) {
            final HttpBasicAuthCredentials httpBasicAuthCredentials = this.basicAuthCredentialsSupplier.get();
            if (httpBasicAuthCredentials != null) {
                final String user = httpBasicAuthCredentials.getUser();
                final String password = httpBasicAuthCredentials.getPassword();

                if (StringUtil.hasText(user) && StringUtil.hasText(password)) {
                    rb.setUser(user);
                    rb.setPassword(password);
                }
            }
        }
    }

    private void openGisPortalWindow() {
        Window.open(getLink(this.portalBaseUrl), "_blank", "resizable=yes"); //$NON-NLS$
        if (shutdownHandlerRegistration == null) {
            this.logger.debug("<" + SIMPLE_NAME + ".execute> adding ShutdownHandler for GisPortal logout");
            shutdownHandlerRegistration = this.eventBus.addHandler(ShutdownEvent.getType(), new GisPortalShutdownHandler(this.portalBaseUrl, this.logger));
        }
    }

    private static String getLink(String portalBaseUrl) {
        return encodeUrl(portalBaseUrl, true);
    }

    private static String getLogoutLink(String portalBaseUrl) {
        final String genoId = SessionData.INSTANCE.getUser().getLogin();
        final String date = DateTimeFormat.getFormat("yyyy-MM-dd").format(new Date()); // $NON-NLS$
        return portalBaseUrl + "?option=com_community&view=sso&noredirect=1&genoid=" + // $NON-NLS$
                genoId +
                "&sso=" + // $NON-NLS$
                MD5Util.md5(genoId + ":" + date + ":" + KEY);
    }

    private static String encodeUrl(String url, boolean redirect) {
        final String genoId = SessionData.INSTANCE.getUser().getLogin();
        final String date = DateTimeFormat.getFormat("yyyy-MM-dd").format(new Date()); // $NON-NLS$
        if (!url.contains("?")) { //$NON-NLS$
            url += "?"; //$NON-NLS$
        }
        else {
            url += "&"; //$NON-NLS$
        }
        if (!redirect) {
            url += "noredirect=1&"; //$NON-NLS$
        }
        return url + "genoid=" + genoId + "&sso=" + MD5Util.md5(genoId + ":" + date + ":" + KEY); //$NON-NLS$
    }
}
