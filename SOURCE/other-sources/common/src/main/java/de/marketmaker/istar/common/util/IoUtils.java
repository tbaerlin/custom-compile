/*
 * IoUtils.java
 *
 * Created on 18.02.2005 15:13:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IoUtils {
    private static final Logger logger = LoggerFactory.getLogger(IoUtils.class);

    private IoUtils() {
    }

    public static boolean close(Closeable c) {
        if (c == null) {
            return true;
        }
        try {
            c.close();
            return true;
        } catch (IOException e) {
            logger.error("<close> failed for " + c, e);
            return false;
        }
    }
}
