package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.pmxml.GetWorkspaceRequest;
import de.marketmaker.iview.pmxml.GetWorkspaceResponse;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 01.10.13 13:47
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class PmWorkspaceHandler {
    private static PmWorkspaceHandler instance;

    private final DmxmlContext context;
    private final DmxmlContext.Block<GetWorkspaceResponse> block;
    private final HashMap<ShellMMType, GetWorkspaceResponse> workspaceMap = new HashMap<>();
    private final HashMap<ShellMMType, GetWorkspaceResponse> workspaceMapPrivacyMode = new HashMap<>();

    private PmWorkspaceHandler() {
        this.context = new DmxmlContext();
        this.block = this.context.addBlock("PM_GetWorkspace"); // $NON-NLS$
        this.context.setCancellable(false);
    }

    public static PmWorkspaceHandler getInstance() {
        if (instance == null) {
            instance = new PmWorkspaceHandler();
        }
        return instance;
    }

    public void update(boolean privacyModeActive, Map<ShellMMType, DmxmlContext.Block<GetWorkspaceResponse>> typeMap) {
        doUpdate(typeMap, privacyModeActive ? this.workspaceMapPrivacyMode : this.workspaceMap);
    }

    private void doUpdate(Map<ShellMMType, DmxmlContext.Block<GetWorkspaceResponse>> typeMap, HashMap<ShellMMType, GetWorkspaceResponse> workspaceMap) {
        for (Map.Entry<ShellMMType, DmxmlContext.Block<GetWorkspaceResponse>> entry : typeMap.entrySet()) {
            workspaceMap.put(entry.getKey(), entry.getValue().getResult());
        }
    }

    public void getPmWorkspace(boolean privacyModeActive, final ShellMMType shellMMType, final PmWorkspaceCallback callback) {
        doGetPmWorkspace(shellMMType, callback, privacyModeActive ? this.workspaceMapPrivacyMode : this.workspaceMap, privacyModeActive);
    }

    private void doGetPmWorkspace(final ShellMMType shellMMType, final PmWorkspaceCallback callback, final HashMap<ShellMMType, GetWorkspaceResponse> shellTypeToWorkspaceMap, boolean privacyModeActive) {
        if (shellTypeToWorkspaceMap.containsKey(shellMMType)) {
            callback.onWorkspaceAvailable(shellTypeToWorkspaceMap.get(shellMMType));
            return;
        }

        final GetWorkspaceRequest request = new GetWorkspaceRequest();
        request.setCustomerDesktopActive(privacyModeActive);
        request.setShellMMType(shellMMType);
        this.block.setParameter(request);

        this.context.issueRequest(new AsyncCallback<ResponseType>() {
            public void onFailure(Throwable caught) {
                AbstractMainController.INSTANCE.showError(I18n.I.workspaceNotAvailable());
                Firebug.error("no workspace available for type " + shellMMType.value(), caught); // $NON-NLS-0$
            }

            public void onSuccess(ResponseType result) {
                if (!block.isResponseOk()) {
                    AbstractMainController.INSTANCE.showError(I18n.I.workspaceNotAvailable());
                    Firebug.log("no workspace available for type " + shellMMType.value() + " - " // $NON-NLS$
                            + block.getError().getDescription());
                    return;
                }
                final GetWorkspaceResponse response = block.getResult();
                shellTypeToWorkspaceMap.put(shellMMType, response);
                callback.onWorkspaceAvailable(response);
            }
        });
    }
}
