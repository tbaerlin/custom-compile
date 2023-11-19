/*
 * Node.java
 *
 * Created on 15.03.2012 10:15:58
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public abstract class Node implements Serializable {
    private String label;
    protected Element parent;

    public Node() { // needed for GWT serialization
    }

    public Node(String label) {
        this.label = label;
    }

    public abstract String getType();

    public String getLabel() {
        return label;
    }

    public Element getParent() {
        return parent;
    }

    public String getSubtreeAsString() {
        final StringBuilder result = new StringBuilder();
        this.appendSubtree(result, "");
        return result.toString();
    }


    public void appendSubtree(StringBuilder sb, String indent) {
        sb.append(indent).append(this.getLabel()).append('\n');
    }

}
