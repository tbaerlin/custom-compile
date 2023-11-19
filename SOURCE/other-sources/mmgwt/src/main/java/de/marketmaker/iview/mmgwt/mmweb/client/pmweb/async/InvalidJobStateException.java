package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import com.google.gwt.user.client.rpc.IsSerializable;
import de.marketmaker.iview.pmxml.AsyncState;

/**
 * Created on 15.04.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class InvalidJobStateException extends Exception implements IsSerializable {
    private final String handle;
    private final String state;

    public InvalidJobStateException(String handle, String state) {
        this.handle = handle;
        this.state = state;
    }

    public InvalidJobStateException() {
        this.state = null;
        this.handle = null;
    }

    public String getHandle() {
        return handle;
    }

    public String getState() {
        return state;
    }
}
