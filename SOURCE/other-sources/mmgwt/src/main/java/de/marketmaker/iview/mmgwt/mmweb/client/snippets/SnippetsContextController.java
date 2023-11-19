package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

/**
 * Created on 19.03.13 09:30
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface SnippetsContextController {
    void reload();
    Snippet getSnippet(String id);
    void updateVisibility(SnippetView sv, boolean visible);
    void handleVisibility();
}
