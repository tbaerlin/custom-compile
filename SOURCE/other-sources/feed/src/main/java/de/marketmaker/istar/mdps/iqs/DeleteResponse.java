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

import static de.marketmaker.istar.domain.data.SnapRecord.CP_1252;
import static de.marketmaker.istar.mdps.iqs.Constants.*;

/**
 * @author oflege
 */
class DeleteResponse implements Response {

    static class Factory {
        private final byte[] prefix;

        Factory(int quality) {
            this.prefix = new IqsMessageBuilder(128).prepare(MSG_DATA_STATUS_RESPONSE)
                    .header(FID_SERVICEID, SERVICE_ID_PRICE)
                            // delete messages are only sent to subscribers registered with this type
                    .header(FID_DATA_REQUEST_TYPE, DATA_REQUEST_TYPE_RECAP_AND_UPDATES)
                    .header(FID_DATA_STATUS, DATA_STATUS_DELETE)
                    .header(FID_TEXT, "Object Deleted.".getBytes(CP_1252))
                    .header(FID_OBJECTNAME, OBJECT_PREFIXES[quality])
                    .asBytes();
        }


        DeleteResponse create(FeedData feedData, Subscriptions subscription) {
            return new DeleteResponse(feedData, subscription, this.prefix);
        }
    }

    private final FeedData feedData;

    private final Subscriptions subscription;

    private final byte[] prefix;

    private DeleteResponse(FeedData feedData, Subscriptions subscription, byte[] prefix) {
        this.feedData = feedData;
        this.subscription = subscription;
        this.prefix = prefix;
    }

    @Override
    public int size() {
        return prefix.length + feedData.getVwdcode().length() + subscription.getReplyFieldsLength() + 1;
    }

    @Override
    public void appendTo(ByteBuffer bb) {
        bb.put(this.prefix);
        this.feedData.getVwdcode().writeTo(bb, ByteString.LENGTH_ENCODING_NONE);
        this.subscription.appendReplyFieldsTo(bb);
        bb.put(Constants.ETX);
    }
}
