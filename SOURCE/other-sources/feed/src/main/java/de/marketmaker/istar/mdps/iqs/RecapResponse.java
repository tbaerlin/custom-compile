/*
 * DeleteResponse.java
 *
 * Created on 17.10.13 13:35
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.nio.ByteBuffer;
import java.util.Arrays;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;
import de.marketmaker.istar.mdps.MdpsMain;

import static de.marketmaker.istar.domain.data.SnapRecord.CP_1252;
import static de.marketmaker.istar.mdps.iqs.Constants.*;

/**
 * @author oflege
 */
class RecapResponse implements Response {
    private static final byte[] SOURCE;

    private static final byte[][] SEC_TYPES = new byte[100][];

    private static final byte[] NULL_TYPE = new byte[]{'0'};

    static {
        final IqsMessageBuilder b = new IqsMessageBuilder(128);

        SOURCE = b.clear().header(FID_SOURCE,
                MdpsMain.getMdpsName().getBytes(CP_1252)).asBytes();

        Arrays.fill(SEC_TYPES, NULL_TYPE);
        assert MdpsTypeMappings.getMdpsKeyTypeByVwdType(19) == null;
        for (int i = 0; i < SEC_TYPES.length; i++) {
            String type = MdpsTypeMappings.getMdpsKeyTypeByVwdType(i);
            if (type != null) {
                SEC_TYPES[i] = b.clear().header(FID_SECURITYTYPE, i).asBytes();
            }
        }
    }

    private static byte[] getSecType(FeedData feedData) {
        int t = feedData.getVendorkeyType();
        return t >= 0 && t < SEC_TYPES.length ? SEC_TYPES[t] : NULL_TYPE;
    }

    static class Factory {
        private final byte[] onRequestPrefix;

        private final byte[] onSubscribePrefix;

        private final byte[] onUpdatePrefix;

        Factory(int quality) {
            this.onRequestPrefix = new IqsMessageBuilder(64).prepare(MSG_DATA_RESPONSE)
                    .header(FID_SERVICEID, SERVICE_ID_PRICE)
                    .header(FID_DATA_RESPONSE_TYPE, DATA_RESPONSE_TYPE_RECAP)
                    .header(FID_DATA_TYPE, DATA_TYPE_FIELD)
                    .header(FID_DATA_RESPONSE_FLAG, DATA_RESPONSE_FLAG_RECAP_COMPLETE, DATA_RESPONSE_FLAG_RESPONSE_COMPLETE)
                    .header(FID_DATA_REQUEST_TYPE, DATA_REQUEST_TYPE_RECAP)
                    .header(FID_OBJECTNAME, OBJECT_PREFIXES[quality])
                    .asBytes();

            this.onSubscribePrefix = new IqsMessageBuilder(64).prepare(MSG_DATA_RESPONSE)
                    .header(FID_SERVICEID, SERVICE_ID_PRICE)
                    .header(FID_DATA_RESPONSE_TYPE, DATA_RESPONSE_TYPE_RECAP)
                    .header(FID_DATA_TYPE, DATA_TYPE_FIELD)
                    .header(FID_DATA_RESPONSE_FLAG, DATA_RESPONSE_FLAG_RECAP_COMPLETE)
                    .header(FID_DATA_REQUEST_TYPE, DATA_REQUEST_TYPE_RECAP_AND_UPDATES)
                    .header(FID_OBJECTNAME, OBJECT_PREFIXES[quality])
                    .asBytes();

            this.onUpdatePrefix = new IqsMessageBuilder(64).prepare(MSG_DATA_RESPONSE)
                    .header(FID_SERVICEID, SERVICE_ID_PRICE)
                    .header(FID_DATA_RESPONSE_TYPE, DATA_RESPONSE_TYPE_RECAP)
                    .header(FID_DATA_TYPE, DATA_TYPE_FIELD)
                    .header(FID_DATA_RESPONSE_FLAG, DATA_RESPONSE_FLAG_RECAP_COMPLETE)
                    .header(FID_DATA_REQUEST_TYPE, DATA_REQUEST_TYPE_RECAP_AND_UPDATES)
                    .header(FID_OBJECTNAME, OBJECT_PREFIXES[quality])
                    .asBytes();
        }


        RecapResponse onRequest(FeedData feedData, Subscriptions subscription,
                ByteBuffer update) {
            return new RecapResponse(feedData, subscription, update, onRequestPrefix);
        }

        RecapResponse onSubscribe(FeedData feedData, Subscriptions subscription,
                ByteBuffer update) {
            return new RecapResponse(feedData, subscription, update, onSubscribePrefix);
        }

        RecapResponse onUpdate(FeedData feedData, Subscriptions subscription,
                ByteBuffer update) {
            return new RecapResponse(feedData, subscription, update, onUpdatePrefix);
        }
    }


    private final FeedData feedData;

    private final Subscriptions subscription;

    private final ByteBuffer update;

    private final byte[] prefix;

    private final byte[] sectype;

    private RecapResponse(FeedData feedData, Subscriptions subscription, ByteBuffer update, byte[] prefix) {
        this.feedData = feedData;
        this.subscription = subscription;
        this.update = update;
        this.prefix = prefix;
        this.sectype = getSecType(feedData);
    }

    @Override
    public int size() {
        return prefix.length
                + feedData.getVwdcode().length()
                + this.sectype.length
                + subscription.getReplyFieldsLength()
                + DateTimeReceived.LENGTH
                + SOURCE.length
                + update.remaining()
                + 1;
    }

    @Override
    public void appendTo(ByteBuffer bb) {
        bb.put(prefix);
        this.feedData.getVwdcode().writeTo(bb, ByteString.LENGTH_ENCODING_NONE);
        bb.put(this.sectype);
        this.subscription.appendReplyFieldsTo(bb);
        bb.put(DateTimeReceived.INSTANCE.getDateTimeReceived());
        bb.put(SOURCE);
        bb.put(this.update);
        bb.put(Constants.ETX);
    }

    public static void main(String[] args) {
        new RecapResponse(null, null, null, null);
    }
}
