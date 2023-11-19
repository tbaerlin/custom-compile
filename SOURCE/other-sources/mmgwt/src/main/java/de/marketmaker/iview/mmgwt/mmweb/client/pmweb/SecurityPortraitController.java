/*
 * SecurityPortraitController.java
 *
 * Created on 07.06.13 14:26
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.AbstractPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.List;
import java.util.Map;

/**
 * @author Markus Dick
 */
public class SecurityPortraitController extends AbstractPortraitController {
    private static final String DEF_PM_STATIC = "pm_s"; //$NON-NLS$
    public static final String PM_REPORTS = "PMR"; //$NON-NLS$

    private final WorkspaceSupport workspaceSupport;
    private String currentSecurityId = "";
    private String currentObjectId = "";
    private String currentName = "";

    public SecurityPortraitController(ContentContainer contentContainer) {
        super(contentContainer, "portrait_pm_security"); // $NON-NLS$
        this.context.setCancellable(false);
        this.setMetadataAwareEnabled(false);

        this.workspaceSupport = new WorkspaceSupport(getContentContainer(), "pm_r_for_security_id", null); //$NON-NLS$
        this.workspaceSupport.setRootNavItem(getNavItemSpecRoot().findChildById(PM_REPORTS));
    }

    @Override
    protected boolean isHandleCurrentControllerAndNavItemImmediately() {
        return false;
    }

    @Override
    protected void initNavItems() {
        final NavItemSpec securitySpec = addNavItemSpec(getNavItemSpecRoot(), PmWebModule.HISTORY_TOKEN_CUSTOM_SECURITY,
                PmRenderers.SHELL_MM_TYPE_PLURAL.render(ShellMMType.ST_WP), null);
        addNavItemSpec(securitySpec, PmWebModule.HISTORY_TOKEN_CUSTOM_SECURITY_OVERVIEW, I18n.I.pmInstrumentData(),
                newSecurityPortraitOverviewController(DEF_PM_STATIC));
        addNavItemSpec(getNavItemSpecRoot(), PM_REPORTS, I18n.I.pmReports(), null);
    }

    private void updateSecurityRootNavItem(ShellMMType type) {
        assert type != null;
        final NavItemSpec securityRoot = getNavItemSpecRoot().findChildById(PmWebModule.HISTORY_TOKEN_CUSTOM_SECURITY);
        securityRoot.setBaseName(PmRenderers.SHELL_MM_TYPE_PLURAL.render(type));
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String sid = PmSecurityUtil.stripOfSecurityIdSuffix(historyToken.get(1, null));

        ShellMMType shellMMType;
        try {
            shellMMType = ShellMMType.valueOf(historyToken.get(PmWebModule.TOKEN_NAME_CUSTOM_SECURITY_TYPE));
        }
        catch(IllegalArgumentException iae) {
            shellMMType = ShellMMType.ST_WP;
        }

        this.currentSecurityId = sid;

        updateSecurityRootNavItem(shellMMType);

        // call super here, because this ensures that super.lastPlaceChangeEvent and super.historyToken are set,
        // which are necessary for subsequent calls to handleCurrentControllerAndNavItemSelection
        super.onPlaceChange(event);

        PmSecurityUtil.resolveBySecurityId(sid, new AsyncCallback<ShellMMInfo>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.warn("<SecurityPortraitController.onPlaceChange> instrument search failed", caught);
                clearReportNavItemOnFailure();
            }

            @Override
            public void onSuccess(ShellMMInfo result) {
                Firebug.debug("<SecurityPortraitController.onPlaceChange> instrument search success");
                onObjectIdResult(result);
            }
        });
    }

    private void onObjectIdResult(ShellMMInfo shellMMInfo) {
        final ShellMMType shellMMType = shellMMInfo.getTyp();

        this.currentObjectId = shellMMInfo.getId();
        this.currentName = shellMMInfo.getBezeichnung();

        this.workspaceSupport.requestShellMmType(shellMMType, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.error("<SecurityPortraitController.onObjectIdResult> requestShellMmType failed", caught);
                handleCurrentControllerAndNavItemSelection();
            }

            @Override
            public void onSuccess(Boolean result) {
                if(Boolean.TRUE == result) {
                    context.issueRequest(SecurityPortraitController.this);
                }
                else {
                    handleCurrentControllerAndNavItemSelection();
                }
            }
        });
    }

    private void clearReportNavItemOnFailure() {
        final NavItemSpec pmReportNavItem = getNavItemSpecRoot().findChildById(PM_REPORTS);
        final List<NavItemSpec> children = pmReportNavItem.getChildren();
        if(children != null) {
            children.clear();
        }
        pmReportNavItem.setVisible(false);
        doOnNavItemsChanged();
    }

    @Override
    protected void onResult() {
        Firebug.debug("<SecurityPortraitController.onResult>");

        super.onResult();

        this.workspaceSupport.updateNavItems(PM_REPORTS, this.currentName, this.currentObjectId, this.currentSecurityId);
        doOnNavItemsChanged();
    }

    @Override
    protected PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map) {
        return null;
    }

    protected SecurityPortraitOverviewController newSecurityPortraitOverviewController(String def) {
        return new SecurityPortraitOverviewController(getInnerContainer(), def);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void goTo(NavItemSpec navItemSpec) {
        final PageController controller = navItemSpec.getController();

        assert (controller != null) : "goTo \"" + navItemSpec.getId() + "\" without controller should not occur";
        if(controller == null) return;

        final HistoryToken historyToken = getHistoryToken();
        final HistoryToken.Builder builder = HistoryToken.builder(historyToken.getControllerId(), historyToken.get(1), navItemSpec.getId());


        for(Map.Entry<String, String> entry : historyToken.getNamedParams().entrySet()) {
            builder.with(entry.getKey(), entry.getValue());
        }
        builder.fire();
    }

    @Override
    protected void setSymbol(String symbol) {
        //symbols are not supported here!
    }

    @Override
    protected String getSymbol() {
        return null;
    }
}
