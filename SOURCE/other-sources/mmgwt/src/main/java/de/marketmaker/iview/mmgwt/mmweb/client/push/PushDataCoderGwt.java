/*
 * PushDataCoderGwt.java
 *
 * Created on 16.07.15
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.push;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import de.marketmaker.itools.gwtutil.client.util.Firebug;

/**
 * @author mloesch
 */
public class PushDataCoderGwt {
    @SuppressWarnings("unused")
    public static String serialize(PushData pushData) {
        try {
            SerializationStreamFactory factory = GWT.create(PushService.class);
            SerializationStreamWriter writer = factory.createStreamWriter();
            writer.writeObject(pushData);
            return writer.toString();
        } catch (final SerializationException e) {
            Firebug.warn("cannot serialize message", e);
            throw new RuntimeException("cannot serialize message"); // $NON-NLS$
        }
    }

    public static PushData deserialize(String data) {
        try {
            SerializationStreamFactory factory = GWT.create(PushService.class);
            final SerializationStreamReader streamReader = factory.createStreamReader(data);
            return (PushData) streamReader.readObject();
        } catch (final SerializationException e) {
            Firebug.warn("cannot deserialize message", e);
            throw new RuntimeException("cannot deserialize message"); // $NON-NLS$
        }
    }
}
