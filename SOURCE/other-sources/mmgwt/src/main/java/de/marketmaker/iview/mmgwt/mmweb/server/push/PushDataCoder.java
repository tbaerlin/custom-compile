package de.marketmaker.iview.mmgwt.mmweb.server.push;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushData;
import de.marketmaker.iview.mmgwt.mmweb.server.MySerializationPolicyProvider;
import de.marketmaker.iview.mmgwt.mmweb.server.SimpleSerializationPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * Created on 16.07.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 *
 * TODO: this is (more or less) a copy of de.marketmaker.iview.mmgwt.mmweb.server.async.AsyncDataCoder -> Merge them if possible.
 *
 */
public class PushDataCoder implements Encoder.Text<PushData>, Decoder.Text<PushData> {
    private final Log logger = LogFactory.getLog(getClass());

    public static String serialize(final PushData data) throws SerializationException {
        final ServerSerializationStreamWriter serverSerializationStreamWriter = new ServerSerializationStreamWriter(new SimpleSerializationPolicy());
        serverSerializationStreamWriter.writeObject(data);
        return serverSerializationStreamWriter.toString();
    }

    public static PushData deserialize(String data) throws SerializationException {
        final ServerSerializationStreamReader streamReader = new ServerSerializationStreamReader(Thread.currentThread().getContextClassLoader(), new MySerializationPolicyProvider());
        streamReader.prepareToRead(data);
        return (PushData) streamReader.readObject();
    }

    @Override
    public boolean willDecode(String s) {
        return s != null;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        //nothing to do
    }

    @Override
    public void destroy() {
        //nothing to do
    }

    @Override
    public String encode(PushData data) throws EncodeException {
        try {
            return serialize(data);
        }
        catch (SerializationException e) {
            this.logger.warn("cannot serialize PushData", e);
            return null;
        }
    }

    @Override
    public PushData decode(String s) throws DecodeException {
        try {
            return deserialize(s);
        }
        catch (SerializationException e) {
            this.logger.warn("cannot deserialize PushData", e);
            return null;
        }
    }
}