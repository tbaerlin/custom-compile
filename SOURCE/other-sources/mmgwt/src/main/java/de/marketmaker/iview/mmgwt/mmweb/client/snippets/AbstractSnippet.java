/*
 * AbstractSnippet.java
 *
 * Created on 01.04.2008 13:21:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractSnippet<S extends Snippet<S>, K extends SnippetView<S>> implements Snippet<S> {

    protected final DmxmlContext context;

    protected SnippetsContextController contextController;

    private final SnippetConfiguration configuration;

    private final String id;

    /**
     * gwt-1.5.0 does not like this field to be protected and each subclass assigning
     * a value to it; for some reason, the assignments do not get recorded and the variable is
     * treated as if it were null. Encapsulation and using a setter solves the problem.
     */
    private K view;

    private final Set<String> referenceNames;

    protected AbstractSnippet(DmxmlContext context, SnippetConfiguration configuration,
            Set<String> referenceNames) {
        this.context = context;
        this.configuration = configuration;
        this.id = this.configuration.getString("id", ""); // $NON-NLS$
        this.referenceNames = referenceNames;
    }

    protected AbstractSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        this(context, configuration, Collections.<String>emptySet());
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        // empty
    }

    public void setReferences(String name, Snippet[] others) {
        throw new IllegalArgumentException("Unknown property: " + name); // $NON-NLS-0$
    }

    public final Set<String> getReferenceNames() {
        return this.referenceNames;
    }

    public void configure(Widget triggerWidget) {
        assert false;
    }

    public SnippetConfiguration getConfiguration() {
        return configuration;
    }

    public HashMap<String, String> getCopyOfParameters() {
        return this.configuration.getCopyOfParameters();
    }

    public String getDropTargetGroup() {
        return null;
    }

    public final String getId() {
        return id;
    }

    public final K getView() {
        return view;
    }

    public boolean isConfigurable() {
        return false;
    }

    public boolean isStandalone() {
        final String configContext = getConfiguration().getString("context", null); // $NON-NLS$
        return CONFIG_CONTEXT_MDI.equals(configContext) || CONFIG_CONTEXT_DASHBOARD.equals(configContext);
    }

    public boolean notifyDrop(QuoteWithInstrument qwi) {
        return false;
    }

    public void setContextController(SnippetsContextController controller) {
        this.contextController = controller;
    }

    public void setParameters(HashMap<String, String> params) {
        this.configuration.setParameters(params);
        ackParametersChanged();
    }

    protected void ackParametersChanged() {
        onParametersChanged();
        this.getView().reloadTitle();
        this.contextController.reload();
    }

    protected final <V extends BlockType> DmxmlContext.Block<V> createBlock(final String key) {
        return this.context.addBlock(key);
    }

    protected final void destroyBlock(final DmxmlContext.Block block) {
        if (block == null) {
            return;
        }
        final boolean removed = this.context.removeBlock(block);
        assert removed : "block was already removed: " + block.getKey() + " (" + block.getId() + ") in " + this.getClass().getSimpleName();
    }

    protected void onParametersChanged() {
        // empty, subclasses override this to adjust block parameters to changed config
    }

    protected void setView(K view) {
        this.view = view;
    }

    public void onControllerInitialized() {
        // empty, subclasses may override
    }

    @Override
    public void onAddedToSnippetsView(FlexTable table, int row, int column) {
        // empty, subclasses may override
    }

    public void activate() {
        // empty, subclasses may override
    }

    public void deactivate() {
        // empty, subclasses may override
    }

}
