/*
 * AggregatedTickSerializer.java
 *
 * Created on 04.07.12 14:16
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import de.marketmaker.istar.domain.data.AggregatedTickImpl;
import de.marketmaker.istar.domain.data.AggregatedValue;

/**
 * @author zzhao
 */
public interface AggregatedTickSerializer {

    ByteBuffer serialize(List<AggregatedTickImpl> ticks) throws IOException;

    ByteBuffer serializeValues(List<AggregatedValue> ticks) throws IOException;

    int getNumObjects();
}
