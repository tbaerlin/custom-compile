/*
 * IndexAndOffsetFactoryVwd.java
 *
 * Created on 28.10.2004 09:09:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.snap.AbstractIndexAndOffsetFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IndexAndOffsetFactoryVwd extends AbstractIndexAndOffsetFactory {
    public static final int INLINE_STRING_LENGTH = 6;

    public static final int PRICE_LENGTH = 5;

    public static int getLength(VwdFieldDescription.Field field) {
        switch (field.type()) {
            case PRICE:
                return PRICE_LENGTH;
            case STRING:
                if (field.length() > INLINE_STRING_LENGTH) {
                    return 2;
                }
            // fallthrough
            default:
                return field.length();
        }
    }

    protected int getLength(int fieldid) {
        return getLength(VwdFieldDescription.getField(fieldid));
    }
}

