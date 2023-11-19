/*
 * SnippetView.java
 *
 * Created on 21.10.2009 15:43:35
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.widgets.ContentPanelIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;

/**
 * @author oflege
 */
public class SnippetView<V extends Snippet<V>> {
    protected final V snippet;
    protected ContentPanelIfc container;
    private String title;
    private FloatingToolbar toolbar = null;

    protected SnippetView(V snippet) {
        this.snippet = snippet;
    }

    protected SnippetConfiguration getConfiguration() {
        return snippet.getConfiguration();
    }

    public void setContainer(final ContentPanelIfc container, boolean withSnippetAttributes) {
        setContainer(container);
        if (withSnippetAttributes) {
            container.addAttachHandler(attachEvent -> {
                if (attachEvent.isAttached()) {
                    setSnippetAttributes();
                }
            });
        }
    }

    public void setContainer(ContentPanelIfc container) {
        this.container = container;
        if (this.title != null) {
            this.container.setHeading(this.title);
        }
        if (this.toolbar != null) {
            this.container.setTopWidget(this.toolbar);
        }
        onContainerAvailable();
    }

    public ContentPanelIfc getContainer() {
        return this.container;
    }

    private void setSnippetAttributes() {
        final String snippetId = this.snippet.getId();
        if (snippetId != null) {
            this.container.getElement().setAttribute("mm-snippet-id", snippetId); // $NON-NLS-0$
        }
        final String snippetName = getConfiguration().getName();
        if (snippetName != null) {
            this.container.getElement().setAttribute("mm-snippet-name", snippetName); // $NON-NLS-0$
        }
    }

    protected void onContainerAvailable() {
        // empty
    }

    public Element getElement() {
        if(this.container == null) {
            return null;
        }
        return this.container.getElement();
    }

    public void reloadTitle() {
        final SnippetConfiguration conf = getConfiguration();
        this.title = conf.getString("title", null); // $NON-NLS-0$
        final String suffix = conf.getString("titleSuffix", null); // $NON-NLS-0$
        if (suffix != null) {
            this.title = this.title + " - " + suffix; // $NON-NLS-0$
        }
        updateTitle();
    }

    protected void updateTitle() {
        if (this.title != null && this.container != null) {
            this.container.setHeading(this.title);
            this.title = null;
        }
    }

    protected void setTitleForNextUpdate(String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        if (title == null) {
            return;
        }
        if (this.container != null) {
            this.container.setHeading(title);
            this.title = null;
        }
        else {
            this.title = title;
        }
    }

    public String getTitle() {
        if(this.container != null) {
            return this.container.getHeading();
        }

        return this.title;
    }

    public void setTopComponent(FloatingToolbar toolbar) {
        // TODO: this method is weird, creating/setting should happen during onContainerAvailable
        if (toolbar == null) {
            return;
        }
        if (this.container != null) {
            this.container.setTopWidget(toolbar);
            this.toolbar = null;
        }
        else {
            this.toolbar = toolbar;
        }
    }


    public FloatingToolbar getOrCreateTopToolbar() {
        if (this.toolbar != null) {
            return this.toolbar;
        }
        if (this.container != null) {
            final Widget top = this.container.getTopWidget();
            if (top == null) {
                final FloatingToolbar tb = new FloatingToolbar();
                this.container.setTopWidget(tb);
                return tb;
            }
            return (FloatingToolbar) top;
        }
        this.toolbar = new FloatingToolbar();
        return this.toolbar;
    }
}
