/*
 * AnalysisImageRequest.java
 *
 * Created on 20.04.12 10:04
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.frontend;

import de.marketmaker.istar.domain.profile.Selector;

/**
 * @author oflege
 */
public class AnalysisImageRequest extends AbstractAnalysesRequest {
    protected static final long serialVersionUID = 1L;

    private final String name;

    public AnalysisImageRequest(Selector selector, String name) {
        super(selector);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        sb.append(", name=").append(this.name);
    }
}
