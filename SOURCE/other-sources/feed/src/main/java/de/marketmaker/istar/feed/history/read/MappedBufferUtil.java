/*
 * MappedBufferUtil.java
 *
 * Created on 21.08.2014 11:29
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history.read;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @author zzhao
 */
public final class MappedBufferUtil {
    public static final boolean UNMAP_SUPPORTED;

    static {
        boolean v;
        try {
            Class.forName("sun.misc.Cleaner");
            Class.forName("java.nio.DirectByteBuffer")
                    .getMethod("cleaner");
            v = true;
        } catch (Exception e) {
            v = false;
        }
        UNMAP_SUPPORTED = v;
    }

    /**
     * Try to unmap the buffer, this method silently fails if no support
     * for that in the JVM. On Windows, this leads to the fact,
     * that mmapped files cannot be modified or deleted.
     */
    public static void unmap(final ByteBuffer buffer) throws IOException {
        if (UNMAP_SUPPORTED) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                    @Override
                    public Object run() throws Exception {
                        final Method getCleanerMethod = buffer.getClass()
                                .getMethod("cleaner");
                        getCleanerMethod.setAccessible(true);
                        final Object cleaner = getCleanerMethod.invoke(buffer);
                        if (cleaner != null) {
                            cleaner.getClass().getMethod("clean")
                                    .invoke(cleaner);
                        }
                        return null;
                    }
                });
            } catch (PrivilegedActionException e) {
                final IOException ioe = new IOException("unable to unmap the mapped buffer");
                ioe.initCause(e.getCause());
                throw ioe;
            }
        }
    }
}
