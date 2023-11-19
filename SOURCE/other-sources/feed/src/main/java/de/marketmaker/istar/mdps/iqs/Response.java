/*
 * MessageAppender.java
 *
 * Created on 17.10.13 13:19
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.nio.ByteBuffer;

/**
 * Interface for data that will be appended to a client's out buffer
 * @author oflege
 */
interface Response {
    int size();

    void appendTo(ByteBuffer bb);
}
