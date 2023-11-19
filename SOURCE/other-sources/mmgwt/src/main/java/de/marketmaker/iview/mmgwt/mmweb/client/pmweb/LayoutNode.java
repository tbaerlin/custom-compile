package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * Created on 10.06.13 11:39
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class LayoutNode {
    private final String guid;
    private final String nodeId;
    private static final String SEP = ";"; // $NON-NLS$
    private static final String NULL = "null"; // $NON-NLS$

    public static LayoutNode create(String nodeAndGuid) {
        if (!StringUtil.hasText(nodeAndGuid)) {
            return null;
        }
        if (!nodeAndGuid.contains(SEP)) {
            throw new IllegalArgumentException("nodeAndGuid must be separated by " + SEP + ". found: " + nodeAndGuid); // $NON-NLS$
        }
        final String[] split = nodeAndGuid.split(SEP);
        final String nodeId = split[0];
        final String guid = split[1];
        if (NULL.equals(guid)) {
            return new LayoutNode(nodeId, null);
        }
        else {
            return new LayoutNode(nodeId, guid);
        }
    }

    public static LayoutNode create(String nodeId, String guid) {
        return new LayoutNode(nodeId, guid);
    }

    private LayoutNode(String nodeId, String guid) {
        this.guid = guid;
        this.nodeId = nodeId;
    }

    public String getGuid() {
        return this.guid;
    }

    public String getNodeId() {
        return this.nodeId;
    }

    public boolean equals(String s) {
        int pos = s.indexOf(SEP);
        return pos >= 0 && equals(LayoutNode.create(s));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LayoutNode)) return false;

        final LayoutNode that = (LayoutNode) o;

        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (!nodeId.equals(that.nodeId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = guid != null ? guid.hashCode() : 0;
        result = 31 * result + nodeId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.nodeId + ";" +
                (StringUtil.hasText(this.guid)
                        ? this.guid
                        : NULL);
    }
}
