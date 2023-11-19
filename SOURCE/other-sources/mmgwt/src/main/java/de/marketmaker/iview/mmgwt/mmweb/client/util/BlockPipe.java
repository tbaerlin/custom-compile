package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.ResponseType;

/**
 * In some cases it´s necessary to get some data from the backend as input for another request.
 * BlockPipe is a Decorator for DmxmlContext.Block which concatenates blocks and pipes
 * the result of block n to block n+1.
 * <p/>
 * BlockPipe.java
 * Created on Sep 15, 2009 9:32:04 AM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class BlockPipe {

    private BlockPipe next = null;
    private final DmxmlContext context;
    private final String parameterToSet;
    private final boolean isList;
    private final BlockPipeResult result;
    private final BlockPipeComplexResult complexResult;
    private final DmxmlContext.Block block;

    /**
     * @param block          .
     * @param context        if context is not null, issueRequest-Method of context will be invoked instead of block.issueRequest
     * @param parameterToSet is the parameter of block n+1, which will be set by {@link #result} of block n.
     * @param isList         defines the way how the parameter will be set (setParameter; setParameters).
     * @param result         contains the method getResult(), which is called after a successful request of block n.
     */
    public BlockPipe(DmxmlContext.Block block, DmxmlContext context, String parameterToSet, boolean isList, BlockPipeResult result) {
        this.block = block;
        this.context = context;
        this.parameterToSet = parameterToSet;
        this.isList = isList;
        this.result = result;
        this.complexResult = null;
    }

    /**
     * @param block          .
     * @param context        if context is not null, issueRequest-Method of context will be invoked instead of block.issueRequest
     * @param result         contains the method getResult(), which is called after a successful request of block n.
     */
    public BlockPipe(DmxmlContext.Block block, DmxmlContext context, BlockPipeComplexResult result) {
        this.block = block;
        this.context = context;
        this.result = null;
        this.complexResult = result;
        this.parameterToSet = null;
        this.isList = false;
    }

    private void internalRequest(final AsyncCallback<ResponseType> delegate) {
        if (this.context != null) {
            this.context.issueRequest(delegate);
        } else {
            this.block.issueRequest(delegate);
        }
    }

    public void issueRequest(final AsyncCallback<ResponseType> delegate) {
        if (this.next != null) {
            internalRequest(new AsyncCallback<ResponseType>() {

                public void onFailure(Throwable throwable) {
                    failure(delegate, throwable);
                }

                public void onSuccess(ResponseType responseType) {
                    if (block.isResponseOk()) {
                        if (complexResult != null) {
                            next.block.setParameter(complexResult.getResult(block));
                        }
                        else if (isList) {
                            next.block.setParameters(parameterToSet, result.getResult(block));
                        } else {
                            next.block.setParameter(parameterToSet, result.getResult(block)[0]);
                        }
                        next.issueRequest(delegate);
                    } else {
                        try {
                            throw new Exception("response error of block " + block.getId()); // $NON-NLS-0$
                        }
                        catch (Exception e) {
                            failure(delegate, e);
                        }
                    }
                }
            });
        } else {
            internalRequest(delegate);
        }
    }

    private void failure(AsyncCallback<ResponseType> delegate, Throwable throwable) {
        setError(this.next);
        delegate.onFailure(throwable);
    }

    private void setError(BlockPipe next) {
        if (next.next != null) {
            setError(next.next);
        } else {
            next.block.setError();
        }
    }

    public BlockPipe setNext(DmxmlContext.Block block) {
        if (this.parameterToSet != null) {
            return setNext(new BlockPipe(block, null, this.parameterToSet, this.isList, this.result));
        }
        return setNext(new BlockPipe(block, null, this.complexResult));
    }

    public BlockPipe setNext(BlockPipe next) {
        if (this.next != null) {
            this.next.setNext(next);
        } else {
            this.next = next;
        }
        return this;
    }
}