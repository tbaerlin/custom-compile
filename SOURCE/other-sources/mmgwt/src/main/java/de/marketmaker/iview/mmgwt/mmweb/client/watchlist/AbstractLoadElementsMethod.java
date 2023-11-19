/*
 * AbstractLoadElementsMethod.java
 *
 * Created on 18.02.2016 15:35
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import java.util.Optional;
import java.util.function.Supplier;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.iview.dmxml.BlockListType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
@NonNLS
public abstract class AbstractLoadElementsMethod<P extends AbstractLoadElementsMethod<P, T>,
        T extends BlockListType> implements Command {

    private final DmxmlContext context = new DmxmlContext();

    private final DmxmlContext.Block<T> block;

    private final AsyncCallback<T> callback;

    private final Supplier<String> userIdSupplier = SessionData.INSTANCE.getUser()::getVwdId;

    private Optional<String> userId = Optional.empty();

    public AbstractLoadElementsMethod(String blockName, AsyncCallback<T> callback) {
        this.block = this.context.addBlock(blockName);
        this.callback = callback;
        this.block.setParameter("sortBy", "name");
        this.block.setParameter("extendedPriceData", "false");
        this.block.setParameter("disablePaging", "true");
    }

    @SuppressWarnings({"unused", "unchecked"})
    public P withUserId(String userId) {
        this.userId = Optional.ofNullable(userId);
        return (P)this;
    }

    @Override
    public void execute() {
        this.block.setParameter("userid", this.userId.orElse(this.userIdSupplier.get()));

        this.block.setToBeRequested();

        this.context.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }

            @Override
            public void onSuccess(ResponseType responseType) {
                if (block.isResponseOk()) {
                    callback.onSuccess(block.getResult());
                }
                else {
                    onFailure(new RuntimeException(block.getError().getDescription()));
                }
            }
        });
    }
}
