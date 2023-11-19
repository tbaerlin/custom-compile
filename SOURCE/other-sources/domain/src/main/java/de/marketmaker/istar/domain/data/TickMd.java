/*
 * TickMd.java
 *
 * Created on 11.01.2005 15:59:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

/**
 * A market depth tick.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id: TickMd.java,v 1.1 2005/01/14 08:31:13 oliver Exp $
 */
public interface TickMd {
    int getTime();

    int getBidDepth();

    long getBidPrice(final int i);

    int getBidVolume(final int i);

    int getAskDepth();

    long getAskPrice(final int i);

    int getAskVolume(final int i);
}
