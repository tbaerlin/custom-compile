/*
 * AppConfig.java
 *
 * Created on 29.04.2008 11:08:28
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@NonNLS
public class AppConfig implements Serializable {
    protected static final long serialVersionUID = 1L;

    public static final String PROP_KEY_STARTPAGE = "startpage";

    public static final String PROP_KEY_MSGID = "msgid";

    public static final String PROP_KEY_KAPITALMARKTFAVORITENID = "kmfid";

    public static final String PROP_KEY_SESSIONID = "JSESSIONID";

    public static final String PROP_KEY_PENDING_ALERTS = "alerts";

    public static final String PROP_KEY_MUSTERDEPOT_USERID = "mduid";

    public static final String NEWS_HEADLINES_AUTO_RELOAD = "nwsAutoReload";

    public static final String HIDE_AMAZON = "hideAmaz";

    public static final String HIDE_PROD_TEASER = "hideProdTeaser";

    public static final String HIDE_LIMITS = "hideLimits";

    public static final String CUSTOM_ISSUER_ONLY = "cio";

    public static final String PDF_ORIENTATION_PORTRAIT = "pdfOrientPortrait";

    public static final String SEARCH_BY_VALOR = "searchByValor";

    public static final String DISPLAY_VWDCODE = "vwdcode";

    public static final String PROP_KEY_MIN_CHARGE = "minCharge";

    public static final String PROP_KEY_PERCENTAGE_CHARGE = "percentageCharge";

    public static final String PROP_KEY_PUSH = "push";

    public static final String PROP_KEY_DEV = "dev";

    public static final String PROP_KEY_CREDENTIALS = "credentials";

    public static final String SHOW_PUSH_IN_TITLE = "pushtitle";

    public static final String SHOW_CASH_IN_PORTFOLIO = "showCashInPortfolio";

    public static final String COLOR_PUSH = "colorPush";

    public static final String TRIGGER_BROWSER_PRINT_DIALOG = "triggerBrowserPrintDialog";

    public static final String OPEN_WATCHLIST_ON_ADD = "openWatchlistOnAdd";

    public static final String LIVE_FINDER_ELEMENT_PREFIX = "lfe_";

    public static final String LIVE_FINDER_SECTION_PREFIX = "lfs_";

    public static final String CLIENT_PROP_PREFIX = "cpp_";

    public static final String PROP_KEY_DASHBOARD = "dash";

    public static final String PROP_KEY_FINDER_PAGE_SIZE = "entriesPerPageInFinderSearchResults";

    public static String getPropKeyWlColumns(String watchlistId) {
        return getPropKey("tcols:wl:", watchlistId);
    }

    public static String getPropKeyPfColumns(String portfolioId) {
        return getPropKey("tcols:pf:", portfolioId);
    }

    private static String getPropKey(String prefix, String suffix) {
        return (suffix != null) ? (prefix + suffix) : null;
    }

    /**
     * Configuration for user defined content pages
     */
    private ArrayList<MyspaceConfig> myspaceConfigs = new ArrayList<>();

    /**
     * Simple preferences that do not fit anywhere else; keep until all serialized objects
     * have been converted and stored again
     * @deprecated use {@link #properties}
     */
    @Deprecated
    private HashMap<String, String> preferences = null;

    /**
     * General properties
     */
    private HashMap<String, String> properties = new HashMap<>();

    /**
     * Configures the contents of the various workspaces
     */
    private HashMap<String, WorkspaceConfig> workspaces = new HashMap<>();

    /**
     * Searches for various forms that have been saved by the user
     */
    private HashMap<String, ArrayList<FinderFormConfig>> searches = new HashMap<>();


    public AppConfig() {
        // for GWT-RPC
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(500);
        sb.append("AppConfig[prefs={");
        for (String key : properties.keySet()) {
            sb.append(", ").append(key).append(" => ").append(this.properties.get(key));
        }
        sb.append("}");
        sb.append(", spaces=[");
        for (MyspaceConfig config : this.myspaceConfigs) {
            sb.append(", ").append(config);
        }
        sb.append("], workspaces=[");
        for (WorkspaceConfig config : this.workspaces.values()) {
            sb.append(", ").append(config);
        }
        return sb.append("]").toString();
    }

    public void ensureInitialMySpace(String locale) {
        if (this.myspaceConfigs.isEmpty()) {
            addMyspace(getDefaultMyspaceName(locale));
        }
    }

    private String getDefaultMyspaceName(String locale) {
        if (locale != null) {
            if (locale.startsWith("it")) {
                return "Workspace 1";
            }
            if (locale.startsWith("de")) {
                return "Seite 1";
            }
        }
        return "Page 1";
    }

    public MyspaceConfig addMyspace(String name) {
        final MyspaceConfig config = new MyspaceConfig();
        config.setAppConfig(this);
        config.setId(getNextMyspaceId());
        config.setName(name);
        this.myspaceConfigs.add(config);
        firePropertyChange("myspace.add", null, config.getId());
        return config;
    }

    public void addWorkspace(String id, WorkspaceConfig config) {
        this.workspaces.put(id, config);
        firePropertyChange("workspace.update", null, id);
    }

    public ArrayList<MyspaceConfig> getMyspaceConfigs() {
        return this.myspaceConfigs;
    }

    @SuppressWarnings("unused")
    public MyspaceConfig getMyspaceConfig(String name) {
        for (MyspaceConfig config : this.myspaceConfigs) {
            if (name.equals(config.getName())) {
                return config;
            }
        }
        return null;
    }

    public String getProperty(String key) {
        return this.properties.get(key);
    }

    public String getProperty(String key, String defaultValue) {
        final String result = this.properties.get(key);
        return result != null ? result : defaultValue;
    }

    public int getIntProperty(String key, int defaultValue) {
        final String result = this.properties.get(key);
        return result != null ? Integer.parseInt(result) : defaultValue;
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        final String result = this.properties.get(key);
        return result != null ? Boolean.valueOf(result) : defaultValue;
    }

    public WorkspaceConfig getWorkspaceConfig(String id) {
        return this.workspaces.get(id);
    }

    public boolean addProperty(String key, boolean value) {
        return addProperty(key, value, true);
    }

    public boolean addProperty(String key, boolean value, boolean fireEvent) {
        return addProperty(key, Boolean.toString(value), fireEvent);
    }

    public boolean addProperty(String key, int value) {
        return addProperty(key, value, true);
    }

    public boolean addProperty(String key, int value, boolean fireEvent) {
        return addProperty(key, Integer.toString(value), fireEvent);
    }

    public boolean addProperty(String key, String value) {
        return addProperty(key, value, true);
    }

    public boolean addProperty(String key, String value, boolean fireEvent) {
        final String old = replaceProperty(key, value);
        final boolean changed = !StringUtil.equals(old, value);
        if (fireEvent && changed) {
            firePropertyChange(value, old);
        }
        return changed;
    }

    private void firePropertyChange(String value, String old) {
        firePropertyChange("property.change", value, old);
    }

    public void firePropertyChange() {
        firePropertyChange("property.change", null, null);
    }

    private String replaceProperty(String key, String value) {
        if (value != null) {
            return this.properties.put(key, value);
        }
        return this.properties.remove(key);
    }

    public void removeMyspace(int index) {
        final MyspaceConfig myspaceConfig = this.myspaceConfigs.remove(index);
        firePropertyChange("myspace.remove", null, myspaceConfig.getId());
    }

    @SuppressWarnings("unused")
    public boolean removeWorkspace(String id) {
        final boolean removed = this.workspaces.remove(id) != null;
        if (removed) {
            firePropertyChange("workspace.remove", null, id);
        }
        return removed;
    }

    public ArrayList<FinderFormConfig> getSearches(String id) {
        if (this.searches == null) {
            this.searches = new HashMap<>();
        }
        ArrayList<FinderFormConfig> result = this.searches.get(id);
        if (result == null) {
            result = new ArrayList<>();
            this.searches.put(id, result);
        }
        return result;
    }

    public FinderFormConfig getSavedSearch(String id, int n) {
        final ArrayList<FinderFormConfig> configs = getSearches(id);
        assert n < configs.size();
        return configs.get(n);
    }

    public FinderFormConfig getSavedSearch(String id, String name) {
        final ArrayList<FinderFormConfig> configs = getSearches(id);
        for (FinderFormConfig config : configs) {
            if (config.getName().equals(name)) {
                return config;
            }
        }
        return null;
    }

    /**
     * Saves the search defined by config
     * @param config defines search
     */
    public void saveSearch(FinderFormConfig config) {
        final FinderFormConfig existing = getSavedSearch(config.getFinderFormId(), config.getName());
        if (existing != null) {
            existing.copyParameters(config);
            firePropertyChange("search.replace", null, config);
        }
        else {
            getSearches(config.getFinderFormId()).add(config);
            firePropertyChange("search.add", null, config);
        }
    }

    public void removeSearch(String id, int n) {
        final ArrayList<FinderFormConfig> configs = getSearches(id);
        assert n < configs.size();
        final FinderFormConfig config = configs.remove(n);
        firePropertyChange("search.remove", null, config);
    }

    public void moveSearch(String id, int n, boolean up) {
        final ArrayList<FinderFormConfig> configs = getSearches(id);
        assert n < configs.size();
        final FinderFormConfig config = configs.remove(n);
        configs.add(up ? n - 1 : n + 1, config);
        firePropertyChange("search.move", null, config);
    }

    private String getNextMyspaceId() {
        int result = 0;
        for (MyspaceConfig config : this.myspaceConfigs) {
            result = Math.max(Integer.parseInt(config.getId()), result);
        }
        return Integer.toString(result + 1);
    }

    @SuppressWarnings("deprecation")
    private Object readResolve() {
        if (this.workspaces == null) {
            // avoid problems with objects serialized before workspaces field was introduced
            this.workspaces = new HashMap<>();
        }
        for (MyspaceConfig config : this.myspaceConfigs) {
            // avoid problems with objects serialized before appConfig field was set on init
            config.setAppConfig(this);
            for (SnippetConfiguration c : config.getSnippetConfigs()) {
                c.setAppConfig(this);
                // nameInTitle is missing in older myspace configurations
                if ("PortraitChart".equals(c.getName()) && !c.containsKey("nameInTitle")) {
                    c.put("nameInTitle", true);
                }
            }
        }
        if (this.preferences != null) {
            if (this.properties == null) {
                this.properties = new HashMap<>();
            }
            this.properties.putAll(this.preferences);
            this.preferences = null;
        }
        return this;
    }

    public void renameMyspace(int i, String name) {
        this.myspaceConfigs.get(i).setName(name);
        firePropertyChange("myspace.rename", null, name);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (EventBusRegistry.isSet()) { // will not be available on server side
            EventBusRegistry.get().fireEvent(new ConfigChangedEvent(propertyName, oldValue, newValue));
        }
    }

    public void appendHtmlTable(final StringBuilder sb) {
        sb.append("<table><tr><td>prefs</td></tr>");
        for (String key : new TreeSet<>(this.properties.keySet())) {
            sb.append("<tr><td>&nbsp;</td><td>").append(key).append("</td><td>").append(this.properties.get(key)).append("</td></tr>");
        }
        sb.append("<table><tr><td>spaces</td></tr>");
        for (MyspaceConfig config : this.myspaceConfigs) {
            sb.append("<tr><td>&nbsp;</td><td>").append(config).append("</td></tr>");
        }
        sb.append("<table><tr><td>workspaces</td></tr>");
        for (WorkspaceConfig config : this.workspaces.values()) {
            sb.append("<tr><td>&nbsp;</td><td>").append(config).append("</td></tr>");
        }
        sb.append("</table>");
    }

    @SuppressWarnings("unused")
    public List<String> getPropertyList() {
        return new ArrayList<>(this.properties.keySet());
    }

    public Set<String> getPropertyKeysWithPrefix(String keyPrefix) {
        final HashSet<String> set = new HashSet<>();
        for (String key : this.properties.keySet()) {
            if (key.startsWith(keyPrefix)) {
                set.add(key);
            }
        }
        return set;
    }

    public void addClientProps(Map<String, String> properties) {
        for (Map.Entry<String, String> prop : properties.entrySet()) {
            addProperty(CLIENT_PROP_PREFIX + prop.getKey(), prop.getValue());
        }
    }
}
