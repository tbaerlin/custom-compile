/*
 * ConfigurableSnippet.java
 *
 * Created on 19.06.2008 15:15:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.HashMap;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ConfigurableSnippet {
    HashMap<String, String> getCopyOfParameters();

    void setParameters(HashMap<String, String> params);
}
