package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import de.marketmaker.iview.pmxml.AsyncState;
import de.marketmaker.iview.pmxml.GetStateResponse;

/**
 * Created on 14.04.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class AsyncStateResult extends AsyncHandleResult {

    private final GetStateResponse res;

    public AsyncStateResult(String handle, String objectName, GetStateResponse res) {
        super(handle, objectName);
        this.res = res;
    }

    public AsyncStateResult() {
        super();
        this.res = null;
    }

    public Boolean isFinished() {
        return (this.res.getState() == AsyncState.AS_COMPLETED || this.res.getState() == AsyncState.AS_COMPLETED_WITH_NOTES);
    }

    public GetStateResponse getStateResponse() {
        return this.res;
    }
}
