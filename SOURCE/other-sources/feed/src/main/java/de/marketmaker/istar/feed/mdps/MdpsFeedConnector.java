/*
 * MdpsFeedConnector.java
 *
 * Created on 04.03.2010 13:34:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.nio.ByteOrder;

import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.feed.connect.FeedConnector;

/**
 * A FeedConnector that adapts the byte order of the buffer used to read from a socket
 * according to the mdps protocol version.
 * @author oflege
 */
@ManagedResource
public class MdpsFeedConnector extends FeedConnector {
    private int protocolVersion = 1;

    public void setProtocolVersion(int protocolVersion) {
        if (protocolVersion != 1 && protocolVersion != 3) {
            throw new IllegalArgumentException("unsupported " + protocolVersion);
        }
        this.protocolVersion = protocolVersion;
        this.logger.info("<setProtocolVersion> = " + this.protocolVersion);
    }

    @Override
    protected ByteOrder getByteOrder() {
        return MdpsFeedUtils.getByteOrder(this.protocolVersion);
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }
}
