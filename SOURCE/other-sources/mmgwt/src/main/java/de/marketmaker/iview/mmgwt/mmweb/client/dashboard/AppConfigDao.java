/*
 * AppConfigDao.java
 *
 * Created on 21.05.15
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.MyspaceConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.myspace.MyspaceToDashboard;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Finalizer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mloesch
 */
public class AppConfigDao extends ConfigDao {
    private static final String GUIDEFS_DASHBOARD_PREFIX = "guidefs:"; // $NON-NLS$

    private static final String PRIVATE_DASHBOARD_PREFIX = AppConfig.PROP_KEY_DASHBOARD + ":"; // $NON-NLS$

    private static final String PRIVATE_MYSPACE_PREFIX = "myspace:"; // $NON-NLS$

    private static final String MIGRATED_DASHBOARD_PROPERTY_PREFIX = "myspace_migrated_to_dashboard:"; // $NON-NLS$

    private final Map<String, DashboardConfig> dashboardsById = new HashMap<>();

    private final Map<String, List<DashboardConfig>> dashboardsByRole = new HashMap<>();

    public AppConfigDao() {
        final List<DashboardConfig> listFromGuidefs = readConfigsFromGuidefs();
        addListEntriesToMapByRole(listFromGuidefs, this.dashboardsByRole);
        addListEntriesToMapById(listFromGuidefs, this.dashboardsById);

        final List<DashboardConfig> listFromAppConfig = readConfigsFromAppConfig();
        addListEntriesToMapByRole(listFromAppConfig, this.dashboardsByRole);
        addListEntriesToMapById(listFromAppConfig, this.dashboardsById);

        importConfigsFromMyspace();
    }


    /**
     * Imports and converts myspaces into dashboards.
     * <p>
     * The myspaces are not deleted after migration. Instead, a property stores for each Myspace
     * if its dashboard has already been migrated or not. After the conclusive migration to
     * ICE/AS design we should opt to remove migrated Myspace configs with a backend job.
     * </p>
     * <p>
     * The method silently changes the app config. Only if at least one myspace has been migrated
     * to a dashboard, an app config changed event is fired.
     * </p>
     */
    private void importConfigsFromMyspace() {
        final AppConfig appConfig = SessionData.INSTANCE.getUser().getAppConfig();

        final Finalizer<Boolean> changedAppConfig = new Finalizer<>(false);

        for (MyspaceConfig mc : appConfig.getMyspaceConfigs()) {
            final String myspaceId = mc.getId();
            if (isMyspaceDashboardMigrated(appConfig, myspaceId)) {
                continue;
            }

            final DashboardConfig dc = new DashboardConfig();
            dc.setName(mc.getName());
            dc.setAccess(DashboardConfig.Access.PRIVATE);
            dc.setRoles(Collections.singletonList(DASHBOARD_ROLE_GLOBAL));

            try {
                MyspaceToDashboard.convertSnippetConfigs(mc, dc);

                saveNew(dc, false, newId -> {
                    appConfig.addProperty(toMigratedDashboardPropertyKey(myspaceId), newId, false);
                    Firebug.info("<AppConfigDao.importConfigsFromMyspace> migrated myspace config with ID " + myspaceId + " to private dashboard with ID " + newId);
                    changedAppConfig.set(true);
                });
            }
            catch(Exception e) {
                final String msg = "<AppConfigDao.importConfigsFromMyspace> failed to migrate snippet configs of myspace with ID " + mc.getId() + " and name '" + mc.getName() + "' to private dashboard configs";  // $NON-NLS$
                Firebug.error(msg, e);
                DebugUtil.logToServer(msg, e);
            }
        }

        if(changedAppConfig.get()) {
            Firebug.info("<AppConfigDao.importConfigsFromMyspace> some myspace migrated to dashboard. Firing app config property change event to trigger save.");
            appConfig.firePropertyChange();
        }
    }

    private void addListEntriesToMapByRole(List<DashboardConfig> list, Map<String, List<DashboardConfig>> map) {
        for (DashboardConfig dc : list) {
            for (String role : dc.getRoles()) {
                List<DashboardConfig> roleList = map.get(role);
                if (roleList == null) {
                    Firebug.debug("add dc to role " + role + "   with new role list: " + dc);
                    roleList = new ArrayList<>();
                    map.put(role, roleList);
                }
                else {
                    Firebug.debug("add dc to role " + role + ": " + dc);
                }
                roleList.add(dc);
            }
        }
    }

    private void addListEntriesToMapById(List<DashboardConfig> list, Map<String, DashboardConfig> map) {
        for (DashboardConfig dc : list) {
            map.put(dc.getId(), dc);
        }
    }

    private List<DashboardConfig> readConfigsFromGuidefs() {
        final List<DashboardConfig> list = new ArrayList<>();
        final JSONWrapper dashboardsWrapper = SessionData.INSTANCE.getGuiDef("dashboards"); // $NON-NLS$
        if (!dashboardsWrapper.isArray()) {
            return list;
        }

        final JSONArray dashboardsArray = dashboardsWrapper.getValue().isArray();
        for (int i = 0, size = dashboardsArray.size(); i < size; i++) {
            final JSONObject dashboardObject = dashboardsArray.get(i).isObject();
            if (dashboardObject != null) {
                list.add(readConfigFromGuidefs(dashboardObject));
            }
        }

        return list;
    }

