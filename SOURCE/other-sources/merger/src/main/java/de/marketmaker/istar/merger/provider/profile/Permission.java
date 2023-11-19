/*
 * Permissions.java
 *
 * Created on 30.12.13 13:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

/**
 * @author oflege
 */
public enum Permission {
    INDEX_COMPOSITION(7, 12173, true),
    INDEX_WEIGHT(7, 12174, true);

    public final int vwdContextId;

    public final int vwdFieldInstanceId;

    public final boolean allowIfNoFilterMatches;

    private Permission(int vwdContextId, int vwdFieldInstanceId, boolean allowIfNoFilterMatches) {
        this.vwdContextId = vwdContextId;
        this.vwdFieldInstanceId = vwdFieldInstanceId;
        this.allowIfNoFilterMatches = allowIfNoFilterMatches;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "vwdContextId=" + vwdContextId +
                ", vwdFieldInstanceId=" + vwdFieldInstanceId +
                ", allowIfNoFilterMatches=" + allowIfNoFilterMatches +
                '}';
    }
}
