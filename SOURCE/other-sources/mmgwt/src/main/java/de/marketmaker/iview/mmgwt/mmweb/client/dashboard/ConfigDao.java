package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 21.05.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public abstract class ConfigDao {
    public static final String DASHBOARD_ROLE_GLOBAL = "global"; // $NON-NLS$

    private static ConfigDao instance = null;

    protected final Map<String, Boolean> mapPrivEditById = new HashMap<>();
    protected final Map<String, Boolean> mapPrivDeleteById = new HashMap<>();

    public static void setInstance(ConfigDao dao) {
        if (instance != null) {
            throw new IllegalStateException("ConfigDao <setInstance> instance already initialized"); // $NON-NLS$
        }
        instance = dao;
    }

    public static ConfigDao getInstance() {
        if (instance == null) {
            instance = new AppConfigDao();
        }
        return instance;
    }

    public abstract void save(DashboardConfig config, DashboardSavedCallback callback);

    public abstract void saveNew(DashboardConfig config, DashboardSavedCallback callback);

    public abstract void delete(String id);

    public abstract List<DashboardConfig> getConfigsByRole(String role);

    public abstract DashboardConfig getConfigById(String id);

    public boolean isCreateAllowed() {
        return true; // TODO: check pm feature
    }

    public boolean isEditAllowed(String id) {
        if (id == null) {
            return false;
        }
        final Boolean allowed = this.mapPrivEditById.get(id);
        if (allowed == null) {
            throw new NullPointerException("couldn't get edit privilege for GUID: " + id); // $NON-NLS$
        }
        return allowed;
    }

    public boolean isDeleteAllowed(String id) {
        if (id == null) {
            return false;
        }
        final Boolean allowed = this.mapPrivDeleteById.get(id);
        if (allowed == null) {
            throw new NullPointerException("couldn't get delete privilege for GUID: " + id); // $NON-NLS$
        }
        return allowed;
    }
}