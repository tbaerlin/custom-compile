/*
 * WorkspaceSupport.java
 *
 * Created on 14.06.13 10:49
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.PmReportOverviewController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.PmReportSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.pmxml.GetWorkspaceResponse;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.WorksheetDefaultMode;
import de.marketmaker.iview.pmxml.WorkspaceSheetDesc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Markus Dick
 */
public class WorkspaceSupport {
    private GetWorkspaceResponse workspace = null;
    private boolean privacyModeWorkspace = false;

    private final ContentContainer contentContainer;
    private final SetDefaultNavItemSpecCallback callback;

    private ShellMMType shellMMType = ShellMMType.ST_UNBEKANNT;
    private NavItemSpec rootNavItem;
    private final PmReportOverviewController overviewController;

    public WorkspaceSupport(ContentContainer contentContainer, String def, SetDefaultNavItemSpecCallback callback) {
        this.contentContainer = contentContainer;
        this.callback = callback;
        this.overviewController = new PmReportOverviewController(getContentContainer(), def);
        this.privacyModeWorkspace = PrivacyMode.isActive();

        PmWorkspaceHandler.getInstance().getPmWorkspace(this.privacyModeWorkspace, this.shellMMType, new PmWorkspaceCallback() {
            @Override
            public void onWorkspaceAvailable(GetWorkspaceResponse response) {
                workspace = response;
            }
        });
    }

    public ContentContainer getContentContainer() {
        return this.contentContainer;
    }

    public void setRootNavItem(NavItemSpec rootNavItem) {
        this.rootNavItem = rootNavItem;
    }

    public void requestShellMmType(ShellMMType type, final AsyncCallback<Boolean> callback) {
        if (type == null) {
            type = ShellMMType.ST_UNBEKANNT;
        }

        final boolean privacyModeActive = PrivacyMode.isActive();

        if (this.privacyModeWorkspace == privacyModeActive && type.equals(this.shellMMType)) {
            callback.onSuccess(Boolean.FALSE);
            return;
        }
        this.shellMMType = type;
        PmWorkspaceHandler.getInstance().getPmWorkspace(privacyModeActive, type, new PmWorkspaceCallback() {
            @Override
            public void onWorkspaceAvailable(GetWorkspaceResponse response) {
                privacyModeWorkspace = privacyModeActive;
                workspace = response;
                callback.onSuccess(Boolean.TRUE);
            }
        });
    }

    public void updateNavItems(String defaultToken, String objectName, String objectId, String securityId) {
        Firebug.debug("<WorkspaceSupport.updateNavItems> defaultToken=" + defaultToken + " objectName=" + objectName + " objectId=" + objectId + " securityId=" + securityId);
        if (this.workspace == null) {
            clearChildren();
            this.rootNavItem.setVisible(false);
            return;
        }
        updateNavItems(defaultToken, objectName, objectId, securityId, this.workspace.getSheets(), PmWebSupport.toDefaultSheetsMap(this.workspace));
    }

    private void updateNavItems(String defaultToken, String titleSuffix, String objectId, String securityId, List<WorkspaceSheetDesc> sheets, Map<String, Set<WorksheetDefaultMode>> worksheetDefaultModeMap) {
        if (this.rootNavItem == null) {
            return;
        }

        clearChildren();
        this.rootNavItem.addChildren(createLayoutChildren(defaultToken, titleSuffix, objectId, securityId, sheets, worksheetDefaultModeMap, true));

        final List<NavItemSpec> children = this.rootNavItem.getChildren();
        this.rootNavItem.setVisible(children != null && !children.isEmpty());
    }

    private void clearChildren() {
        final List<NavItemSpec> children = this.rootNavItem.getChildren();
        if (children != null) {
            children.clear();
        }
    }

    private NavItemSpec[] createLayoutChildren(String defaultToken, String titleSuffix, String objectId,
                                               String securityId, List<WorkspaceSheetDesc> sheets, Map<String, Set<WorksheetDefaultMode>> worksheetDefaultModeMap, boolean firstLevel) {
        final List<NavItemSpec> result = new ArrayList<>();
        for (WorkspaceSheetDesc sheet : sheets) {
            final NavItemSpec spec = createLayoutChild(defaultToken, sheet, worksheetDefaultModeMap, titleSuffix, objectId, securityId);
            if (firstLevel) {
                spec.withClosingSiblings();
            }
            else {
                spec.withOpenWithParent();
            }
            result.add(spec);
            if (PmWebSupport.isMainDefaultSheet(sheet, worksheetDefaultModeMap)) {
                if (this.callback != null) {
                    this.callback.setDefaultNavItemSpec(spec);
                }
            }
        }
        return result.toArray(new NavItemSpec[result.size()]);
    }

    private NavItemSpec createLayoutChild(String defaultToken, WorkspaceSheetDesc sheet, Map<String, Set<WorksheetDefaultMode>> worksheetDefaultModeMap, String titleSuffix, String databaseId, String securityId) {
        final NavItemSpec navItemSpec;
        final LayoutNode layoutNode = LayoutNode.create(sheet.getNodeId(), sheet.getLayoutGuid());
        final String layoutNodeString = layoutNode.toString();

        if (StringUtil.hasText(sheet.getLayoutGuid())) {
            navItemSpec = new NavItemSpec(layoutNodeString, sheet.getCaption(),
                    HistoryToken.builder(layoutNodeString).with(PmReportSnippet.SECURITY_ID, securityId).build(),
                    this.overviewController);
        }
        else {
            navItemSpec = new NavItemSpec(layoutNode.toString(), sheet.getCaption());
        }
        if (sheet.getSheets() != null && !sheet.getSheets().isEmpty()) {
            navItemSpec.addChildren(createLayoutChildren(defaultToken, titleSuffix, databaseId, securityId, sheet.getSheets(), worksheetDefaultModeMap, false));
        }
        if (sheet.isDelegateOpenToSubSheet()) {
            navItemSpec.withHasDelegate();
        }
        return navItemSpec;
    }

    public ShellMMType getShellMMType() {
        return shellMMType;
    }

    public NavItemSpec getRootNavItem() {
        return rootNavItem;
    }

    public interface SetDefaultNavItemSpecCallback {
        void setDefaultNavItemSpec(NavItemSpec navItemSpec);
    }
}