/*
 * SerializableCollator.java
 *
 * Created on 01.08.2006 13:45:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.util.Comparator;
import java.util.Locale;
import java.text.Collator;
import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SerializableCollator<K> implements Serializable, Comparator<K> {
    protected static final long serialVersionUID = -4670409262214493143L;

    private final Locale locale;
    private final transient Collator collator;

    public SerializableCollator(Locale locale) {
        this.locale = locale;
        this.collator = Collator.getInstance(this.locale);
    }

    protected Collator getCollator(){
        return this.collator;
    }

    public int compare(K o1, K o2) {
        return this.collator.compare(o1, o2);
    }

    private Object readResolve() {
        return new SerializableCollator(this.locale);
    }
}
