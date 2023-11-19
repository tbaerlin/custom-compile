/*
 * XsdType.java
 *
 * Created on 15.03.2012 10:18:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree;

import java.io.Serializable;

/**
 * Represents an xsd type; actually just a String, as we do not need to distinguish namespaces.
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class XsdType implements Serializable {

    public XsdType() { // needed for GWT serialization
    }

    public XsdType(String localName ) {//, String namespace) {
//        if (localName == null) throw new IllegalArgumentException();
        this.localName = localName;
    }

    private String localName;


    public String getLocalName() {
        return localName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XsdType xsdType = (XsdType) o;
        return !(localName != null ? !localName.equals(xsdType.localName) : xsdType.localName != null);
    }

    @Override
    public int hashCode() {
        return localName != null ? localName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "XsdType{localName='" + localName + '\'' + '}'; // $NON-NLS$
    }
}
