/*
 * AbstractAnalysesRequest.java
 *
 * Created on 21.03.12 08:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.frontend;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * @author oflege
 */
public abstract class AbstractAnalysesRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 2L;

    private final Selector selector;

    protected AbstractAnalysesRequest(Selector selector) {
        this.selector = selector;
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        sb.append(", selector=").append(this.selector);
    }
}
