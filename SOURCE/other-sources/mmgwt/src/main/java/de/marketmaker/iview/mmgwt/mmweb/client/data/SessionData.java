/*
 * SessionData.java
 *
 * Created on 13.08.2008 12:26:00
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.data;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.MSCUserConfiguration;
import de.marketmaker.iview.dmxml.MSCUserLists;
import de.marketmaker.iview.dmxml.PortfolioElement;
import de.marketmaker.iview.dmxml.WatchlistElement;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.GrainsMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.MetalsMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.as.AsMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.UserListUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PortfolioUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import static de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig.CLIENT_PROP_PREFIX;
import static de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig.PROP_KEY_DEV;
import static de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig.PROP_KEY_PUSH;

/**
 * @author Ulrich Maurer
 */
@NonNLS
public class SessionData {
    /**
     * @deprecated  Use inversion of control and, if possible, injection!
     */
    @Deprecated
    @Inject
    public static SessionData INSTANCE;

    private int maxNumPortfolios;
    private int maxNumPositionsPerPortfolio;
    private int maxNumPositionsPerWatchlist;
    private int maxNumWatchlists;
    private int maxNumMyspaces = -1; // signal for initialEvent action if maxNumMyspaces!=-1
    private boolean withPush = false;
    private boolean dev = false;
    private Boolean showWkn = null;
    private Boolean showIsin = null;

    private boolean iceDesign = false;
    private boolean withMarketData = false;
    private boolean withPmBackend = false;

    private VwdCustomerServiceContact pmCustomerServiceContact = null; //Customer Service Contact of the bank that operates the PM installation
    private MmJsDate loggedInSince = null;
    private User user = null;
    private List<WatchlistElement> watchlists = null;
    private List<PortfolioElement> portfolios = null;

    private JSONWrapper guiDefs;

    private Map<String, List<QuoteWithInstrument>> lists = Collections.emptyMap();

    private List<PortfolioElement> profiPortfolios;

    public boolean isDev() {
        return dev;
    }

    /**
     * @deprecated Use the {@see isIceDesign} variant instead!
     */
    @Deprecated
    public static boolean isAsDesign() {
        return INSTANCE.isIceDesign();
    }

    public boolean isIceDesign() {
        return this.iceDesign;
    }

    public void setIceDesign() {
        this.iceDesign = true;
    }

    public static boolean isWithMarketData() {
        return INSTANCE.withMarketData;
    }

    public void setWithMarketData() {
        this.withMarketData = true;
    }

    private void determineIsWithMarketData() {
        if (AbstractMainController.INSTANCE instanceof AsMainController) {
            this.withMarketData = Selector.AS_MARKETDATA.isAllowed();
        }
        else if (AbstractMainController.INSTANCE instanceof MainController) {
            this.withMarketData = !Selector.KWT_NO_MARKETDATA.isAllowed();
        }
        else if(AbstractMainController.INSTANCE instanceof MetalsMainController
                || AbstractMainController.INSTANCE instanceof GrainsMainController) {
            setWithMarketData();
        }
    }

    public static boolean isWithPmBackend() {
        return INSTANCE.withPmBackend;
    }

    public void setWithPmBackend() {
        this.withPmBackend = true;
    }

    public static boolean isWithLimits() {
        return !isWithPmBackend();
    }

    public int getMaxNumPortfolios() {
        return maxNumPortfolios;
    }

    @SuppressWarnings("unused")
    public int getMaxNumPositionsPerPortfolio() {
        return maxNumPositionsPerPortfolio;
    }

    @SuppressWarnings("unused")
    public int getMaxNumPositionsPerWatchlist() {
        return maxNumPositionsPerWatchlist;
    }

    public int getMaxNumWatchlists() {
        return maxNumWatchlists;
    }

    public int getMaxNumMyspaces() {
        return maxNumMyspaces;
    }

    private void adaptAppConfigByParameters(User user) {
        final AppConfig appConfig = user.getAppConfig();
        final String paramProps = Window.Location.getParameter("props");
        if (paramProps != null) {
            final String[] props = paramProps.split(",");
            for (String prop : props) {
                final String[] kv = prop.split(":");
                if (kv.length == 2) {
                    final String key = kv[0];
                    final String value = "null".equals(kv[1]) ? null : kv[1];
                    appConfig.addProperty(key, value);
                }
            }
        }
    }

