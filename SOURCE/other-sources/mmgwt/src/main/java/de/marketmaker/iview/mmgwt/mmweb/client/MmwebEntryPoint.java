/*
 * MmwebEntryPoint.java
 *
 * Created on 21.11.12
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.MissingResourceException;

import javax.inject.Inject;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.as.AsMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.logging.Logger;
import de.marketmaker.iview.mmgwt.mmweb.client.style.LazyIceCssResource;
import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Ulrich Maurer
 */
public class MmwebEntryPoint implements EntryPoint {
    @Inject
    SessionData sessionData;

    @Inject
    Logger logger;

    @Override
    public void onModuleLoad() {
        // init injection do not remove!
        final Ginjector ginjector = Ginjector.INSTANCE;
        ginjector.injectIntoEntryPoint(this);

        if (this.sessionData == null) {
            this.logger.error("<MmwebEntryPoint.onModuleLoad> SessionData NOT injected!");
            return;
        }
        this.logger.debug("<MmwebEntryPoint.onModuleLoad> SessionData injected");

        if (!isDictionaryAvailable("zoneVariables")) { // $NON-NLS$
            new MainController().onModuleLoad();
            return;
        }

        final Dictionary zoneVariables = Dictionary.getDictionary("zoneVariables"); // $NON-NLS$
        final String controller = zoneVariables.get("Controller"); // $NON-NLS$
        final String webapp = getWebapp();
        final String contextPath = StringUtil.hasText(webapp)
                ? "/" + webapp
                : null;
        if ("true".equals(tryGet(zoneVariables, "isWithPmBackend"))) { // $NON-NLS$
            this.sessionData.setWithPmBackend();
        }
        if ("AsMainController".equals(controller)) { // $NON-NLS$
            this.sessionData.setIceDesign();
        }
        if (zoneVariables.keySet().contains("View") && "AsView".equals(zoneVariables.get("View"))) { // $NON-NLS$
            this.sessionData.setIceDesign();
        }
        else {
            this.sessionData.setWithMarketData();
        }

        if (this.sessionData.isIceDesign()) {
            Styles.set(new LazyIceCssResource());
            Button.setRendererType(Button.RendererType.SIMPLE);
        }

        getMainController(controller, contextPath).onModuleLoad();
    }

    public String tryGet(Dictionary dict, String key) {
        try {
            return dict.get(key);
        } catch (MissingResourceException mre) {
            return null;
        }
    }

    private native boolean isDictionaryAvailable(String name) /*-{
        return typeof($wnd[name]) == "object"; // $NON-NLS$
    }-*/;

    private String getWebapp() {
        String webapp = null;
        try {
            final Dictionary serverSettings = Dictionary.getDictionary("serverSettings");// $NON-NLS$
            webapp = serverSettings.get("webapp"); // $NON-NLS$
            Firebug.log("serverSettings/webapp: " + webapp);
        } catch (Exception ignored) {
        }
        return webapp;
    }

