/*
 * WorkspaceSupportMetadataAware.java
 *
 * Created on 19.06.13 09:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.InstrumentMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.AbstractPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.MetadataAware;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.pmxml.MMTalkResponse;

import java.util.List;

/**
 * @author Markus Dick
 */
public class WorkspaceSupportMetadataAware implements MetadataAware, AbstractPortraitController.NavItemExtension, PrivacyMode.InterestedParty {
    private final DmxmlContext adapterContext;
    private String defaultToken;

    private WorkspaceSupport workspaceSupport;
    private AbstractPortraitController.NavItemsChangedCallback navItemsChangedCallback;
    private String currentSymbol = "";
    private boolean reloadRequired = false;

    public WorkspaceSupportMetadataAware() {
        this.adapterContext = new DmxmlContext();
        PrivacyMode.subscribe(this);
    }

    @Override
    public boolean isMetadataNeeded() {
        return true;
    }

    @Override
    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        final DmxmlContext context = this.adapterContext;
        context.setCancellable(false);

        final DmxmlContext.Block<MMTalkResponse> block = context.addBlock("PM_MMTalk");// $NON-NLS$
        final InstrumentMetadata.Talker metaTalker = new InstrumentMetadata.Talker();
        final String iid = metadata.getInstrumentdata().getIid();

        if(StringUtil.equals(iid, this.currentSymbol) && !this.reloadRequired) {
            Firebug.debug("<WorkspaceSupportMetadataAware.onMetadataAvailable> iid has not changed. PrivacyMode state has not changed. No update required.");
            return;
        }

        this.currentSymbol = iid;
        this.reloadRequired = true;

        metaTalker.setSecurityId(iid.substring(0, iid.indexOf(".iid"))); // $NON-NLS$

        block.setParameter(metaTalker.createRequest());

        block.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.warn("<WorkspaceSupportMetadataAware.onMetadataAvailable> error while requesting pm instruments metadata", caught);
                onAnyFailure();
            }

            @Override
            public void onSuccess(ResponseType result) {
                if(!block.isResponseOk()) {
                    Firebug.warn("<WorkspaceSupportMetadataAware.onMetadataAvailable> response not ok");
                    onAnyFailure();
                    return;
                }
                onInstrumentMetadataResult(metaTalker.createResultObject(block.getResult()));
            }
        });
    }

    private void onInstrumentMetadataResult(final InstrumentMetadata instrumentMetadata) {
        if(instrumentMetadata == null) {
            Firebug.warn("<WorkspaceSupportMetadataAware.onInstrumentMetadataResult> parameter instrumentMetadata is null. Nothing to do.");
            onAnyFailure();
            return;
        }

        this.workspaceSupport.requestShellMmType(instrumentMetadata.getType(), new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.warn("<WorkspaceSupportMetadataAware.onInstrumentMetadataResult> workspace request failed!", caught);
                onAnyFailure();
            }

            @Override
            public void onSuccess(Boolean result) {
                onWorkspaceSupportSuccess(instrumentMetadata);
            }
        });
    }


    private void onWorkspaceSupportSuccess(InstrumentMetadata metadata) {
        this.reloadRequired = false;

        this.workspaceSupport.updateNavItems(this.defaultToken, metadata.getName(), metadata.getId(), metadata.getSecurityId());
        doOnNavItemsChanged();
    }

    private void onAnyFailure() {
        final NavItemSpec navItem = this.workspaceSupport.getRootNavItem().findChildById(getTargetNavItemId());
        final List<NavItemSpec> children = navItem.getChildren();
        if(children != null) {
            children.clear();
        }
        navItem.setVisible(false);
        doOnNavItemsChanged();
    }

    private void doOnNavItemsChanged() {
        if(this.navItemsChangedCallback != null) {
            this.navItemsChangedCallback.onNavItemsChanged(this.workspaceSupport.getRootNavItem());
        }
    }

    @Override
    public void init(ContentContainer contentContainer, NavItemSpec rootNavItem, String defaultToken, AbstractPortraitController.NavItemsChangedCallback callback) {
        if(this.workspaceSupport != null) {
            throw new IllegalArgumentException("WorkspaceSupportMetadataAware has been initialized already!"); //$NON-NLS$
        }
        this.workspaceSupport = new WorkspaceSupport(contentContainer, "pm_r_for_symbol", null); //$NON-NLS$
        this.workspaceSupport.setRootNavItem(rootNavItem);
        this.defaultToken = defaultToken;
        this.navItemsChangedCallback = callback;
    }

    @Override
    public String getTargetNavItemId() {
        return "PMR"; //$NON-NLS$
    }

    @Override
    public void privacyModeStateChanged(boolean privacyModeActive, PrivacyMode.StateChangeProcessedCallback processed) {
        this.reloadRequired = true;
        processed.privacyModeStateChangeProcessed(this);
    }
}