    public void setUser(User user) {
        if (this.user != null) {
            throw new IllegalStateException("user is already specified");
        }
        this.loggedInSince = new MmJsDate();
        this.user = user;
        this.withPush = this.user.getAppConfig().getBooleanProperty(PROP_KEY_PUSH, false);
        this.dev = this.user.getAppConfig().getBooleanProperty(PROP_KEY_DEV, false);
        adaptAppConfigByParameters(user);
        determineIsWithMarketData();
    }

    @SuppressWarnings("unused")
    public MmJsDate getLoggedInSince() {
        return this.loggedInSince;
    }

    public String getLoggedInSinceWithLabel() {
        if (this.loggedInSince == null) {
            return null;
        }
        return this.loggedInSince.isSameDay(new MmJsDate())
                ? I18n.I.loggedinSinceWithLabel(JsDateFormatter.formatHhmm(this.loggedInSince))
                : I18n.I.loggedinSinceWithLabel(JsDateFormatter.formatDdmmyyyyHhmm(this.loggedInSince));
    }

    public String getLoggedInSinceString() {
        if (this.loggedInSince == null) {
            return null;
        }
        return this.loggedInSince.isSameDay(new MmJsDate())
                ? JsDateFormatter.formatHhmm(this.loggedInSince)
                : JsDateFormatter.formatDdmmyyyyHhmm(this.loggedInSince);
    }

    public User getUser() {
        return this.user;
    }

    public String getUserName() {
        final String first = this.user.getFirstName();
        final String last = this.user.getLastName();

        // In case of Infront Advisory Solution, replace VM/NN placeholder for actual names, which was
        // introduced after a hacker attack in fall 2015, with the login (vwd portfolio manager).
        // The pm login is usually shown if a user has no license for market data. Cf. AS-1545.
        if(isWithPmBackend() && "VN".equalsIgnoreCase(first) && "NN".equalsIgnoreCase(last)) {
            return this.user.getLogin();
        }

        return first == null
                ? (last == null ? this.user.getLogin() : last)
                : (last == null ? first : (first + " " + last));
    }

    public JSONWrapper getGuiDef(String s) {
        if (this.guiDefs != null) {
            return this.guiDefs.get(s);
        }
        return JSONWrapper.INVALID;
    }

    public String getGuiDefValue(String... keys) {
        if (keys.length == 0) {
            return null;
        }
        final JSONValue jsonValue = SessionData.INSTANCE.getGuiDef(keys[0]).getValue();
        return getGuiDefValue(jsonValue, 1, keys);
    }

    @SuppressWarnings("unused")
    public boolean isGuiDefValueTrue(String... keys) {
        return "true".equals(getGuiDefValue(keys));
    }

