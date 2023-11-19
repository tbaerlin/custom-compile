/*
 * PmMainController.java
 *
 * Created on 02.09.2009 16:45:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PmReportMainController extends SimpleMainController {

    public PmReportMainController() {
    }

    public PmReportMainController(String contextPath) {
        super(contextPath);
    }

    @Override
    public String getStartPage() {
        return "PM_R"; // $NON-NLS$
    }

    @Override
    protected void onBeforeInit() {
        // overwrite behavior of SimpleMainController -> only goto startpage, if no page is specified
    }

    @Override
    public SafeHtml getContentHeader(String controllerId) {
        return SafeHtmlUtils.fromString("this is the content header"); // $NON-NLS$
    }

    @Override
    public boolean isValidToken(String token) {
        return "PM_R".equals(token); // $NON-NLS$
    }

    @Override
    protected void initControllers() {
        getView();
//        super.initControllers();
        // pm controllers are added, when pm module is loaded
    }

    @Override
    protected void initializeNativeFunctions() {
        super.initializeNativeFunctions();
        _initializeNativeFunctions();
    }

    private native void _initializeNativeFunctions() /*-{
        this.@de.marketmaker.iview.mmgwt.mmweb.client.PmReportMainController::log(Ljava/lang/String;)("_initializeNativeFunctions()"); // $NON-NLS$
        $wnd.mmShowReport = this.@de.marketmaker.iview.mmgwt.mmweb.client.PmReportMainController::mmShowReport(Ljava/lang/String;Ljava/lang/String;);
    }-*/;

    public void log(String message) {
        Firebug.log(message);
    }

    public void mmShowReport(String investorId, String layoutGuid) {
        PlaceUtil.goTo(PmWebModule.HISTORY_TOKEN_REPORT + "/investorId=" + investorId + "/layoutGuid=" + layoutGuid); // $NON-NLS$
    }

    public void mmLogin(String username, String password) {
        LoginView.login(username, password, new LoginView.LoginCallback() {
            @Override
            public void onError(String message) {
                mmErrorCallback(message);
            }

            @Override
            public void onSuccess(User user, String message) {
                SessionData.INSTANCE.setUser(user);
                AbstractMainController.INSTANCE.runInitSequence();
            }
        });
    }

    private native void mmErrorCallback(String message) /*-{
        $wnd.mmErrorCallback(message);
    }-*/;

    private native void mmModuleLoadCallback() /*-{
        $wnd.mmModuleLoadCallback();
    }-*/;

    private native void mmInitializedCallback() /*-{
        $wnd.mmInitializedCallback();
    }-*/;

    @Override
    public String getAuthentication() {
        return null; // session-based
    }

    @Override
    protected List<Command> initInitCommands() {
        ArrayList<Command> result = new ArrayList<Command>();
        result.addAll(super.initInitCommands());
        result.add(PmWebModule.createLoader());
        return result;
    }

    private native void _initializeLoginFunctions() /*-{
        this.@de.marketmaker.iview.mmgwt.mmweb.client.PmReportMainController::log(Ljava/lang/String;)("_initializeLoginFunctions()"); // $NON-NLS$
        $wnd.mmLogin = this.@de.marketmaker.iview.mmgwt.mmweb.client.PmReportMainController::mmLogin(Ljava/lang/String;Ljava/lang/String;);
    }-*/;

    @Override
    protected Command getOnModuleLoadCommand() {
        return new Command() {
            public void execute() {
                _initializeLoginFunctions();
                mmModuleLoadCallback();
            }
        };
    }

    @Override
    protected void onAfterInit() {
        super.onAfterInit();
        mmInitializedCallback();
    }
}
