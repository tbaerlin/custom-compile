/*
 * IqsUpdateBuilder.java
 *
 * Created on 25.09.13 14:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.nio.ByteBuffer;

import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;
import de.marketmaker.istar.mdps.util.FieldFormatter;
import de.marketmaker.istar.mdps.util.OrderedEntitlementProvider;

/**
 * @author oflege
 */
@SuppressWarnings("UnusedDeclaration")
public class IqsUpdateBuilder implements OrderedUpdateBuilder {

    private final EntitledFieldsBuilder builder;

    private final IqsClients clients;

    private final Subscriptions subscriptions = new Subscriptions();

    private final DeleteResponse.Factory deleteResponseFactory;

    private final UpdateResponse.Factory updateResponseFactory;

    private final RecapResponse.Factory recapResponseFactory;


    @SuppressWarnings("UnusedDeclaration")
    public IqsUpdateBuilder(OrderedEntitlementProvider entitlementProvider, IqsClients clients,
            FieldFormatter formatter, int quality) {
        this.builder = new EntitledFieldsBuilder(entitlementProvider, formatter, quality);
        this.clients = clients;
        this.deleteResponseFactory = new DeleteResponse.Factory(quality);
        this.updateResponseFactory = new UpdateResponse.Factory(quality);
        this.recapResponseFactory = new RecapResponse.Factory(quality);
    }

    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {
        final IqsFeedData iqsFeedData = (IqsFeedData) data;
        if (iqsFeedData.hasSubscriptions()) {
            final ByteBuffer msg = this.builder.formatMessage(iqsFeedData, update.getFieldData());
            if (msg != null) {
                dispatch(iqsFeedData, update, msg);
            }
        }
    }

    private void dispatch(IqsFeedData data, OrderedUpdate update, ByteBuffer bb) {
        this.subscriptions.reset(data.getSubscriptions());
        Response r = toResponse(data, update, bb);
        while (this.subscriptions.hasData()) {
            final IqsClients.Client client = getClient(this.subscriptions.getClientId());
            if (client == null) {
                this.subscriptions.remove();
                continue;
            }
            client.appendMessage(r);
            this.subscriptions.next();
            bb.reset();
        }
    }

    private Response toResponse(IqsFeedData data, OrderedUpdate update, ByteBuffer bb) {
        if (update.isDelete()) {
            return this.deleteResponseFactory.create(data, this.subscriptions);
        }
        if (update.isRecap()) {
            return this.recapResponseFactory.onUpdate(data, this.subscriptions, bb);
        }
        return this.updateResponseFactory.create(data, this.subscriptions, bb);
    }

    private IqsClients.Client getClient(int id) {
        return this.clients.getClient(id);
    }
}
