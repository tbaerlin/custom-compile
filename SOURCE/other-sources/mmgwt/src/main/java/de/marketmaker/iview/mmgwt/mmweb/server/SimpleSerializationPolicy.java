package de.marketmaker.iview.mmgwt.mmweb.server;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;

import java.io.Serializable;

/**
 * Author: umaurer
 * Created: 09.02.15
 */
public class SimpleSerializationPolicy extends SerializationPolicy {
    @Override
    public boolean shouldDeserializeFields(Class<?> clazz) {
        return isSerializable(clazz);
    }

    @Override
    public boolean shouldSerializeFields(Class<?> clazz) {
        return isSerializable(clazz);
    }

    private boolean isSerializable(Class<?> clazz) {
        if (clazz != null) {
            if (clazz.isPrimitive() || Serializable.class.isAssignableFrom(clazz) || IsSerializable.class.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validateDeserialize(Class<?> clazz) throws SerializationException {
        if (!isSerializable(clazz)) {
            throw new SerializationException();
        }
    }

    @Override
    public void validateSerialize(Class<?> clazz) throws SerializationException {
        if (!isSerializable(clazz)) {
            throw new SerializationException();
        }
    }
}
