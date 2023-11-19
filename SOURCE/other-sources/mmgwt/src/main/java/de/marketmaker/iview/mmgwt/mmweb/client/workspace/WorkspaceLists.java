package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.google.gwt.json.client.JSONObject;

import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.TeaserConfigForm;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author umaurer
 */
public class WorkspaceLists {
    private static final String KEY_PREFIX_WEST = "ws"; // $NON-NLS$

    private static final String KEY_PREFIX_EAST = "ws.east"; // $NON-NLS$

    private static final String KEY_PREFIX_HIDDEN = "ws.hidden"; // $NON-NLS$

    public static final WorkspaceLists INSTANCE = new WorkspaceLists();

    private List<AbstractWorkspaceItem> listAllWorkspaceItems;

    private Set<String> keysEast;

    private Set<String> keysHidden;

    private WorkspaceList workspaceListWest = null;

    private WorkspaceList workspaceListEast = null;

    private WorkspaceLists() {
        this.listAllWorkspaceItems = new ArrayList<>();

        add(MarketsWorkspace.INSTANCE);
        add(WatchlistWorkspace.INSTANCE);
        add(PortfolioWorkspace.INSTANCE);

        if (!(SessionData.isAsDesign())) {
            add(InstrumentWorkspace.INSTANCE);
            add(PagesWorkspace.INSTANCE);
        }
    }

    public void add(AbstractWorkspaceItem workspaceItem) {
        this.listAllWorkspaceItems.add(workspaceItem);
    }

    public WorkspaceList getWorkspaceListWest() {
        initializeWestAndEast();
        return this.workspaceListWest.hasPanels() ? this.workspaceListWest : null;
    }

    public WorkspaceList getWorkspaceListEast() {
        initializeWestAndEast();
        return this.workspaceListEast.hasPanels() ? this.workspaceListEast : null;
    }

    public void reset() {
        this.workspaceListWest.reset();
        this.workspaceListWest = null;
        this.workspaceListEast.reset();
        this.workspaceListEast = null;
    }

    private void initializeWestAndEast() {
        if (this.workspaceListWest != null) {
            return;
        }

        this.keysEast = getConstituentKeys(KEY_PREFIX_EAST);
        this.keysHidden = getConstituentKeys(KEY_PREFIX_HIDDEN);

        final Set<String> allKeys = new HashSet<>();
        final List<AbstractWorkspaceItem> westItems = new ArrayList<>();
        final List<AbstractWorkspaceItem> eastItems = new ArrayList<>();

        for (AbstractWorkspaceItem workspaceItem : this.listAllWorkspaceItems) {
            final String key = workspaceItem.getStateKey();
            if (!allKeys.add(key)) {
                throw new IllegalArgumentException("state key is defined twice: " + key); // $NON-NLS$
            }

            if (this.keysEast.contains(key)) {
                eastItems.add(workspaceItem);
            }
            else if (!this.keysHidden.contains(key)) {
                westItems.add(workspaceItem);
            }
        }

        if (westItems.isEmpty() && eastItems.isEmpty() && !this.listAllWorkspaceItems.isEmpty()) {
            westItems.add(this.listAllWorkspaceItems.get(0));
        }

        this.workspaceListWest = new WorkspaceList(KEY_PREFIX_WEST, westItems);
        this.workspaceListEast = new WorkspaceList(KEY_PREFIX_EAST, eastItems);

        updateTeasers();
    }

    public void updateTeasers() {
        if (workspaceListWest.hasPanels()) {
            pushTeaserStringToUi(workspaceListWest);
        }
        else {
            pushTeaserStringToUi(workspaceListEast);
        }
    }

    private void pushTeaserStringToUi(final WorkspaceList workspaceList) {
        if (!Selector.DZ_TEASER.isAllowed()) {
            return;
        }

        new TeaserConfigForm.TeaserRequest() {
            @Override
            public void teaserReady(JSONObject teaser) {
                workspaceList.setTeaser(new TeaserConfigData(teaser));
            }
        }.fireRequest();
    }

    public void saveConstituentKeys(String east, String hidden) {
        final AppConfig config = SessionData.INSTANCE.getUser().getAppConfig();
        config.addProperty(KEY_PREFIX_EAST + WorkspaceList.KEY_SUFFIX_CONSTITUENTS, east);
        config.addProperty(KEY_PREFIX_HIDDEN + WorkspaceList.KEY_SUFFIX_CONSTITUENTS, hidden);
    }

    private Set<String> getConstituentKeys(String keyPrefix) {
        final AppConfig config = SessionData.INSTANCE.getUser().getAppConfig();
        final String sConstituents = config.getProperty(keyPrefix + WorkspaceList.KEY_SUFFIX_CONSTITUENTS);
        if (sConstituents == null) {
            return Collections.emptySet();
        }

        return new HashSet<>(Arrays.asList(sConstituents.split("-"))); // $NON-NLS$
    }

    public List<AbstractWorkspaceItem> getAllWorkspaceItems() {
        return this.listAllWorkspaceItems;
    }

    public boolean isEast(String stateKey) {
        initializeWestAndEast();
        return this.keysEast.contains(stateKey);
    }

    public boolean isHidden(String stateKey) {
        initializeWestAndEast();
        return this.keysHidden.contains(stateKey);
    }

}
