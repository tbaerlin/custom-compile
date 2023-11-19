/*
 * PmEnvSupport.java
 *
 * Created on 08.09.2015 09:54
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Version;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.pmxml.GetEnvironmentResponse;
import de.marketmaker.iview.pmxml.GetEnvironmentResponseModuleInfo;
import de.marketmaker.iview.pmxml.VoidRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author mdick
 */
public class PmEnvSupport {
    private static final PmEnvSupport INSTANCE = new PmEnvSupport();

    public static void requestPmEnv(AsyncCallback<GetEnvironmentResponse> callback) {
        INSTANCE.request(callback);
    }

    private DmxmlContext.Block<GetEnvironmentResponse> block;
    private HashSet<AsyncCallback<GetEnvironmentResponse>> pending = new HashSet<>();

    public PmEnvSupport() {
        final DmxmlContext context = new DmxmlContext();
        context.setCancellable(false);

        this.block = context.addBlock("PM_GetEnv"); // $NON-NLS$
        final VoidRequest vr = new VoidRequest();
        this.block.setParameter(vr);
        this.block.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                processPendingFailure(caught);
            }

            @Override
            public void onSuccess(ResponseType responseType) {
                processPendingSuccess();
            }
        });
    }

    private void request(AsyncCallback<GetEnvironmentResponse> callback) {
        if(this.block.isNotRequested() || this.block.isPending()) {
            this.pending.add(callback);
        }
        else if(this.block.isResponseOk()) {
            callback.onSuccess(this.block.getResult());
        }
        else {
            callback.onFailure(new RuntimeException("Cannot get environment from PM"));  // $NON-NLS$
        }
    }

    private void processPendingSuccess() {
        for (AsyncCallback<GetEnvironmentResponse> callback : this.pending) {
            if(this.block.isResponseOk()) {
                callback.onSuccess(this.block.getResult());
            }
        }
        this.pending.clear();
    }

    private void processPendingFailure(Throwable caught) {
        for (AsyncCallback<GetEnvironmentResponse> callback : this.pending) {
            callback.onFailure(caught);
        }
        this.pending.clear();
    }

    public static String[] getModules(GetEnvironmentResponse envRes) {
        final List<GetEnvironmentResponseModuleInfo> modulesWebgui = envRes.getModulesWebgui();
        final ArrayList<String> moduleDescs = new ArrayList<>();
        for (GetEnvironmentResponseModuleInfo module : modulesWebgui) {
            moduleDescs.add(module.getDesc());
        }
        final List<GetEnvironmentResponseModuleInfo> modules = envRes.getModules();
        for (GetEnvironmentResponseModuleInfo module : modules) {
            moduleDescs.add(module.getDesc());
        }
        return moduleDescs.toArray(new String[moduleDescs.size()]);
    }

    public static Entry[] getVersionDetails(GetEnvironmentResponse envRes) {
        return new Entry[] {
                new Entry("Build", Version.INSTANCE.build()),  // $NON-NLS$
                new Entry("vwd portfolio manager", (envRes.getMain() != null ? envRes.getMain().getCompleteVersion() : I18n.I.notAvailable("Version"))), // $NON-NLS$
                new Entry("Infront Advisory Solution", (envRes.getWebgui() != null ? envRes.getWebgui().getCompleteVersion() : I18n.I.notAvailable("Version"))), // $NON-NLS$
                new Entry("Portfolio Sync Interface", (envRes.getPsi() != null ? envRes.getPsi().getCompleteVersion() : I18n.I.notAvailable("Version"))) // $NON-NLS$
        };
    }

    public static class Entry {
        private final String label;
        private final String value;

        private Entry(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry)) return false;

            final Entry entry = (Entry) o;

            if (label != null ? !label.equals(entry.label) : entry.label != null) return false;
            return !(value != null ? !value.equals(entry.value) : entry.value != null);

        }

        @Override
        public int hashCode() {
            int result = label != null ? label.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            // do not change! some code depends on this format.
            return getLabel() + ": " + getValue();
        }
    }
}
