/*
 * SharedDmxmlContextFacade.java
 *
 * Created on 15.01.2015 09:05
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.context;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.Activatable;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements a DmxmlContextFacade that allows multiple subscriptions.
 *
 * @author mdick
 */
@NonNLS
public class SharedDmxmlContextFacade implements DmxmlContextFacade, Activatable {
    public static final int MAX_BLOCK_COUNT = 100;

    private final ArrayList<DmxmlContext> dmxmlContexts = new ArrayList<>();
    private final HashMap<DmxmlContext, Integer> blocksPerContext = new HashMap<>();
    private final HashMap<DmxmlContext, ArrayList<AsyncCallback<ResponseType>>> callbacks = new HashMap<>();
    private final boolean cancelable;

    private boolean active = false;

    public SharedDmxmlContextFacade(boolean cancelable) {
        this.cancelable = cancelable;
    }

    @Override
    public DmxmlContext createOrGetContext(int numberOfBlocks) {
        if(this.dmxmlContexts.isEmpty()) {
            createAndRegisterContext(numberOfBlocks);
        }
        for (DmxmlContext dmxmlContext : this.dmxmlContexts) {
            final int currentNumberOfBlocks = this.blocksPerContext.get(dmxmlContext);
            final int newNumberOfBlocks = currentNumberOfBlocks + numberOfBlocks;
            if(newNumberOfBlocks <= MAX_BLOCK_COUNT) {
                this.blocksPerContext.put(dmxmlContext, newNumberOfBlocks);
                return dmxmlContext;
            }
        }
        return createAndRegisterContext(numberOfBlocks);
    }

    public DmxmlContext createAndRegisterContext(int numberOfBlocks) {
        if(numberOfBlocks > MAX_BLOCK_COUNT) {
            throw new IllegalArgumentException("more than " + MAX_BLOCK_COUNT + " blocks cannot be handled by one context");
        }
        final DmxmlContext dmxmlContext = new DmxmlContext();
        dmxmlContext.setCancellable(this.cancelable);
        this.dmxmlContexts.add(dmxmlContext);
        this.blocksPerContext.put(dmxmlContext, numberOfBlocks);
        this.callbacks.put(dmxmlContext, new ArrayList<AsyncCallback<ResponseType>>());
        return dmxmlContext;
    }

    @Override
    public void reload() {
        if(this.active) {
            for (final DmxmlContext dmxmlContext : this.dmxmlContexts) {
                dmxmlContext.issueRequest(new AsyncCallback<ResponseType>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        processOnFailure(dmxmlContext, caught);
                    }

                    @Override
                    public void onSuccess(ResponseType result) {
                        processOnSuccess(dmxmlContext, result);
                    }
                });
            }
        }
    }

    private void processOnFailure(DmxmlContext context, Throwable caught) {
        for (AsyncCallback<ResponseType> callback : this.callbacks.get(context)) {
            try {
                callback.onFailure(caught);
            }
            catch(Exception e) {
                Firebug.error("<SharedDmxmlContext.onFailure> callback threw exception", e);
            }
        }
    }

    private void processOnSuccess(DmxmlContext context, ResponseType result) {
        for (AsyncCallback<ResponseType> callback : this.callbacks.get(context)) {
            try {
                callback.onSuccess(result);
            }
            catch(Exception e) {
                Firebug.error("<SharedDmxmlContext.onSuccess> callback threw exception", e);
            }
        }
    }

    @Override
    public void activate() {
        this.active = true;
    }

    @Override
    public void deactivate() {
        this.active = false;
    }

    @Override
    public boolean subscribe(DmxmlContext reference, final AsyncCallback<ResponseType> callback) {
        return this.callbacks.get(reference).add(callback);
    }

    @Override
    public boolean unsubscribe(DmxmlContext reference, final AsyncCallback<ResponseType> callback) {
        return this.callbacks.get(reference).remove(callback);
    }
}
