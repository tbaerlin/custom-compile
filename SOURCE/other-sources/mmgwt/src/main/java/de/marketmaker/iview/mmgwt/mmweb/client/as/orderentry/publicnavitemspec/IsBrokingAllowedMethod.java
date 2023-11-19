/*
 * IsBrokingAllowedMethod.java
 *
 * Created on 19.09.2014 11:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author mdick
 */
public class IsBrokingAllowedMethod implements AsyncCallback<ResponseType> {
    public static class IsBrokingAllowedMethodResult {
        private final boolean allowed;
        private final String depotId;

        public IsBrokingAllowedMethodResult(boolean allowed, String depotId) {
            this.allowed = allowed;
            this.depotId = depotId;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getDepotId() {
            return depotId;
        }
    }

    public enum Type {STANDALONE, ACTIVITY}

    private final DmxmlContext context = new DmxmlContext();
    private final IsBrokingAllowedFeature feature;
    private final AsyncCallback<IsBrokingAllowedMethodResult> callback;
    private final Type type;

    public IsBrokingAllowedMethod(Type type, AsyncCallback<IsBrokingAllowedMethodResult> callback) {
        this.feature = new IsBrokingAllowedFeature(this.context);
        this.type = type;
        this.callback = callback;
        this.context.setCancellable(false);
    }

    public void invoke(String shellMMId) {
        if(!Selector.AS_ORDERING.isAllowed()) {
            this.callback.onSuccess(new IsBrokingAllowedMethodResult(false, null));
            return;
        }

        if (this.feature.isPending() && StringUtil.equals(shellMMId, this.feature.getId())) {
            return;
        }
        this.feature.setId(shellMMId);
        this.context.issueRequest(this);
    }

    @Override
    public void onFailure(Throwable caught) {
        this.callback.onFailure(caught);
    }

    @Override
    public void onSuccess(ResponseType result) {
        if (!this.feature.isResponseOk()) {
            final RuntimeException exception = new RuntimeException(I18n.I.error() + ": " + this.feature.getError());  // $NON-NLS$
            this.callback.onFailure(exception);
            Firebug.warn("<HasBrokingPrivilegeMethod.onSuccess> response not ok", exception);
            return;
        }
        switch(this.type) {
            case STANDALONE:
                this.callback.onSuccess(new IsBrokingAllowedMethodResult(this.feature.isStandaloneBrokingAllowed(), this.feature.getDepotId()));
                break;
            case ACTIVITY:
                this.callback.onSuccess(new IsBrokingAllowedMethodResult(this.feature.isActivityBrokingAllowed(), this.feature.getDepotId()));
                break;
            default:
                throw new IllegalStateException("type not handled");  // $NON-NLS$
        }
    }
}
