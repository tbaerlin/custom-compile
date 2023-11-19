/*
 * ResponseTypeCallback.java
 *
 * Created on 07.01.2009 16:49:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.iview.dmxml.ResponseType;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class ResponseTypeCallback implements AsyncCallback<ResponseType> {
    protected Throwable throwable = null;
    
    public void onFailure(Throwable throwable) {
        this.throwable = throwable;
        onResult();
    }

    public void onSuccess(ResponseType responseType) {
        this.throwable = null;
        onResult();
    }

    /**
     * Invoked by the default implementation of {@link #onFailure(Throwable)} and
     * {@link #onSuccess(de.marketmaker.iview.dmxml.ResponseType)}. 
     */
    abstract protected void onResult();
}
