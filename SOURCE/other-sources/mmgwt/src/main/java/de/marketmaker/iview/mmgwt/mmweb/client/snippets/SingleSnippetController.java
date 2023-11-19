/*
 * SingleSnippetController.java
 *
 * Created on 18.06.13 16:25
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.List;

/**
 * @author Markus Dick
 */
public class SingleSnippetController extends SnippetsController {
    public SingleSnippetController(ContentContainer contentContainer, DmxmlContext context, List<Snippet> mySnippets, List<Snippet> snippets) {
        super(contentContainer, context, mySnippets, snippets);
    }

    @Override
    protected Widget createView() {
        return new SingleSnippetView(getSnippets());
    }
}
