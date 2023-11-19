/*
 * SimpleMainController.java
 *
 * Created on 17.03.2008 14:58:41
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.BeforeRequestEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * A MainController for a standalone version that requires no login, does not support any
 * user related actions, no top menu, no workspaces, no finders, etc. Supported are
 * just portraits with different views and news details.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SimpleMainController extends AbstractMainController {
    private Map<String, String> mapAllowedContent = null;

    public SimpleMainController() {
    }

    public SimpleMainController(String contextPath) {
        super(contextPath);
    }

    @Override
    public AbstractMainView createView() {
        return new SimpleMainView(this);
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Override
    public boolean isWithStoreAppConfig() {
        return false;
    }

    @Override
    public void selectionChanged(String id) {
        // no menu in view, so no way that this gets called
        throw new UnsupportedOperationException("selectionChanged"); // $NON-NLS-0$
    }

    @Override
    protected Command getOnModuleLoadCommand() {
        return () -> {
            GetPublicProfileCommand profileCommand = new GetPublicProfileCommand(getLogin());
            profileCommand.execute();
        };
    }

    public String getAuthentication() {
        return SessionData.INSTANCE.getUser().getLogin();
    }

    protected String getLogin() {
        return Window.Location.getParameter("Xun"); // $NON-NLS-0$
    }

    @Override
    protected List<Command> initInitCommands() {
        return Arrays.asList(
                new GuiDefsLoader()
                , new ListLoader(I18n.I.loadCurrentData())
                , this.loadStylesheetsInitFunction
        );
    }

    @Override
    protected void onBeforeInit() {
        final String startpage = getStartPage();
        if (StringUtil.hasText(startpage)) {
            Firebug.log("startpage = " + startpage); // $NON-NLS-0$
            PlaceUtil.goTo(startpage);
        }
        else {
            Firebug.log("no startpage in Window.Location specified"); // $NON-NLS-0$
        }
    }

    @Override
    protected void initRequestAuthentication() {
        final String auth = getAuthentication();
        if (auth != null) {
            ChartUrlFactory.setAuthentication(auth);
            EventBusRegistry.get().addHandler(BeforeRequestEvent.getType(),
                    event -> event.getRequest().getDmxmlRequest().setAuthentication(auth));
        }
    }

    @Override
    public String getStartPage() {
        return Window.Location.getParameter("startpage"); // $NON-NLS-0$
    }

    @Override
    protected void onAfterInit() {
        setupView();
    }

    @Override
    protected void onAfterFirstPage() {
        // empty
    }

    @Override
    protected void initSnippetClasses() {
        SimpleControllerInitializer.initSnippetClasses();
    }

    @Override
    protected void initControllers() {
        SimpleControllerInitializer.initControllers(this);
    }

    @Override
    protected MenuModel initMenuModel() {
        return null;
    }

    private Map<String, String> getMapAllowedContent() {
        if (this.mapAllowedContent == null) {
            this.mapAllowedContent = new HashMap<>();
            final JSONValue jvAllowedContent = SessionData.INSTANCE.getGuiDef("allowed-content").getValue(); // $NON-NLS-0$
            if (jvAllowedContent != null) {
                final JSONObject joAllowedContent = jvAllowedContent.isObject();
                if (joAllowedContent != null) {
                    for (final String id : joAllowedContent.keySet()) {
                        final String value = joAllowedContent.get(id).isString().stringValue();
                        this.mapAllowedContent.put(id, value);
                    }
                }
            }
        }
        return this.mapAllowedContent;
    }

    public boolean isValidToken(String token) {
        for (String regEx : getMapAllowedContent().keySet()) {
            if (token.matches(regEx)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void syncMenuModelAndView(HistoryToken historyToken) {
        // empty
    }

    @Override
    public SafeHtml getContentHeader(String controllerId) {
        for (Map.Entry<String, String> entry : getMapAllowedContent().entrySet()) {
            if (controllerId.matches(entry.getKey())) {
                return SafeHtmlUtils.fromString(entry.getValue());
            }
        }
        throw new NullPointerException("invalid content"); // $NON-NLS-0$
    }

    @Override
    public void refresh() {
        DmxmlContext.refreshRequested = true;

        if (this.currentPageController != null) {
            this.currentPageController.refresh();
        }

        DmxmlContext.refreshRequested = false;
    }

    @Override
    public void logoutExpiredSession() {
        // no session to expire, so no way that this gets called
        throw new UnsupportedOperationException("forceLogout"); // $NON-NLS-0$
    }

    @Override
    public void addControllerCheckJson(boolean jsonMenuElementNeeded, String menuItemId,
            PageController value) {
        if (isValidToken(menuItemId)) {
            super.addControllerCheckJson(jsonMenuElementNeeded, menuItemId, value);
        }
    }

    @Override
    public ChangePasswordDisplay.Presenter.PasswordStrategy getPasswordStrategy() {
        return null;
    }
}
