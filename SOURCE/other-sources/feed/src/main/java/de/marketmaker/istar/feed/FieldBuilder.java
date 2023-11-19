/*
 * FieldBuilder.java
 *
 * Created on 29.10.2004 16:07:23
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface FieldBuilder {
    void set(VwdFieldDescription.Field field, int value);

    void set(VwdFieldDescription.Field field, long value);

    void set(VwdFieldDescription.Field field, byte[] value, int start, int length);

    /**
     * Returns flags for the fields this builder is interested in.
     * Most likely a a combination
     * of {@link de.marketmaker.istar.feed.vwd.VwdFieldDescription#FLAG_DYNAMIC},
     * and {@link de.marketmaker.istar.feed.vwd.VwdFieldDescription#FLAG_STATIC},
     * and {@link de.marketmaker.istar.feed.vwd.VwdFieldDescription#FLAG_RATIO}.
     * and {@link de.marketmaker.istar.feed.vwd.VwdFieldDescription#FLAG_NEWS}.
     * <p>
     * The various set methods will only be invoked if the field's flags intersect
     * with the flags returned by this method OR if this method returns <tt>0</tt>
     * @return flags
     */
    int getFieldFlags();
}
