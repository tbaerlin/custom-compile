package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import de.marketmaker.itools.gwtutil.client.util.Firebug;

/**
 * Author: umaurer
 * Created: 09.02.15
 */
public class AsyncDataCoderGwt {
    public static String serialize(AsyncData asyncData) {
        try {
            SerializationStreamFactory factory = GWT.create(AsyncService.class);
            SerializationStreamWriter writer = factory.createStreamWriter();
            writer.writeObject(asyncData);
            return writer.toString();
        } catch (final SerializationException e) {
            Firebug.warn("cannot serialize message", e);
            throw new RuntimeException("cannot serialize message"); // $NON-NLS$
        }
    }

    public static AsyncData deserialize(String data) {
        try {
            SerializationStreamFactory factory = GWT.create(AsyncService.class);
            final SerializationStreamReader streamReader = factory.createStreamReader(data);
            return (AsyncData) streamReader.readObject();
        } catch (final SerializationException e) {
            Firebug.warn("cannot deserialize message", e);
            throw new RuntimeException("cannot deserialize message"); // $NON-NLS$
        }
    }

}
