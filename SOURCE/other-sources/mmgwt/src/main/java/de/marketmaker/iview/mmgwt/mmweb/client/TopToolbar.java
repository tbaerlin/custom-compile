/*
 * TopToolbar.java
 *
 * Created on 28.02.12 15:08
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.ui.IsWidget;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.as.SouthPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PendingRequestsEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PendingRequestsHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushActivationHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.ServiceStatistics;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LogWindow;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StatisticsWindow;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.PdfWidget;

/**
 * @author oflege
 */
public interface TopToolbar extends PushActivationHandler, PendingRequestsHandler, ConfigChangedHandler, IsWidget {

    String SEARCH_DEFAULT = I18n.I.searchBoxDefaultText();

    String WP_SEARCH_KEY = "M_S"; // $NON-NLS-0$

    void updatePdfButtonState();

    void setPdfButtonState(PdfWidget.PdfButtonState pdfButtonState);

    void setPrintButtonEnabled(boolean enabled);

    void updatePrintButtonEnabledState();

    void onPendingRequestsUpdate(PendingRequestsEvent event);

    void ackSave();

    void onConfigChange(ConfigChangedEvent event);

    void updateLimitsIcon(String pending);

    class Util {
        public static void search(String ctrlKey, String value) {
            if (!StringUtil.hasText(value)) {
                return;
            }
            final String trimmedValue = value.trim();
            if (processInternalCommand(trimmedValue)) {
                AbstractMainController.INSTANCE.showMessage(I18n.I.ok());
                return;
            }
            AbstractMainController.INSTANCE.search(ctrlKey, trimmedValue);
        }


        public static boolean processInternalCommand(String value) {
            final SessionData sessionData = Ginjector.INSTANCE.getSessionData();
            final SouthPanel southPanel = Ginjector.INSTANCE.getSouthPanel();

            if ("stats:reset".equals(value)) { // $NON-NLS-0$
                ServiceStatistics.getOrCreate().reset();
            }
            else if ("stats:show".equals(value)) { // $NON-NLS-0$
                StatisticsWindow.show();
            }
            else if ("log:show".equals(value)) { // $NON-NLS-0$
                LogWindow.show();
            }
            else if ("log:user".equals(value)) { // $NON-NLS-0$
                LogWindow.showUserData();
            }
            else if (value.startsWith("log:response=")) { // $NON-NLS$
                LogWindow.setWithResponse(Boolean.parseBoolean(value.substring("log:response=".length()))); // $NON-NLS$
            }
            else if ("log:flip".equals(value)) { // $NON-NLS-0$
                LogWindow.flipMode();
            }
            else if ("debug:on".equals(value)) { // $NON-NLS-0$
                DebugUtil.DEBUG = true;
            }
            else if ("debug:off".equals(value)) { // $NON-NLS-0$
                DebugUtil.DEBUG = false;
            }
            else if (value.startsWith("+prop:")) { // $NON-NLS-0$
                final String[] keyValue = value.substring(6).split("="); // $NON-NLS-0$
                sessionData.getUser().getAppConfig().addProperty(keyValue[0], keyValue[1]);
                southPanel.setDeveloperToolsVisibility();
            }
            else if (value.startsWith("-prop:")) { // $NON-NLS-0$
                sessionData.getUser().getAppConfig().addProperty(value.substring(6), null);
                southPanel.setDeveloperToolsVisibility();
            }
            else if (value.startsWith("-notify:")) { // $NON-NLS$
                Notifications.add("Test", value.substring("-notify:".length())); // $NON-NLS$
            }
            else if ("version:show".equals(value)) { // $NON-NLS-0$
                Dialog.info("Version", Version.INSTANCE.build()); // $NON-NLS$
            }
            else if (value.equals("force:logout")) { // $NON-NLS$
                AbstractMainController.INSTANCE.logoutExpiredSession();
            }
            else if (value.equals("log:server")) { // $NON-NLS$
                DebugUtil.logToServer("Serverlog Ping!", new RuntimeException("TestException"));
            }
            else {
                return "".equals(value) || SEARCH_DEFAULT.equals(value);
            }
            return true;
        }
    }
}