/*
 * Subscription.java
 *
 * Created on 21.04.2010 16:53:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.exporttools.aboretriever;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Subscription {
    private final short id;

    private final String name;

    private final String description;

    Subscription(short id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public short getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Subscription");
        sb.append("{id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
