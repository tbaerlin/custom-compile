package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;

import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * @author umaurer
 */
public class WorkspaceList {
    static final String KEY_SUFFIX_CONSTITUENTS = ".constituents"; // $NON-NLS$

    static final String KEY_SUFFIX_COLLAPSED = ".collapsed"; // $NON-NLS$

    static final String KEY_SUFFIX_ACTIVE = ".active"; // $NON-NLS$

    static final String KEY_SUFFIX_WIDTH = ".width"; // $NON-NLS$

    private final String keyPrefix;

    private final Workspace workspace;

    private final List<AbstractWorkspaceItem> listWorkspaceItems;

    private AbstractWorkspaceItem currentWorkspace;

    public WorkspaceList(String keyPrefix, List<AbstractWorkspaceItem> items) {
        this.keyPrefix = keyPrefix;
        this.listWorkspaceItems = items == null ? new ArrayList<AbstractWorkspaceItem>() : items;
        initializeWorkspaceItems();
        this.workspace = new Workspace(this);
    }

    private void initializeWorkspaceItems() {
        for (final AbstractWorkspaceItem workspaceItem : this.listWorkspaceItems) {
            workspaceItem.setListenerForCurrentWorkspace(new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                    setCurrentWorkspaceItem(workspaceItem);
                }
            });
        }
    }

    public void setTeaser(TeaserConfigData teaserConfigData) {
        this.workspace.setTeaser(teaserConfigData);
    }

    public void setTeaser(String imageUrl) {
        this.workspace.setTeaser(imageUrl);
    }

    public boolean hasPanels() {
        return !this.listWorkspaceItems.isEmpty();
    }

    public List<AbstractWorkspaceItem> getPanels() {
        return this.listWorkspaceItems;
    }

    public Workspace getWorkspace() {
        return this.workspace;
    }

    public void reset() {
        if (this.listWorkspaceItems.isEmpty()) {
            return;
        }
        for (AbstractWorkspaceItem workspaceItem : this.listWorkspaceItems) {
            this.workspace.remove(workspaceItem);
        }
        this.currentWorkspace = null;
    }

    public String getProperty(String keySuffix) {
        final AppConfig config = SessionData.INSTANCE.getUser().getAppConfig();
        return config.getProperty(this.keyPrefix + keySuffix);
    }

    public String getProperty(String keySuffix, String defaultValue) {
        final String value = getProperty(keySuffix);
        return value == null ? defaultValue : value;
    }

    public boolean getBooleanProperty(String keySuffix, boolean defaultValue) {
        final String value = getProperty(keySuffix);
        return value == null ? defaultValue : Boolean.valueOf(value);
    }

    public int getIntProperty(String keySuffix, int defaultValue) {
        final String value = getProperty(keySuffix);
        return value == null ? defaultValue : Integer.valueOf(value);
    }

    public void addProperty(String keySuffix, String value) {
        final AppConfig config = SessionData.INSTANCE.getUser().getAppConfig();
        config.addProperty(this.keyPrefix + keySuffix, value);
    }

    public void addProperty(String keySuffix, int value) {
        addProperty(keySuffix, Integer.toString(value));
    }

    public int getPropertyWidth(int defaultValue) {
        return getIntProperty(KEY_SUFFIX_WIDTH, defaultValue);
    }

    public void setCurrentWorkspaceItem(AbstractWorkspaceItem workspaceItem) {
        this.currentWorkspace = workspaceItem;
    }

    public AbstractWorkspaceItem getCurrentWorkspaceItem() {
        return this.currentWorkspace;
    }
}
