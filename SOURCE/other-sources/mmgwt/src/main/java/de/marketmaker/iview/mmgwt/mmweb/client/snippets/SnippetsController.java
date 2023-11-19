/*
 * SnippetsController.java
 *
 * Created on 01.04.2008 12:59:19
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentViewAdapter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnippetsController extends AbstractPageController implements SnippetsContextController {
    protected Widget view;

    private final List<Snippet> mySnippets = new ArrayList<>();

    // mySnippets and all mySnippets in subcontrollers in the order in which the snippets
    // have been defined.
    private List<Snippet> snippets = this.mySnippets;

    private boolean deferredRequestPending = false;

    private ContentViewAdapter contentView;

    private ArrayList<SnippetsController> subcontrollers;

    private SnippetsController parent;

    private String defKey = null;

    private Boolean visibilityCheckNeeded = null;

    public SnippetsController(ContentContainer contentContainer, DmxmlContext context,
                              List<Snippet> mySnippets, List<Snippet> snippets) {
        super(contentContainer, context);
        for (Snippet snippet : mySnippets) {
            add(snippet);
        }
        if (snippets != null) {
            this.snippets = snippets;
        }
    }

    void onControllerInitialized() {
        for (Snippet snippet : this.mySnippets) {
            snippet.onControllerInitialized();
        }
        if (this.subcontrollers != null) {
            for (SnippetsController subcontroller : this.subcontrollers) {
                subcontroller.onControllerInitialized();
            }
        }
    }

    public Snippet getSnippet(String id) {
        // goto root to ensure subcontrollers can access any snippet they need
        final SnippetsController root = (this.parent != null) ? this.parent : this;
        return root.doGetSnippet(id);
    }

    private Snippet doGetSnippet(String id) {
        for (Snippet snippet : this.mySnippets) {
            if (snippet.getId().equals(id)) {
                return snippet;
            }
        }
        if (this.subcontrollers != null) {
            for (SnippetsController subcontroller : subcontrollers) {
                final Snippet result = subcontroller.doGetSnippet(id);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public Widget getView() {
        return this.view;
    }

    @Override
    public String getPrintHtml() {
        final Element dom = this.view.getElement();
        final NodeList<Element> inputs = dom.getElementsByTagName("input"); // $NON-NLS-0$

        if (inputs != null) {
            for (int cii = 0; cii < inputs.getLength(); cii++) {
                updateDOM(InputElement.as(inputs.getItem(cii)));
            }
        }
        return dom.getString();
    }

    public static void updateDOM(InputElement item) {
        item.setDefaultValue(item.getValue());
        item.setDefaultChecked(item.isChecked());
    }


    @Override
    public void activate() {
        for (Snippet s : this.mySnippets) {
            s.activate();
        }
        if (this.subcontrollers != null) {
            for (SnippetsController subcontroller : this.subcontrollers) {
                subcontroller.activate();
            }
        }
    }

    @Override
    public void deactivate() {
        for (Snippet s : this.mySnippets) {
            s.deactivate();
        }
        if (this.subcontrollers != null) {
            for (SnippetsController subcontroller : this.subcontrollers) {
                subcontroller.deactivate();
            }
        }
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        onPlaceChange(event, true);
    }

    public void onPlaceChange(PlaceChangeEvent event, boolean forwardToSubcontrollers) {
        for (Snippet s : this.mySnippets) {
            s.onPlaceChange(event);
        }
        if (forwardToSubcontrollers && this.subcontrollers != null) {
            for (SnippetsController subcontroller : this.subcontrollers) {
                subcontroller.onPlaceChange(event);
            }
        }

        reload();
    }

    public void reload() {
        if (this.deferredRequestPending) {
            return;
        }

        this.deferredRequestPending = true;

        // it is important that the reload happens deferred, so that for example
        // all property change listeners will be invoked before a reload is triggered
        Scheduler.get().scheduleDeferred(() -> {
            refresh();
            deferredRequestPending = false;
        });
    }

    public void remove(Snippet snippet) {
        this.mySnippets.remove(snippet);
        this.visibilityCheckNeeded = null;
    }

    protected void add(Snippet snippet) {
        this.mySnippets.add(snippet);
        snippet.setContextController(this);
        this.visibilityCheckNeeded = null;
    }

    private boolean isVisibilityCheckNeeded() {
        if (this.visibilityCheckNeeded == null) {
            boolean visibilityCheckNeeded = false;
            for (final Snippet snippet : this.mySnippets) {
                if (snippet instanceof IsVisible) {
                    visibilityCheckNeeded = true;
                    break;
                }
            }
            this.visibilityCheckNeeded = visibilityCheckNeeded;
        }
        return this.visibilityCheckNeeded;
    }

    public void setDefKey(String defKey) {
        this.defKey = defKey;
    }

    protected Widget createView() {
        return new FlexSnippetsView(this.snippets);
    }

    protected void initView() {
        if (this.view != null || this.parent != null) {
            return;
        }
        this.view = createView();
        if (this.defKey != null) {
            this.view.getElement().setAttribute("mm:controller-id", this.defKey); // $NON-NLS-0$
        }
        this.contentView = new ContentViewAdapter(this.view) {
            public void onBeforeHide() {
                SnippetsController.this.onBeforeHide();
            }
        };
    }

    public final void onResult() {
        doOnResult();
        handleVisibility();
    }

    public void handleVisibility() {
        if (!isVisibilityCheckNeeded()) {
            return;
        }
        for (final Snippet snippet : this.mySnippets) {
            if (snippet instanceof IsVisible) {
                updateVisibility(snippet.getView(), ((IsVisible) snippet).isVisible());
            }
        }
    }

    public void updateVisibility(SnippetView sv, boolean visible) {
        if (this.view instanceof FlexSnippetsView) {
            ((FlexSnippetsView) this.view).updateVisibility(sv, visible);
        }
        else if (this.parent != null) {
            this.parent.updateVisibility(sv, visible);
        }
    }

    protected void doOnResult() {
        initView();
        updateSubviews();
        if (this.contentView != null) {
            if (this.view != null) {
                this.view.setVisible(true); // important to fix IE7 rendering problem
            }
            getContentContainer().setContent(this.contentView);
        }
    }

    protected void onBeforeHide() {
        if (this.view != null) {
            this.view.setVisible(false); // important to fix IE7 rendering problem
        }
    }

    protected void updateSubviews() {
        for (Snippet s : this.mySnippets) {
            try {
                s.updateView();
            }
            catch (Exception e) {
                Firebug.error("cannot update snippet view: " + s.getId(), e); // $NON-NLS-0$
            }
        }
    }

    public List<Snippet> getSnippets() {
        return this.snippets;
    }

    public Snippet getSnippet(Class clazz) {
        if (this.subcontrollers != null) {
            for (SnippetsController subcontroller : this.subcontrollers) {
                final Snippet snippet = subcontroller.getSnippet(clazz);
                if (snippet != null) {
                    return snippet;
                }
            }
            return null;
        }

        for (Snippet snippet : this.mySnippets) {
            if (snippet.getClass().equals(clazz)) {
                return snippet;
            }
        }
        return null;
    }

    public void setSubcontrollers(ArrayList<SnippetsController> subcontrollers) {
        this.subcontrollers = subcontrollers;
        for (SnippetsController subcontroller : subcontrollers) {
            subcontroller.parent = this;
        }
    }
}
