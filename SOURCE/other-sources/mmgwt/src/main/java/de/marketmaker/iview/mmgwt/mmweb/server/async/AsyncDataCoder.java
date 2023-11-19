package de.marketmaker.iview.mmgwt.mmweb.server.async;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncData;
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
 * Author: umaurer
 * Created: 29.08.14
 */
public class AsyncDataCoder implements Encoder.Text<AsyncData>, Decoder.Text<AsyncData> {
    private final Log logger = LogFactory.getLog(getClass());

    public static String serialize(final AsyncData asyncData) throws SerializationException {
        final ServerSerializationStreamWriter serverSerializationStreamWriter = new ServerSerializationStreamWriter(new SimpleSerializationPolicy());
        serverSerializationStreamWriter.writeObject(asyncData);
        return serverSerializationStreamWriter.toString();
    }

    public static AsyncData deserialize(String data) throws SerializationException {
        final ServerSerializationStreamReader streamReader = new ServerSerializationStreamReader(Thread.currentThread().getContextClassLoader(), new MySerializationPolicyProvider());
        streamReader.prepareToRead(data);
        return (AsyncData) streamReader.readObject();
    }

    @Override
    public String encode(AsyncData data) throws EncodeException {
        try {
            return serialize(data);
        } catch (SerializationException e) {
            this.logger.warn("cannot serialize AsyncData", e);
            return null;
        }
    }

    @Override
    public AsyncData decode(String s) throws DecodeException {
        try {
            return deserialize(s);
        } catch (SerializationException e) {
            this.logger.warn("cannot deserialize AsyncData", e);
            return null;
        }
    }

    @Override
    public boolean willDecode(String s) {
        return s != null;
    }

    @Override
    public void init(EndpointConfig config) {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
