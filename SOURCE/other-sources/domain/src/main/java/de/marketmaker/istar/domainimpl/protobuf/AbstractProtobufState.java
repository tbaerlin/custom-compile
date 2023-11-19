/*
 * AbstractProtobufState.java
 *
 * Created on 01.12.2009 17:32:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;

/**
 * Base class for objects that need the last value of a field to compute the next value to be
 * serialized.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
abstract class AbstractProtobufState<E> {
    protected static Descriptors.FieldDescriptor findField(Descriptors.Descriptor descriptor,
            String fieldname) {
        for (final Descriptors.FieldDescriptor d : descriptor.getFields()) {
            if (d.getName().equals(fieldname)) {
                return d;
            }
        }
        return null;
    }

    protected final Descriptors.FieldDescriptor field;

    protected E lastValue = null;

    public AbstractProtobufState(Descriptors.Descriptor descriptor, String fieldname) {
        this.field = findField(descriptor, fieldname);
    }

    protected abstract void doUpdate(GeneratedMessage.Builder element, E value);

    public void update(GeneratedMessage.Builder element, E value) {
        doUpdate(element, value);
        this.lastValue = value;
    }
}