    private String getGuiDefValue(JSONValue jsonValue, int index, String[] keys) {
        if (jsonValue == null || keys.length < index) {
            return null;
        }
        if (keys.length == index) {
            final JSONString jsonString = jsonValue.isString();
            if (jsonString == null) {
                return null;
            }
            return jsonString.stringValue();
        }
        final JSONArray jsonArray = jsonValue.isArray();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); i++) {
                final String s = getGuiDefValue(jsonArray.get(i), index, keys);
                if (s != null) {
                    return s;
                }
            }
            return null;
        }
        final JSONObject jsonObject = jsonValue.isObject();
        if (jsonObject != null) {
            final JSONValue jsonValueRupv = jsonObject.get("requiredUserProductVariant");
            if (jsonValueRupv != null) {
                final String rupv = jsonValueRupv.isString().stringValue();
                if (!SessionData.INSTANCE.getUser().getAppProfile().isProductAllowed(rupv)) {
                    return null;
                }
            }

            final JSONValue jsonValueRpid = jsonObject.get("requiredProduktId");
            if (jsonValueRpid != null) {
                final String rpid = jsonValueRpid.isString().stringValue();
                if (!rpid.equals(SessionData.INSTANCE.getUser().getAppProfile().getProduktId())) {
                    return null;
                }
            }

            final JSONValue jsonValueRsel = jsonObject.get("requiredSelector");
            if (jsonValueRsel != null) {
                final String rsel = jsonValueRsel.isString().stringValue();
                if (!Selector.valueOf(rsel).isAllowed()) {
                    return null;
                }
            }

            final JSONValue jsonValueModule = jsonObject.get("requiredModuleName");
            if (jsonValueModule != null) {
                final String moduleName = JsUtil.getMetaValue("mmModuleName");
                if (!StringUtil.equals(moduleName, jsonValueModule.isString().stringValue())) {
                    return null;
                }
            }

            return getGuiDefValue(jsonObject.get(keys[index]), index + 1, keys);
        }
        return null;
    }

    public JSONWrapper getGuiDefs() {
        return guiDefs;
    }

    public void setGuiDefs(JSONValue guiDefs) {
        if (this.guiDefs == null) {
            this.guiDefs = new JSONWrapper(guiDefs);
        }
        else {
            final JSONObject target = this.guiDefs.getValue().isObject();
            final JSONObject source = guiDefs.isObject();
            for (String key : source.keySet()) {
                target.put(key, source.get(key));
            }
        }
    }

    public boolean hasGuiDef(String key) {
        final JSONValue jsonValue = getGuiDef(key).getValue();
        if (jsonValue == null) {
            return false;
        }
        final JSONString jsonString = jsonValue.isString();
        return jsonString == null || !"hidden".equals(jsonString.stringValue());
    }

    public String getUserProperty(String s) {
        return this.user.getAppConfig().getProperty(s);
    }

    public String getClientProperty(String name) {
        return this.user.getAppConfig().getProperty(CLIENT_PROP_PREFIX + name);
    }

    public boolean isUserPropertyTrue(String s) {
        return this.user != null && "true".equals(getUserProperty(s));
    }

    @SuppressWarnings("unused")
    public boolean isUserPropertyFalse(String s) {
        return this.user != null && "false".equals(getUserProperty(s));
    }

    public boolean setWatchlists(List<WatchlistElement> elements) {
        final List<WatchlistElement> oldValue = this.watchlists;
        this.watchlists = elements;
        final boolean result = PortfolioUtil.watchlistsDiffer(oldValue, elements);
        EventBusRegistry.get().fireEvent(UserListUpdatedEvent.forWatchlists(result));
        return result;
    }

    public List<WatchlistElement> getWatchlists() {
        return this.watchlists;
    }

    public boolean setPortfolios(List<PortfolioElement> elements) {
        final List<PortfolioElement> oldValue = this.portfolios;
        this.portfolios = elements;
        final boolean result = PortfolioUtil.portfoliosDiffer(oldValue, elements);
        EventBusRegistry.get().fireEvent(UserListUpdatedEvent.forPortfolios(result));
        return result;
    }

    public List<PortfolioElement> getPortfolios() {
        return this.portfolios;
    }

    public boolean isWithProfiDepots() {
        return getUserProperty(AppConfig.PROP_KEY_MUSTERDEPOT_USERID) != null;
    }

    public boolean isWithPush() {
        return this.withPush && !isWithPmBackend();
    }

    public boolean isAnonymous() {
        return AbstractMainController.INSTANCE.isAnonymous();
    }

    public void setProfiData(MSCUserLists userLists) {
        setProfiPortfolios(userLists.getPortfolios().getElement());
    }

    public void setUserData(MSCUserLists userLists, MSCUserConfiguration userConfiguration) {
        this.maxNumPortfolios = Integer.parseInt(userLists.getMaxNumPortfolios());
        this.maxNumPositionsPerPortfolio = Integer.parseInt(userLists.getMaxNumPositionsPerPortfolio());
        this.maxNumWatchlists = Integer.parseInt(userLists.getMaxNumWatchlists());
        this.maxNumPositionsPerWatchlist = Integer.parseInt(userLists.getMaxNumPositionsPerWatchlist());
        setWatchlists(userLists.getWatchlists().getElement());
        setPortfolios(userLists.getPortfolios().getElement());
        Firebug.log("#pfs: " + size(this.portfolios) + ", #wls: " + size(this.watchlists));
        this.maxNumMyspaces = Integer.parseInt(userConfiguration.getMaxNumMyspaces());
        Firebug.log("maxNumMyspaces: " + this.maxNumMyspaces);
    }

    private int size(List l) {
        return l == null ? -1 : l.size();
    }

    public void setLists(Map<String, List<QuoteWithInstrument>> lists) {
        this.lists = lists;
    }

    public void updateLists(Map<String, List<QuoteWithInstrument>> lists) {
        this.lists.putAll(lists);
    }

    public List<QuoteWithInstrument> getList(String name) {
        final List<QuoteWithInstrument> result = this.lists.get(name);
        return result != null ? result : Collections.emptyList();
    }

    public String getListAsFinderQuery(String name) {
        final List<QuoteWithInstrument> listQwi = this.lists.get(name);
        if (listQwi == null || listQwi.isEmpty()) {
            return null;
        }
        final StringBuilder sbQuery = new StringBuilder();
        String divider = "";
        sbQuery.append("qid=='");
        for (QuoteWithInstrument qwi : listQwi) {
            sbQuery.append(divider);
            divider = "@";
            sbQuery.append(qwi.getQuoteData().getQid().replaceFirst("\\.qid", ""));
        }
        sbQuery.append("'");
        return sbQuery.toString();
    }


    public String getStyleSuffix() {
        return getGuiDefValue("pdf-style-variant", "styleVariant");
    }

    public String getJsessionID() {
        return getUserProperty(AppConfig.PROP_KEY_SESSIONID);
    }

    public String getCredentials() {
        return getUserProperty(AppConfig.PROP_KEY_CREDENTIALS);
    }

    public void setProfiPortfolios(List<PortfolioElement> profiPortfolios) {
        this.profiPortfolios = profiPortfolios;
    }

    public List<PortfolioElement> getProfiPortfolios() {
        return this.profiPortfolios;
    }

    public boolean isShowWkn() {
        if (this.showWkn != null) {
            return this.showWkn;
        }
        final String showSymbol = getGuiDefValue("showSymbol");
        // preferred symbol
        this.showWkn = showSymbol == null || showSymbol.contains("wkn");
        return this.showWkn;
    }

    public boolean isShowIsin() {
        if (this.showIsin != null) {
            return this.showIsin;
        }
        final String showSymbol = getGuiDefValue("showSymbol");
        // alternative symbol
        this.showIsin = showSymbol != null && showSymbol.contains("isin");
        return this.showIsin;
    }

    /**
     * The GuiDefs has not yet been loaded when the LoginView is viewed. Hence, the customer service
     * contact is defined as a Dictionary when the host page/script.nocache.js is loaded.
     */
    public VwdCustomerServiceContact getFirstVwdCustomerServiceContact() {
        return getVwdCustomerServiceContacts().get(0);
    }

    public JsArray<VwdCustomerServiceContact> getVwdCustomerServiceContacts() {
        JsArray<VwdCustomerServiceContact> contact = getVwdCustomerServiceContactArray();
        if (contact == null || contact.length() < 1) {
            Firebug.log("No vwd customer service contact data available. Using default data.");
            contact = getDefaultCustomerServiceContactArray();
        }
        return contact;
    }

    /**
     * If service contact is not loaded from backend, set asterisks. this should never happen.
     */
    public VwdCustomerServiceContact getPmCustomerServiceContact() {
        if (this.pmCustomerServiceContact == null) {
            Firebug.log("No Advisory Solution with PM [web] customer service contact data available. Using default data.");
            final String S = "*****";
            this.pmCustomerServiceContact = VwdCustomerServiceContact.create("", S, S, S);
        }
        return this.pmCustomerServiceContact;
    }

    public void setPmCustomerServiceContact(Map<String, String> map) {
        if (map.containsKey("serviceName")) {
            final String name = map.get("serviceName"), email = map.get("serviceEMail"),
                    phone = map.get("servicePhone"), fax = map.get("serviceFax");
            this.pmCustomerServiceContact = VwdCustomerServiceContact.create(name, email, phone, fax);
        }
    }

    private native JsArray<VwdCustomerServiceContact> getVwdCustomerServiceContactArray() /*-{
        return $wnd.vwdCustomerServiceContactArray;
    }-*/;

    private native JsArray<VwdCustomerServiceContact> getDefaultCustomerServiceContactArray() /*-{
        return [{name: "Customer Service vwd group Germany", email: "service@vwd.com", phone: "+49 69 260 95 760", fax: ""}];
    }-*/;

    public static class VwdCustomerServiceContact extends JavaScriptObject {

        protected VwdCustomerServiceContact() {}

        public static native VwdCustomerServiceContact create(String name, String email, String phone, String fax) /*-{
            return {name: name, email: email, phone: phone, fax: fax};
        }-*/;

        public final native String getKey() /*-{
            return this.key;
        }-*/;

        public final native String getName() /*-{
            return this.name;
        }-*/;

        public final native String getEmail() /*-{
            return this.email;
        }-*/;

        public final native String getPhone() /*-{
            return this.phone;
        }-*/;

        public final native String getFax() /*-{
            return this.fax;
        }-*/;
    }
}