    private AbstractMainController getMainController(String controller, String contextPath) {
        if ("MainController".equals(controller)) { // $NON-NLS$
            return contextPath != null
                    ? new MainController(contextPath)
                    : new MainController();
        }
        if ("AsMainController".equals(controller)) { // $NON-NLS$
            return contextPath != null
                    ? new AsMainController(contextPath)
                    : new AsMainController();
        }
        if ("SimpleMainController".equals(controller)) { // $NON-NLS$
            return contextPath != null
                    ? new SimpleMainController(contextPath)
                    : new SimpleMainController();
        }
        if ("PmReportMainController".equals(controller)) { // $NON-NLS$
            return contextPath != null
                    ? new PmReportMainController(contextPath)
                    : new PmReportMainController();
        }
        if ("PmMainController".equals(controller)) { // $NON-NLS$
            return contextPath != null
                    ? new PmMainController(contextPath)
                    : new PmMainController();
        }
        if ("MetalsMainController".equals(controller)) { // $NON-NLS$
            return contextPath != null
                    ? new MetalsMainController(contextPath)
                    : new MetalsMainController();
        }
        if ("GrainsMainController".equals(controller)) { // $NON-NLS$
            return contextPath != null
                    ? new GrainsMainController(contextPath)
                    : new GrainsMainController();
        }
        return new MainController(contextPath);
    }


/*
    Die folgende Variante mit Code-Splitting teilt den Code in verschiedene Dateien unterhalb von deferredjs auf.
    Die Gesamtmenge des herunterzuladenden Codes wird dadurch etwas reduziert. Allerdings ist eine der Dateien
    recht groß. Mitten beim Download dieser Datei stoppt der Browser für ca. 10 sec (Ursache noch unbekannt).
    Insgesamt wird die Startzeit der Anwendung dadurch wesentlich länger.

    @Override
    public void onModuleLoad() {
        try {
            final Dictionary zoneVariables = Dictionary.getDictionary("zoneVariables"); // $NON-NLS$
            final String controller = zoneVariables.get("Controller"); // $NON-NLS$
            if ("MainController".equals(controller)) { // $NON-NLS$
                startMainController();
            }
            else if ("AsMainController".equals(controller)) { // $NON-NLS$
                startAsMainController();
            }
            else if ("SimpleMainController".equals(controller)) { // $NON-NLS$
                startSimpleMainController();
            }
            else if ("PmReportMainController".equals(controller)) { // $NON-NLS$
                startPmReportMainController();
            }
            else if ("PmMainController".equals(controller)) { // $NON-NLS$
                startPmMainController();
            }
            else if ("MetalsMainController".equals(controller)) { // $NON-NLS$
                startMetalsMainController();
            }
            else if ("GrainsMainController".equals(controller)) { // $NON-NLS$
                startGrainsMainController();
            }
            else {
                DebugUtil.logToFirebugConsole("'Controller' not specified in Dictionary 'zoneVariables' -> use MainController");
                startMainController();
            }
        }
        catch (MissingResourceException e) {
            DebugUtil.logToFirebugConsole("Dictionary 'zoneVariables' not defined -> use MainController");
            startMainController();
        }
    }

    private void startMainController() {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                Window.alert("Fehler: " + reason.getMessage()); // $NON-NLS$
                DebugUtil.logToFirebugConsole("error startMainController", reason);
            }

            @Override
            public void onSuccess() {
                new MainController().onModuleLoad();
            }
        });
    }

    private void startAsMainController() {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                Window.alert("Fehler: " + reason.getMessage()); // $NON-NLS$
                DebugUtil.logToFirebugConsole("error startAsMainController", reason);
            }

            @Override
            public void onSuccess() {
                new AsMainController().onModuleLoad();
            }
        });
    }

    private void startSimpleMainController() {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                Window.alert("Fehler: " + reason.getMessage()); // $NON-NLS$
                DebugUtil.logToFirebugConsole("error startSimpleMainController", reason);
            }

            @Override
            public void onSuccess() {
                new SimpleMainController().onModuleLoad();
            }
        });
    }

    private void startPmReportMainController() {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                Window.alert("Fehler: " + reason.getMessage()); // $NON-NLS$
                DebugUtil.logToFirebugConsole("error startPmReportMainController", reason);
            }

            @Override
            public void onSuccess() {
                new PmReportMainController().onModuleLoad();
            }
        });
    }

    private void startPmMainController() {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                Window.alert("Fehler: " + reason.getMessage()); // $NON-NLS$
                DebugUtil.logToFirebugConsole("error startPmMainController", reason);
            }

            @Override
            public void onSuccess() {
                new PmMainController().onModuleLoad();
            }
        });
    }

    private void startMetalsMainController() {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                Window.alert("Fehler: " + reason.getMessage()); // $NON-NLS$
                DebugUtil.logToFirebugConsole("error startMetalsMainController", reason);
            }

            @Override
            public void onSuccess() {
                new MetalsMainController().onModuleLoad();
            }
        });
    }

    private void startGrainsMainController() {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                Window.alert("Fehler: " + reason.getMessage()); // $NON-NLS$
                DebugUtil.logToFirebugConsole("error startGrainsMainController", reason);
            }

            @Override
            public void onSuccess() {
                new GrainsMainController().onModuleLoad();
            }
        });
    }
*/
}
