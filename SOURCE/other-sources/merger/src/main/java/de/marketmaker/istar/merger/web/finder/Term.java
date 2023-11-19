/*
 * Term.java
 *
 * Created on 13.08.2008 14:07:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.finder;

import java.io.Serializable;

/**
 * Marker interface for components of a parsed finder query
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Term extends Serializable {

    void accept(TermVisitor visitor);
}
