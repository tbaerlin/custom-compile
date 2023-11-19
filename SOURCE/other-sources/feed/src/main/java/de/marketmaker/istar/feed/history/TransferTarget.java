/*
 * TransferTarget.java
 *
 * Created on 26.10.12 13:33
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author zzhao
 */
public interface TransferTarget {
    int transfer(ByteBuffer bb) throws IOException;
}
