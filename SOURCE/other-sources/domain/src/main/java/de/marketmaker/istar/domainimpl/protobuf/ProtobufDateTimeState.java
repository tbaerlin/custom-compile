/*
 * ProtobufDateTimeState.java
 *
 * Created on 01.12.2009 17:32:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

/**
 * Helper for protobuf encoding of DateTime objects
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class ProtobufDateTimeState extends AbstractProtobufState<DateTime> {

    private final boolean useMillis;

    public ProtobufDateTimeState(Descriptors.Descriptor descriptor, boolean useMillis) {
        super(descriptor, "time");
        this.useMillis = useMillis;
    }

    boolean isUseMillis() {
        return this.useMillis;
    }

    @Override
    public void update(GeneratedMessage.Builder element, DateTime value) {
        DateTime tmp = useMillis ? value : value.withMillisOfSecond(0);
        doUpdate(element, tmp);
        this.lastValue = tmp;
    }

    protected void doUpdate(GeneratedMessage.Builder element, DateTime value) {
        if (lastValue != null && lastValue.equals(value)) {
            return;
        }
        if (useMillis) {
            element.setField(field, getValueToSet(value));
        }
        else {
            element.setField(field, getValueToSet(value) / DateTimeConstants.MILLIS_PER_SECOND);
        }
    }

    private long getValueToSet(DateTime value) {
        if (this.lastValue == null) {
            return value.getMillis();
        }
        return (value.getMillis() - lastValue.getMillis());
    }
}
