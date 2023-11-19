/*
 * ProviderSelectionCommand.java
 *
 * Created on 12.04.2010 18:56:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ProviderSelectionCommand {

    /**
     * Specifies data from which provider is preferred. Often there is a restricted set of providers
     * that user can specify.
     *
     * @return a preferred provider.
     */
    public String getProviderPreference();
}
