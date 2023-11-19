/*
 * Snippet.java
 *
 * Created on 31.03.2008 11:21:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.Activatable;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeHandler;

import java.util.Set;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Snippet<K extends Snippet<K>> extends ConfigurableSnippet, Activatable, PlaceChangeHandler {
    String CONFIG_CONTEXT_MDI = "mdi"; // $NON-NLS$
    String CONFIG_CONTEXT_DASHBOARD = "dashboard"; // $NON-NLS$
    String DEFAULT_SNIPPET_WIDTH = "280px"; // $NON-NLS$
    String DROP_TARGET_GROUP_INSTRUMENT = "ins"; // $NON-NLS$

    void configure(Widget triggerWidget);

    void destroy();

    SnippetConfiguration getConfiguration();

    String getDropTargetGroup();

    String getId();

    SnippetView<K> getView();
    
    boolean isConfigurable();

    boolean notifyDrop(QuoteWithInstrument qwi);

    void setContextController(SnippetsContextController controller);

    void updateView();

    void onControllerInitialized();

    void onAddedToSnippetsView(FlexTable table, int row, int column);


    void setReferences(String name, Snippet[] others);

    /**
     * Names of properties that should be initialized with references to other snippet(s)
     * @return names or empty set
     */
    Set<String> getReferenceNames();        
}
