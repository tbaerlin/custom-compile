/*
 * SimpleDmxmlContextFacade.java
 *
 * Created on 15.01.2015 11:05
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.context;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * Implements a DmxmlContextFacade that allows only one subscription.
 *
 * @author mdick
 */
public class SimpleDmxmlContextFacade implements DmxmlContextFacade {
    private DmxmlContext dmxmlContext;
    private AsyncCallback<ResponseType> callback;

    @Override
    public DmxmlContext createOrGetContext(int numberOfBlocks) {

        if(this.dmxmlContext == null) {
            this.dmxmlContext = new DmxmlContext();
        }
        return this.dmxmlContext;
    }

    @Override
    public void reload() {
        if(this.dmxmlContext != null && this.callback != null) {
            this.dmxmlContext.issueRequest(this.callback);
            return;
        }

        throw new IllegalStateException("dmxmlContext not created or callback not set");  // $NON-NLS$
    }

    @Override
    public boolean subscribe(DmxmlContext reference, AsyncCallback<ResponseType> callback) {
        if(this.dmxmlContext == reference && this.callback == null) {
            this.callback = callback;
            return true;
        }
        return false;
    }

    @Override
    public boolean unsubscribe(DmxmlContext reference, AsyncCallback<ResponseType> callback) {
        if(this.dmxmlContext == reference && this.callback == callback) {
            this.callback = null;
            return true;
        }
        return false;
    }

    @Override
    public void activate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deactivate() {
        throw new UnsupportedOperationException();
    }
}
