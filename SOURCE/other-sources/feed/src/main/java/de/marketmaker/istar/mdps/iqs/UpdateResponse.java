/*
 * DeleteResponse.java
 *
 * Created on 17.10.13 13:35
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.nio.ByteBuffer;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;

import static de.marketmaker.istar.mdps.iqs.Constants.*;

/**
 * @author oflege
 */
class UpdateResponse implements Response {

    static class Factory {
        private final byte[] prefix;

        Factory(int quality) {
            this.prefix = new IqsMessageBuilder(128).prepare(MSG_DATA_RESPONSE)
                    .header(FID_SERVICEID, SERVICE_ID_PRICE)
                    .header(FID_DATA_RESPONSE_TYPE, DATA_RESPONSE_TYPE_UPDATE)
                    .header(FID_DATA_RESPONSE_FLAG, DATA_RESPONSE_FLAG_RECAP_COMPLETE) // ???
                    .header(FID_DATA_TYPE, DATA_TYPE_FIELD)
                    .header(FID_DATA_REQUEST_TYPE, DATA_REQUEST_TYPE_RECAP_AND_UPDATES)
                    .header(FID_OBJECTNAME, OBJECT_PREFIXES[quality])
                    .asBytes();
        }


        UpdateResponse create(FeedData feedData, Subscriptions subscription,ByteBuffer update) {
            return new UpdateResponse(feedData, subscription, update, this.prefix);
        }
    }


    private final FeedData feedData;

    private final Subscriptions subscription;

    private final ByteBuffer update;

    private final byte[] prefix;

    private UpdateResponse(FeedData feedData, Subscriptions subscription, ByteBuffer update, byte[] prefix) {
        this.feedData = feedData;
        this.subscription = subscription;
        this.update = update;
        this.prefix = prefix;
    }

    @Override
    public int size() {
        return this.prefix.length + feedData.getVwdcode().length()
                + subscription.getReplyFieldsLength()
                + DateTimeReceived.LENGTH
                + update.remaining()
                + 1;
    }

    @Override
    public void appendTo(ByteBuffer bb) {
        bb.put(this.prefix);
        this.feedData.getVwdcode().writeTo(bb, ByteString.LENGTH_ENCODING_NONE);
        this.subscription.appendReplyFieldsTo(bb);
        bb.put(DateTimeReceived.INSTANCE.getDateTimeReceived());
        bb.put(this.update);
        bb.put(Constants.ETX);
    }
}
