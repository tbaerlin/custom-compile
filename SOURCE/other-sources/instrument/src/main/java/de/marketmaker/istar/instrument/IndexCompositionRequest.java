/*
 * IndexCompositionResponse.java
 *
 * Created on 21.03.14 11:06
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author oflege
 */
public class IndexCompositionRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 122L;

    private final String key;

    public IndexCompositionRequest(String key) {
        this.key = key;
    }

    public IndexCompositionRequest(long quoteid) {
        this(Long.toString(quoteid));
    }

    public String getKey() {
        return key;
    }
}
