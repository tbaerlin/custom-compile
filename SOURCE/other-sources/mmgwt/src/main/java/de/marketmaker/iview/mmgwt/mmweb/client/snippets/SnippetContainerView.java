/*
 * SnippetContainerView.java
 *
 * Created on 25.04.2008 13:43:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

/**
 * Implemented by a view that wraps a snippet's view. Allows the snippet to interact with its
 * container, for example, to set the title.
 * 
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface SnippetContainerView {
    String getTitle();

    void setTitle(String title);
}
