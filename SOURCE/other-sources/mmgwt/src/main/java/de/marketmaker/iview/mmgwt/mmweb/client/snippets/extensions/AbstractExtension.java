/*
 * AbstractExtension.java
 *
 * Created on 23.10.2012 13:08:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions;

import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Markus Dick
 */
abstract class AbstractExtension implements Extension {
    private DmxmlContext context;
    private SnippetConfiguration config;

    AbstractExtension(DmxmlContext context, SnippetConfiguration config) {
        this.context = context;
        this.config = config;
    }

    protected DmxmlContext getContext() {
        return context;
    }

    protected SnippetConfiguration getConfig() {
        return config;
    }

    protected final <V extends BlockType> DmxmlContext.Block<V> createBlock(final String key) {
        return this.context.addBlock(key);
    }

    protected final void destroyBlock(final DmxmlContext.Block block) {
        if (block == null) {
            return;
        }
        final boolean removed = this.context.removeBlock(block);
        assert removed;
    }

    @Override
    public boolean isAllowed() {
        return true;
    }
}
