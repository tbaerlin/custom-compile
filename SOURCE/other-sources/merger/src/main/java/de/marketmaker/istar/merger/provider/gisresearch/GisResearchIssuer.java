/*
 * GisResearchIssuer.java
 *
 * Created on 17.04.14 14:26
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import java.io.Serializable;

/**
* @author oflege
*/
public final class GisResearchIssuer implements Serializable {
    static final long serialVersionUID = 1L;

    final String number;

    final String name;

    GisResearchIssuer(String number, String name) {
        this.number = (number != null) ? number : name.replace(' ', '_');
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name + '(' + this.number + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GisResearchIssuer issuer = (GisResearchIssuer) o;
        return name.equals(issuer.name) && number.equals(issuer.number);
    }

    @Override
    public int hashCode() {
        return 31 * number.hashCode() + name.hashCode();
    }
}
