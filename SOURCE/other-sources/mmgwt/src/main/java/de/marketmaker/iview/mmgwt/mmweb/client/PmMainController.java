/*
 * PmMainController.java
 *
 * Created on 02.09.2009 16:45:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderNews;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderCER;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderWNT;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.CerListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ChartRatioUniverseSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MostActiveWNTSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.TopFlopSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.UnderlyingListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.WatchlistController;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PmMainController extends SimpleMainController {
    private final HashMap<String, String> pages = new LinkedHashMap<>();

    public PmMainController() {
    }

    public PmMainController(String contextPath) {
        super(contextPath);
    }

    @Override
    public AbstractMainView createView() {
        this.pages.clear();
        this.pages.put("M_LF_CER", I18n.I.certificatesSearch());  // $NON-NLS$
        this.pages.put("M_Z_UB", I18n.I.certificateOverview());  // $NON-NLS$
        this.pages.put("M_LF_WNT", I18n.I.warrantFinder());  // $NON-NLS$
        this.pages.put("M_O_UB", I18n.I.warrantOverview());  // $NON-NLS$
        this.pages.put("B_W", I18n.I.watchlist());  // $NON-NLS$

        if (isWithNews()) {
            this.pages.put("N_UB", I18n.I.news());  // $NON-NLS$
            this.pages.put("N_S", I18n.I.newsSearch());  // $NON-NLS$
        }

        return new PmMainView(this);
    }

    Set<Map.Entry<String, String>> getPages() {
        return this.pages.entrySet();
    }

    @Override
    public String getStartPage() {
        return "M_LF_CER"; // $NON-NLS$
    }

    @Override
    protected void onBeforeInit() {
        // overwrite behavior of SimpleMainController -> only goto startpage, if no page is specified
    }

    @Override
    public SafeHtml getContentHeader(String controllerId) {
        final String header = this.pages.get(controllerId);
        return (header != null) ? SafeHtmlUtils.fromString(header) : super.getContentHeader(controllerId);
    }

    @Override
    public boolean isValidToken(String token) {
        return this.pages.containsKey(token) || super.isValidToken(token);
    }

    @Override
    protected void initSnippetClasses() {
        super.initSnippetClasses();

        SnippetClass.addClass(new CerListSnippet.Class());
        SnippetClass.addClass(new UnderlyingListSnippet.Class());
        SnippetClass.addClass(new ChartRatioUniverseSnippet.Class());
        SnippetClass.addClass(new TopFlopSnippet.Class());
        SnippetClass.addClass(new MostActiveWNTSnippet.Class());
    }

    @Override
    protected void initControllers() {
        super.initControllers();

        addControllerCheckJson(false, "M_LF_CER", LiveFinderCER.INSTANCE_CER); // $NON-NLS$
        addControllerCheckJson(false, "M_Z_UB", new SimpleOverviewController(getView(), "ov_cer")); // $NON-NLS$

        addControllerCheckJson(false, "M_LF_WNT", LiveFinderWNT.INSTANCE); // $NON-NLS$
        addControllerCheckJson(false, "M_O_UB", new SimpleOverviewController(getView(), "ov_wnt")); // $NON-NLS$

        addControllerCheckJson(false, "B_W", WatchlistController.INSTANCE); // $NON-NLS$

        if (isWithNews()) {
            addControllerCheckJson(false, "N_UB", new OverviewNWSController(getView())); // $NON-NLS$
            addControllerCheckJson(false, "N_S", FinderNews.INSTANCE); // $NON-NLS$
        }
    }

    boolean isWithNews() {
        return SessionData.INSTANCE.getUser().getAppProfile().isAnyNewsAllowed();
    }

    @Override
    public String getAuthentication() {
        return null; // session-based
    }

    @Override
    protected List<Command> initInitCommands() {
        ArrayList<Command> result = new ArrayList<>();
        result.add(new LoginCommand("$mmauth", "")); // $NON-NLS$
        result.addAll(super.initInitCommands());
        result.add(new UserDataLoader());
        return result;
    }

    @Override
    protected Command getOnModuleLoadCommand() {
        return this::runInitSequence;
    }

    @Override
    protected void syncMenuModelAndView(HistoryToken historyToken) {
        ((PmMainView) getView()).setActiveTabItem(historyToken);
    }

    @Override
    public ChangePasswordDisplay.Presenter.PasswordStrategy getPasswordStrategy() {
        return null;
    }
}