    private DashboardConfig readConfigFromGuidefs(JSONObject dashboardObject) {
        final DashboardConfig dc = new DashboardConfig();
        dc.setId(GUIDEFS_DASHBOARD_PREFIX + JsonUtil.getString(dashboardObject, "id")); // $NON-NLS$

        //small compatibility layer for 1.30 beta version dashboards.
        final String role = JsonUtil.getString(dashboardObject, "role"); // $NON-NLS$
        if (role != null) {
            dc.setRoles(Collections.singletonList(role));
        }
        else {
            dc.setRoles(JsonUtil.getStringList(dashboardObject, "roles")); // $NON-NLS$
        }

        dc.setName(JsonUtil.getString(dashboardObject, "name")); // $NON-NLS$
        final String access = JsonUtil.getString(dashboardObject, "access"); // $NON-NLS$
        dc.setAccess(DashboardConfig.Access.valueOf(access));
        final JSONArray snippetsArray = dashboardObject.get("snippets").isArray(); // $NON-NLS$
        for (int i = 0; i < snippetsArray.size(); i++) {
            final JSONValue jsonValue = snippetsArray.get(i);
            dc.addSnippet(SnippetConfiguration.createFrom(new JSONWrapper(jsonValue)));
        }
        return dc;
    }

    private List<DashboardConfig> readConfigsFromAppConfig() {
        final List<DashboardConfig> list = new ArrayList<>();
        final AppConfig appConfig = SessionData.INSTANCE.getUser().getAppConfig();
        for (String key : appConfig.getPropertyKeysWithPrefix(PRIVATE_DASHBOARD_PREFIX)) {
            final String json = appConfig.getProperty(key);
            final DashboardConfig dc = JsonUtil.fromJson(json);
            list.add(dc);
        }
        return list;
    }

    private boolean isMyspaceDashboardMigrated(AppConfig appConfig, String id) {
        return StringUtil.hasText(appConfig.getProperty(toMigratedDashboardPropertyKey(id)));
    }

    public List<DashboardConfig> getConfigsByRole(String role) {
        final List<DashboardConfig> list = this.dashboardsByRole.get(role);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public DashboardConfig getConfigById(String id) {
        return this.dashboardsById.get(id);
    }

    @Override
    public boolean isEditAllowed(String id) {
        return id.startsWith(PRIVATE_DASHBOARD_PREFIX);
    }

    @Override
    public boolean isDeleteAllowed(String id) {
        return id.startsWith(PRIVATE_DASHBOARD_PREFIX) || id.startsWith(PRIVATE_MYSPACE_PREFIX);
    }

    private void removeConfig(String id) {
        final DashboardConfig oldDc = this.dashboardsById.remove(id);
        if (oldDc == null) {
            Firebug.warn("AppConfigDao.save(" + id + ") id not found in dashboardsById");
        }
        else {
            for (String role : oldDc.getRoles()) {
                this.dashboardsByRole.get(role).remove(oldDc);
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private void addConfig(DashboardConfig dc) {
        final String id = dc.getId();
        this.dashboardsById.put(id, dc);
        for (String role : dc.getRoles()) {
            List<DashboardConfig> roleList = this.dashboardsByRole.get(role);
            if (roleList == null) {
                roleList = new ArrayList<>();
                this.dashboardsByRole.put(role, roleList);
            }
            roleList.add(dc);
        }
    }

    public String createNextId() {
        int i = 0;
        String id = PRIVATE_DASHBOARD_PREFIX + i;
        while (this.dashboardsById.containsKey(id)) {
            i++;
            id = PRIVATE_DASHBOARD_PREFIX + i;
        }
        return id;
    }

    @Override
    public void save(DashboardConfig dc, DashboardSavedCallback callback) {
        final String id = dc.getId();
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(id, JsonUtil.toJson(dc));
        removeConfig(id);
        addConfig(dc);
        callback.onDashboardSaved(id);
    }

    @Override
    public void saveNew(DashboardConfig dc, DashboardSavedCallback callback) {
        saveNew(dc, true, callback);
    }

    private void saveNew(DashboardConfig dc, boolean fireChangeEvent, DashboardSavedCallback callback) {
        /* never do any asynchronous tasks here, without adapting the impl. of #importConfigsFromMyspace */
        final String id = createNextId();
        dc.setId(id);
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(dc.getId(), JsonUtil.toJson(dc), fireChangeEvent);
        addConfig(dc);
        callback.onDashboardSaved(id);
    }

    @NonNLS
    @Override
    public void delete(String id) {
        if (id.startsWith(PRIVATE_DASHBOARD_PREFIX)) {
            SessionData.INSTANCE.getUser().getAppConfig().addProperty(id, null);
        }
        else {
            throw new IllegalArgumentException("dashbaord id must either start with " +
                    "'" + PRIVATE_DASHBOARD_PREFIX + "' or '" + PRIVATE_MYSPACE_PREFIX + "'");
        }
        removeConfig(id);
    }

    protected String toMigratedDashboardPropertyKey(String id) {
        return MIGRATED_DASHBOARD_PROPERTY_PREFIX + id;
    }
}